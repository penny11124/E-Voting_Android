package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ureka.framework.Conftest.attackerServer;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.deviceOwnerAgentAndHerDevice;
import static ureka.framework.Conftest.enterpriseProviderServer;
import static ureka.framework.Conftest.waitSimulatedCommCompleted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.crypto.SerializationUtil;

public class Test15FailWhenAccessDeviceByOthers {
    private DeviceController iotDevice;
    private DeviceController userAgentDO;
    private DeviceController cloudServerATK;
    private DeviceController cloudServerEP;

    // RE-GIVEN: Reset the test environment
    @BeforeEach
    public void setup() {
        currentSetupLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // RE-GIVEN: Reset the test environment
    @AfterEach
    public void teardown() {
        currentTeardownLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // Threat: (S) Spoofing, (T) Tampering, (E) Elevation of Privilege
    @Test
    public void testFailWhenForgeHolderIdAndIssuerSigAndApplyTheUTicket() {
        currentTestGivenLog();

        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept, Forge, & Apply
        currentTestWhenAndThenLog();

        // WHEN: Interception (Know Latest State)
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceUTicketForOwner();
        String interceptedRTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceRTicketForOwner();

        // WHEN: Pretend Issuer: Owner
        this.cloudServerATK.getSharedData().getDeviceTable().put(targetDeviceId, new OtherDevice(
                targetDeviceId, interceptedUTicketJson, interceptedRTicketJson, 2
        ));
        // WHEN: Forge Flow (issuerIssueUTicketToHolder)
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.cloudServerATK.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_ACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        String generatedUTicketJson = this.cloudServerATK.getMsgGenerator().generateXxxUTicket(generatedRequest);

        // WHEN: Pretend Holder: Other
        // WHEN: Forge Flow (_holderRecvUTicket)
        this.cloudServerATK.getFlowIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(generatedUTicketJson));

        // WHEN: Apply Flow (holderApplyUTicket)
        createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal issuer private key, legal authorization (issuer signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_ISSUER_SIGNATURE on ACCESS UTICKET"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    @Test
    public void testFailWhenInterceptAndPreemptToApplyTheUTicket() {
        currentTestGivenLog();

        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized EP's CS
        this.cloudServerEP = enterpriseProviderServer();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Preempt
        currentTestWhenAndThenLog();

        // WHEN: Interception (TYPE_ACCESS_UTICKET)
        // Because TYPE_ACCESS_UTICKET has sent on BC, & maybe have been sent in WPAN (Reopen Session) the attacker can intercept & preempt it.
        createSimulatedCommConnection(this.userAgentDO, this.cloudServerEP);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_ACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        waitSimulatedCommCompleted(this.cloudServerEP, this.userAgentDO);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (_holderRecvUTicket)
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.cloudServerEP.getSharedData().getReceivedMessageJson();
        this.cloudServerATK.getFlowIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));

        // WHEN: Preempt (holderApplyUTicket)
        createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId2, generatedCommand);
        waitSimulatedCommCompleted(this.cloudServerATK,this.iotDevice);

        // THEN: Because no legal holder private key, legal authentication (holder signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_HOLDER_SIGNATURE"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    @Test
    public void testFailWhenInterceptAndReuseTheUTicket() {
        currentTestGivenLog();

        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized EP's CS
        this.cloudServerEP = enterpriseProviderServer();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Preempt
        currentTestWhenAndThenLog();

        // WHEN: Interception (TYPE_ACCESS_UTICKET)
        // Because TYPE_ACCESS_UTICKET has sent on BC, & maybe have been sent in WPAN (Reopen Session) the attacker can intercept & preempt it.
        createSimulatedCommConnection(this.userAgentDO, this.cloudServerEP);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_ACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        waitSimulatedCommCompleted(this.cloudServerEP, this.userAgentDO);

        // WHEN: Holder: EP's CS forward the accessUTicket
        createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerEP.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // GIVEN: EP's CS close the session (ACCESS_END)
        createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String generatedCommand2 = "ACCESS_END";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId, generatedCommand2, true);
        waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (_holderRecvUTicket)
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId2).getDeviceUTicketForOwner();
        this.cloudServerATK.getFlowIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));

        // WHEN: Reuse (holderApplyUTicket)
        createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand3 = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId2, generatedCommand3);
        waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal holder private key, legal authentication (holder signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_TICKET_ORDER"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }
}

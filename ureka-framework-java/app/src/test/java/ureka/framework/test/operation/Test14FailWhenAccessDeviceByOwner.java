package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ureka.framework.Conftest.attackerServer;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.deviceManufacturerServerAndHerDevice;
import static ureka.framework.Conftest.deviceOwnerAgent;
import static ureka.framework.Conftest.deviceOwnerAgentAndHerDevice;
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

public class Test14FailWhenAccessDeviceByOwner {
    private DeviceController iotDevice;
    private DeviceController userAgentDO;
    private DeviceController cloudServerATK;

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

        // WHEN: Pretend Holder: Owner
        this.cloudServerATK.getSharedData().getDeviceTable().put(targetDeviceId, new OtherDevice(
                targetDeviceId, interceptedUTicketJson, interceptedRTicketJson, 2
        ));
        // WHEN: Forge Flow (issuerIssueUTicketToHerself)
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.cloudServerATK.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        this.cloudServerATK.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

        // WHEN: Apply Flow (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK,this.iotDevice);

        // THEN: Because no legal holder private key, legal authentication (holder id + signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_HOLDER_ID"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    @Test
    public void testFailWhenInterceptAndPreemptToApplyTheUTicket() {
        currentTestGivenLog();

        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Preempt
        currentTestWhenAndThenLog();

        // WHEN: Interception (Know Latest State)
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceUTicketForOwner();
        String interceptedRTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceRTicketForOwner();

        // WHEN: Pretend Holder: Owner
        this.cloudServerATK.getSharedData().getDeviceTable().put(targetDeviceId, new OtherDevice(
                targetDeviceId, interceptedUTicketJson, interceptedRTicketJson, 2
        ));
        // WHEN: Interception (TYPE_SELFACCESS_UTICKET)
        // Enough TYPE_SELFACCESS_UTICKET has NOT on BC, it maybe has been sent in WPAN (Reopen Session) the attacker can intercept & preempt it.
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId2,
                "holderId", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (issuer_issue_u_ticket_to_herself)
        String targetDeviceId3 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson2 = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId3).getDeviceUTicketForOwner();
        this.cloudServerATK.getGeneratedMsgStorer().storeGeneratedXxxUTicket(interceptedUTicketJson2);
        this.cloudServerATK.getExecutor().executeUpdateTicketOrder("holderGenerateOrReceiveUTicket", UTicket.jsonStrToUTicket(interceptedUTicketJson2));

        // WHEN: Preempt (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId3,generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK,this.iotDevice);

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

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Reuse
        currentTestWhenAndThenLog();

        // WHEN: Interception (Know Latest State)
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceUTicketForOwner();
        String interceptedRTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceRTicketForOwner();

        // WHEN: Pretend Holder: Other
        this.cloudServerATK.getSharedData().getDeviceTable().put(targetDeviceId, new OtherDevice(
                targetDeviceId, interceptedUTicketJson, interceptedRTicketJson, 2
        ));
        // WHEN: Interception (TYPE_SELFACCESS_UTICKET)
        // Enough TYPE_SELFACCESS_UTICKET has NOT on BC, it maybe has been sent in WPAN (Reopen Session) the attacker can intercept & preempt it.
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId2,
                "holderId", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

        // WHEN: Holder: DO's UA forward the selfAccessUTicket
        // createSimulatedCommConnection(this.userAgentDO, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.iotDevice);

        // WHEN: DO's UA close the session (ACCESS_END)
        // createSimulatedCommConnection(this.userAgentDO, this.iotDevice);
        String generatedCommand2 = "ACCESS_END";
        this.userAgentDO.getFlowIssueUToken().holderSendCmd(targetDeviceId,generatedCommand2,true);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.iotDevice);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (issuerIssueUTicketToHerself)
        String targetDeviceId3 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson2 = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId3).getDeviceUTicketForOwner();
        this.cloudServerATK.getGeneratedMsgStorer().storeGeneratedXxxUTicket(interceptedUTicketJson2);
        this.cloudServerATK.getExecutor().executeUpdateTicketOrder("holderGenerateOrReceiveUTicket",UTicket.jsonStrToUTicket(interceptedUTicketJson2));

        // WHEN: Reuse (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand3 = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId3,generatedCommand3);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal holder private key, legal authentication (holder signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_TICKET_ORDER"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }
}


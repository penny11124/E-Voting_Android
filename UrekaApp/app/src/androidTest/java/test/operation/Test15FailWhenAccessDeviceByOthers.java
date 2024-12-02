package test.operation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static Conftest.attackerServer;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceOwnerAgentAndHerDevice;
import static Conftest.enterpriseProviderServer;

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
    // @BeforeEach
    public void setup() {
        currentSetupLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // RE-GIVEN: Reset the test environment
    // @AfterEach
    public void teardown() {
        currentTeardownLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // Threat: (S) Spoofing, (T) Tampering, (E) Elevation of Privilege
    // @Test
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
                "device_id", targetDeviceId,
                "holder_id", this.cloudServerATK.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "u_ticket_type", UTicket.TYPE_ACCESS_UTICKET,
                "task_scope", generatedTaskScope
        );
        String generatedUTicketJson = this.cloudServerATK.getMsgGenerator().generateXxxUTicket(generatedRequest);

        // WHEN: Pretend Holder: Other
        // WHEN: Forge Flow (_holderRecvUTicket)
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(generatedUTicketJson));

        // WHEN: Apply Flow (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal issuer private key, legal authorization (issuer signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_ISSUER_SIGNATURE on ACCESS UTICKET"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    // @Test
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
        // createSimulatedCommConnection(this.userAgentDO, this.cloudServerEP);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "device_id", targetDeviceId,
                "holder_id", this.cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "u_ticket_type", UTicket.TYPE_ACCESS_UTICKET,
                "task_scope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP, this.userAgentDO);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (_holderRecvUTicket)
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.cloudServerEP.getSharedData().getReceivedMessageJson();
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));

        // WHEN: Preempt (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId2, generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK,this.iotDevice);

        // THEN: Because no legal holder private key, legal authentication (holder signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_HOLDER_SIGNATURE"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    // @Test
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
        // createSimulatedCommConnection(this.userAgentDO, this.cloudServerEP);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "device_id", targetDeviceId,
                "holder_id", this.cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "u_ticket_type", UTicket.TYPE_ACCESS_UTICKET,
                "task_scope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP, this.userAgentDO);

        // WHEN: Holder: EP's CS forward the accessUTicket
        // createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerEP.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // GIVEN: EP's CS close the session (ACCESS_END)
        // createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String generatedCommand2 = "ACCESS_END";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId, generatedCommand2, true);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (_holderRecvUTicket)
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId2).getDeviceUTicketForOwner();
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));

        // WHEN: Reuse (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand3 = "HELLO-1";
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId2, generatedCommand3);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal holder private key, legal authentication (holder signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_TICKET_ORDER"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    public void runAll() {
        setup();
        testFailWhenForgeHolderIdAndIssuerSigAndApplyTheUTicket();
        teardown();
        setup();
        testFailWhenInterceptAndPreemptToApplyTheUTicket();
        teardown();
        setup();
        testFailWhenInterceptAndReuseTheUTicket();
        teardown();
    }
}

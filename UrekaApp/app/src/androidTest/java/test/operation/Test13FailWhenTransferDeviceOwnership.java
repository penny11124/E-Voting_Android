package test.operation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static Conftest.attackerServer;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceManufacturerServerAndHerDevice;
import static Conftest.deviceOwnerAgent;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;

public class Test13FailWhenTransferDeviceOwnership {
    private DeviceController cloudServerDM;
    private DeviceController iotDevice;
    private DeviceController cloudServerATK;
    private DeviceController userAgentDO;

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

        // GIVEN: Initialized DM's CS and DM's IoTD
        currentTestGivenLog();
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDM = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept, Forge, & Apply
        currentTestWhenAndThenLog();

        // WHEN: Interception (Know Latest State)
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.cloudServerDM.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceUTicketForOwner();
        String interceptedRTicketJson = this.cloudServerDM.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceRTicketForOwner();

        // WHEN: Pretend Issuer: Owner
        this.cloudServerATK.getSharedData().getDeviceTable().put(targetDeviceId, new OtherDevice(
                targetDeviceId, interceptedUTicketJson, interceptedRTicketJson,1
        ));
        // WHEN: Forge Flow (issuer_issue_u_ticket_to_holder)
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.cloudServerATK.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET
        );
        String generatedUTicketJson = this.cloudServerATK.getMsgGenerator().generateXxxUTicket(generatedRequest);

        // WHEN: Pretend Holder: Other
        // WHEN: Forge Flow (_holderRecvUTicket)
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(generatedUTicketJson));

        // WHEN: Apply Flow (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK,this.iotDevice);
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal issuer private key, legal authorization (issuer signature) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_ISSUER_SIGNATURE on OWNERSHIP UTICKET"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    // @Test
    public void testFailWhenInterceptAndPreemptToApplyTheUTicket() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        currentTestGivenLog();
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDM = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized DO's UA
        this.userAgentDO = deviceOwnerAgent();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Preempt
        currentTestWhenAndThenLog();


        // WHEN: Interception (TYPE_OWNERSHIP_UTICKET)
        // Because TYPE_OWNERSHIP_UTICKET has sent on BC, the attacker can intercept & preempt it.
        // createSimulatedCommConnection(this.cloudServerDM, this.userAgentDO);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET
        );
        this.cloudServerDM.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId,generatedRequest);
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.cloudServerDM);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (_holderRecvUTicket)
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.userAgentDO.getSharedData().getReceivedMessageJson();
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));

        // WHEN: Preempt (holder_apply_u_ticket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId2);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no CR involved, attacker can execute ownership transfer for new owner, but the RTicket will be lost
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> SUCCESS: VERIFY_UT_CAN_EXECUTE"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> SUCCESS: VERIFY_UT_HAS_EXECUTED"));
    }

    // @Test
    public void testFailWhenInterceptAndReuseTheUTicket() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        currentTestGivenLog();
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDM = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized DO's UA
        this.userAgentDO = deviceOwnerAgent();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Reuse
        currentTestWhenAndThenLog();

        // WHEN: Interception (TYPE_OWNERSHIP_UTICKET)
        // Because TYPE_OWNERSHIP_UTICKET has sent on BC, the attacker can intercept & reuse it.
        // createSimulatedCommConnection(this.cloudServerDM, this.userAgentDO);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET
        );
        this.cloudServerDM.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId,generatedRequest);
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.cloudServerDM);

        // WHEN: Holder: DO's UA forward the TYPE_OWNERSHIP_UTICKET
        // createSimulatedCommConnection(this.userAgentDO, this.iotDevice);
        this.userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.iotDevice);

        // WHEN: Pretend Holder: Other
        // WHEN: Interception (_holderRecvUTicket)
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId2).getDeviceUTicketForOwner();
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));

        // WHEN: Reuse (holderApplyUTicket)
        // createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        this.cloudServerATK.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId2);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerATK.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no CR involved, attacker can execute ownership transfer for new owner, but no effective harm for new owner (just a little strange)
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


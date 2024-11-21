package test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceManufacturerServerAndHerDevice;
import static Conftest.deviceOwnerAgent;
import static Conftest.deviceOwnerAgentAndHerDevice;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;

public class Test03SuccessWhenTransferDeviceOwnership {
    private DeviceController cloudServerDm;
    private DeviceController iotDevice;
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

    // @Test
    public void testSuccessWhenApplyOwnershipUTicketOnDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDm = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(), this.cloudServerDm.getSharedData().getThisPerson().getPersonPubKeyStr());

        // GIVEN: Initialized DO's UA
        this.userAgentDO = deviceOwnerAgent();

        // WHEN:
        currentTestWhenAndThenLog();
        // WHEN: Issuer: DM's CS generate & send the ownership_u_ticket to DO's UA
        // createSimulatedCommConnection(this.cloudServerDm,this.userAgentDO);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        Integer originalDeviceOrder = this.iotDevice.getSharedData().getThisDevice().getTicketOrder();
        Integer originalAgentOrder = this.cloudServerDm.getSharedData().getDeviceTable().get(targetDeviceId).getTicketOrder();

        Map<String, String> generatedRequest = Map.of(
                "deviceId", targetDeviceId,
                "holderId", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET
        );
        this.cloudServerDm.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId,generatedRequest);
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.cloudServerDm);

        // WHEN: Holder: DO's UA forward the ownershipUTicket
        // createSimulatedCommConnection(this.userAgentDO,this.iotDevice);
        this.userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.iotDevice);

        // THEN: Succeed to transfer ownership (become DO's IoTD)
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.userAgentDO.getSharedData().getResultMessage().contains("SUCCESS"));

        // THEN: Device: Set New Owner
        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(), this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr());
        // THEN: Device: Update ticket order, DM's CS cannot access DO's IoTD anymore
        assertEquals(originalDeviceOrder + 1, this.iotDevice.getSharedData().getThisDevice().getTicketOrder());
        assertEquals(originalAgentOrder + 1, this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId).getTicketOrder());

        // WHEN: Holder: DO's UA return the ownershipRTicket to DM's CS
        // createSimulatedCommConnection(this.cloudServerDm,this.userAgentDO);
        this.userAgentDO.getFlowIssuerIssueUTicket().holderSendRTicketToIssuer(targetDeviceId);
        this.cloudServerDm.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerDm,this.userAgentDO);

        // THEN: Issuer: DM's CS know that DO's UA has become the new owner of DO's IoTD (& ticket order++)
        assertTrue(this.cloudServerDm.getSharedData().getResultMessage().contains("SUCCESS"));
        assertNull(this.cloudServerDm.getSharedData().getDeviceTable().get(targetDeviceId));
    }

    // Skipped
    // @Test
    public void testSuccessWhenRebootDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // WHEN: Reboot DM's IoTD
        currentTestWhenAndThenLog();
        this.iotDevice.rebootDevice();

        // THEN: Still be Initialized DM's IoTD
        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(), this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr());
    }

    public void runAll() {
        setup();
        testSuccessWhenApplyOwnershipUTicketOnDevice();
        teardown();
    }
}


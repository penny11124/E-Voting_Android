package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.deviceManufacturerServer;
import static ureka.framework.Conftest.deviceManufacturerServerAndHerDevice;
import static ureka.framework.Conftest.waitSimulatedCommCompleted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class Test02SuccessWhenInitializeDevice {
    private DeviceController cloudServerDm;
    private DeviceController iotDevice;

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

    @Test
    public void testSuccessWhenApplyInitializationUTicketOnDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS
        this.cloudServerDm = deviceManufacturerServer();

        // GIVEN: Uninitialized IoTD
        this.iotDevice = new DeviceController(ThisDevice.IOT_DEVICE,"iotDevice");

        assertEquals(0,this.iotDevice.getSharedData().getThisDevice().getTicketOrder());
        assertNull(this.iotDevice.getSharedData().getThisDevice().getDevicePrivKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisPerson().getPersonPrivKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisPerson().getPersonPubKeyStr());

        // WHEN:
        currentTestWhenAndThenLog();
        // WHEN: Issuer: DM's CS generate the initializationUTicket to herself
        String idForInitializationUTicket = "noId";
        Map<String, String> generatedRequest = Map.of(
                "deviceId", idForInitializationUTicket,
                "holderId", this.cloudServerDm.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET
        );
        this.cloudServerDm.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(idForInitializationUTicket,generatedRequest);
        // WHEN: Holder: DM's CS forward the accessUTicket to Uninitialized IoTD
        // createSimulatedCommConnection(this.cloudServerDm, this.iotDevice);
        this.cloudServerDm.getFlowApplyUTicket().holderApplyUTicket(idForInitializationUTicket);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerDm.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerDm, this.iotDevice);

        // THEN: Succeed to initialize DM's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerDm.getSharedData().getResultMessage().contains("SUCCESS"));

        // THEN: Device: Set New Owner, Update ticket order
        assertEquals(1, this.iotDevice.getSharedData().getThisDevice().getTicketOrder());
        assertNotNull(this.iotDevice.getSharedData().getThisDevice().getDevicePrivKeyStr());
        assertNotNull(this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr());
        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(),this.cloudServerDm.getSharedData().getThisPerson().getPersonPubKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisPerson().getPersonPrivKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisPerson().getPersonPubKeyStr());
    }

//    @Test
    public void testSuccessWhenRebootDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDm = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // WHEN: Reboot DM's IoTD
        currentTestWhenAndThenLog();
        this.iotDevice.rebootDevice();

        // THEN: Still be Initialized DM's IoTD
        assertEquals(1,this.iotDevice.getSharedData().getThisDevice().getTicketOrder());
        assertNotNull(this.iotDevice.getSharedData().getThisDevice().getDevicePrivKeyStr());
        assertNotNull(this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr());
        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(), this.iotDevice.getSharedData().getThisPerson().getPersonPubKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisPerson().getPersonPrivKeyStr());
        assertNull(this.iotDevice.getSharedData().getThisPerson().getPersonPubKeyStr());
    }
}



package test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceManufacturerServer;


import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;

// Test Fixtures
public class Test01SuccessWhenInitializeAgentOrServer {
    private DeviceController cloudServerDm;

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
    public void testSuccessWhenInitializeAgentOrServer() {
        currentTestGivenLog();

        // GIVEN: Uninitialized CS
        this.cloudServerDm = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "cloudServerDm");

        assertEquals(0, this.cloudServerDm.getSharedData().getThisDevice().getTicketOrder());
        assertNull(this.cloudServerDm.getSharedData().getThisDevice().getDevicePrivKeyStr());
        assertNull(this.cloudServerDm.getSharedData().getThisDevice().getDevicePubKeyStr());
        assertNull(this.cloudServerDm.getSharedData().getThisDevice().getOwnerPubKeyStr());
        assertNull(this.cloudServerDm.getSharedData().getThisPerson().getPersonPrivKeyStr());
        assertNull(this.cloudServerDm.getSharedData().getThisPerson().getPersonPubKeyStr());

        // WHEN: DM apply _executeOneTimeInitializeAgentOrServer() on Uninitialized CS
        currentTestWhenAndThenLog();
        this.cloudServerDm.getExecutor()._executeOneTimeInitializeAgentOrServer();

        // THEN: Succeed to initialized DM's CS
        assertEquals(1, this.cloudServerDm.getSharedData().getThisDevice().getTicketOrder());
        assertNotNull(this.cloudServerDm.getSharedData().getThisDevice().getDevicePrivKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisDevice().getDevicePubKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisDevice().getOwnerPubKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisPerson().getPersonPrivKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisPerson().getPersonPubKeyStr());
    }

    // Skipped
    // @Test
    public void testSuccessWhenReboot() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS
        this.cloudServerDm = deviceManufacturerServer();

        // WHEN: Reboot DM's CS
        currentTestWhenAndThenLog();
        this.cloudServerDm.rebootDevice();

        // THEN: Still be Initialized DM's CS
        assertEquals(1, this.cloudServerDm.getSharedData().getThisDevice().getTicketOrder());
        assertNotNull(this.cloudServerDm.getSharedData().getThisDevice().getDevicePrivKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisDevice().getDevicePubKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisDevice().getOwnerPubKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisPerson().getPersonPrivKeyStr());
        assertNotNull(this.cloudServerDm.getSharedData().getThisPerson().getPersonPubKeyStr());
    }

    public void runAll() {
        setup();
        testSuccessWhenInitializeAgentOrServer();
        teardown();
    }
}

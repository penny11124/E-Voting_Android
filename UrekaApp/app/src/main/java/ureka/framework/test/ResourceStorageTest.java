package ureka.framework.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.deviceManufacturerServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class ResourceStorageTest {
    private SimpleStorage simpleStorage;
    private DeviceController cloudServerDM;

    // RE-GIVEN: Reset the test environment
    @BeforeEach
    public void setup() {
        currentSetupLog();
        // simpleStorage.deleteStorageInTest();
    }

    // RE-GIVEN: Reset the test environment
    @AfterEach
    public void teardown() {
        currentTeardownLog();
        // simpleStorage.deleteStorageInTest();
    }

    @Test
    public void testStoreAndLoadThisDevice() {
        currentTestGivenLog();

        // GIVEN: GIVEN: A SimpleStorage
        this.simpleStorage = new SimpleStorage("testStorage");

        // GIVEN: An initialized DM's CS as test data
       this.cloudServerDM = deviceManufacturerServer();

        // WHEN: Variables are modified in the RAM
        currentTestWhenAndThenLog();
        this.cloudServerDM.getSharedData().getThisDevice().setDeviceName("anotherNewDeviceName");

        // WHEN: Variables are stored in the Storage
        this.simpleStorage.storeStorage(
                this.cloudServerDM.getSharedData().getThisDevice(),
                this.cloudServerDM.getSharedData().getDeviceTable(),
                this.cloudServerDM.getSharedData().getThisPerson(),
                this.cloudServerDM.getSharedData().getCurrentSession()
        );

        // WHEN: Variables are loaded from the Storage
        SimpleStorage.Tuple tuple = this.simpleStorage.loadStorage();
        ThisDevice updatedThisDevice = tuple.thisDevice;
        System.out.println(updatedThisDevice.getDeviceName());

        // THEN: Check variables loaded from the Storage
        assertEquals("anotherNewDeviceName", updatedThisDevice.getDeviceName());
    }

    @Test
    public void testStoreAndLoadDeviceTable() {
        currentTestGivenLog();

        // GIVEN: A SimpleStorage
        simpleStorage = new SimpleStorage("testStorage");

        // GIVEN: An initialized DM's CS as test data
        this.cloudServerDM = deviceManufacturerServer();
        SimpleLogger.simpleLog("debug","Original Other Devices in RAM: " + OtherDevice.deviceTableToJsonStr(this.cloudServerDM.getSharedData().getDeviceTable()));

        // WHEN: Variables are modified in the RAM
        currentTestWhenAndThenLog();
        this.cloudServerDM.getSharedData().getThisDevice().setDeviceName("anotherNewDeviceName");

        // Modify the device table in the RAM
        OtherDevice device1 = new OtherDevice("deviceId1", "uTicketJson1");
        OtherDevice device2 = new OtherDevice("deviceId2", "uTicketJson2");
        Map<String, OtherDevice> deviceTable = new HashMap<>();
        deviceTable.put("deviceId1", device1);
        deviceTable.put("deviceId2", device2);
        this.cloudServerDM.getSharedData().setDeviceTable(deviceTable);
        SimpleLogger.simpleLog("debug","Modified Other Devices in RAM: " + OtherDevice.deviceTableToJsonStr(this.cloudServerDM.getSharedData().getDeviceTable()));

        // WHEN: Variables are stored in the Storage
        this.simpleStorage.storeStorage(
                this.cloudServerDM.getSharedData().getThisDevice(),
                this.cloudServerDM.getSharedData().getDeviceTable(),
                this.cloudServerDM.getSharedData().getThisPerson(),
                this.cloudServerDM.getSharedData().getCurrentSession()
        );

        // WHEN: Variables are loaded from the Storage
        SimpleStorage.Tuple tuple = this.simpleStorage.loadStorage();
        Map<String, OtherDevice> updatedDeviceTable = tuple.deviceTable;

        SimpleLogger.simpleLog("debug", "Loaded Other Devices from Storage =" + OtherDevice.deviceTableToJsonStr(updatedDeviceTable));

        // THEN: Check SimpleStorage/testStorage/deviceTable.json to ensure the variables are stored correctly
        // THEN: The variables loaded from the Storage should be the same with the variables modified in the RAM
        assertEquals("deviceId1", updatedDeviceTable.get("deviceId1").getDeviceId());
        assertEquals("uTicketJson1", updatedDeviceTable.get("deviceId1").getDeviceUTicketForOwner());
        assertEquals("deviceId2", updatedDeviceTable.get("deviceId2").getDeviceId());
        assertEquals("uTicketJson2", updatedDeviceTable.get("deviceId2").getDeviceUTicketForOwner());
    }

    @Test
    public void testCreateExistedDir() {
        currentTestGivenLog();

        // GIVEN: A SimpleStorage
        simpleStorage = new SimpleStorage("testStorage");

        // WHEN: A repeated SimpleStorage is created
        currentTestWhenAndThenLog();
        SimpleStorage anotherStorage = new SimpleStorage("testStorage");

        // THEN: It's ok to create a repeated SimpleStorage
        SimpleLogger.simpleLog("info", "Successfully created or verified the existence of the directory.");
    }
}

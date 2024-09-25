package ureka.framework.resource.storage;

import com.google.common.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.resource.logger.SimpleLogger;

public class SimpleStorage {
    private final String deviceName;
    private Map<String, String> inMemoryStorage;

    public SimpleStorage(String deviceName) {
        this.deviceName = deviceName;
        this.inMemoryStorage = new HashMap<>();
    }
    public void storeStorage(ThisDevice thisDevice, Map<String, OtherDevice> deviceTable,
                             ThisPerson thisPerson, CurrentSession currentSession) {
        try {
            this.inMemoryStorage.put("thisDevice", ThisDevice.thisDeviceToJsonStr(thisDevice));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.inMemoryStorage.put("deviceTable", OtherDevice.deviceTableToJsonStr(deviceTable));
        this.inMemoryStorage.put("thisPerson", ThisPerson.thisPersonToJsonStr(thisPerson));
        this.inMemoryStorage.put("currentSession", CurrentSession.currentSessionToJsonStr(currentSession));

        System.out.println("In-memory storage contents:");
        for (Map.Entry<String, String> entry : this.inMemoryStorage.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public Tuple loadStorage() {
        String thisDeviceJsonStr = this.inMemoryStorage.get("thisDevice");
        String deviceTableJsonStr = this.inMemoryStorage.get("deviceTable");
        String thisPersonJsonStr = this.inMemoryStorage.get("thisPerson");
        String currentSessionJsonStr = this.inMemoryStorage.get("currentSession");

        System.out.println("Load Device: " + thisDeviceJsonStr);
        System.out.println("Load DeviceTable: " + deviceTableJsonStr);
        System.out.println("Load Person: " + thisPersonJsonStr);
        System.out.println("Load CS: " + currentSessionJsonStr);

        ThisDevice loadThisDevice = null;
        try {
            loadThisDevice = ThisDevice.jsonStrToThisDevice(thisDeviceJsonStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, OtherDevice> loadDeviceTable = OtherDevice.jsonStrToDeviceTable(deviceTableJsonStr);
        ThisPerson loadThisPerson = ThisPerson.jsonStrToThisPerson(thisPersonJsonStr);
        CurrentSession loadCurrentSession = CurrentSession.jsonStrToCurrentSession(currentSessionJsonStr);
        return new Tuple(loadThisDevice, loadDeviceTable, loadThisPerson, loadCurrentSession);
    }

    public void deleteStorageInTest() {
        this.setInMemoryStorage(null);
        SimpleLogger.simpleLog("info", "Storage cleared.");
    }

    public static class Tuple {
        public ThisDevice thisDevice;
        public Map<String, OtherDevice> deviceTable;
        public ThisPerson thisPerson;
        public CurrentSession currentSession;
        public Tuple(ThisDevice thisDevice, Map<String, OtherDevice> deviceTable,
                     ThisPerson thisPerson, CurrentSession currentSession) {
            this.thisDevice = thisDevice;
            this.deviceTable = deviceTable;
            this.thisPerson = thisPerson;
            this.currentSession = currentSession;
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Map<String, String> getInMemoryStorage() {
        return inMemoryStorage;
    }

    public void setInMemoryStorage(Map<String, String> inMemoryStorage) {
        this.inMemoryStorage = inMemoryStorage;
    }
}

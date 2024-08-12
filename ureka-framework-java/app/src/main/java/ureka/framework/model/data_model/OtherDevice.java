package ureka.framework.model.data_model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class OtherDevice {
    private String deviceId = null;
    private Integer ticketOrder = null;
    // URequest, UTicket, UReject, etc.
    private String deviceUTicketForOwner = null;
    private String deviceOwnershipUTicketForOthers = null;
    private String deviceAccessUTicketForOthers = null;

    // URequest, RTicket, etc.
    private String deviceRTicketForOwner = null;
    private String deviceOwnershipRTicketForOthers = null;
    private String deviceAccessEndRTicketForOthers = null;

    public OtherDevice() {}
    public OtherDevice(String deviceId, String deviceUTicketForOwner, String deviceRTicketForOwner, Integer ticketOrder) {
        this.deviceId = deviceId;
        this.deviceUTicketForOwner = deviceUTicketForOwner;
        this.deviceRTicketForOwner = deviceRTicketForOwner;
        this.ticketOrder = ticketOrder;
    }
    public OtherDevice(String deviceId, String deviceUTicketForOwner, String deviceRTicketForOwner) {
        this.deviceId = deviceId;
        this.deviceUTicketForOwner = deviceUTicketForOwner;
        this.deviceRTicketForOwner = deviceRTicketForOwner;
    }
    public OtherDevice(String deviceId, String deviceUTicketForOwner) {
        this.deviceId = deviceId;
        this.deviceUTicketForOwner = deviceUTicketForOwner;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getTicketOrder() {
        return ticketOrder;
    }

    public void setTicketOrder(Integer ticketOrder) {
        this.ticketOrder = ticketOrder;
    }

    public String getDeviceUTicketForOwner() {
        return deviceUTicketForOwner;
    }

    public void setDeviceUTicketForOwner(String deviceUTicketForOwner) {
        this.deviceUTicketForOwner = deviceUTicketForOwner;
    }

    public String getDeviceOwnershipUTicketForOthers() {
        return deviceOwnershipUTicketForOthers;
    }

    public void setDeviceOwnershipUTicketForOthers(String deviceOwnershipUTicketForOthers) {
        this.deviceOwnershipUTicketForOthers = deviceOwnershipUTicketForOthers;
    }

    public String getDeviceAccessUTicketForOthers() {
        return deviceAccessUTicketForOthers;
    }

    public void setDeviceAccessUTicketForOthers(String deviceAccessUTicketForOthers) {
        this.deviceAccessUTicketForOthers = deviceAccessUTicketForOthers;
    }

    public String getDeviceRTicketForOwner() {
        return deviceRTicketForOwner;
    }

    public void setDeviceRTicketForOwner(String deviceRTicketForOwner) {
        this.deviceRTicketForOwner = deviceRTicketForOwner;
    }

    public String getDeviceOwnershipRTicketForOthers() {
        return deviceOwnershipRTicketForOthers;
    }

    public void setDeviceOwnershipRTicketForOthers(String deviceOwnershipRTicketForOthers) {
        this.deviceOwnershipRTicketForOthers = deviceOwnershipRTicketForOthers;
    }

    public String getDeviceAccessEndRTicketForOthers() {
        return deviceAccessEndRTicketForOthers;
    }

    public void setDeviceAccessEndRTicketForOthers(String deviceAccessEndRTicketForOthers) {
        this.deviceAccessEndRTicketForOthers = deviceAccessEndRTicketForOthers;
    }

    public Map<String, String> _otherDeviceToMap() {
        Map<String, String> otherDeviceMap = new HashMap<>();
        Field[] fields = OtherDevice.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true); // Ensure private fields are accessible
                Object value = field.get(this);
                otherDeviceMap.put(field.getName(), value != null ? value.toString() : null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return otherDeviceMap;
    }

    public static OtherDevice _mapToOtherDevice(Map<String, String> otherDeviceMap) {
        OtherDevice otherDevice = new OtherDevice();
        Field[] fields = OtherDevice.class.getDeclaredFields();
        for (Field field : fields) {
            if (otherDeviceMap.containsKey(field.getName())) {
                try {
                    field.setAccessible(true);
                    Object value = otherDeviceMap.get(field.getName());
                    if (field.getType() == Integer.class) {
                        value = Integer.valueOf((String) value);
                    }
                    field.set(otherDevice, value);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return otherDevice;
    }

    public static String deviceTableToJsonstr(Map<String, OtherDevice> otherDeviceMap) {
        Map<String, Map<String, String>> convertedMap = new HashMap<>();
        for (Map.Entry<String, OtherDevice> entry : otherDeviceMap.entrySet()) {
            convertedMap.put(entry.getKey(), entry.getValue()._otherDeviceToMap());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(convertedMap);
    }

    public static Map<String, OtherDevice> jsonstrToDeviceTable(String jsonStr) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
        try {
            Map<String, Map<String, String>> rawMap = gson.fromJson(jsonStr, type);
            Map<String, OtherDevice> deviceTable = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> entry : rawMap.entrySet()) {
                deviceTable.put(entry.getKey(), OtherDevice._mapToOtherDevice(entry.getValue()));
            }
            return deviceTable;
        } catch (JsonParseException e) {
            String failureMsg = "NOT VALID JSON or VALID SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

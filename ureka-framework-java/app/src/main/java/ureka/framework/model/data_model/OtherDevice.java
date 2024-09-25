package ureka.framework.model.data_model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.dsig.SignatureMethod;

import ureka.framework.resource.logger.SimpleLogger;

public class OtherDevice {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Class<?> otherDeviceClass = this.getClass();

        for (Field field : otherDeviceClass.getDeclaredFields()) {
            try {
                Object value1 = field.get(this), value2 = field.get(obj);
                if (value1 == null && value2 != null) {
                    return false;
                } else if (value1 != null && value2 == null) {
                    return false;
                } else if (value1 != null && !value1.equals(value2)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return true;
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

    public static Map<String, String> _otherDeviceToMap(OtherDevice otherDevice) throws IllegalAccessException {
        Map<String, String> otherDeviceMap = new HashMap<>();
        Class<?> otherDeviceClass = otherDevice.getClass();

        for (Field field : otherDeviceClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(otherDevice);

                if (value != null) {
                    if (field.getType().equals(Integer.class)) {
                        otherDeviceMap.put(field.getName(), value.toString());
                    } else if (field.getType().equals(String.class)) {
                        otherDeviceMap.put(field.getName(), (String) value);
                    }
                } else {
                    otherDeviceMap.put(field.getName(), null);
                }
            } catch (IllegalAccessException e) {
                String failureMsg = "OtherDevice._otherDeviceToMap: IllegalAccessException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            }
        }

        return otherDeviceMap;
    }

    public static OtherDevice _mapToOtherDevice(Map<String, String> otherDeviceMap)
        throws NoSuchFieldException, IllegalAccessException {
        OtherDevice otherDevice = new OtherDevice();
        Class<?> otherDeviceClass = otherDevice.getClass();

        for (Map.Entry<String, String> entry : otherDeviceMap.entrySet()) {
            try {
                Field field = otherDeviceClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);

                String value = entry.getValue();
                if (value == null) {
                    field.set(otherDevice, null);
                } else if (field.getType().equals(String.class)) {
                    field.set(otherDevice, value);
                } else if (field.getType().equals(Integer.class)) {
                    field.set(otherDevice, Integer.valueOf(value));
                }
            } catch (NoSuchFieldException e) {
                String failureMsg = "OtherDevice._mapToOtherDevice: NoSuchFieldException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            } catch (IllegalAccessException e) {
                String failureMsg = "OtherDevice._mapToOtherDevice: IllegalAccessException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            }
        }

        return otherDevice;
    }

    public static String deviceTableToJsonStr(Map<String, OtherDevice> otherDeviceMap) {
        // We don't need to apply _otherDeviceToMap since GSON will automatically handle it.
        return gson.toJson(otherDeviceMap);
    }

    public static Map<String, OtherDevice> jsonStrToDeviceTable(String jsonStr) {
        Type mapType = new TypeToken<Map<String, OtherDevice>>(){}.getType();

        try {
            return gson.fromJson(jsonStr, mapType);
        } catch (JsonParseException e) {
            String failureMsg = "OtherDevice._mapToOtherDevice: NoSuchFieldException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        }
    }
}

package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Field;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.resource.crypto.SerializationUtil;

public class ThisDevice {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    // Device Type (can be refactored by Inheritance)
    public static final String IOT_DEVICE = "IOT_DEVICE";
    public static final String USER_AGENT_OR_CLOUD_SERVER = "USER_AGENT_OR_CLOUD_SERVER";

    // Device State
    // IOT_DEVICE
    public static final String STATE_DEVICE_WAIT_FOR_UT = "STATE_DEVICE_WAIT_FOR_UT";
    public static final String STATE_DEVICE_WAIT_FOR_CRKE2 = "STATE_DEVICE_WAIT_FOR_CRKE2";
    public static final String STATE_DEVICE_WAIT_FOR_CMD = "STATE_DEVICE_WAIT_FOR_CMD";
    // USER_AGENT_OR_CLOUD_SERVER
    public static final String STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT = "STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT";
    public static final String STATE_AGENT_WAIT_FOR_RT = "STATE_AGENT_WAIT_FOR_RT";
    public static final String STATE_AGENT_WAIT_FOR_CRKE1 = "STATE_AGENT_WAIT_FOR_CRKE1";
    public static final String STATE_AGENT_WAIT_FOR_CRKE3 = "STATE_AGENT_WAIT_FOR_CRKE3";
    public static final String STATE_AGENT_WAIT_FOR_DATA = "STATE_AGENT_WAIT_FOR_DATA";

    // Device Type (Device can be User Agent, Cloud Server, or IoT Device...)
    private String deviceType = null;
    private String deviceName = null;
    private Boolean hasDeviceType = false;

    // Ticket Order
    private Integer ticketOrder = null;

    // Generate Device Key after Initialization
    private ECPrivateKey devicePrivKey = null;
    private ECPublicKey devicePubKey = null;

    // Generate Owner Key after Initialization
    private ECPublicKey ownerPubKey = null;

    public ThisDevice() {}

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Class<?> thisDeviceClass = this.getClass();

        for (Field field : thisDeviceClass.getDeclaredFields()) {
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Boolean getHasDeviceType() {
        return hasDeviceType;
    }

    public void setHasDeviceType(Boolean hasDeviceType) {
        this.hasDeviceType = hasDeviceType;
    }

    public Integer getTicketOrder() {
        return ticketOrder;
    }

    public void setTicketOrder(Integer ticketOrder) {
        this.ticketOrder = ticketOrder;
    }

    public ECPrivateKey getDevicePrivKey() {
        return devicePrivKey;
    }

    // Adapted from @property in Python
    public String getDevicePrivKeyStr() {
        if (this.devicePrivKey == null) {
            return null;
        }
        return SerializationUtil.keyToStr(this.devicePrivKey, "eccPrivateKey");
    }

    public void setDevicePrivKey(ECPrivateKey devicePrivKey) {
        this.devicePrivKey = devicePrivKey;
    }

    public ECPublicKey getDevicePubKey() {
        return devicePubKey;
    }

    // Adapted from @property in Python
    public String getDevicePubKeyStr() {
        if (this.devicePubKey == null) {
            return null;
        }
        return SerializationUtil.keyToStr(this.devicePubKey, "eccPublicKey");
    }

    public void setDevicePubKey(ECPublicKey devicePubKey) {
        this.devicePubKey = devicePubKey;
    }

    public ECPublicKey getOwnerPubKey() {
        return ownerPubKey;
    }

    // Adapted from @property in Python
    public String getOwnerPubKeyStr() {
        if (this.devicePrivKey == null) {
            return null;
        }
        return SerializationUtil.keyToStr(this.ownerPubKey, "eccPublicKey");
    }

    public void setOwnerPubKey(ECPublicKey ownerPubKey) {
        this.ownerPubKey = ownerPubKey;
    }

    public static Map<String, String> _thisDeviceToMap(ThisDevice thisDevice) throws IllegalAccessException {
        Map<String, String> thisDeviceMap = new HashMap<>();
        Class<?> otherDeviceClass = thisDevice.getClass();

        for (Field field : otherDeviceClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(thisDevice);

                if (value != null) {
                    if (field.getType().equals(Integer.class)) {
                        thisDeviceMap.put(field.getName(), value.toString());
                    } else if (field.getType().equals(String.class)) {
                        thisDeviceMap.put(field.getName(), (String) value);
                    } else if (field.getType().equals(ECPrivateKey.class)) {
                        thisDeviceMap.put(field.getName(), SerializationUtil.keyToStr(value));
                    } else if (field.getType().equals(ECPublicKey.class)) {
                        thisDeviceMap.put(field.getName(), SerializationUtil.keyToStr(value));
                    }
                } else {
                    thisDeviceMap.put(field.getName(), null);
                }
            } catch (IllegalAccessException e) {
                String failureMsg = "ThisDevice._thisDeviceToMap: IllegalAccessException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            }
        }
        return thisDeviceMap;
    }

    public static ThisDevice _mapToThisDevice(Map<String, String> thisDeviceMap)
        throws NoSuchFieldException, IllegalAccessException {
        ThisDevice thisDevice = new ThisDevice();
      
        Class<?> thisDeviceClass = thisDevice.getClass();

        for (Map.Entry<String, String> entry : thisDeviceMap.entrySet()) {
            try {
                Field field = thisDeviceClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);

                String value = entry.getValue();
                if (value == null) {
                    field.set(thisDevice, null);
                } else if (field.getType().equals(String.class)) {
                    field.set(thisDevice, value);
                } else if (field.getType().equals(Integer.class)) {
                    field.set(thisDevice, Integer.valueOf(value));
                } else if (field.getType().equals(ECPrivateKey.class)) {
                    field.set(thisDevice, SerializationUtil.strToKey(value, "ecc-private-key"));
                } else if (field.getType().equals(ECPublicKey.class)) {
                    field.set(thisDevice, SerializationUtil.strToKey(value, "ecc-public-key"));
                }
            } catch (NoSuchFieldException e) {
                String failureMsg = "ThisDevice._mapToThisDevice: NoSuchFieldException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            } catch (IllegalAccessException e) {
                String failureMsg = "ThisDevice._mapToThisDevice: IllegalAccessException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            }
        }
        return thisDevice;
    }

    public static String thisDeviceToJsonStr(ThisDevice thisDevice) {
        // We don't need to apply _otherDeviceToMap since GSON will automatically handle it.
        return gson.toJson(thisDevice);
    }

    public static ThisDevice jsonStrToThisDevice(String jsonStr) {
        try {
            if (!isValidJson(jsonStr)) {
                throw new RuntimeException("Invalid JSON string");
            } else {
                System.out.println("VALID!!!");
            }
            return gson.fromJson(jsonStr, ThisDevice.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID RTICKET SCHEMA";
            throw new RuntimeException(failureMsg);
        }
    }

    public static boolean isValidJson(String jsonStr) {
        try {
            JsonParser.parseString(jsonStr);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}

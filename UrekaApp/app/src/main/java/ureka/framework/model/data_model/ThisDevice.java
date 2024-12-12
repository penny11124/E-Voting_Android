package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

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
    private Integer ticket_order = null;

    // Generate Device Key after Initialization
    private PrivateKey devicePrivKey = null;
    private PublicKey devicePubKey = null;

    // Generate Owner Key after Initialization
    private PublicKey ownerPubKey = null;

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
        return ticket_order;
    }

    public void setTicketOrder(Integer ticket_order) {
        this.ticket_order = ticket_order;
    }

    public PrivateKey getDevicePrivKey() {
        return devicePrivKey;
    }

    public void setDevicePrivKey(PrivateKey devicePrivKey) {
        this.devicePrivKey = devicePrivKey;
    }

    public PublicKey getDevicePubKey() {
        return devicePubKey;
    }

    // Adapted from @property in Python
    public String getDevicePubKeyStr() {
        return SerializationUtil.publicKeyToBase64(this.devicePubKey);
    }

    public void setDevicePubKey(PublicKey devicePubKey) {
        this.devicePubKey = devicePubKey;
    }

    public PublicKey getOwnerPubKey() {
        return ownerPubKey;
    }

    // Adapted from @property in Python
    public String getOwnerPubKeyStr() {
        return SerializationUtil.publicKeyToBase64(this.ownerPubKey);
    }

    public void setOwnerPubKey(PublicKey ownerPubKey) {
        this.ownerPubKey = ownerPubKey;
    }

    private static Map<String, String> _thisDeviceToMap(ThisDevice thisDevice) {
        Map<String, String> thisDeviceMap = new HashMap<>();
        Class<?> otherDeviceClass = thisDevice.getClass();

        for (Field field : otherDeviceClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(thisDevice);

                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                if (value != null) {
                    if (field.getType().equals(Integer.class)) {
                        thisDeviceMap.put(field.getName(), value.toString());
                    } else if (field.getType().equals(String.class)) {
                        thisDeviceMap.put(field.getName(), (String) value);
                    } else if (field.getType().equals(PrivateKey.class)) {
                        thisDeviceMap.put(field.getName(), SerializationUtil.privateKeyToBase64((PrivateKey) value));
                    } else if (field.getType().equals(PublicKey.class)) {
                        thisDeviceMap.put(field.getName(), SerializationUtil.publicKeyToBase64((PublicKey) value));
                    }
                } else {
                    thisDeviceMap.put(field.getName(), null);
                }
            } catch (Exception e) {
                String failureMsg = "ThisDevice._thisDeviceToMap: " + e.getMessage();
                throw new RuntimeException(failureMsg);
            }
        }
        return thisDeviceMap;
    }

    private static ThisDevice _mapToThisDevice(Map<String, String> thisDeviceMap) {
        ThisDevice thisDevice = new ThisDevice();
      
        Class<?> thisDeviceClass = thisDevice.getClass();

        for (Map.Entry<String, String> entry : thisDeviceMap.entrySet()) {
            try {
                Field field = thisDeviceClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);

                if (Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                String value = entry.getValue();
                if (value == null) {
                    field.set(thisDevice, null);
                } else if (field.getType().equals(String.class)) {
                    field.set(thisDevice, value);
                } else if (field.getType().equals(Integer.class)) {
                    field.set(thisDevice, Integer.valueOf(value));
                } else if (field.getType().equals(ECPrivateKey.class)) {
                    field.set(thisDevice, SerializationUtil.base64ToPrivateKey(value));
                } else if (field.getType().equals(ECPublicKey.class)) {
                    field.set(thisDevice, SerializationUtil.base64ToPublicKey(value));
                }
            } catch (Exception e) {
                String failureMsg = "ThisDevice._mapToThisDevice: " + e.getMessage();
                throw new RuntimeException(failureMsg);
            }
        }
        return thisDevice;
    }

    public static String thisDeviceToJsonStr(ThisDevice thisDevice) {
        Map<String, String> thisDeviceMap = _thisDeviceToMap(thisDevice);
        return gson.toJson(thisDeviceMap);
    }

    public static ThisDevice jsonStrToThisDevice(String jsonStr) {
        Map<String, String> thisDeviceMap = gson.fromJson(jsonStr, new TypeToken<Map<String, String>>() {}.getType());
        return _mapToThisDevice(thisDeviceMap);
    }
}

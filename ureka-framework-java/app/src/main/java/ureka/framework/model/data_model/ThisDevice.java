package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;

import ureka.framework.resource.crypto.SerializationUtil;

public class ThisDevice {
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
        return SerializationUtil.keyToStr(this.devicePrivKey, "ecc-private-key");
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
        return SerializationUtil.keyToStr(this.devicePubKey, "ecc-public-key");
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
        return SerializationUtil.keyToStr(this.ownerPubKey, "ecc-public-key");
    }

    public void setOwnerPubKey(ECPublicKey ownerPubKey) {
        this.ownerPubKey = ownerPubKey;
    }

    private static Map<String, String> _thisDeviceToMap(ThisDevice thisDevice) {
        Map<String, String> thisDeviceDict = new HashMap<>();

        thisDeviceDict.put("deviceType", thisDevice.getDeviceType());
        thisDeviceDict.put("deviceName", thisDevice.getDeviceName());
        thisDeviceDict.put("hasDeviceType", String.valueOf(thisDevice.getHasDeviceType()));
        if (thisDevice.getTicketOrder() == null) {
            thisDeviceDict.put("ticketOrder", null);
        } else {
            thisDeviceDict.put("ticketOrder",thisDevice.getTicketOrder().toString());
        }

        if (thisDevice.getDevicePrivKey() == null) {
            thisDeviceDict.put("devicePrivKey", null);
        } else {
            thisDeviceDict.put("devicePrivKey", SerializationUtil.keyToStr(thisDevice.getDevicePrivKey(), "ecc-private-key"));
        }
        if (thisDevice.getDevicePubKey() == null) {
            thisDeviceDict.put("devicePubKey", null);
        } else {
            thisDeviceDict.put("devicePubKey", SerializationUtil.keyToStr(thisDevice.getDevicePubKey(), "ecc-public-key"));
        }
        if (thisDevice.getOwnerPubKey() == null) {
            thisDeviceDict.put("ownerPubKey", null);
        } else {
            thisDeviceDict.put("ownerPubKey", SerializationUtil.keyToStr(thisDevice.getOwnerPubKey(), "ecc-public-key"));
        }

        return thisDeviceDict;
    }

    public static ThisDevice _mapToThisDevice(Map<String, String> thisDeviceDict) {
        ThisDevice thisDevice = new ThisDevice();

        thisDevice.setDeviceType(thisDeviceDict.get("deviceType"));
        thisDevice.setDeviceName(thisDeviceDict.get("deviceName"));
        thisDevice.setHasDeviceType(Boolean.parseBoolean(thisDeviceDict.get("hasDeviceType")));
        if (thisDeviceDict.get("ticketOrder") == null) {
            thisDevice.setTicketOrder(null);
        } else {
            thisDevice.setTicketOrder(Integer.parseInt(thisDeviceDict.get(("ticketOrder"))));
        }

        if (thisDeviceDict.get("devicePrivKey") == null) {
            thisDevice.setDevicePrivKey(null);
        } else {
            thisDevice.setDevicePrivKey((ECPrivateKey) SerializationUtil.strToKey(thisDeviceDict.get("devicePrivKey"), "ecc-private-key"));
        }
        if (thisDeviceDict.get("devicePubKey") == null) {
            thisDevice.setDevicePubKey(null);
        } else {
            thisDevice.setDevicePubKey((ECPublicKey) SerializationUtil.strToKey(thisDeviceDict.get("devicePubKey"), "ecc-public-key"));
        }
        if (thisDeviceDict.get("ownerPubKey") == null) {
            thisDevice.setOwnerPubKey(null);
        } else {
            thisDevice.setOwnerPubKey((ECPublicKey) SerializationUtil.strToKey(thisDeviceDict.get("ownerPubKey"), "ecc-public-key"));
        }

        return thisDevice;
    }

    public static String thisDeviceToJsonstr(ThisDevice thisDevice) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ThisDevice.class, (JsonSerializer<ThisDevice>) (src, typeOfSrc, context) -> {
                return context.serialize(_thisDeviceToMap(src));
            })
            .create();

        return gson.toJson(thisDevice);
    }

    public static ThisDevice jsonStrToThisDevice(String jsonStr) {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(ThisDevice.class, (JsonDeserializer<ThisDevice>) (json, typeOfT, context) -> {
                Map<String, String> map = context.deserialize(json, Map.class);
                try {
                    return _mapToThisDevice(map);
                } catch (Exception e) {
                    String failureMsg = "NOT VALID JSON or VALID SCHEMA";
                    // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                    throw new RuntimeException(failureMsg);
                }
            })
            .create();

        try {
            return gson.fromJson(jsonStr, ThisDevice.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

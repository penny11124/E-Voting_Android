package ureka.framework.model.message_model;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ureka.framework.resource.logger.SimpleMeasurer;

public class UTicket {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    //////////////////////////////////////////////////////
    // Protocol Version
    //////////////////////////////////////////////////////
    public static final String PROTOCOL_VERSION = "UREKA-1.0";

    //////////////////////////////////////////////////////
    // Message Type
    //////////////////////////////////////////////////////
    public static final String MESSAGE_TYPE = "UTICKET";

    //////////////////////////////////////////////////////
    // Uticket Type
    //////////////////////////////////////////////////////
    // UTicket
    public static final String TYPE_INITIALIZATION_UTICKET = "INITIALIZATION";
    public static final String TYPE_OWNERSHIP_UTICKET = "OWNERSHIP";
    public static final String TYPE_SELFACCESS_UTICKET = "SELFACCESS";
    public static final String TYPE_ACCESS_UTICKET = "ACCESS";
    // UToken
    public static final String TYPE_CMD_UTOKEN = "CMD_UTOKEN";
    // RToken (ACCESS_END)
    public static final String TYPE_ACCESS_END_UTOKEN = "ACCESS_END";
    // UTicket
    public static final String[] LEGAL_UTICKET_TYPES = {
        TYPE_INITIALIZATION_UTICKET,
        TYPE_OWNERSHIP_UTICKET,
        TYPE_SELFACCESS_UTICKET,
        TYPE_ACCESS_UTICKET,
        TYPE_CMD_UTOKEN,
        TYPE_ACCESS_END_UTOKEN,
    };

    // UT
    private String protocolVersion;
    private String uTicketId;
    private String uTicketType;
    private String deviceId;
    private Integer ticketOrder;
    private String holderId;
    private String taskScope;
    private String issuerSignature;
    // PS-Cmd
    private String associatedPlaintextCmd;
    private String ciphertextCmd;
    private String gcmAuthenticationTagCmd;
    // PS-Data
    private String ivData;

    public UTicket(Map<String, String> values) {
        this.protocolVersion = values.getOrDefault("protocolVersion", PROTOCOL_VERSION);
        this.uTicketId = values.getOrDefault("uTicketId", null);
        this.uTicketType = values.getOrDefault("uTicketType", null);
        this.deviceId = values.getOrDefault("deviceId", null);
        this.ticketOrder = Integer.valueOf(values.getOrDefault("ticketOrder", null));
        this.holderId = values.getOrDefault("holderId", null);
        this.taskScope = values.getOrDefault("taskScope", null);
        this.issuerSignature = values.getOrDefault("issuerSignature", null);
        this.associatedPlaintextCmd = values.getOrDefault("associatedPlaintextCmd", null);
        this.ciphertextCmd = values.getOrDefault("ciphertextCmd", null);
        this.gcmAuthenticationTagCmd = values.getOrDefault("gcmAuthenticationTagCmd", null);
        this.ivData = values.getOrDefault("ivData", null);
    }
    public UTicket(UTicket uTicket) {
        Class<?> uTicketClass = this.getClass();

        for (Field field : uTicketClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(uTicket);
                field.set(this, value);
            } catch (IllegalAccessException e) {
                String failure_msg = "UTicket copy constructor: IllegalAccessException.";
                throw new RuntimeException(failure_msg);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UTicket ut) {
            return Objects.equals(this.uTicketId, ut.uTicketId);
        }
        return false;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getUTicketId() {
        return uTicketId;
    }

    public void setUTicketId(String uTicketId) {
        this.uTicketId = uTicketId;
    }

    public String getUTicketType() {
        return uTicketType;
    }

    public void setuTicketType(String uTicketType) {
        this.uTicketType = uTicketType;
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

    public String getHolderId() {
        return holderId;
    }

    public void setHolderId(String holderId) {
        this.holderId = holderId;
    }

    public String getTaskScope() {
        return taskScope;
    }

    public void setTaskScope(String taskScope) {
        this.taskScope = taskScope;
    }

    public String getIssuerSignature() {
        return issuerSignature;
    }

    public void setIssuerSignature(String issuerSignature) {
        this.issuerSignature = issuerSignature;
    }

    public String getAssociatedPlaintextCmd() {
        return associatedPlaintextCmd;
    }

    public void setAssociatedPlaintextCmd(String associatedPlaintextCmd) {
        this.associatedPlaintextCmd = associatedPlaintextCmd;
    }

    public String getCiphertextCmd() {
        return ciphertextCmd;
    }

    public void setCiphertextCmd(String ciphertextCmd) {
        this.ciphertextCmd = ciphertextCmd;
    }

    public String getGcmAuthenticationTagCmd() {
        return gcmAuthenticationTagCmd;
    }

    public void setGcmAuthenticationTagCmd(String gcmAuthenticationTagCmd) {
        this.gcmAuthenticationTagCmd = gcmAuthenticationTagCmd;
    }

    public String getIvData() {
        return ivData;
    }

    public void setIvData(String ivData) {
        this.ivData = ivData;
    }

    public static String uTicketToJsonStr(UTicket uTicket) {
        return SimpleMeasurer.measureWorkerFunc(UTicket::_uTicketToJsonStr, uTicket);
    }
    private static String _uTicketToJsonStr(UTicket uTicket) {
        return gson.toJson(uTicket);
    }

    public static UTicket jsonStrToUTicket(String json) {
        try {
            return gson.fromJson(json, UTicket.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID UTICKET SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

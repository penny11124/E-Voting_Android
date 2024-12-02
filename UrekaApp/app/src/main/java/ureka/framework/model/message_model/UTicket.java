package ureka.framework.model.message_model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ureka.framework.resource.logger.SimpleMeasurer;

public class UTicket {
    private static final Gson gson = new Gson();
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
    private String protocol_version;
    private String u_ticket_id;
    private String u_ticket_type;
    private String device_id;
    private Integer ticket_order;
    private String holder_id;
    private String task_scope;
    private String issuer_signature;
    // PS-Cmd
    private String associated_plaintext_cmd;
    private String ciphertext_cmd;
    private String gcm_authentication_tag_cmd;
    // PS-Data
    private String iv_data;

    public UTicket() {}
    public UTicket(Map<String, String> values) {
        Class<?> uTicketClass = this.getClass();

        for (Field field : uTicketClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            String fieldValue = values.get(fieldName);

            try {
                field.setAccessible(true);
                if (field.getType().equals(Integer.class)) {
                    field.set(this, fieldValue != null ? Integer.valueOf(fieldValue) : null);
                } else {
                    if (fieldName.equals("protocol_version")) {
                        field.set(this, fieldValue != null ? fieldValue : UTicket.PROTOCOL_VERSION);
                    } else {
                        field.set(this, fieldValue);
                    }

                }
            } catch (IllegalAccessException e) {
                String failureMsg = "UTicket Map constructor: IllegalAccessException.";
                throw new RuntimeException(failureMsg, e);
            }
        }
    }
    public UTicket(UTicket uTicket) {
        Class<?> uTicketClass = this.getClass();

        for (Field field : uTicketClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
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
        if (obj instanceof UTicket) {
            UTicket ut = (UTicket) obj;
            return Objects.equals(this.u_ticket_id, ut.u_ticket_id);
        }
        return false;
    }

    public void setProtocolVersion(String protocol_version) {
        this.protocol_version = protocol_version;
    }

    public String getProtocolVersion() {
        return protocol_version;
    }

    public String getUTicketId() {
        return u_ticket_id;
    }

    public void setUTicketId(String u_ticket_id) {
        this.u_ticket_id = u_ticket_id;
    }

    public String getUTicketType() {
        return u_ticket_type;
    }

    public void setUTicketType(String u_ticket_type) {
        this.u_ticket_type = u_ticket_type;
    }

    public String getDeviceId() {
        return device_id;
    }

    public void setDeviceId(String device_id) {
        this.device_id = device_id;
    }

    public Integer getTicketOrder() {
        return ticket_order;
    }

    public void setTicketOrder(Integer ticket_order) {
        this.ticket_order = ticket_order;
    }

    public String getHolderId() {
        return holder_id;
    }

    public void setHolderId(String holder_id) {
        this.holder_id = holder_id;
    }

    public String getTaskScope() {
        return task_scope;
    }

    public void setTaskScope(String task_scope) {
        this.task_scope = task_scope;
    }

    public String getIssuerSignature() {
        return issuer_signature;
    }

    public void setIssuerSignature(String issuer_signature) {
        this.issuer_signature = issuer_signature;
    }

    public String getAssociatedPlaintextCmd() {
        return associated_plaintext_cmd;
    }

    public void setAssociatedPlaintextCmd(String associated_plaintext_cmd) {
        this.associated_plaintext_cmd = associated_plaintext_cmd;
    }

    public String getCiphertextCmd() {
        return ciphertext_cmd;
    }

    public void setCiphertextCmd(String ciphertext_cmd) {
        this.ciphertext_cmd = ciphertext_cmd;
    }

    public String getGcmAuthenticationTagCmd() {
        return gcm_authentication_tag_cmd;
    }

    public void setGcmAuthenticationTagCmd(String gcm_authentication_tag_cmd) {
        this.gcm_authentication_tag_cmd = gcm_authentication_tag_cmd;
    }

    public String getIvData() {
        return iv_data;
    }

    public void setIvData(String iv_data) {
        this.iv_data = iv_data;
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

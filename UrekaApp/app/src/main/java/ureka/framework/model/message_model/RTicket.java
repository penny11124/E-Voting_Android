package ureka.framework.model.message_model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ureka.framework.resource.logger.SimpleMeasurer;

public class RTicket {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    //////////////////////////////////////////////////////
    // Message Type
    //////////////////////////////////////////////////////
    public static final String MESSAGE_TYPE = "RTICKET";

    //////////////////////////////////////////////////////
    // RTicket Type (same as UTicket Type)
    // RTicket Type (CRKE)
    // RTicket Type (PS)
    //////////////////////////////////////////////////////
    // CRKE
    public static final String TYPE_CRKE1_RTICKET = "CR-KE-1";
    public static final String TYPE_CRKE2_RTICKET = "CR-KE-2";
    public static final String TYPE_CRKE3_RTICKET = "CR-KE-3";
    public static final String[] LEGAL_CRKE_TYPES = {TYPE_CRKE1_RTICKET, TYPE_CRKE2_RTICKET, TYPE_CRKE3_RTICKET};
    // RToken
    public static final String TYPE_DATA_RTOKEN = "DATA_RTOKEN";
    // All
    public static final String[] LEGAL_RTICKET_TYPES = {
        UTicket.TYPE_INITIALIZATION_UTICKET,
        UTicket.TYPE_OWNERSHIP_UTICKET,
        TYPE_CRKE1_RTICKET,
        TYPE_CRKE2_RTICKET,
        TYPE_CRKE3_RTICKET,
        TYPE_DATA_RTOKEN,
        UTicket.TYPE_ACCESS_END_UTOKEN,
    };

    // RT
    private String protocol_version;
    private String r_ticket_id;
    private String r_ticket_type;
    private String device_id;
    private String result;
    private Integer ticket_order;
    private String audit_start;
    private String audit_end;
    // CR-KE
    private String challenge_1;
    private String challenge_2;
    private String key_exchange_salt_1;
    private String key_exchange_salt_2;
    // PS-Cmd
    private String associated_plaintext_cmd;
    private String ciphertext_cmd;
    private String iv_cmd;
    private String gcm_authentication_tag_cmd;
    // PS-Data
    private String associated_plaintext_data;
    private String ciphertext_data;
    private String iv_data;
    private String gcm_authentication_tag_data;
    // RT
    private String device_signature;

    public RTicket() {}
    public RTicket(Map<String, String> values) {
        Class<?> rTicketClass = this.getClass();

        for (Field field : rTicketClass.getDeclaredFields()) {
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
    public RTicket(RTicket rTicket) {
        Class<?> rTicketClass = this.getClass();

        for (Field field : rTicketClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(rTicket);
                field.set(this, value);
            } catch (IllegalAccessException e) {
                String failure_msg = "RTicket copy constructor: IllegalAccessException.";
                throw new RuntimeException(failure_msg);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RTicket) {
            RTicket rt = (RTicket) obj;
            return Objects.equals(this.r_ticket_id, rt.r_ticket_id);
        }
        return false;
    }

    public String getProtocolVersion() {
        return protocol_version;
    }

    public void setProtocolVersion(String protocol_version) {
        this.protocol_version = protocol_version;
    }

    public String getRTicketId() {
        return r_ticket_id;
    }

    public void setRTicketId(String r_ticket_id) {
        this.r_ticket_id = r_ticket_id;
    }

    public String getRTicketType() {
        return r_ticket_type;
    }

    public void setRTicketType(String r_ticket_type) {
        this.r_ticket_type = r_ticket_type;
    }

    public String getDeviceId() {
        return device_id;
    }

    public void setDeviceId(String device_id) {
        this.device_id = device_id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getTicketOrder() {
        return ticket_order;
    }

    public void setTicketOrder(Integer ticket_order) {
        this.ticket_order = ticket_order;
    }

    public String getAuditStart() {
        return audit_start;
    }

    public void setAuditStart(String audit_start) {
        this.audit_start = audit_start;
    }

    public String getAuditEnd() {
        return audit_end;
    }

    public void setAuditEnd(String audit_end) {
        this.audit_end = audit_end;
    }

    public String getChallenge1() {
        return challenge_1;
    }

    public void setChallenge1(String challenge_1) {
        this.challenge_1 = challenge_1;
    }

    public String getChallenge2() {
        return challenge_2;
    }

    public void setChallenge2(String challenge_2) {
        this.challenge_2 = challenge_2;
    }

    public String getKeyExchangeSalt1() {
        return key_exchange_salt_1;
    }

    public void setKeyExchangeSalt1(String key_exchange_salt_1) {
        this.key_exchange_salt_1 = key_exchange_salt_1;
    }

    public String getKeyExchangeSalt2() {
        return key_exchange_salt_2;
    }

    public void setKeyExchangeSalt2(String key_exchange_salt_2) {
        this.key_exchange_salt_2 = key_exchange_salt_2;
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

    public String getIvCmd() {
        return iv_cmd;
    }

    public void setIvCmd(String iv_cmd) {
        this.iv_cmd = iv_cmd;
    }

    public String getGcmAuthenticationTagCmd() {
        return gcm_authentication_tag_cmd;
    }

    public void setGcmAuthenticationTagCmd(String gcm_authentication_tag_cmd) {
        this.gcm_authentication_tag_cmd = gcm_authentication_tag_cmd;
    }

    public String getAssociatedPlaintextData() {
        return associated_plaintext_data;
    }

    public void setAssociatedPlaintextData(String associated_plaintext_data) {
        this.associated_plaintext_data = associated_plaintext_data;
    }

    public String getCiphertextData() {
        return ciphertext_data;
    }

    public void setCiphertextData(String ciphertextData) {
        this.ciphertext_data = ciphertextData;
    }

    public String getIvData() {
        return iv_data;
    }

    public void setIvData(String iv_data) {
        this.iv_data = iv_data;
    }

    public String getGcmAuthenticationTagData() {
        return gcm_authentication_tag_data;
    }

    public void setGcmAuthenticationTagData(String gcm_authentication_tag_data) {
        this.gcm_authentication_tag_data = gcm_authentication_tag_data;
    }

    public String getDeviceSignature() {
        return device_signature;
    }

    public void setDeviceSignature(String device_signature) {
        this.device_signature = device_signature;
    }

    public static String rTicketToJsonStr(RTicket rTicket) {
        return SimpleMeasurer.measureWorkerFunc(RTicket::_rTicketToJsonStr, rTicket);
    }
    private static String _rTicketToJsonStr(RTicket rTicket) {
//        return gson.toJson(rTicket);
        JsonObject jsonObject = new JsonObject();
        if (rTicket.protocol_version != null) {
            jsonObject.addProperty("protocol_version", rTicket.protocol_version);
        }
        if (rTicket.r_ticket_id != null) {
            jsonObject.addProperty("r_ticket_id", rTicket.r_ticket_id);
        }
        if (rTicket.r_ticket_type != null) {
            jsonObject.addProperty("r_ticket_type", rTicket.r_ticket_type);
        }
        if (rTicket.device_id != null) {
            jsonObject.addProperty("device_id", rTicket.device_id);
        }
        if (rTicket.result != null) {
            jsonObject.addProperty("result", rTicket.result);
        }
        jsonObject.addProperty("ticket_order", rTicket.ticket_order);
        if (rTicket.audit_start != null) {
            jsonObject.addProperty("audit_start", rTicket.audit_start);
        }
        if (rTicket.audit_end != null) {
            jsonObject.addProperty("audit_end", rTicket.audit_end);
        }
        if (rTicket.challenge_1 != null) {
            jsonObject.addProperty("challenge_1", rTicket.challenge_1);
        }
        if (rTicket.challenge_2 != null) {
            jsonObject.addProperty("challenge_2", rTicket.challenge_2);
        }
        if (rTicket.key_exchange_salt_1 != null) {
            jsonObject.addProperty("key_exchange_salt_1", rTicket.key_exchange_salt_1);
        }
        if (rTicket.key_exchange_salt_2 != null) {
            jsonObject.addProperty("key_exchange_salt_2", rTicket.key_exchange_salt_2);
        }
        if (rTicket.associated_plaintext_cmd != null) {
            jsonObject.addProperty("associated_plaintext_cmd", rTicket.associated_plaintext_cmd);
        }
        if (rTicket.ciphertext_cmd != null) {
            jsonObject.addProperty("ciphertext_cmd", rTicket.ciphertext_cmd);
        }
        if (rTicket.iv_cmd != null) {
            jsonObject.addProperty("iv_cmd", rTicket.iv_cmd);
        }
        if (rTicket.gcm_authentication_tag_cmd != null) {
            jsonObject.addProperty("gcm_authentication_tag_cmd", rTicket.gcm_authentication_tag_cmd);
        }
        if (rTicket.associated_plaintext_data != null) {
            jsonObject.addProperty("associated_plaintext_data", rTicket.associated_plaintext_data);
        }
        if (rTicket.ciphertext_data != null) {
            jsonObject.addProperty("ciphertext_data", rTicket.ciphertext_data);
        }
        if (rTicket.iv_data != null) {
            jsonObject.addProperty("iv_data", rTicket.iv_data);
        }
        if (rTicket.gcm_authentication_tag_data != null) {
            jsonObject.addProperty("gcm_authentication_tag_data", rTicket.gcm_authentication_tag_data);
        }
        if (rTicket.device_signature != null) {
            jsonObject.addProperty("device_signature", rTicket.device_signature);
        }

        return jsonObject.toString();
    }

    public static RTicket jsonStrToRTicket(String json) {
        try {
            return gson.fromJson(json, RTicket.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID RTICKET SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

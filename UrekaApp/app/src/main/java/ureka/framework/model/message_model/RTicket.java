package ureka.framework.model.message_model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ureka.framework.resource.logger.SimpleMeasurer;

public class RTicket {
    private static final Gson gson = new Gson();
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
    private String challenge1;
    private String challenge2;
    private String keyExchangeSalt1;
    private String keyExchangeSalt2;
    // PS-Cmd
    private String associated_plaintext_cmd;
    private String ciphertext_cmd;
    private String ivCmd;
    private String gcm_authentication_tag_cmd;
    // PS-Data
    private String associated_plaintext_data;
    private String ciphertextData;
    private String iv_data;
    private String gcmAuthenticationTagData;
    // RT
    private String deviceSignature;

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
        return challenge1;
    }

    public void setChallenge1(String challenge1) {
        this.challenge1 = challenge1;
    }

    public String getChallenge2() {
        return challenge2;
    }

    public void setChallenge2(String challenge2) {
        this.challenge2 = challenge2;
    }

    public String getKeyExchangeSalt1() {
        return keyExchangeSalt1;
    }

    public void setKeyExchangeSalt1(String keyExchangeSalt1) {
        this.keyExchangeSalt1 = keyExchangeSalt1;
    }

    public String getKeyExchangeSalt2() {
        return keyExchangeSalt2;
    }

    public void setKeyExchangeSalt2(String keyExchangeSalt2) {
        this.keyExchangeSalt2 = keyExchangeSalt2;
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
        return ivCmd;
    }

    public void setIvCmd(String ivCmd) {
        this.ivCmd = ivCmd;
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
        return ciphertextData;
    }

    public void setCiphertextData(String ciphertextData) {
        this.ciphertextData = ciphertextData;
    }

    public String getIvData() {
        return iv_data;
    }

    public void setIvData(String iv_data) {
        this.iv_data = iv_data;
    }

    public String getGcmAuthenticationTagData() {
        return gcmAuthenticationTagData;
    }

    public void setGcmAuthenticationTagData(String gcmAuthenticationTagData) {
        this.gcmAuthenticationTagData = gcmAuthenticationTagData;
    }

    public String getDeviceSignature() {
        return deviceSignature;
    }

    public void setDeviceSignature(String deviceSignature) {
        this.deviceSignature = deviceSignature;
    }

    public static String rTicketToJsonStr(RTicket rTicket) {
        return SimpleMeasurer.measureWorkerFunc(RTicket::_rTicketToJsonStr, rTicket);
    }
    private static String _rTicketToJsonStr(RTicket rTicket) {
        return gson.toJson(rTicket);
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

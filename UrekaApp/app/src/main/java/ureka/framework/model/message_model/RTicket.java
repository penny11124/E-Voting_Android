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
    private String protocolVersion;
    private String rTicketId;
    private String rTicketType;
    private String deviceId;
    private String result;
    private Integer ticketOrder;
    private String auditStart;
    private String auditEnd;
    // CR-KE
    private String challenge1;
    private String challenge2;
    private String keyExchangeSalt1;
    private String keyExchangeSalt2;
    // PS-Cmd
    private String associatedPlaintextCmd;
    private String ciphertextCmd;
    private String ivCmd;
    private String gcmAuthenticationTagCmd;
    // PS-Data
    private String associatedPlaintextData;
    private String ciphertextData;
    private String ivData;
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
                    if (fieldName.equals("protocolVersion")) {
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
            return Objects.equals(this.rTicketId, rt.rTicketId);
        }
        return false;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getRTicketId() {
        return rTicketId;
    }

    public void setRTicketId(String rTicketId) {
        this.rTicketId = rTicketId;
    }

    public String getRTicketType() {
        return rTicketType;
    }

    public void setRTicketType(String rTicketType) {
        this.rTicketType = rTicketType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getTicketOrder() {
        return ticketOrder;
    }

    public void setTicketOrder(Integer ticketOrder) {
        this.ticketOrder = ticketOrder;
    }

    public String getAuditStart() {
        return auditStart;
    }

    public void setAuditStart(String auditStart) {
        this.auditStart = auditStart;
    }

    public String getAuditEnd() {
        return auditEnd;
    }

    public void setAuditEnd(String auditEnd) {
        this.auditEnd = auditEnd;
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

    public String getIvCmd() {
        return ivCmd;
    }

    public void setIvCmd(String ivCmd) {
        this.ivCmd = ivCmd;
    }

    public String getGcmAuthenticationTagCmd() {
        return gcmAuthenticationTagCmd;
    }

    public void setGcmAuthenticationTagCmd(String gcmAuthenticationTagCmd) {
        this.gcmAuthenticationTagCmd = gcmAuthenticationTagCmd;
    }

    public String getAssociatedPlaintextData() {
        return associatedPlaintextData;
    }

    public void setAssociatedPlaintextData(String associatedPlaintextData) {
        this.associatedPlaintextData = associatedPlaintextData;
    }

    public String getCiphertextData() {
        return ciphertextData;
    }

    public void setCiphertextData(String ciphertextData) {
        this.ciphertextData = ciphertextData;
    }

    public String getIvData() {
        return ivData;
    }

    public void setIvData(String ivData) {
        this.ivData = ivData;
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

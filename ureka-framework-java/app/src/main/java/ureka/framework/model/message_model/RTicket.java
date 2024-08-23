package ureka.framework.model.message_model;

import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ureka.framework.resource.logger.SimpleMeasurer;

public class RTicket {
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

    public RTicket(Map<String, String> values) {
        this.protocolVersion = values.getOrDefault("protocolVersion", UTicket.PROTOCOL_VERSION);
        this.rTicketId = values.getOrDefault("rTicketId", null);
        this.rTicketType = values.getOrDefault("rTicketType", null);
        this.deviceId = values.getOrDefault("deviceId", null);
        this.result = values.getOrDefault("result", null);
        this.ticketOrder = Integer.valueOf(values.getOrDefault("ticketOrder", null));
        this.auditStart = values.getOrDefault("auditStart", null);
        this.auditEnd = values.getOrDefault("auditEnd", null);
        this.challenge1 = values.getOrDefault("challenge1", null);
        this.challenge2 = values.getOrDefault("challenge2", null);
        this.keyExchangeSalt1 = values.getOrDefault("keyExchangeSalt1", null);
        this.keyExchangeSalt2 = values.getOrDefault("keyExchangeSalt2", null);
        this.associatedPlaintextCmd = values.getOrDefault("associatedPlaintextCmd", null);
        this.ciphertextCmd = values.getOrDefault("ciphertextCmd", null);
        this.ivCmd = values.getOrDefault("ivCmd", null);
        this.gcmAuthenticationTagCmd = values.getOrDefault("gcmAuthenticationTagCmd", null);
        this.associatedPlaintextData = values.getOrDefault("associatedPlaintextData", null);
        this.ciphertextData = values.getOrDefault("ciphertextData", null);
        this.ivData = values.getOrDefault("ivData", null);
        this.gcmAuthenticationTagData = values.getOrDefault("gcmAuthenticationTagData", null);
        this.deviceSignature = values.getOrDefault("deviceSignature", null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RTicket rt) {
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
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(rTicket);
    }

    public static RTicket jsonStrToRTicket(String json) {
        Gson gson = new GsonBuilder().create();
        try {
            return gson.fromJson(json, RTicket.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID RTICKET SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

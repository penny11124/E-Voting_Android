package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class CurrentSession {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    // Access UT
    private String currentUTicketId = null;
    private String currentDeviceId = null;
    private String currentHolderId = null;
    private String currentTaskScope = null;
    // CR-KE
    private String challenge1 = null;
    private String challenge2 = null;
    private String keyExchangeSalt1 = null;
    private String keyExchangeSalt2 = null;
    // PS
    private String currentSessionKeyStr = null;

    private String plaintextCmd = null;
    private String associatedPlaintextCmd = null;
    private String ivCmd = null;
    private String ciphertextCmd = null;
    private String gcmAuthenticationTagCmd = null;

    private String plaintextData = null;
    private String associatedPlaintextData = null;
    private String ivData = null;
    private String ciphertextData = null;
    private String gcmAuthenticationTagData = null;

    public CurrentSession() {}
    public CurrentSession(Map<String, String> values) {
        this.currentUTicketId = values.getOrDefault("currentUTicketId", null);
        this.currentDeviceId = values.getOrDefault("currentDeviceId", null);
        this.currentHolderId = values.getOrDefault("currentHolderId", null);
        this.currentTaskScope = values.getOrDefault("currentTaskScope", null);
        this.challenge1 = values.getOrDefault("challenge1", null);
        this.challenge2 = values.getOrDefault("challenge2", null);
        this.keyExchangeSalt1 = values.getOrDefault("keyExchangeSalt1", null);
        this.keyExchangeSalt2 = values.getOrDefault("keyExchangeSalt2", null);
        this.currentSessionKeyStr = values.getOrDefault("currentSessionKeyStr", null);
        this.plaintextCmd = values.getOrDefault("plaintextCmd", null);
        this.associatedPlaintextCmd = values.getOrDefault("associatedPlaintextCmd", null);
        this.ciphertextCmd = values.getOrDefault("ciphertextCmd", null);
        this.ivCmd = values.getOrDefault("ivCmd", null);
        this.gcmAuthenticationTagCmd = values.getOrDefault("gcmAuthenticationTagCmd", null);
        this.plaintextData = values.getOrDefault("plaintextData", null);
        this.associatedPlaintextData = values.getOrDefault("associatedPlaintextData", null);
        this.ciphertextData = values.getOrDefault("ciphertextData", null);
        this.ivData = values.getOrDefault("ivData", null);
        this.gcmAuthenticationTagData = values.getOrDefault("gcmAuthenticationTagData", null);
    }

    public String getCurrentUTicketId() {
        return currentUTicketId;
    }

    public void setCurrentUTicketId(String currentUTicketId) {
        this.currentUTicketId = currentUTicketId;
    }

    public String getCurrentDeviceId() {
        return currentDeviceId;
    }

    public void setCurrentDeviceId(String currentDeviceId) {
        this.currentDeviceId = currentDeviceId;
    }

    public String getCurrentHolderId() {
        return currentHolderId;
    }

    public void setCurrentHolderId(String currentHolderId) {
        this.currentHolderId = currentHolderId;
    }

    public String getCurrentTaskScope() {
        return currentTaskScope;
    }

    public void setCurrentTaskScope(String currentTaskScope) {
        this.currentTaskScope = currentTaskScope;
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

    public String getCurrentSessionKeyStr() {
        return currentSessionKeyStr;
    }

    public void setCurrentSessionKeyStr(String currentSessionKeyStr) {
        this.currentSessionKeyStr = currentSessionKeyStr;
    }

    public String getPlaintextCmd() {
        return plaintextCmd;
    }

    public void setPlaintextCmd(String plaintextCmd) {
        this.plaintextCmd = plaintextCmd;
    }

    public String getAssociatedPlaintextCmd() {
        return associatedPlaintextCmd;
    }

    public void setAssociatedPlaintextCmd(String associatedPlaintextCmd) {
        this.associatedPlaintextCmd = associatedPlaintextCmd;
    }

    public String getIvCmd() {
        return ivCmd;
    }

    public void setIvCmd(String ivCmd) {
        this.ivCmd = ivCmd;
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

    public String getPlaintextData() {
        return plaintextData;
    }

    public void setPlaintextData(String plaintextData) {
        this.plaintextData = plaintextData;
    }

    public String getAssociatedPlaintextData() {
        return associatedPlaintextData;
    }

    public void setAssociatedPlaintextData(String associatedPlaintextData) {
        this.associatedPlaintextData = associatedPlaintextData;
    }

    public String getIvData() {
        return ivData;
    }

    public void setIvData(String ivData) {
        this.ivData = ivData;
    }

    public String getCiphertextData() {
        return ciphertextData;
    }

    public void setCiphertextData(String ciphertextData) {
        this.ciphertextData = ciphertextData;
    }

    public String getGcmAuthenticationTagData() {
        return gcmAuthenticationTagData;
    }

    public void setGcmAuthenticationTagData(String gcmAuthenticationTagData) {
        this.gcmAuthenticationTagData = gcmAuthenticationTagData;
    }

    public static String currentSessionToJsonStr(CurrentSession currentSession) {
        return gson.toJson(currentSession);
    }

    public static CurrentSession jsonStrToCurrentSession(String json) {
        try {
            return gson.fromJson(json, CurrentSession.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID R TICKET SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

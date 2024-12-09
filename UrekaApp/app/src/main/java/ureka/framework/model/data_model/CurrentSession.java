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
    private String associated_plaintext_cmd = null;
    private String ivCmd = null;
    private String ciphertext_cmd = null;
    private String gcm_authentication_tag_cmd = null;

    private String plaintextData = null;
    private String associated_plaintext_data = null;
    private String iv_data = null;
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
        this.associated_plaintext_cmd = values.getOrDefault("associated_plaintext_cmd", null);
        this.ciphertext_cmd = values.getOrDefault("ciphertext_cmd", null);
        this.ivCmd = values.getOrDefault("ivCmd", null);
        this.gcm_authentication_tag_cmd = values.getOrDefault("gcm_authentication_tag_cmd", null);
        this.plaintextData = values.getOrDefault("plaintextData", null);
        this.associated_plaintext_data = values.getOrDefault("associated_plaintext_data", null);
        this.ciphertextData = values.getOrDefault("ciphertextData", null);
        this.iv_data = values.getOrDefault("iv_data", null);
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
        return associated_plaintext_cmd;
    }

    public void setAssociatedPlaintextCmd(String associated_plaintext_cmd) {
        this.associated_plaintext_cmd = associated_plaintext_cmd;
    }

    public String getIvCmd() {
        return ivCmd;
    }

    public void setIvCmd(String ivCmd) {
        this.ivCmd = ivCmd;
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

    public String getPlaintextData() {
        return plaintextData;
    }

    public void setPlaintextData(String plaintextData) {
        this.plaintextData = plaintextData;
    }

    public String getAssociatedPlaintextData() {
        return associated_plaintext_data;
    }

    public void setAssociatedPlaintextData(String associated_plaintext_data) {
        this.associated_plaintext_data = associated_plaintext_data;
    }

    public String getIvData() {
        return iv_data;
    }

    public void setIvData(String iv_data) {
        this.iv_data = iv_data;
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

package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class CurrentSession {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    // Access UT
    private String currentUTicketId;
    private String currentDeviceId;
    private String currentHolderId;
    private String currentTaskScope;
    // CR-KE
    private String challenge1;
    private String challenge2;
    private String keyExchangeSalt1;
    private String keyExchangeSalt2;
    // PS
    private String currentSessionKeyStr;

    private String plaintextCmd;
    private String associatedPlaintextCmd;
    private String ivCmd;
    private String ciphertextCmd;
    private String gcmAuthenticationTagCmd;

    private String plaintextData;
    private String associatedPlaintextData;
    private String ivData;
    private String ciphertextData;
    private String gcmAuthenticationTagData;

    public CurrentSession(Map<String, String> values) {
        this.currentUTicketId = values.getOrDefault("current_u_ticket_id", null);
        this.currentDeviceId = values.getOrDefault("current_device_id", null);
        this.currentHolderId = values.getOrDefault("current_holder_id", null);
        this.currentTaskScope = values.getOrDefault("current_task_scope", null);
        this.challenge1 = values.getOrDefault("challenge_1", null);
        this.challenge2 = values.getOrDefault("challenge_2", null);
        this.keyExchangeSalt1 = values.getOrDefault("key_exchange_salt_1", null);
        this.keyExchangeSalt2 = values.getOrDefault("key_exchange_salt_2", null);
        this.currentSessionKeyStr = values.getOrDefault("current_session_key_str", null);
        this.plaintextCmd = values.getOrDefault("plaintext_cmd", null);
        this.associatedPlaintextCmd = values.getOrDefault("associated_plaintext_cmd", null);
        this.ciphertextCmd = values.getOrDefault("ciphertext_cmd", null);
        this.ivCmd = values.getOrDefault("iv_cmd", null);
        this.gcmAuthenticationTagCmd = values.getOrDefault("gcm_authentication_tag_cmd", null);
        this.plaintextData = values.getOrDefault("plaintext_data", null);
        this.associatedPlaintextData = values.getOrDefault("associated_plaintext_data", null);
        this.ciphertextData = values.getOrDefault("ciphertext_data", null);
        this.ivData = values.getOrDefault("iv_data", null);
        this.gcmAuthenticationTagData = values.getOrDefault("gcm_authentication_tag_data", null);
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

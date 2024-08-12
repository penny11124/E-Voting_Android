package ureka.framework.model.message_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class Message {
    //////////////////////////////////////////////////////
    // Message Operation
    //////////////////////////////////////////////////////
    public static final String MESSAGE_RECV_AND_STORE = "MESSAGE_RECV_AND_STORE";
    public static final String MESSAGE_VERIFY_AND_EXECUTE = "MESSAGE_VERIFY_AND_EXECUTE";

    private String messageOperation;
    private String messageType;
    private String messageStr;

    public Message(Map<String, String> values) {
        this.messageOperation = values.getOrDefault("messageOperation", null);
        this.messageType = values.getOrDefault("messageType", null);
        this.messageStr = values.getOrDefault("messageStr", null);
    }

    public String getMessageOperation() {
        return messageOperation;
    }

    public void setMessageOperation(String messageOperation) {
        this.messageOperation = messageOperation;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageStr() {
        return messageStr;
    }

    public void setMessageStr(String messageStr) {
        this.messageStr = messageStr;
    }

    public static String messageToJsonstr(Message messageObj) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(messageObj);
    }

    public static Message jsonstrToMessage(String json) {
        Gson gson = new GsonBuilder().create();
        try {
            return gson.fromJson(json, Message.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID MESSAGE SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

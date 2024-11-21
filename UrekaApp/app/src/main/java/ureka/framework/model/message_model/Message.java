package ureka.framework.model.message_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class Message {
    private static final Gson gson = new Gson();
    //////////////////////////////////////////////////////
    // Message Operation
    //////////////////////////////////////////////////////
    public static final String MESSAGE_REQUEST = "MESSAGE_REQUEST"; // Only used when voter request UTicket from admin
    public static final String MESSAGE_RECV_AND_STORE = "MESSAGE_RECV_AND_STORE";
    public static final String MESSAGE_VERIFY_AND_EXECUTE = "MESSAGE_VERIFY_AND_EXECUTE";

    private String messageOperation;
    private String messageType;
    private String messageStr;

    public Message() {}
    public Message(Map<String, String> values) {
        this.messageOperation = values.getOrDefault("messageOperation", null);
        this.messageType = values.getOrDefault("messageType", null);
        this.messageStr = values.getOrDefault("messageStr", null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Class<?> messageClass = this.getClass();

        for (Field field : messageClass.getDeclaredFields()) {
            try {
                Object value1 = field.get(this), value2 = field.get(obj);
                if (value1 == null && value2 != null) {
                    return false;
                } else if (value1 != null && value2 == null) {
                    return false;
                } else if (value1 != null && !value1.equals(value2)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return true;
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
        return gson.toJson(messageObj);
    }

    public static Message jsonstrToMessage(String json) {
        try {
            return gson.fromJson(json, Message.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID MESSAGE SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

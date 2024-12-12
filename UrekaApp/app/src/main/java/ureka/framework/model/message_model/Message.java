package ureka.framework.model.message_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class Message {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    //////////////////////////////////////////////////////
    // Message Operation
    //////////////////////////////////////////////////////
    public static final String MESSAGE_REQUEST = "MESSAGE_REQUEST"; // Only used when voter request UTicket from admin
    public static final String MESSAGE_RECV_AND_STORE = "MESSAGE_RECV_AND_STORE";
    public static final String MESSAGE_VERIFY_AND_EXECUTE = "MESSAGE_VERIFY_AND_EXECUTE";

    private String message_operation;
    private String message_type;
    private String message_str;

    public Message() {}
    public Message(Map<String, String> values) {
        this.message_operation = values.getOrDefault("message_operation", null);
        this.message_type = values.getOrDefault("message_type", null);
        this.message_str = values.getOrDefault("message_str", null);
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
        return message_operation;
    }

    public void setMessageOperation(String messageOperation) {
        this.message_operation = messageOperation;
    }

    public String getMessageType() {
        return message_type;
    }

    public void setMessageType(String messageType) {
        this.message_type = messageType;
    }

    public String getMessageStr() {
        return message_str;
    }

    public void setMessageStr(String messageStr) {
        this.message_str = messageStr;
    }

    public static String messageToJsonstr(Message messageObj) {
//        return gson.toJson(messageObj);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message_operation", messageObj.message_operation);
        jsonObject.addProperty("message_type", messageObj.message_type);
        jsonObject.addProperty("message_str", messageObj.message_str);
        return jsonObject.toString();
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

package ureka.framework.logic.stage_worker;

import java.util.Objects;

import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleLogger;

public class MsgVerifierMessage {
    //////////////////////////////////////////////////////
    // Message Verification Flow
    //////////////////////////////////////////////////////
    public static Message verifyJsonSchema(String arbitrary_json) {
        String successMsg = "-> SUCCESS: VERIFY_JSON_SCHEMA";
        String failureMsg = "-> FAILURE: VERIFY_JSON_SCHEMA: ";

        try {
            Message messageIn = Message.jsonstrToMessage(arbitrary_json);
            SimpleLogger.simpleLog("info", successMsg);
            return messageIn;
        } catch (Exception e) {
            // pragma: no cover -> Weird Message
            SimpleLogger.simpleLog("error", failureMsg + e.getMessage());
            throw e;
        }
    }

    public static Message verifyMessageOperation(Message messageIn) {
        String success_msg = "-> SUCCESS: VERIFY_MESSAGE_OPERATION";
        String failure_msg = "-> FAILURE: VERIFY_MESSAGE_OPERATION";

        if (Objects.equals(messageIn.getMessageOperation(), Message.MESSAGE_RECV_AND_STORE)
            || Objects.equals(messageIn.getMessageOperation(), Message.MESSAGE_VERIFY_AND_EXECUTE)
            || Objects.equals(messageIn.getMessageOperation(), Message.MESSAGE_REQUEST)) {
            SimpleLogger.simpleLog("info", success_msg);
            return messageIn;
        } else {
            // pragma: no cover -> Weird Message
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }

    public static Message verifyMessageType(Message messageIn) {
        String success_msg = "-> SUCCESS: VERIFY_MESSAGE_TYPE";
        String failure_msg = "-> FAILURE: VERIFY_MESSAGE_TYPE";

        if (Objects.equals(messageIn.getMessageType(), UTicket.MESSAGE_TYPE)
            || Objects.equals(messageIn.getMessageType(), RTicket.MESSAGE_TYPE)
            || Objects.equals(messageIn.getMessageType(), Message.MESSAGE_REQUEST)) {
            SimpleLogger.simpleLog("info", success_msg);
            return messageIn;
        } else {
            // pragma: no cover -> Weird Message
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }

    public static Message verifyMessageStr(Message messageIn) {
        String success_msg = "-> SUCCESS: VERIFY_MESSAGE_STR";
        String failure_msg = "-> FAILURE: VERIFY_MESSAGE_STR";

        if (messageIn.getMessageStr() != null) {
            SimpleLogger.simpleLog("info", success_msg);
            return messageIn;
        } else {
            // pragma: no cover -> Weird Message
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }
}
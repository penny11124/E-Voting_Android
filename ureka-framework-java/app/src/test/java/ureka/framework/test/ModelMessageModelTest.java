package ureka.framework.test;

import org.junit.jupiter.api.Test;

import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;

public class ModelMessageModelTest {
    // model.message_model - COMPLETED
    @Test
    public void messageModelSerializationTest() {
        // Since the serialization and deserialization of the message models are the same,
        // we only test one of the classes.
        Message message = new Message();
        message.setMessageOperation(Message.MESSAGE_RECV_AND_STORE);
        message.setMessageType(RTicket.MESSAGE_TYPE);

        String messageJson = Message.messageToJsonstr(message);
        System.out.println(messageJson);
        Message newMessage = Message.jsonstrToMessage(messageJson);
        assert (message.equals(newMessage));
    }
}

package ureka.framework.test;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;

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

    @Test
    public void messageModelMapConstructorTest() {
        UTicket uTicket = new UTicket();
        uTicket.setUTicketId("foobar");
        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);

        Map<String, String> map = new HashMap<>();
        map.put("uTicketId", "foobar");
        map.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);
        UTicket mapUTicket = new UTicket(map);
        assert  (uTicket.equals(mapUTicket));
    }

    @Test
    public void messageModelCopyConstructorTest() {
        UTicket uTicket = new UTicket();
        uTicket.setUTicketId("foo");
        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);

        UTicket newUTicket = new UTicket(uTicket);
        assert (uTicket.equals(newUTicket));
    }
}

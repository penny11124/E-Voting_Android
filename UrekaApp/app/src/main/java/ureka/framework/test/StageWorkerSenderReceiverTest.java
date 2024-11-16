package ureka.framework.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgReceiver;
import ureka.framework.logic.stage_worker.MsgSender;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.communication.simulated_comm.SimulatedCommChannel;
import ureka.framework.resource.crypto.ECDH;

public class StageWorkerSenderReceiverTest {
    private MsgSender msgSender;
    private MsgReceiver msgReceiver;

    @BeforeEach
    public void init() {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SharedData sharedData = new SharedData(thisDevice, currentSession, thisPerson);
        MeasureHelper measureHelper = new MeasureHelper(sharedData);
//        msgSender = new MsgSender(sharedData, measureHelper);
        msgReceiver = new MsgReceiver();
        msgReceiver.setSharedData(sharedData);
        msgReceiver.getSharedData().setMeasureRec(new HashMap<>());
        msgReceiver.setMsgVerifier(new MsgVerifier(sharedData, measureHelper));
        msgReceiver.setMeasureHelper(new MeasureHelper(sharedData));
    }

    // MsgSender
//    @Test
//    public void sendXxxMessageTest() throws InterruptedException {
//        msgSender.getSharedData().setSimulatedCommChannel(new SimulatedCommChannel());
//        msgSender.getSharedData().getSimulatedCommChannel().setSenderQueue(new LinkedList<>());
//
//        Message message = new Message();
//        message.setMessageOperation(Message.MESSAGE_RECV_AND_STORE);
//        message.setMessageType(RTicket.MESSAGE_TYPE);
//        String messageJson = Message.messageToJsonstr(message);
//        msgSender.sendXxxMessage(message.getMessageOperation(), message.getMessageType(), messageJson);
//        message.setMessageStr(messageJson);
//        messageJson = Message.messageToJsonstr(message);
//        assert (Objects.equals(Environment.transmittedMessage, messageJson));
//    }

    // MsgReceiver
    @Test
    public void recvXxxMessageTest() throws Exception {
        msgReceiver.getSharedData().setSimulatedCommChannel(new SimulatedCommChannel());
        msgReceiver.getSharedData().getSimulatedCommChannel().setReceiverQueue(new LinkedList<>());

        UTicket validUTicket = new UTicket();
        validUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        validUTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validUTicket.setDeviceId("foo");
        String validJson = UTicket.uTicketToJsonStr(validUTicket);
        validJson = ECDH.generateSha256HashStr(validJson);
        validUTicket.setUTicketId(validJson);
        validJson = UTicket.uTicketToJsonStr(validUTicket);

        Message validMessage = new Message();
        validMessage.setMessageOperation(Message.MESSAGE_VERIFY_AND_EXECUTE);
        validMessage.setMessageType(UTicket.MESSAGE_TYPE);
        validMessage.setMessageStr(validJson);
//        Environment.transmittedMessage = Message.messageToJsonstr(validMessage);
//        msgReceiver._recvXxxMessage();
        assert (Objects.equals(msgReceiver.getSharedData().getReceivedMessageJson(), UTicket.uTicketToJsonStr(validUTicket)));
    }
}

package test;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import ureka.framework.logic.pipeline_flow.FlowOpenSession;
import ureka.framework.logic.stage_worker.Executor;
import ureka.framework.logic.stage_worker.GeneratedMsgStorer;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgGenerator;
import ureka.framework.logic.stage_worker.MsgSender;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.logic.stage_worker.ReceivedMsgStorer;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.resource.communication.simulated_comm.SimulatedCommChannel;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class FlowOpenSessionTest {
    private FlowOpenSession flowOpenSession;

    @BeforeEach
    public void init() throws Exception {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SimpleStorage simpleStorage = new SimpleStorage("deviceName");

        KeyPair keyPair = ECC.generateKeyPair();
        thisDevice.setDeviceName("deviceName");
        thisDevice.setDevicePubKey((ECPublicKey) keyPair.getPublic());
        thisDevice.setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());
        thisDevice.setTicketOrder(1);
        currentSession.setCurrentUTicketId("currentUTicketId");
        currentSession.setChallenge1(ECDH.generateRandomStr(32));
        currentSession.setKeyExchangeSalt1(ECDH.generateRandomStr(32));
        currentSession.setIvCmd(SerializationUtil.byteToBase64Str(ECDH.gcmGenIv()));

        SharedData sharedData = new SharedData(thisDevice, currentSession, thisPerson);
        sharedData.setDeviceTable(new HashMap<>());
        sharedData.setMeasureRec(new HashMap<>());
        sharedData.setSimulatedCommChannel(new SimulatedCommChannel());
        sharedData.getSimulatedCommChannel().setSenderQueue(new LinkedList<>());
        MeasureHelper measureHelper = new MeasureHelper(sharedData);
        ReceivedMsgStorer receivedMsgStorer = new ReceivedMsgStorer(sharedData, measureHelper, simpleStorage);
        MsgVerifier msgVerifier = new MsgVerifier(sharedData, measureHelper);
        Executor executor = new Executor(sharedData, measureHelper, simpleStorage, msgVerifier);
        MsgGenerator msgGenerator = new MsgGenerator(sharedData, measureHelper);
        GeneratedMsgStorer generatedMsgStorer = new GeneratedMsgStorer(sharedData, measureHelper, simpleStorage);
        MsgSender msgSender = new MsgSender(sharedData, measureHelper);

        flowOpenSession = new FlowOpenSession(sharedData, measureHelper, receivedMsgStorer,
            msgVerifier, executor, msgGenerator, generatedMsgStorer, msgSender);
    }

    @Test
    public void deviceSendCrKe1Test() {
        flowOpenSession._deviceSendCrKe1("SUCCESS");
        String messageJson = flowOpenSession.getMsgSender().getSharedData().getSimulatedCommChannel().getSenderQueue().peek();
        Message message = Message.jsonstrToMessage(messageJson);
        assert (Objects.equals(message.getMessageOperation(), Message.MESSAGE_VERIFY_AND_EXECUTE));
        assert (Objects.equals(message.getMessageType(), RTicket.MESSAGE_TYPE));
        Map<String, String> rTicketRequest = new HashMap<>();
        rTicketRequest.put("r_ticket_type", RTicket.TYPE_CRKE1_RTICKET);
        rTicketRequest.put("device_id", flowOpenSession.getSharedData().getThisDevice().getDevicePubKeyStr());
        rTicketRequest.put("result", "SUCCESS");
        rTicketRequest.put("audit_start", flowOpenSession.getSharedData().getCurrentSession().getCurrentUTicketId());
        rTicketRequest.put("challenge1", flowOpenSession.getSharedData().getCurrentSession().getChallenge1());
        rTicketRequest.put("keyExchangeSalt1", flowOpenSession.getSharedData().getCurrentSession().getKeyExchangeSalt1());
        rTicketRequest.put("ivCmd", flowOpenSession.getSharedData().getCurrentSession().getIvCmd());
        RTicket generatedRTicket = RTicket.jsonStrToRTicket(message.getMessageStr());
        generatedRTicket.setDeviceSignature(null);
        RTicket testRTicket = RTicket.jsonStrToRTicket(flowOpenSession.getMsgGenerator().generateXxxRTicket(rTicketRequest));
        testRTicket.setDeviceSignature(null);
        assert (Objects.equals(RTicket.rTicketToJsonStr(generatedRTicket), RTicket.rTicketToJsonStr(testRTicket)));
    }
}

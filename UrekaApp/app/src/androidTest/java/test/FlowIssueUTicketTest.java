package test;

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

import ureka.framework.logic.pipeline_flow.FlowIssueUTicket;
import ureka.framework.logic.stage_worker.Executor;
import ureka.framework.logic.stage_worker.GeneratedMsgStorer;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgGenerator;
import ureka.framework.logic.stage_worker.MsgSender;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.logic.stage_worker.ReceivedMsgStorer;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.communication.simulated_comm.SimulatedCommChannel;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class FlowIssueUTicketTest {
    private FlowIssueUTicket flowIssueUTicket;

    @BeforeEach
    public void init() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SimpleStorage simpleStorage = new SimpleStorage("deviceName");

        thisDevice.setDeviceName("deviceName");
        KeyPair keyPair = ECC.generateKeyPair();
        thisDevice.setDevicePubKey((ECPublicKey) keyPair.getPublic());
        thisDevice.setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());

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
        // MsgSender msgSender = new MsgSender(sharedData, measureHelper, );

        // flowIssueUTicket = new FlowIssueUTicket(sharedData, measureHelper, receivedMsgStorer,
           //  msgVerifier, executor, msgGenerator, generatedMsgStorer, msgSender);
    }

    @Test
    public void issuerIssueUTicketToHerselfTest() {
        String device_id = "noId";
        Map<String, String> uTicketMap = new HashMap<>();
        uTicketMap.put("u_ticket_type", UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicketMap.put("device_id", device_id);
        flowIssueUTicket.issuerIssueUTicketToHerself(device_id, uTicketMap);
        assert (flowIssueUTicket.getGeneratedMsgStorer().getSharedData().getDeviceTable().containsKey(device_id));
        assert (flowIssueUTicket.getExecutor().getSharedData().getDeviceTable().get(device_id).getTicketOrder() == 0);
    }

    @Test
    public void issuerIssueUTicketToHolderTest() throws Exception {
        String device_id = "noId";
        Map<String, String> uTicketMap = new HashMap<>();
        uTicketMap.put("u_ticket_type", UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicketMap.put("device_id", device_id);
        flowIssueUTicket.getSharedData().getDeviceTable().put(device_id, new OtherDevice());
        flowIssueUTicket.issuerIssueUTicketToHolder(device_id, uTicketMap);
        assert (flowIssueUTicket.getGeneratedMsgStorer().getSharedData().getDeviceTable().containsKey(device_id));
        String messageJson = flowIssueUTicket.getMsgSender().getSharedData().getSimulatedCommChannel().getSenderQueue().peek();
        Message message = Message.jsonstrToMessage(messageJson);
        assert (Objects.equals(message.getMessageOperation(), Message.MESSAGE_RECV_AND_STORE));
        assert (Objects.equals(message.getMessageType(), UTicket.MESSAGE_TYPE));
        uTicketMap.put("ticket_order", String.valueOf(0));
        UTicket uTicket = new UTicket(uTicketMap);
        uTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(uTicket)));
        assert (Objects.equals(message.getMessageStr(), UTicket.uTicketToJsonStr(uTicket)));
    }

    @Test
    public void _holderRecvUTicketTest() {
        UTicket uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        uTicket.setDeviceId("device_id");
        uTicket.setTicketOrder(1234);
        flowIssueUTicket._holderRecvUTicket(uTicket);
        assert (flowIssueUTicket.getReceivedMsgStorer().getSharedData().getDeviceTable().containsKey(uTicket.getDeviceId()));
        assert (Objects.equals(flowIssueUTicket.getExecutor().getSharedData().getDeviceTable().get(uTicket.getDeviceId()).getTicketOrder(), uTicket.getTicketOrder()));
    }

    @Test
    public void holderSendRTicketToIssuerTest() {
        String device_id = "device_id";
        flowIssueUTicket.getSharedData().getDeviceTable().put(device_id, new OtherDevice());
        flowIssueUTicket.getSharedData().getDeviceTable().get(device_id).setDeviceRTicketForOwner(RTicket.rTicketToJsonStr(new RTicket()));
        flowIssueUTicket.holderSendRTicketToIssuer(device_id);
        String messageJson = flowIssueUTicket.getExecutor().getSharedData().getSimulatedCommChannel().getSenderQueue().peek();
        Message message = Message.jsonstrToMessage(messageJson);
        assert (Objects.equals(message.getMessageOperation(), Message.MESSAGE_RECV_AND_STORE));
        assert (Objects.equals(message.getMessageType(), RTicket.MESSAGE_TYPE));
        assert (Objects.equals(message.getMessageStr(), RTicket.rTicketToJsonStr(new RTicket())));
    }

    @Test
    public void _issuerRecvRTicketTest() throws Exception {
        String device_id = "device_id";
        UTicket uTicket = new UTicket();
        uTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        uTicket.setUTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        uTicket.setDeviceId(device_id);
        uTicket.setTicketOrder(114514);
        uTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(uTicket)));

        RTicket rTicket = new RTicket();
        rTicket.setRTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        rTicket.setDeviceId(device_id);
        rTicket.setResult("SUCCESS");
        rTicket.setTicketOrder(114515);
        rTicket.setAuditStart(uTicket.getUTicketId());
        KeyPair keyPair = ECC.generateKeyPair();
        rTicket.setRTicketId(SerializationUtil.keyToStr(keyPair.getPublic()));
        rTicket.setDeviceSignature(SerializationUtil.bytesToBase64(
            ECC.signSignature(SerializationUtil.strToBytes(RTicket.rTicketToJsonStr(rTicket)), (ECPrivateKey) keyPair.getPrivate())));

        flowIssueUTicket.getSharedData().getDeviceTable().put(device_id, new OtherDevice());
        flowIssueUTicket.getSharedData().getDeviceTable().get(device_id).setTicketOrder(114514);
        flowIssueUTicket.getSharedData().getDeviceTable().get(device_id).setDeviceOwnershipUTicketForOthers(UTicket.uTicketToJsonStr(uTicket));

        flowIssueUTicket._issuerRecvRTicket(rTicket);
        assert (Objects.equals(flowIssueUTicket.getSharedData().getResultMessage(), "-> SUCCESS: VERIFY_UT_HAS_EXECUTED"));
        assert (Objects.equals(flowIssueUTicket.getSharedData().getState(), ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT));
    }
}

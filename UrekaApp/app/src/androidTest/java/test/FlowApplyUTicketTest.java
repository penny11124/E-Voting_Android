package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import ureka.framework.logic.pipeline_flow.FlowApplyUTicket;
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

public class FlowApplyUTicketTest {
    private FlowApplyUTicket flowApplyUTicket;

    @BeforeEach
    public void init() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SimpleStorage simpleStorage = new SimpleStorage("deviceName");

        thisDevice.setDeviceName("deviceName");
        KeyPair keyPair = ECC.generateKeyPair();
        thisDevice.setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());
        thisDevice.setDevicePubKey((ECPublicKey) keyPair.getPublic());
        keyPair = ECC.generateKeyPair();
        thisPerson.setPersonPrivKey((ECPrivateKey) keyPair.getPrivate());
        thisPerson.setPersonPubKey((ECPublicKey) keyPair.getPublic());
        thisDevice.setOwnerPubKey((ECPublicKey) keyPair.getPublic());

        SharedData sharedData = new SharedData(thisDevice, currentSession, thisPerson);
        sharedData.getThisDevice().setTicketOrder(0);
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
        FlowOpenSession flowOpenSession = new FlowOpenSession(sharedData, measureHelper, receivedMsgStorer,
            msgVerifier, executor, msgGenerator, generatedMsgStorer, msgSender);
        flowApplyUTicket = new FlowApplyUTicket(sharedData, measureHelper, receivedMsgStorer, msgVerifier,
            executor, msgGenerator, generatedMsgStorer, msgSender, flowOpenSession);
    }

    @Test
    public void holderApplyUTicketTest() throws Exception {
        String device_id = "device_id";
        flowApplyUTicket.getSharedData().getDeviceTable().put(device_id, new OtherDevice());
        UTicket uTicket = new UTicket();
        uTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);

        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicket.setDeviceId(device_id);
        uTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(uTicket)));
        flowApplyUTicket.getSharedData().getDeviceTable().get(device_id).setDeviceUTicketForOwner(UTicket.uTicketToJsonStr(uTicket));

        flowApplyUTicket.holderApplyUTicket(device_id, "");
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getState(), ThisDevice.STATE_AGENT_WAIT_FOR_RT));
        String uTicketJson = flowApplyUTicket.getMsgSender().getSharedData().getSimulatedCommChannel().getSenderQueue().poll();
        Message message = Message.jsonstrToMessage(uTicketJson);
        assert (Objects.equals(message.getMessageOperation(), Message.MESSAGE_VERIFY_AND_EXECUTE));
        assert (Objects.equals(message.getMessageType(), UTicket.MESSAGE_TYPE));
        assert (Objects.equals(message.getMessageStr(), UTicket.uTicketToJsonStr(uTicket)));

        uTicket = new UTicket();
        uTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicket.setDeviceId(device_id);
        uTicket.setUTicketType(UTicket.TYPE_ACCESS_UTICKET);
        uTicket.setHolderId("holder_id");
        uTicket.setTaskScope("task_scope");
        uTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(uTicket)));
        flowApplyUTicket.getSharedData().getDeviceTable().get(device_id).setDeviceUTicketForOwner(UTicket.uTicketToJsonStr(uTicket));

        flowApplyUTicket.holderApplyUTicket(device_id, "holder");
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getState(), ThisDevice.STATE_AGENT_WAIT_FOR_CRKE1));
        uTicketJson = flowApplyUTicket.getMsgSender().getSharedData().getSimulatedCommChannel().getSenderQueue().poll();
        message = Message.jsonstrToMessage(uTicketJson);
        assert (Objects.equals(message.getMessageOperation(), Message.MESSAGE_VERIFY_AND_EXECUTE));
        assert (Objects.equals(message.getMessageType(), UTicket.MESSAGE_TYPE));
        assert (Objects.equals(message.getMessageStr(), UTicket.uTicketToJsonStr(uTicket)));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentUTicketId(), uTicket.getUTicketId()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentDeviceId(), uTicket.getDeviceId()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentHolderId(), uTicket.getHolderId()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentTaskScope(), uTicket.getTaskScope()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getPlaintextCmd(), "holder"));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getAssociatedPlaintextCmd(), "additional unencrypted cmd"));
    }

    @Test
    public void deviceRecvUTicketTest() throws Exception {
        UTicket uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicket.setDeviceId("noId");
        uTicket.setTicketOrder(0);
        uTicket.setHolderId(SerializationUtil.keyToStr(ECC.generateKeyPair().getPublic()));
        flowApplyUTicket.getExecutor().getSharedData().getThisDevice().setDeviceType(ThisDevice.IOT_DEVICE);
        flowApplyUTicket._deviceRecvUTicket(uTicket);
        SimpleLogger.simpleLog("info", "result = " + flowApplyUTicket.getSharedData().getResultMessage());
        assert (Objects.equals(flowApplyUTicket.getSharedData().getResultMessage(), "-> SUCCESS: VERIFY_UT_CAN_EXECUTE"));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getState(), ThisDevice.STATE_DEVICE_WAIT_FOR_UT));
        assert (flowApplyUTicket.getExecutor().getSharedData().getThisDevice().getDevicePubKey() != null);
        assert (flowApplyUTicket.getExecutor().getSharedData().getThisDevice().getDevicePrivKey() != null);
        assert (Objects.equals(SerializationUtil.keyToStr(flowApplyUTicket.getExecutor().getSharedData().getThisDevice().getOwnerPubKey()), uTicket.getHolderId()));

        SimpleLogger.simpleLog("info", "NEXT TESTCASE");
        uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_ACCESS_UTICKET);
        uTicket.setDeviceId(flowApplyUTicket.getMsgVerifier().getSharedData().getThisDevice().getDevicePubKeyStr());
        uTicket.setTicketOrder(flowApplyUTicket.getMsgVerifier().getSharedData().getThisDevice().getTicketOrder());
        uTicket.setTaskScope("task_scope");
        uTicket.setHolderId(SerializationUtil.keyToStr(ECC.generateKeyPair().getPublic()));
        uTicket.setIssuerSignature(SerializationUtil.byteToBase64Str(ECC.signSignature
            (SerializationUtil.strToByte(UTicket.uTicketToJsonStr(uTicket)),
                flowApplyUTicket.getMsgVerifier().getSharedData().getThisPerson().getPersonPrivKey())));
        flowApplyUTicket.getExecutor().getSharedData().getThisDevice().setDeviceType(ThisDevice.IOT_DEVICE);
        flowApplyUTicket._deviceRecvUTicket(uTicket);
        assert (Objects.equals(flowApplyUTicket.getSharedData().getResultMessage(), "-> SUCCESS: VERIFY_UT_CAN_EXECUTE"));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentUTicketId(), uTicket.getUTicketId()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentDeviceId(), uTicket.getDeviceId()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentHolderId(), uTicket.getHolderId()));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getCurrentTaskScope(), uTicket.getTaskScope()));
        assert (flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getChallenge1() != null);
        assert (flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getKeyExchangeSalt1() != null);
        assert (flowApplyUTicket.getExecutor().getSharedData().getCurrentSession().getIvCmd() != null);
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getState(), ThisDevice.STATE_DEVICE_WAIT_FOR_CRKE2));
    }

    @Test
    public void holderRecvRTicketTest() throws Exception {
        UTicket uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        uTicket.setDeviceId("noId");
        uTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(uTicket)));

        RTicket rTicket = new RTicket();
        rTicket.setDeviceId(uTicket.getDeviceId());
        rTicket.setRTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        rTicket.setResult("SUCCESS");
        rTicket.setTicketOrder(1);
        rTicket.setAuditStart(uTicket.getUTicketId());
        rTicket.setRTicketId(flowApplyUTicket.getSharedData().getThisDevice().getDevicePubKeyStr());
        rTicket.setDeviceSignature(SerializationUtil.byteToBase64Str(ECC.signSignature(SerializationUtil.strToByte(RTicket.rTicketToJsonStr(rTicket)),
            flowApplyUTicket.getSharedData().getThisDevice().getDevicePrivKey())));

        OtherDevice otherDevice = new OtherDevice();
        otherDevice.setDeviceUTicketForOwner(UTicket.uTicketToJsonStr(uTicket));
        flowApplyUTicket.getSharedData().getDeviceTable().put(rTicket.getDeviceId(), otherDevice);

        flowApplyUTicket._holderRecvRTicket(rTicket);
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getResultMessage(), "-> SUCCESS: VERIFY_UT_HAS_EXECUTED"));
        assert (Objects.equals(flowApplyUTicket.getExecutor().getSharedData().getState(), ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT));
    }
}

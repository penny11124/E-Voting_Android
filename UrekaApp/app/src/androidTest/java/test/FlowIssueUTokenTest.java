package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serial;
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

import ureka.framework.logic.pipeline_flow.FlowApplyUTicket;
import ureka.framework.logic.pipeline_flow.FlowIssueUToken;
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
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.communication.simulated_comm.SimulatedCommChannel;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class FlowIssueUTokenTest {
    private FlowIssueUToken flowIssueUToken;

    @BeforeEach
    public void init() {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SimpleStorage simpleStorage = new SimpleStorage("device_id");

        thisDevice.setDeviceName("deviceName");
        Map<String, String> task_scope = new HashMap<>();
        task_scope.put("ALL", "allow");
        currentSession.setCurrentTaskScope(SerializationUtil.mapToJson(task_scope));

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
        FlowOpenSession flowOpenSession = new FlowOpenSession(sharedData, measureHelper, receivedMsgStorer, msgVerifier, executor
            , msgGenerator, generatedMsgStorer, msgSender);
        FlowApplyUTicket flowApplyUTicket = new FlowApplyUTicket(sharedData, measureHelper, receivedMsgStorer, msgVerifier, executor
            , msgGenerator, generatedMsgStorer, msgSender, flowOpenSession);

        flowIssueUToken = new FlowIssueUToken(sharedData, measureHelper, receivedMsgStorer, msgVerifier, executor
            , msgGenerator, generatedMsgStorer, msgSender, flowApplyUTicket);
    }

    @Test
    public void holderSendCmdTest() throws Exception {
        String device_id = "device_id", cmd = "cmd";
        flowIssueUToken.getSharedData().getDeviceTable().put(device_id, new OtherDevice());
        KeyPair k1 = ECC.generateKeyPair(), k2 = ECC.generateKeyPair();
        byte[] salt = ECDH.generateRandomByte(32);
        byte[] ecdh_key = ECDH.generateEcdhKey((ECPrivateKey) k1.getPrivate(), salt, null, (ECPublicKey) k2.getPublic());
        flowIssueUToken.getSharedData().getCurrentSession().setCurrentSessionKeyStr(SerializationUtil.bytesToBase64(ecdh_key));
        flowIssueUToken.getSharedData().getCurrentSession().setCurrentDeviceId(device_id);
        flowIssueUToken.getSharedData().getCurrentSession().setIvData(SerializationUtil.bytesToBase64(ECDH.gcmGenIv()));

        flowIssueUToken.holderSendCmd(device_id, cmd, false);
        assert (Objects.equals(flowIssueUToken.getExecutor().getSharedData().getCurrentSession().getPlaintextCmd(), cmd));
        assert (Objects.equals(flowIssueUToken.getExecutor().getSharedData().getCurrentSession().getAssociatedPlaintextCmd(), "AUC"));
        assert (flowIssueUToken.getExecutor().getSharedData().getCurrentSession().getCiphertextCmd() != null);
        assert (flowIssueUToken.getExecutor().getSharedData().getCurrentSession().getGcmAuthenticationTagCmd() != null);
        assert (flowIssueUToken.getExecutor().getSharedData().getCurrentSession().getIvData() != null);
        assert (Objects.equals(flowIssueUToken.getExecutor().getSharedData().getState(), ThisDevice.STATE_AGENT_WAIT_FOR_DATA));
        String messageJson = flowIssueUToken.getMsgSender().getSharedData().getSimulatedCommChannel().getSenderQueue().peek();
        Message message = Message.jsonstrToMessage(messageJson);
        assert (Objects.equals(message.getMessageOperation(), Message.MESSAGE_VERIFY_AND_EXECUTE));
        assert (Objects.equals(message.getMessageType(), UTicket.MESSAGE_TYPE));
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("device_id", flowIssueUToken.getSharedData().getCurrentSession().getCurrentDeviceId());
        generatedRequest.put("u_ticket_type", UTicket.TYPE_CMD_UTOKEN);
        generatedRequest.put("associated_plaintext_cmd", flowIssueUToken.getSharedData().getCurrentSession().getAssociatedPlaintextCmd());
        generatedRequest.put("ciphertext_cmd", flowIssueUToken.getSharedData().getCurrentSession().getCiphertextCmd());
        generatedRequest.put("gcm_authentication_tag_cmd", flowIssueUToken.getSharedData().getCurrentSession().getGcmAuthenticationTagCmd());
        generatedRequest.put("iv_data", flowIssueUToken.getSharedData().getCurrentSession().getIvData());
        assert (Objects.equals(message.getMessageStr(), flowIssueUToken.getMsgGenerator().generateXxxUTicket(generatedRequest)));
    }

    @Test
    public void deviceRecvCmdTest() throws Exception {
        holderSendCmdTest();

        KeyPair keyPair = ECC.generateKeyPair();
        flowIssueUToken.getSharedData().getThisDevice().setDevicePubKey((ECPublicKey) keyPair.getPublic());
        flowIssueUToken.getSharedData().getThisDevice().setTicketOrder(114514);


        UTicket uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_CMD_UTOKEN);
        uTicket.setDeviceId(SerializationUtil.keyToStr(keyPair.getPublic()));
        uTicket.setTicketOrder(114514);

        flowIssueUToken._deviceRecvCmd(uTicket);
        assert (flowIssueUToken.getExecutor().getSharedData().getCurrentSession().getPlaintextCmd() != null);
//        assert (Objects.equals(flowIssueUToken.getSharedData().getResultMessage(), "-> SUCCESS: VERIFY_UT_CAN_EXECUTE"));
        assert (Objects.equals(flowIssueUToken.getExecutor().getSharedData().getState(), ThisDevice.STATE_DEVICE_WAIT_FOR_CMD));
    }
}

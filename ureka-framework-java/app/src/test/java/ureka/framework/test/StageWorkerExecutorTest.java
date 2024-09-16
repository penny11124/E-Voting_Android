package ureka.framework.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.logic.stage_worker.Executor;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class StageWorkerExecutorTest {
    private Executor executor;

    @BeforeEach
    public void init() {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SharedData sharedData = new SharedData(thisDevice, currentSession, thisPerson);
        sharedData.setMeasureRec(new HashMap<>());
        sharedData.setDeviceTable(new HashMap<>());
        sharedData.getCurrentSession().setCurrentTaskScope(SerializationUtil.dictToJsonStr(new HashMap<>()));
        MeasureHelper measureHelper = new MeasureHelper(sharedData);
        SimpleStorage simpleStorage = new SimpleStorage("foo");
        MsgVerifier msgVerifier = new MsgVerifier(sharedData, measureHelper);
        executor = new Executor(sharedData, measureHelper, simpleStorage, msgVerifier);
    }

    @Test
    public void initializeStateTest() {
        executor.getSharedData().getThisDevice().setDeviceType(ThisDevice.IOT_DEVICE);
        executor._initializeState();
        assert (Objects.equals(executor.getSharedData().getState(), ThisDevice.STATE_DEVICE_WAIT_FOR_UT));
    }

    @Test
    public void executeOneTimeSetDeviceTypeAndNameTest() {
        String type = ThisDevice.IOT_DEVICE;
        String name = "foo";
        executor._executeOneTimeSetDeviceTypeAndName(type, name);
        assert (executor.getSharedData().getThisDevice().getHasDeviceType());
        assert (executor.getSharedData().getThisDevice().getTicketOrder() == 0);
    }

    @Test
    public void executeOneTimeInitializeAgentOrServerTest() {
        executor.getSharedData().getThisDevice().setDeviceType(ThisDevice.USER_AGENT_OR_CLOUD_SERVER);
        executor.getSharedData().getThisDevice().setTicketOrder(0);
        executor._executeOneTimeInitializeAgentOrServer();
        assert (executor.getSharedData().getThisDevice().getDevicePubKey() != null);
        assert (executor.getSharedData().getThisDevice().getDevicePrivKey() != null);
        assert (executor.getSharedData().getThisPerson().getPersonPubKey() != null);
        assert (executor.getSharedData().getThisPerson().getPersonPrivKey() != null);
        assert (executor.getSharedData().getThisPerson().getPersonPubKey() == executor.getSharedData().getThisDevice().getOwnerPubKey());

        executor.getSharedData().getThisDevice().setDeviceType(ThisDevice.IOT_DEVICE);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            executor._executeOneTimeInitializeAgentOrServer();
        });
        assertEquals(exception.getMessage(), "-> FAILURE: ONLY USER-AGENT-OR-CLOUD-SERVER CAN DO THIS INITIALIZATION OPERATION");

        executor.getSharedData().getThisDevice().setDeviceType(ThisDevice.USER_AGENT_OR_CLOUD_SERVER);
        executor.getSharedData().getThisDevice().setTicketOrder(1);
        exception = assertThrows(RuntimeException.class, () -> {
            executor._executeOneTimeInitializeAgentOrServer();
        });
        assertEquals(exception.getMessage(), "-> FAILURE: VERIFY_TICKET_ORDER: USER-AGENT-OR-CLOUD-SERVER ALREADY INITIALIZED");
    }

    @Test
    public void executeXxxUTicketTest() throws Exception {
        executor.getSharedData().getThisDevice().setTicketOrder(0);

        UTicket uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        uTicket.setHolderId(SerializationUtil.keyToStr(ECC.generateKeyPair().getPublic()));
        executor.getSharedData().getThisDevice().setDeviceType(ThisDevice.IOT_DEVICE);
        executor.executeXxxUTicket(uTicket);
        assertEquals(SerializationUtil.strToKey(uTicket.getHolderId(), "eccPublicKey"), executor.getSharedData().getThisDevice().getOwnerPubKey());

        uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        uTicket.setHolderId(SerializationUtil.keyToStr(ECC.generateKeyPair().getPublic()));
        executor.executeXxxUTicket(uTicket);
        assertEquals(SerializationUtil.strToKey(uTicket.getHolderId(), "eccPublicKey"), executor.getSharedData().getThisDevice().getOwnerPubKey());

        uTicket = new UTicket();
        uTicket.setUTicketType(UTicket.TYPE_ACCESS_UTICKET);
        uTicket.setUTicketId("uTicketId");
        uTicket.setDeviceId("deviceId");
        uTicket.setHolderId("holderId");
        uTicket.setTaskScope("taskScope");
        executor.getSharedData().setCurrentSession(new CurrentSession());
        executor.executeXxxUTicket(uTicket);
        assertEquals(uTicket.getUTicketId(), executor.getSharedData().getCurrentSession().getCurrentUTicketId());
        assertEquals(uTicket.getDeviceId(), executor.getSharedData().getCurrentSession().getCurrentDeviceId());
        assertEquals(uTicket.getHolderId(), executor.getSharedData().getCurrentSession().getCurrentHolderId());
        assertEquals(uTicket.getTaskScope(), executor.getSharedData().getCurrentSession().getCurrentTaskScope());

        KeyPair keyPair1 = ECC.generateKeyPair(), keyPair2 = ECC.generateKeyPair();
        byte[] salt = ECDH.generateRandomByte(32);
        byte[] ecdh_key = ECDH.generateEcdhKey((ECPrivateKey) keyPair1.getPrivate(), salt, null, (ECPublicKey) keyPair2.getPublic());

        String plaintext = "ACCESS_END", associatedPlaintext = "test";
        byte[][] gcm_result = ECDH.gcmEncrypt(plaintext.getBytes(), associatedPlaintext.getBytes(), ecdh_key, null); // ciphertext, iv, tag

        uTicket = new UTicket();
        executor.getSharedData().setCurrentSession(new CurrentSession());

        uTicket.setUTicketType(UTicket.TYPE_ACCESS_END_UTOKEN);
        uTicket.setCiphertextCmd(SerializationUtil.byteToBase64Str(gcm_result[0]));
        uTicket.setAssociatedPlaintextCmd(associatedPlaintext);
        uTicket.setGcmAuthenticationTagCmd(SerializationUtil.byteToBase64Str(gcm_result[2]));
        executor.getSharedData().getCurrentSession().setCurrentSessionKeyStr(SerializationUtil.byteToBase64Str(ecdh_key));
        executor.getSharedData().getCurrentSession().setIvCmd(SerializationUtil.byteToBase64Str(gcm_result[1]));

        executor.executeXxxUTicket(uTicket);

        assertEquals(executor.getSharedData().getResultMessage(), "-> SUCCESS: VERIFY_ACCESS_END");
    }

    @Test
    public void executeXxxRTicketTest() throws Exception {
        RTicket rTicket;
        KeyPair personKey = ECC.generateKeyPair(), deviceKey = ECC.generateKeyPair();
        executor.getSharedData().getThisPerson().setPersonPrivKey((ECPrivateKey) personKey.getPrivate());
        executor.getSharedData().getThisDevice().setDevicePrivKey((ECPrivateKey) deviceKey.getPrivate());
        executor.getSharedData().getCurrentSession().setCurrentDeviceId(SerializationUtil.keyToStr(deviceKey.getPublic()));
        executor.getSharedData().getCurrentSession().setCurrentHolderId(SerializationUtil.keyToStr(personKey.getPublic()));
        Map<String, String> taskScope = SerializationUtil.jsonStrToDict(executor.getMsgVerifier().getSharedData().getCurrentSession().getCurrentTaskScope());
        taskScope.put("ALL", "allow");
        executor.getMsgVerifier().getSharedData().getCurrentSession().setCurrentTaskScope(SerializationUtil.dictToJsonStr(taskScope));

        rTicket = new RTicket();
        rTicket.setRTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        rTicket.setDeviceId("foo");
        rTicket.setTicketOrder(1);
        executor.getSharedData().getDeviceTable().put("foo", new OtherDevice());
        executor.executeXxxRTicket(rTicket, "holderOrDevice");

        rTicket = new RTicket();
        rTicket.setRTicketType(RTicket.TYPE_CRKE1_RTICKET);
        rTicket.setChallenge1(ECDH.generateRandomStr(32));
        rTicket.setKeyExchangeSalt1(ECDH.generateRandomStr(32));
        rTicket.setIvCmd(SerializationUtil.byteToBase64Str(ECDH.gcmGenIv()));
        executor.getSharedData().getCurrentSession().setPlaintextCmd("foo");
        executor.getSharedData().getCurrentSession().setAssociatedPlaintextCmd("test");
        executor.executeXxxRTicket(rTicket, "holderOrDevice");
        byte[][] resultCrke1 = ECDH.gcmEncrypt(
            SerializationUtil.strToByte(executor.getSharedData().getCurrentSession().getPlaintextCmd()),
            SerializationUtil.strToByte(executor.getSharedData().getCurrentSession().getAssociatedPlaintextCmd()),
            SerializationUtil.base64StrBackToByte(executor.getSharedData().getCurrentSession().getCurrentSessionKeyStr()),
            SerializationUtil.base64StrBackToByte(executor.getSharedData().getCurrentSession().getIvCmd())
        );
        assertEquals(executor.getSharedData().getCurrentSession().getCiphertextCmd(), SerializationUtil.byteToBase64Str(resultCrke1[0]));
        assertEquals(executor.getSharedData().getCurrentSession().getGcmAuthenticationTagCmd(), SerializationUtil.byteToBase64Str(resultCrke1[2]));

        rTicket = new RTicket();
        rTicket.setRTicketType(RTicket.TYPE_CRKE2_RTICKET);
        rTicket.setChallenge2(executor.getSharedData().getCurrentSession().getChallenge2());
        rTicket.setKeyExchangeSalt2(executor.getSharedData().getCurrentSession().getKeyExchangeSalt2());
        rTicket.setCiphertextCmd(executor.getSharedData().getCurrentSession().getCiphertextCmd());
        rTicket.setAssociatedPlaintextCmd(executor.getSharedData().getCurrentSession().getAssociatedPlaintextCmd());
        rTicket.setGcmAuthenticationTagCmd(executor.getSharedData().getCurrentSession().getGcmAuthenticationTagCmd());
        rTicket.setIvData(executor.getSharedData().getCurrentSession().getIvData());
        executor.executeXxxRTicket(rTicket, "holderOrDevice");
        assertEquals(executor.getSharedData().getCurrentSession().getPlaintextCmd(), "foo");

        rTicket = new RTicket();
        rTicket.setRTicketType(RTicket.TYPE_CRKE3_RTICKET);
        rTicket.setIvData(executor.getSharedData().getCurrentSession().getIvData());
        rTicket.setCiphertextData(executor.getSharedData().getCurrentSession().getCiphertextData());
        rTicket.setAssociatedPlaintextData(executor.getSharedData().getCurrentSession().getAssociatedPlaintextData());
        rTicket.setGcmAuthenticationTagData(executor.getSharedData().getCurrentSession().getGcmAuthenticationTagData());
        executor.executeXxxRTicket(rTicket, "holderOrDevice");
        assertEquals(executor.getSharedData().getCurrentSession().getPlaintextData(), "DATA: foo");

        rTicket = new RTicket();
        rTicket.setRTicketType(RTicket.TYPE_DATA_RTOKEN);
        rTicket.setIvCmd(executor.getSharedData().getCurrentSession().getIvCmd());
        rTicket.setIvData(executor.getSharedData().getCurrentSession().getIvData());
        rTicket.setCiphertextData(executor.getSharedData().getCurrentSession().getCiphertextData());
        rTicket.setAssociatedPlaintextData(executor.getSharedData().getCurrentSession().getAssociatedPlaintextData());
        rTicket.setGcmAuthenticationTagData(executor.getSharedData().getCurrentSession().getGcmAuthenticationTagData());
        executor.executeXxxRTicket(rTicket, "holderOrDevice");
        assertEquals(executor.getSharedData().getCurrentSession().getPlaintextData(), "DATA: foo");

        rTicket = new RTicket();
        rTicket.setRTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        rTicket.setDeviceId("foo");
        executor.executeXxxRTicket(rTicket, "issuer");
        assert (executor.getSharedData().getDeviceTable().get("foo") == null);

        rTicket = new RTicket();
        rTicket.setRTicketType(UTicket.TYPE_ACCESS_END_UTOKEN);
        rTicket.setDeviceId("foo");
        rTicket.setTicketOrder(1);
        executor.getSharedData().getDeviceTable().put("foo", new OtherDevice());
        executor.executeXxxRTicket(rTicket, "issuer");
        assertEquals(executor.getSharedData().getDeviceTable().get(rTicket.getDeviceId()).getTicketOrder(), rTicket.getTicketOrder());
    }
}
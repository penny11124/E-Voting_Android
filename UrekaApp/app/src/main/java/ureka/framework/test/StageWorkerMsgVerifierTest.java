package ureka.framework.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.util.concurrent.ClosingFuture;

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
import java.util.Map;
import java.util.Objects;

import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.logic.stage_worker.MsgVerifierUTicket;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;

public class StageWorkerMsgVerifierTest {
    private static MsgVerifier msgVerifier;

    @BeforeEach
    public void init() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ThisDevice thisDevice = new ThisDevice();
        thisDevice.setDeviceName("foo");
        thisDevice.setTicketOrder(0);
        KeyPair keyPair = ECC.generateKeyPair();
        thisDevice.setDevicePubKey((ECPublicKey) keyPair.getPublic());
        thisDevice.setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());
        SharedData sharedData = new SharedData(thisDevice, new CurrentSession(), new ThisPerson());
        msgVerifier = new MsgVerifier(sharedData, new MeasureHelper(sharedData));
    }

    @Test
    public void classifyUTicketIsDefinedTypeTest() throws Exception {
        UTicket validUTicket = new UTicket();
        validUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        validUTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validUTicket.setDeviceId("foo");
        String validJson = UTicket.uTicketToJsonStr(validUTicket);
        validJson = ECDH.generateSha256HashStr(validJson);
        validUTicket.setUTicketId(validJson);
        msgVerifier._classifyUTicketIsDefinedType(UTicket.uTicketToJsonStr(validUTicket));

        UTicket invalidUTicket = new UTicket();
        invalidUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        invalidUTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        invalidUTicket.setDeviceId("foo");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier._classifyUTicketIsDefinedType(UTicket.uTicketToJsonStr(invalidUTicket));
        });
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: -> FAILURE: VERIFY_UTICKET_ID");
    }

    @Test
    public void classifyRTicketIsDefinedTypeTest() throws Exception {
        RTicket validRTicket = new RTicket();
        validRTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        validRTicket.setRTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validRTicket.setDeviceId("foo");
        String validJson = RTicket.rTicketToJsonStr(validRTicket);
        validJson = ECDH.generateSha256HashStr(validJson);
        validRTicket.setRTicketId(validJson);
        msgVerifier._classifyRTicketIsDefinedType(RTicket.rTicketToJsonStr(validRTicket));

        RTicket invalidRTicket = new RTicket();
        invalidRTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        invalidRTicket.setRTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        invalidRTicket.setDeviceId("foo");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier._classifyRTicketIsDefinedType(RTicket.rTicketToJsonStr(invalidRTicket));
        });
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: -> FAILURE: VERIFY_RTICKET_ID");
    }

    @Test
    public void classifyMessageIsDefinedTypeTest() throws Exception {
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
        msgVerifier._classifyMessageIsDefinedType(Message.messageToJsonstr(validMessage));

        Message invalidMessage = new Message();
        invalidMessage.setMessageOperation(Message.MESSAGE_VERIFY_AND_EXECUTE);
        invalidMessage.setMessageType(UTicket.MESSAGE_TYPE);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier._classifyMessageIsDefinedType(Message.messageToJsonstr(invalidMessage));
        });
        assertEquals(exception.getMessage(), "-> FAILURE: VERIFY_MESSAGE_STR");
    }

    @Test
    public void verifyUTicketCanExecuteTest() {
        UTicket validUTicket = new UTicket();
        validUTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validUTicket.setDeviceId("noId");
        validUTicket.setTicketOrder(0);
        msgVerifier.verifyUTicketCanExecute(validUTicket);

        UTicket invalidUTicket = new UTicket();
        invalidUTicket.setUTicketType(UTicket.TYPE_ACCESS_UTICKET);
        invalidUTicket.setDeviceId(msgVerifier.getSharedData().getThisDevice().getDevicePubKeyStr());
        invalidUTicket.setTicketOrder(0);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier.verifyUTicketCanExecute(invalidUTicket);
        });
        assertEquals(exception.getMessage(), "Shouldn't Reach Here");
    }

    @Test
    public void verifyUTicketHasExecutedThroughRTicketTest() throws Exception {
        msgVerifier.getSharedData().getThisDevice().setTicketOrder(10);
        UTicket auditStartTicket = new UTicket();
        auditStartTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        auditStartTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        auditStartTicket.setDeviceId("foo");
        String validJson = UTicket.uTicketToJsonStr(auditStartTicket);
        validJson = ECDH.generateSha256HashStr(validJson);
        auditStartTicket.setUTicketId(validJson);
        UTicket auditEndTicket = new UTicket();
        KeyPair keyPair = ECC.generateKeyPair();

        RTicket validRTicket = new RTicket();
        validRTicket.setRTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validRTicket.setResult("SUCCESS");
        validRTicket.setTicketOrder(1);
        validRTicket.setAuditStart(auditStartTicket.getUTicketId());
        validRTicket.setRTicketId(SerializationUtil.keyToStr(keyPair.getPublic()));
        String validRTicketStr = RTicket.rTicketToJsonStr(validRTicket);
        byte[] validRTicketByte = SerializationUtil.strToByte(validRTicketStr);
        byte[] validRTicketSignature = ECC.signSignature(validRTicketByte, (ECPrivateKey) keyPair.getPrivate());
        validRTicket.setDeviceSignature(SerializationUtil.byteToBase64Str(validRTicketSignature));
        msgVerifier.verifyUTicketHasExecutedThroughRTicket(validRTicket, auditStartTicket, auditEndTicket);

        RTicket invalidRTicket = new RTicket();
        invalidRTicket.setRTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        invalidRTicket.setResult("SUCCESS");
        invalidRTicket.setTicketOrder(11);
        invalidRTicket.setAuditStart(UTicket.uTicketToJsonStr(auditStartTicket));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier.verifyUTicketHasExecutedThroughRTicket(invalidRTicket, auditStartTicket, auditEndTicket);
        });
        assertEquals(exception.getMessage(), "Shouldn't Reach Here");
    }

    @Test
    public void verifyCmdIsInTaskScopeTest() {
        Map<String, String> taskScope = new HashMap<>();
        taskScope.put("ALL", "allow");
        msgVerifier.getSharedData().getCurrentSession().setCurrentTaskScope(SerializationUtil.dictToJsonStr(taskScope));
        msgVerifier.verifyCmdIsInTaskScope("foo");

        taskScope.clear();
        taskScope.put("SAY-HELLO-3", "allow");
        msgVerifier.getSharedData().getCurrentSession().setCurrentTaskScope(SerializationUtil.dictToJsonStr(taskScope));
        msgVerifier.verifyCmdIsInTaskScope("HELLO-3");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier.verifyCmdIsInTaskScope("foo");
        });
        assertEquals(exception.getMessage(), "-> FAILURE: VERIFY_CMD_IN_TASK_SCOPE: Undefined or Forbidden Command: foo");
    }
}

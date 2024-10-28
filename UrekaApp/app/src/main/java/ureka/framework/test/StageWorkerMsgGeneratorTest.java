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
import java.util.HashMap;
import java.util.Map;

import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgGenerator;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;

public class StageWorkerMsgGeneratorTest {
    private MsgGenerator msgGenerator;
    private MsgVerifier msgVerifier;

    @BeforeEach
    public void init() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        SharedData sharedData = new SharedData(new ThisDevice(), new CurrentSession(), new ThisPerson());
        sharedData.getThisDevice().setDeviceName("foo");
        sharedData.getThisDevice().setTicketOrder(10);
        KeyPair keyPair = ECC.generateKeyPair();
        sharedData.getThisDevice().setDevicePubKey((ECPublicKey) keyPair.getPublic());
        sharedData.getThisDevice().setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());
        msgGenerator = new MsgGenerator(sharedData, new MeasureHelper(sharedData));
        msgVerifier = new MsgVerifier(sharedData, new MeasureHelper(sharedData));
    }

    @Test
    public void generateXxxUTicketTest() {
        Map<String, String> validUTicket = new HashMap<>();
        validUTicket.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);
        validUTicket.put("deviceId", "foo");
        String validUTicketJson = msgGenerator.generateXxxUTicket(validUTicket);
        msgVerifier._classifyUTicketIsDefinedType(validUTicketJson);

        Map<String, String> invalidUTicket = new HashMap<>();
        invalidUTicket.put("uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET);
        String invalidUTicketJson = msgGenerator.generateXxxUTicket(invalidUTicket);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier._classifyUTicketIsDefinedType(invalidUTicketJson);
        });
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: -> FAILURE: HAS_DEVICE_ID = null");
    }

    @Test
    public void generateXxxRTicketTest() {
        Map<String, String> validRTicket = new HashMap<>();
        validRTicket.put("rTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);
        validRTicket.put("deviceId", "foo");
        String validRTicketJson = msgGenerator.generateXxxRTicket(validRTicket);
        msgVerifier._classifyRTicketIsDefinedType(validRTicketJson);

        Map<String, String> invalidRTicket = new HashMap<>();
        invalidRTicket.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);
        String invalidRTicketJson = msgGenerator.generateXxxUTicket(invalidRTicket);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            msgVerifier._classifyUTicketIsDefinedType(invalidRTicketJson);
        });
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: -> FAILURE: HAS_DEVICE_ID = null");
    }
}

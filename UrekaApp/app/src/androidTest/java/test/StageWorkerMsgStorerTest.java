package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import ureka.framework.logic.stage_worker.GeneratedMsgStorer;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.ReceivedMsgStorer;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class StageWorkerMsgStorerTest {
    private GeneratedMsgStorer generatedMsgStorer;
    private ReceivedMsgStorer receivedMsgStorer;

    @BeforeEach
    public void init() {
        ThisDevice thisDevice = new ThisDevice();
        CurrentSession currentSession = new CurrentSession();
        ThisPerson thisPerson = new ThisPerson();
        SharedData sharedData = new SharedData(thisDevice, currentSession, thisPerson);
        sharedData.setDeviceTable(new HashMap<>());
        sharedData.getDeviceTable().put("noId", new OtherDevice());
        sharedData.getDeviceTable().get("noId").setDeviceUTicketForOwner("");
        MeasureHelper measureHelper = new MeasureHelper(sharedData);
        SimpleStorage simpleStorage = new SimpleStorage("foo");
        generatedMsgStorer = new GeneratedMsgStorer(sharedData, measureHelper, simpleStorage);
        receivedMsgStorer = new ReceivedMsgStorer(sharedData, measureHelper, simpleStorage);
    }

    // GeneratedMsgStorer
    @Test
    public void storeGeneratedXxxUTicketTest() throws Exception {
        UTicket validUTicket = new UTicket();
        validUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        validUTicket.setUTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validUTicket.setDeviceId("foo");
        validUTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(validUTicket)));
        generatedMsgStorer.storeGeneratedXxxUTicket(UTicket.uTicketToJsonStr(validUTicket));

        UTicket invalidUTicket = new UTicket();
        invalidUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        invalidUTicket.setUTicketType("bar");
        invalidUTicket.setDeviceId("foo");
        invalidUTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(invalidUTicket)));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            generatedMsgStorer.storeGeneratedXxxUTicket(UTicket.uTicketToJsonStr(invalidUTicket));
        });
        assertEquals(exception.getMessage(), "Not implemented yet");
    }

    // ReceivedMsgStorer
    @Test
    public void storeReceivedXxxUTicketTest() throws Exception {
        UTicket validUTicket = new UTicket();
        validUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        validUTicket.setUTicketType(UTicket.TYPE_OWNERSHIP_UTICKET);
        validUTicket.setDeviceId("foo");
        validUTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(validUTicket)));
        receivedMsgStorer.storeReceivedXxxUTicket(validUTicket);

        UTicket invalidUTicket = new UTicket();
        invalidUTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        invalidUTicket.setUTicketType("bar");
        invalidUTicket.setDeviceId("foo");
        invalidUTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(invalidUTicket)));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            receivedMsgStorer.storeReceivedXxxUTicket(invalidUTicket);
        });
        assertEquals(exception.getMessage(), "Not implemented yet");
    }

    @Test
    public void storeReceivedXxxRTicketTest() throws Exception {
        RTicket validRTicket = new RTicket();
        validRTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        validRTicket.setRTicketType(UTicket.TYPE_INITIALIZATION_UTICKET);
        validRTicket.setDeviceId("foo");
        validRTicket.setRTicketId(ECDH.generateSha256HashStr(RTicket.rTicketToJsonStr(validRTicket)));
        receivedMsgStorer.storeReceivedXxxRTicket(validRTicket);

        RTicket invalidRTicket = new RTicket();
        invalidRTicket.setProtocolVersion(UTicket.PROTOCOL_VERSION);
        invalidRTicket.setRTicketType("bar");
        invalidRTicket.setDeviceId("foo");
        invalidRTicket.setRTicketId(ECDH.generateSha256HashStr(RTicket.rTicketToJsonStr(invalidRTicket)));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            receivedMsgStorer.storeReceivedXxxRTicket(invalidRTicket);
        });
        assertEquals(exception.getMessage(), "Shouldn't Reach Here");
    }
}

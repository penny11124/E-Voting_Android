package test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceOwnerAgentAndHerDevice;
import static Conftest.deviceOwnerAgentAndHerSession;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.crypto.SerializationUtil;

public class Test04SuccessWhenAccessDeviceByOwner {
    private DeviceController userAgentDO;
    private DeviceController iotDevice;

    // RE-GIVEN: Reset the test environment
    // @BeforeEach
    public void setup() {
        currentSetupLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // RE-GIVEN: Reset the test environment
    // @AfterEach
    public void teardown() {
        currentTeardownLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // @Test
    public void testSuccessWhenApplySelfAccessUTicketOnDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // WHEN:
        currentTestWhenAndThenLog();

        // WHEN: Issuer: DO's UA generate the self_access_u_ticket to herself
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "device_id", targetDeviceId,
                "holder_id", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "u_ticket_type", UTicket.TYPE_SELFACCESS_UTICKET,
                "task_scope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

        // WHEN: Holder: DO's UA forward the selfAccessUTicket
        // createSimulatedCommConnection(this.userAgentDO,this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.iotDevice);

        // THEN: DO's UA succeed to access DO's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.userAgentDO.getSharedData().getResultMessage().contains("SUCCESS"));
        // THEN: Device: Still DO's IoTD
        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(),this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr());
        // THEN: Device: DO's UA can share a private session with DO's IoTD
        assertEquals(generatedCommand,this.iotDevice.getSharedData().getCurrentSession().getPlaintextCmd());
        assertEquals("DATA: " + generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextData());
        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.userAgentDO.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));
    }

    // Skipped
    // @Test
    public void testSuccessWhenRebootDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a session on DO's IoTD
        Pair devices = deviceOwnerAgentAndHerSession();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.userAgentDO.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));

        // HEN: Reboot DO's IoTD
        currentTestWhenAndThenLog();
        this.userAgentDO.rebootDevice();
        this.iotDevice.rebootDevice();

        // THEN: The session is deleted (RAM-only)
        assertEquals("{}",CurrentSession.currentSessionToJsonStr(this.userAgentDO.getSharedData().getCurrentSession()));
        assertEquals("{}",CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));
    }

    public void runAll() {
        setup();
        testSuccessWhenApplySelfAccessUTicketOnDevice();
        teardown();
    }
}


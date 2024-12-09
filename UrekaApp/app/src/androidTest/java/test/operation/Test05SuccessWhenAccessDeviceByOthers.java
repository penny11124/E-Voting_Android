package test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceOwnerAgentAndHerDevice;
import static Conftest.enterpriseProviderServer;
import static Conftest.enterpriseProviderServerAndHerSession;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.Triple;
import ureka.framework.resource.crypto.SerializationUtil;

public class Test05SuccessWhenAccessDeviceByOthers {
    private DeviceController userAgentDO;
    private DeviceController iotDevice;
    private DeviceController cloudServerEP;

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
    public void testSuccessWhenApplyAccessUTicketOnDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair devices = deviceOwnerAgentAndHerDevice();
        this.userAgentDO = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // GIVEN: Initialized EP's CS
        this.cloudServerEP = enterpriseProviderServer();

        // WHEN:
        currentTestWhenAndThenLog();

        // WHEN: Issuer: DO's UA generate & send the access_u_ticket to EP's CS
        // createSimulatedCommConnection(this.userAgentDO,this.cloudServerEP);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = Map.of(
                "device_id", targetDeviceId,
                "holder_id", this.cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "u_ticket_type", UTicket.TYPE_ACCESS_UTICKET,
                "task_scope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssuerIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId,generatedRequest);
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP, this.userAgentDO);

        // WHEN: Holder: EP's CS forward the access_u_ticket
        // createSimulatedCommConnection(this.cloudServerEP,this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.cloudServerEP.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // THEN: Succeed to allow EP's CS to limited access DO's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerEP.getSharedData().getResultMessage().contains("SUCCESS"));

        // THEN: Device: Still DO's IoTD
        assertEquals(this.iotDevice.getSharedData().getThisDevice().getOwnerPubKeyStr(),this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr());

        // THEN: Device: EP's CS can share a private session with DO's IoTD
        assertEquals(generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextCmd());
        assertEquals("DATA: " + generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextData());
        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.cloudServerEP.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));
    }

    // Skipped
    // @Test
    public void testSuccessWhenRebootDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a session on DO's IoTD
        Triple devices = enterpriseProviderServerAndHerSession();
        this.userAgentDO = (DeviceController) devices.getTripleFirst();
        this.cloudServerEP = (DeviceController) devices.getTripleSecond();
        this.iotDevice = (DeviceController) devices.getTripleThird();

        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.cloudServerEP.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));

        // HEN: Reboot DO's IoTD
        currentTestWhenAndThenLog();
        this.cloudServerEP.rebootDevice();
        this.iotDevice.rebootDevice();

        // THEN: The session is deleted (RAM-only)
        assertEquals("{}",CurrentSession.currentSessionToJsonStr(this.cloudServerEP.getSharedData().getCurrentSession()));
        assertEquals("{}",CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));
    }

    public void runAll() {
        setup();
        testSuccessWhenApplyAccessUTicketOnDevice();
        teardown();
    }
}

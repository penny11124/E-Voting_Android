package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.deviceOwnerAgentAndHerDevice;
import static ureka.framework.Conftest.deviceOwnerAgentAndHerSession;
import static ureka.framework.Conftest.waitSimulatedCommCompleted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @BeforeEach
    public void setup() {
        currentSetupLog();
        // SimpleStorage.deleteStorageInTest();
    }

    // RE-GIVEN: Reset the test environment
    @AfterEach
    public void teardown() {
        currentTeardownLog();
        // SimpleStorage.deleteStorageInTest();
    }

    @Test
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
                "deviceId", targetDeviceId,
                "holderId", this.userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_SELFACCESS_UTICKET,
                "taskScope", generatedTaskScope
        );
        this.userAgentDO.getFlowIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

        // WHEN: Holder: DO's UA forward the self_access_u_ticket
        createSimulatedCommConnection(this.userAgentDO,this.iotDevice);
        String generatedCommand = "HELLO-1";
        this.userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);
        waitSimulatedCommCompleted(this.userAgentDO,this.iotDevice);

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

    @Test
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
}

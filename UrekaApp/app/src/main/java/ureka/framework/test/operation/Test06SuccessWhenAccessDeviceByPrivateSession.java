package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.enterpriseProviderServerAndHerSession;
import static ureka.framework.Conftest.waitSimulatedCommCompleted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.resource.Triple;

public class Test06SuccessWhenAccessDeviceByPrivateSession {
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
    public void testSuccessWhenApplyUTokenOnDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a session on DO's IoTD
        Triple devices = enterpriseProviderServerAndHerSession();
        this.userAgentDO = (DeviceController) devices.getTripleFirst();
        this.cloudServerEP = (DeviceController) devices.getTripleSecond();
        this.iotDevice = (DeviceController) devices.getTripleThird();

        // WHEN:
        currentTestWhenAndThenLog();

        // WHEN: Holder: EP's CS forward the u_token
        // createSimulatedCommConnection(this.cloudServerEP,this.iotDevice);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedCommand = "HELLO-2";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId,generatedCommand,false);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // THEN: Succeed to allow EP's CS to limited access DO's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerEP.getSharedData().getResultMessage().contains("SUCCESS"));
        // THEN: Device: EP's CS can share a private session with DO's IoTD (but Forbidden command is not executed)
        assertEquals(generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextCmd());
        assertEquals("DATA: " + generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextData());
        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.cloudServerEP.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));

        // WHEN: Holder: EP's CS forward the u_token
        // createSimulatedCommConnection(this.cloudServerEP,this.iotDevice);
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedCommand2 = "HELLO-3";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId2,generatedCommand2,false);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // THEN: Succeed to allow EP's CS to limited access DO's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerEP.getSharedData().getResultMessage().contains("SUCCESS"));
        // THEN: Device: EP's CS can share a private session with DO's IoTD (but Forbidden command is not executed)
        assertEquals(generatedCommand2, this.iotDevice.getSharedData().getCurrentSession().getPlaintextCmd());
        assertEquals("DATA: " + generatedCommand2, this.iotDevice.getSharedData().getCurrentSession().getPlaintextData());
        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.cloudServerEP.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));

        // WHEN: Holder: EP's CS forward the u_token (ACCESS_END)
        // createSimulatedCommConnection(this.cloudServerEP,this.iotDevice);
        String targetDeviceId3 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        Integer originalDeviceOrder = this.iotDevice.getSharedData().getThisDevice().getTicketOrder();
        Integer originalAgentOrder = this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId3).getTicketOrder();
        String generatedCommand3 = "ACCESS_END";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId,generatedCommand3,true);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerEP.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        //  THEN: Succeed to end this session
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerEP.getSharedData().getResultMessage().contains("SUCCESS"));
        // THEN: Device: Update ticket order, EP's CS cannot access DO's IoTD anymore
        assertEquals(originalDeviceOrder + 1 ,this.iotDevice.getSharedData().getThisDevice().getTicketOrder());
        // THEN: Holder: Update ticket order, know that she cannot access DO's IoTD anymore
        assertEquals(originalAgentOrder + 1, this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId3).getTicketOrder());

        // WHEN: Holder: EP's CS return the access_end_r_ticket to DO's UA
        // createSimulatedCommConnection(this.userAgentDO,this.cloudServerEP);
        this.cloudServerEP.getFlowIssuerIssueUTicket().holderSendRTicketToIssuer(targetDeviceId3);
        this.userAgentDO.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.userAgentDO,this.cloudServerEP);

        // THEN: Issuer: DM's CS know that EP's CS has ended the private session with DO's IoTD (& ticket order++)
        assertTrue(this.userAgentDO.getSharedData().getResultMessage().contains("SUCCESS"));
        assertEquals(originalAgentOrder + 1 ,this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId3).getTicketOrder());
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

        // Given: Holder: EP's CS forward the u_token
        createSimulatedCommConnection(this.cloudServerEP,this.iotDevice);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedCommand = "HELLO-2";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId,generatedCommand,false);
        waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

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

        /*
            @pytest.mark.skip(reason="TODO: Access Agent")
            def test_success_when_apply_u_token_on_agent(self) -> None:
                current_test_given_log()
                current_test_when_and_then_log()

            @pytest.mark.skip(reason="TODO: Access Agent")
            def test_success_when_reboot_agent(self) -> None:
                current_test_given_log()
                current_test_when_and_then_log()
         */
    }

    public void runAll() {
        setup();
        testSuccessWhenApplyUTokenOnDevice();
        teardown();
    }
}


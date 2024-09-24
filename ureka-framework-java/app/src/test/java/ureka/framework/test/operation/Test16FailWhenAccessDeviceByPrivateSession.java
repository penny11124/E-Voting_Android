package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ureka.framework.Conftest.attackerServer;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.enterpriseProviderServerAndHerLimitedSession;
import static ureka.framework.Conftest.enterpriseProviderServerAndHerSession;
import static ureka.framework.Conftest.waitSimulatedCommCompleted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Triple;

public class Test16FailWhenAccessDeviceByPrivateSession {
    private DeviceController iotDevice;
    private DeviceController userAgentDO;
    private DeviceController cloudServerEP;
    private DeviceController cloudServerATK;
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

    // Threat: (E) Elevation of Privilege
    @Test
    public void testFailWhenApplyWrongTaskScope() {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a seesion on DO's IoTD
        Triple devices = enterpriseProviderServerAndHerLimitedSession();
        this.userAgentDO = (DeviceController) devices.getTripleFirst();
        this.cloudServerEP = (DeviceController) devices.getTripleSecond();
        this.iotDevice = (DeviceController) devices.getTripleThird();

        // WHEN: Intercept, Forge, & Apply
        currentTestWhenAndThenLog();

        // WHEN: Holder: EP's CS forward the UToken
        createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedCommand = "HELLO-2";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId, generatedCommand, false);
        waitSimulatedCommCompleted(this.cloudServerEP, this.iotDevice);

        // THEN: Succeed to allow EP's CS to limited access DO's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerEP.getSharedData().getResultMessage().contains("SUCCESS"));

        // THEN: Device: EP's CS can share a private session with DO's IoTD
        assertEquals(generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextCmd());
        assertEquals("DATA: " + generatedCommand, this.iotDevice.getSharedData().getCurrentSession().getPlaintextData());
        assertEquals(
                CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()),
                CurrentSession.currentSessionToJsonStr(this.cloudServerEP.getSharedData().getCurrentSession())
        );
        assertNotEquals("{}", CurrentSession.currentSessionToJsonStr(this.iotDevice.getSharedData().getCurrentSession()));

        // WHEN: Holder: EP's CS forward the UToken (with Forbidden command)
        createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String targetDeviceId2 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedCommand2 = "HELLO-3";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId2, generatedCommand, false);
        waitSimulatedCommCompleted(this.cloudServerEP, this.iotDevice);

        // THEN: Fail to allow EP's CS to limited access DO's IoTD
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("FAILURE"));
        // THEN: Device: Not execute the forbidden command
        assertNotEquals(generatedCommand2, this.iotDevice.getSharedData().getCurrentSession().getPlaintextCmd());
        assertNotEquals("DATA: " + generatedCommand2, this.iotDevice.getSharedData().getCurrentSession().getPlaintextData());

        // WHEN: Holder: EP's CS forward the u_token (ACCESS_END)
        createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String targetDeviceId3 = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        Integer originalDeviceOrder = this.iotDevice.getSharedData().getThisDevice().getTicketOrder();
        Integer originalAgentOrder = this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId3).getTicketOrder();
        String generatedCommand3 = "ACCESS_END";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId3, generatedCommand3, true);
        waitSimulatedCommCompleted(this.cloudServerEP,this.iotDevice);

        // THEN: Succeed to end this session
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("SUCCESS"));
        assertTrue(this.cloudServerEP.getSharedData().getResultMessage().contains("SUCCESS"));
        // THEN: Device: Update ticket order, EP's CS cannot access DO's IoTD anymore
        assertEquals(originalDeviceOrder + 1, this.iotDevice.getSharedData().getThisDevice().getTicketOrder());
        assertEquals(originalAgentOrder + 1, this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId3).getTicketOrder());

        // WHEN: Holder: EP's CS return the access_end_r_ticket to DO's UA
        createSimulatedCommConnection(this.userAgentDO, this.cloudServerEP);
        this.cloudServerEP.getFlowIssuerIssueUTicket().holderSendRTicketToIssuer(targetDeviceId3);
        waitSimulatedCommCompleted(this.userAgentDO, this.cloudServerEP);

        // THEN: Issuer: DM's CS know that EP's CS has ended the private session with DO's IoTD (& ticket order++)
        assertTrue(this.userAgentDO.getSharedData().getResultMessage().contains("SUCCESS"));
        assertEquals(originalAgentOrder + 1, this.userAgentDO.getSharedData().getDeviceTable().get(targetDeviceId3).getTicketOrder());
    }

    // Threat: (S) Spoofing, (T) Tampering, (E) Elevation of Privilege
    @Test
    public void testFailWhenForgeHMACAndApplyTheUToken() {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a seesion on DO's IoTD
        Triple devices = enterpriseProviderServerAndHerSession();
        this.userAgentDO = (DeviceController) devices.getTripleFirst();
        this.cloudServerEP = (DeviceController) devices.getTripleSecond();
        this.iotDevice = (DeviceController) devices.getTripleThird();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept, Forge, & Apply
        currentTestWhenAndThenLog();

        // WHEN: Interception (Know Latest State)
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String interceptedUTicketJson = this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceUTicketForOwner();

        // WHEN: Pretend Holder: Owner
        this.cloudServerATK.getSharedData().getDeviceTable().put(targetDeviceId, new OtherDevice(
                targetDeviceId, interceptedUTicketJson, null,2
        ));
        // WHEN: Pretend Holder: Session
        // WHEN: Forge Flow (holderApplyUTicket, incl. _executeCKRE + _executePS)
        this.cloudServerATK.getSharedData().getCurrentSession().setCurrentDeviceId(targetDeviceId);
        this.cloudServerATK.getSharedData().getCurrentSession().setCurrentSessionKeyStr("TkVqlRZLmNoBwaso0I04jwMFPEIT0kQu1hJZWK9S90E=");
        this.cloudServerATK.getSharedData().getCurrentSession().setIvCmd(this.cloudServerEP.getSharedData().getCurrentSession().getIvCmd());

        // WHEN: Apply Flow (holderSendCmd, incl. _executePS)
        createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        String generatedCommand = "HELLO-2";
        this.cloudServerATK.getFlowIssueUToken().holderSendCmd(targetDeviceId, generatedCommand, false);
        waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal session key, legal authentication (iv+hmac) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_IV_AND_HMAC"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    @Test
    public void testFailWhenInterceptAndPreemptToApplyTheUToken() {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a seesion on DO's IoTD
        Triple devices = enterpriseProviderServerAndHerSession();
        this.userAgentDO = (DeviceController) devices.getTripleFirst();
        this.cloudServerEP = (DeviceController) devices.getTripleSecond();
        this.iotDevice = (DeviceController) devices.getTripleThird();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Preempt
        currentTestWhenAndThenLog();

        // WHEN: Interception (TYPE_CMD_UTOKEN)
        // Indeed, because TYPE_CMD_UTOKEN has NOT on BC & has NOT been Sent in WPAN (only used once), the attacker cannot intercept & preempt it.
    }

    @Test
    public void testFailWhenInterceptAndReuseTheUToken() throws InterruptedException {
        currentTestGivenLog();

        // GIVEN: Initialized EP's CS open a seesion on DO's IoTD
        Triple devices = enterpriseProviderServerAndHerSession();
        this.userAgentDO = (DeviceController) devices.getTripleFirst();
        this.cloudServerEP = (DeviceController) devices.getTripleSecond();
        this.iotDevice = (DeviceController) devices.getTripleThird();

        // GIVEN: Initialized ATK's CS
        this.cloudServerATK = attackerServer();

        // WHEN: Intercept & Preempt
        currentTestWhenAndThenLog();

        // WHEN: Interception (TYPE_CMD_UTOKEN)
        // Here, after TYPE_CMD_UTOKEN has been Sent in WPAN (i.e., used), the attacker can intercept & use it.
        createSimulatedCommConnection(this.cloudServerEP, this.iotDevice);
        String targetDeviceId = this.iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedCommand = "HELLO-2";
        this.cloudServerEP.getFlowIssueUToken().holderSendCmd(targetDeviceId, generatedCommand, false);
        waitSimulatedCommCompleted(this.cloudServerEP, this.iotDevice);

        // WHEN: Forge Flow (_holderRecvUTicket, incl. _executeCKRE + _executePS)
        String interceptedUTicketJson = this.cloudServerEP.getSharedData().getDeviceTable().get(targetDeviceId).getDeviceUTicketForOwner();

        // WHEN: Pretend Holder: Other
        // WHEN: Pretend Holder: Session
        this.cloudServerATK.getFlowIssuerIssueUTicket()._holderRecvUTicket(UTicket.jsonStrToUTicket(interceptedUTicketJson));
        // WHEN: Interception (_deviceRecvCmd)
        String interceptedUTokenJson = this.iotDevice.getSharedData().getReceivedMessageJson();

        // WHEN: Reuse (holderSendCmd, incl. _executePS)
        createSimulatedCommConnection(this.cloudServerATK, this.iotDevice);
        this.cloudServerATK.getExecutor().changeState(ThisDevice.STATE_AGENT_WAIT_FOR_DATA);
        this.cloudServerATK.getMsgSender().sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE,UTicket.MESSAGE_TYPE,interceptedUTokenJson);
        waitSimulatedCommCompleted(this.cloudServerATK, this.iotDevice);

        // THEN: Because no legal session key & iv will be different in every use, legal authentication (iv+hmac) cannot be generated
        assertTrue(this.iotDevice.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_IV_AND_HMAC"));
        assertTrue(this.cloudServerATK.getSharedData().getResultMessage().contains("-> FAILURE: VERIFY_RESULT"));
    }

    // Threat: (R) Repudiation
    /*
        @pytest.mark.skip(reason="TODO: More complete Tx")
        def test_fail_when_double_issuing_or_double_spending(self) -> None:
                current_test_given_log()
     */

    // Threat: (I) Information Disclosure
    /*
        @pytest.mark.skip(reason="TODO: Not sure how to test")
        def test_fail_when_eavesdropping(self) -> None:
                current_test_given_log()
     */

    // Threat: (D) Denial of Service
    /*
        @pytest.mark.skip(reason="TODO: Not implement yet")
        def test_fail_when_flooding(self) -> None:
                current_test_given_log()

     */
}

package test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceManufacturerServerAndHerDevice;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;

public class Test12FailWhenInitializeDevice {
    private DeviceController cloudServerDM;
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

    // Threat: Reset
    // @Test
    public void testFailWhenReInitializeDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDM = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // WHEN:
        currentTestWhenAndThenLog();
        // WHEN: DM's CS re-apply the initializationUTicket to Initialized IoTD
        // createSimulatedCommConnection(this.cloudServerDM,this.iotDevice);
        String idForInitializationUTicket = "noId";
        Map<String, String> generatedRequest = Map.of(
                "deviceId", idForInitializationUTicket,
                "holderId", this.cloudServerDM.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET
        );
        this.cloudServerDM.getFlowIssuerIssueUTicket().issuerIssueUTicketToHerself(idForInitializationUTicket,generatedRequest);

        this.cloudServerDM.getFlowApplyUTicket().holderApplyUTicket(idForInitializationUTicket);
        this.iotDevice.getMsgReceiver()._recvXxxMessage();
        this.cloudServerDM.getMsgReceiver()._recvXxxMessage();
        // waitSimulatedCommCompleted(this.cloudServerDM,this.iotDevice);

        // THEN: Fail to re-initialize IoTD & R-Ticket will provide the reason
        for (OtherDevice value : this.cloudServerDM.getSharedData().getDeviceTable().values()) {
            if (!value.getDeviceId().equals("noId")) {
                String rTicketResult = RTicket.jsonStrToRTicket(value.getDeviceRTicketForOwner()).getResult();
                assertEquals("-> FAILURE: VERIFY_TICKET_ORDER: IOT_DEVICE ALREADY INITIALIZED", rTicketResult);
            }
        }
    }

    // Function: Wrong API
    // @Test
    public void testFailWhenInitializeDeviceByInitializingAgentOrServer() {
        currentTestGivenLog();

        currentTestWhenAndThenLog();

        // GIVEN: Uninitialized IoTD
        this.iotDevice = new DeviceController(ThisDevice.IOT_DEVICE,"iotDevice");
        assertEquals(0, this.iotDevice.getSharedData().getThisDevice().getTicketOrder());

        // WHEN:
        currentTestWhenAndThenLog();
        // WHEN: DM apply execute_one_time_intialize_agent_or_server() on Uninitialized IoTD
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            this.iotDevice.getExecutor()._executeOneTimeInitializeAgentOrServer();
        });

        // THEN: Fail to initialize IoTD
        assertEquals("-> FAILURE: ONLY USER-AGENT-OR-CLOUD-SERVER CAN DO THIS INITIALIZATION OPERATION", exception.getMessage());
    }

    public void runAll() {
        setup();
        testFailWhenReInitializeDevice();
        teardown();
        setup();
        testFailWhenInitializeDeviceByInitializingAgentOrServer();
        teardown();
    }
}

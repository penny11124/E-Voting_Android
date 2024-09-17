package ureka.framework.test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ureka.framework.Conftest.createSimulatedCommConnection;
import static ureka.framework.Conftest.currentSetupLog;
import static ureka.framework.Conftest.currentTeardownLog;
import static ureka.framework.Conftest.currentTestGivenLog;
import static ureka.framework.Conftest.currentTestWhenAndThenLog;
import static ureka.framework.Conftest.deviceManufacturerServerAndHerDevice;
import static ureka.framework.Conftest.enterpriseProviderServerAndHerSession;
import static ureka.framework.Conftest.waitSimulatedCommCompleted;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.Triple;

public class Test12FailWhenInitializeDevice {
    private DeviceController cloudServerDM;
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

    // Threat: Reset
    @Test
    public void testFailWhenReInitializeDevice() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS and DM's IoTD
        Pair devices = deviceManufacturerServerAndHerDevice();
        this.cloudServerDM = (DeviceController) devices.getPairFirst();
        this.iotDevice = (DeviceController) devices.getPairSecond();

        // WHEN:
        currentTestWhenAndThenLog();
        // WHEN: DM's CS re-apply the initialization_u_ticket to Initialized IoTD
        createSimulatedCommConnection(this.cloudServerDM,this.iotDevice);
        String idForInitializationUTicket = "noId";
        Map<String, String> generatedRequest = Map.of(
                "deviceId", idForInitializationUTicket,
                "holderId", this.cloudServerDM.getSharedData().getThisPerson().getPersonPubKeyStr(),
                "uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET
        );
        this.cloudServerDM.getFlowIssueUTicket().issuerIssueUTicketToHerself(idForInitializationUTicket,generatedRequest);
        this.cloudServerDM.getFlowApplyUTicket().holderApplyUTicket(idForInitializationUTicket);
        waitSimulatedCommCompleted(this.cloudServerDM,this.iotDevice);

        // THEN: Fail to re-initialize IoTD & R-Ticket will provide the reason
        for (OtherDevice value : this.cloudServerDM.getSharedData().getDeviceTable().values()) {
            if (!value.getDeviceId().equals("no_id")) {
                String rTicketResult = RTicket.jsonStrToRTicket(value.getDeviceRTicketForOwner()).getResult();
                assertEquals("-> FAILURE: VERIFY_TICKET_ORDER: IOT_DEVICE ALREADY INITIALIZED", rTicketResult);
            }
        }
    }

    // Function: Wrong API
    @Test
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
}

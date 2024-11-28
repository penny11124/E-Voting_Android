package test.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;
import static Conftest.currentTestGivenLog;
import static Conftest.currentTestWhenAndThenLog;
import static Conftest.deviceManufacturerServer;

import ureka.framework.logic.DeviceController;

public class Test11FailWhenInitializeAgentOrServer {
    private DeviceController cloudServerDM;
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
    public void testFailWhenReInitializeAgentOrServer() {
        currentTestGivenLog();

        // GIVEN: Initialized DM's CS
        this.cloudServerDM = deviceManufacturerServer();

        // WHEN: DM re-apply executeOneTimeInitializeAgentOrServer() on Initialized CS
        currentTestWhenAndThenLog();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            this.cloudServerDM.getExecutor()._executeOneTimeInitializeAgentOrServer();
        });

        // THEN: Fail to re-initialize CS
        assertEquals("-> FAILURE: VERIFY_TICKET_ORDER: USER-AGENT-OR-CLOUD-SERVER ALREADY INITIALIZED", exception.getMessage());

        // Function: Wrong API
        /*
            @pytest.mark.skip(reason="TODO: New way for _execute_one_time_initialize_agent_or_server()")
            def test_fail_when_initialize_agent_or_server_by_initializing_device(self) -> None:
                    current_test_given_log()
                    current_test_when_and_then_log()
         */
    }

    public void runAll() {
        setup();
        testFailWhenReInitializeAgentOrServer();
        teardown();
    }
}




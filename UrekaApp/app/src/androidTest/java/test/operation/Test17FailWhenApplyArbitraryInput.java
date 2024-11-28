package test.operation;

import static Conftest.currentSetupLog;
import static Conftest.currentTeardownLog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import ureka.framework.logic.DeviceController;

public class Test17FailWhenApplyArbitraryInput {
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

    // Function: Weird UI Input
    /*
        @pytest.mark.skip(reason="TODO: Weird UI Input: Better to be tested")
            def test_fail_when_apply_wrong_format_ticket_request(self) -> None:
                current_test_given_log()
     */

    /*
        @pytest.mark.skip(reason="TODO: Weird UI Input: Better to be tested")
            def test_fail_when_apply_wrong_format_token_request(self) -> None:
                current_test_given_log()
     */

    // Function: Weird COMM Input
    /*
        @pytest.mark.skip(reason="TODO: Weird COMM Input: Better to be tested")
            def test_fail_when_apply_wrong_format_u_ticket(self) -> None:
                current_test_given_log()

        @pytest.mark.skip(reason="TODO: Weird COMM Input: Better to be tested")
            def test_fail_when_apply_wrong_format_r_ticket(self) -> None:
                current_test_given_log()

        @pytest.mark.skip(reason="TODO: Weird COMM Input: Better to be tested")
            def test_fail_when_apply_wrong_format_u_token(self) -> None:
                current_test_given_log()

        @pytest.mark.skip(reason="TODO: Weird COMM Input: Better to be tested")
            def test_fail_when_apply_wrong_format_r_token(self) -> None:
                current_test_given_log()
     */
}

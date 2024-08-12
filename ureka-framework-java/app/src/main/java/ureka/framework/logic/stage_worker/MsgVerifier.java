package ureka.framework.logic.stage_worker;

import ureka.framework.model.SharedData;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

public class MsgVerifier {
    private SharedData sharedData;
    private MeasureHelper measureHelper;

    public MsgVerifier(SharedData sharedData, MeasureHelper measureHelper) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
    }

    public SharedData getSharedData() {
        return sharedData;
    }

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public MeasureHelper getMeasureHelper() {
        return measureHelper;
    }

    public void setMeasureHelper(MeasureHelper measureHelper) {
        this.measureHelper = measureHelper;
    }

    //////////////////////////////////////////////////////
    // [STAGE: (V)] Verify Message & Execute
    //   (VR): classify_message_is_defined_type
    //   (VL): has_u_ticket_in_device_table
    //   (VUT): verify_u_ticket_can_execute
    //   (VRT): verify_u_ticket_has_executed_through_r_ticket
    //   (VTK): verify_token_through_hmac (when _execute_decrypt_ciphertext)
    //   (VTS): verify_cmd_is_in_task_scope
    //////////////////////////////////////////////////////
    public Object _classifyMessageIsDefinedType(String arbitraryJson) {
        return SimpleMeasurer.measureWorkerFunc(this::__classifyMessageIsDefinedType, arbitraryJson);
    }
    private Object __classifyMessageIsDefinedType(String arbitraryJson) {
        SimpleLogger.simpleLog("info", "+ " + this.sharedData.getThisDevice().getDeviceName() + " is classifying message type...");

        // [STAGE: (VR: UTicket)]
        return null;
    }
//     # [STAGE: (VR: UTicket)]
//
//    message_verifier = MessageVerifier(this_device=None)
//        try:
//    message_in = message_verifier.verify_json_schema(arbitrary_json)
//    message_in = message_verifier.verify_message_operation(message_in)
//    message_in = message_verifier.verify_message_type(message_in)
//    message_in = message_verifier.verify_message_str(message_in)
//    except RuntimeError as error:  # pragma: no cover -> Weird Message
//    simple_log("error", f"{error}")
//    raise RuntimeError(error)
//
//        if message_in.message_type == u_ticket.MESSAGE_TYPE:
//        return self._classify_u_ticket_is_defined_type(message_in.message_str)
//    elif message_in.message_type == r_ticket.MESSAGE_TYPE:
//        return self._classify_r_ticket_is_defined_type(message_in.message_str)
}

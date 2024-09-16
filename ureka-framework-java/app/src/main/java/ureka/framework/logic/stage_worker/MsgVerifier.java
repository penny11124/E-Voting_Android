package ureka.framework.logic.stage_worker;

import java.util.Map;
import java.util.Objects;

import ureka.framework.model.SharedData;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;
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
        Message messageIn;
        try {
            messageIn = MsgVerifierMessage.verifyJsonSchema(arbitraryJson);
            MsgVerifierMessage.verifyMessageOperation(messageIn);
            MsgVerifierMessage.verifyMessageType(messageIn);
            MsgVerifierMessage.verifyMessageStr(messageIn);
        } catch (RuntimeException e) {
            // pragma: no cover -> Weird Message
            SimpleLogger.simpleLog("error", e.getMessage());
            throw e;
        }

        if (Objects.equals(messageIn.getMessageType(), UTicket.MESSAGE_TYPE)) {
            return this._classifyUTicketIsDefinedType(messageIn.getMessageStr());
        } else {
            return this._classifyRTicketIsDefinedType(messageIn.getMessageStr());
        }
    }

    public UTicket _classifyUTicketIsDefinedType(String arbitraryJson) {
        return SimpleMeasurer.measureWorkerFunc(this::__classifyUTicketIsDefinedType, arbitraryJson);
    }
    private UTicket __classifyUTicketIsDefinedType(String arbitraryJson) {
        SimpleLogger.simpleLog("info", this.sharedData.getThisDevice().getDeviceName() + " is classifying ticket type...");

        try {
            // [STAGE: (VR: UTicket)]
            UTicket uTicketIn = MsgVerifierUTicket.verifyJsonSchema(arbitraryJson);
            MsgVerifierUTicket.verifyProtocolVersion(uTicketIn);
            MsgVerifierUTicket.verifyUTicketId(uTicketIn);
            MsgVerifierUTicket.verifyUTicketType(uTicketIn);
            MsgVerifierUTicket.hasDeviceId(uTicketIn);
            return uTicketIn;
        } catch (Exception e) {
            // pragma: no cover -> Weird Message
            throw new RuntimeException(e);
        }
    }

    public RTicket _classifyRTicketIsDefinedType(String arbitraryJson) {
        return SimpleMeasurer.measureWorkerFunc(this::__classifyRTicketIsDefinedType, arbitraryJson);
    }
    private RTicket __classifyRTicketIsDefinedType(String arbitraryJson) {
        SimpleLogger.simpleLog("info", this.sharedData.getThisDevice().getDeviceName() + " is classifying ticket type...");

        try {
            // [STAGE: (VR: RTicket)]
            RTicket rTicketIn = MsgVerifierRTicket.verifyJsonSchema(arbitraryJson);
            MsgVerifierRTicket.verifyProtocolVersion(rTicketIn);
            MsgVerifierRTicket.verifyRTicketId(rTicketIn);
            MsgVerifierRTicket.verifyRTicketType(rTicketIn);
            MsgVerifierRTicket.hasDeviceId(rTicketIn);
            return rTicketIn;
        } catch (RuntimeException e) {
            // pragma: no cover -> Weird Message
            throw new RuntimeException(e);
        }
    }

    public void verifyUTicketCanExecute(UTicket uTicketIn) {
        SimpleMeasurer.measureWorkerFunc(this::_verifyUTicketCanExecute, uTicketIn);
    }
    private void _verifyUTicketCanExecute(UTicket uTicketIn) {
        SimpleLogger.simpleLog("info", this.sharedData.getThisDevice().getDeviceName() + " is verifying u_ticket...");

        try {
            MsgVerifierUTicket uTicketVerifier = new MsgVerifierUTicket(this.sharedData.getThisDevice());

            uTicketVerifier.verifyDeviceId(uTicketIn);
            uTicketVerifier.verifyTicketOrder(uTicketIn);
            MsgVerifierUTicket.verifyTaskScope(uTicketIn);
            MsgVerifierUTicket.verifyPS(uTicketIn);
            uTicketVerifier.verifyIssuerSignature(uTicketIn);
        } catch (Exception e) {
            // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here");
        }
    }

    public void verifyUTicketHasExecutedThroughRTicket(RTicket rTicketIn, UTicket auditStartTicket, UTicket auditEndTicket) {
        SimpleMeasurer.measureWorkerFunc(this::_verifyUTicketHasExecutedThroughRTicket, rTicketIn, auditStartTicket, auditEndTicket);
    }
    private void _verifyUTicketHasExecutedThroughRTicket(RTicket rTicketIn, UTicket auditStartTicket, UTicket auditEndTicket) {
        SimpleLogger.simpleLog("info", this.sharedData.getThisDevice().getDeviceName() + " is verifying r_ticket");

        try {
            MsgVerifierRTicket rTicketVerifier = new MsgVerifierRTicket(this.sharedData.getThisDevice(), this.sharedData.getDeviceTable(),
                auditStartTicket, this.sharedData.getCurrentSession());

            rTicketVerifier.verifyDeviceId(rTicketIn);
            MsgVerifierRTicket.verifyResult(rTicketIn);
            rTicketVerifier.verifyTicketOrder(rTicketIn);
            rTicketVerifier.verifyAuditStart(rTicketIn);
            MsgVerifierRTicket.verifyAuditEnd(rTicketIn);
            MsgVerifierRTicket.verifyCRKE(rTicketIn);
            MsgVerifierRTicket.verifyPs(rTicketIn);
            rTicketVerifier.verifyDeviceSignature(rTicketIn);
        } catch (Exception e) {
            // # pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here");
        }
    }

    public void verifyCmdIsInTaskScope(String cmd) {
        String success_msg = "-> SUCCESS: VERIFY_CMD_IN_TASK_SCOPE";
        String failure_msg = "-> FAILURE: VERIFY_CMD_IN_TASK_SCOPE";

        Map<String, String> taskScope = SerializationUtil.jsonStrToDict(this.sharedData.getCurrentSession().getCurrentTaskScope());
        // SimpleLogger.simpleLog("debug", "currentTaskScope: " + taskScope);

        if (Objects.equals(taskScope.get("ALL"), "allow")) {
            SimpleLogger.simpleLog("info", success_msg);
        } else if (Objects.equals(cmd, "HELLO-1") && Objects.equals(taskScope.get("SAY-HELLO-1"), "allow")) {
            SimpleLogger.simpleLog("info", success_msg);
        } else if (Objects.equals(cmd, "HELLO-2") && Objects.equals(taskScope.get("SAY-HELLO-2"), "allow")) {
            SimpleLogger.simpleLog("info", success_msg);
        } else if (Objects.equals(cmd, "HELLO-3") && Objects.equals(taskScope.get("SAY-HELLO-3"), "allow")) {
            // pragma: no cover -> FAILURE: (VTS)
            SimpleLogger.simpleLog("info", success_msg);
        } else {
            failure_msg = failure_msg + ": Undefined or Forbidden Command: " + cmd;
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }
}

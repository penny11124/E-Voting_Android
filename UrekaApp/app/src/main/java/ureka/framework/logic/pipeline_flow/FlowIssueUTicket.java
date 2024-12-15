package ureka.framework.logic.pipeline_flow;

import com.example.urekaapp.AdminAgentActivity;
import com.example.urekaapp.VoterAgentActivity;

import java.security.KeyException;
import java.util.Map;

import ureka.framework.Environment;
import ureka.framework.logic.stage_worker.Executor;
import ureka.framework.logic.stage_worker.GeneratedMsgStorer;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgGenerator;
import ureka.framework.logic.stage_worker.MsgSender;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.logic.stage_worker.ReceivedMsgStorer;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleLogger;

public class FlowIssueUTicket {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private ReceivedMsgStorer receivedMsgStorer;
    private MsgVerifier msgVerifier;
    private Executor executor;
    private MsgGenerator msgGenerator;
    private GeneratedMsgStorer generatedMsgStorer;
    private MsgSender msgSender;

    public FlowIssueUTicket(SharedData sharedData, MeasureHelper measureHelper,
                            ReceivedMsgStorer receivedMsgStorer, MsgVerifier msgVerifier,
                            Executor executor, MsgGenerator msgGenerator,
                            GeneratedMsgStorer generatedMsgStorer, MsgSender msgSender) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
        this.receivedMsgStorer = receivedMsgStorer;
        this.msgVerifier = msgVerifier;
        this.executor = executor;
        this.msgGenerator = msgGenerator;
        this.generatedMsgStorer = generatedMsgStorer;
        this.msgSender = msgSender;
    }

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public SharedData getSharedData() {
        return sharedData;
    }

    public void setMeasureHelper(MeasureHelper measureHelper) {
        this.measureHelper = measureHelper;
    }

    public MeasureHelper getMeasureHelper() {
        return measureHelper;
    }

    public void setReceivedMsgStorer(ReceivedMsgStorer receivedMsgStorer) {
        this.receivedMsgStorer = receivedMsgStorer;
    }

    public ReceivedMsgStorer getReceivedMsgStorer() {
        return receivedMsgStorer;
    }

    public void setMsgVerifier(MsgVerifier msgVerifier) {
        this.msgVerifier = msgVerifier;
    }

    public MsgVerifier getMsgVerifier() {
        return msgVerifier;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setMsgGenerator(MsgGenerator msgGenerator) {
        this.msgGenerator = msgGenerator;
    }

    public MsgGenerator getMsgGenerator() {
        return msgGenerator;
    }

    public void setGeneratedMsgStorer(GeneratedMsgStorer generatedMsgStorer) {
        this.generatedMsgStorer = generatedMsgStorer;
    }

    public GeneratedMsgStorer getGeneratedMsgStorer() {
        return generatedMsgStorer;
    }

    public void setMsgSender(MsgSender msgSender) {
        this.msgSender = msgSender;
    }

    public MsgSender getMsgSender() {
        return msgSender;
    }
    /* [PIPELINE FLOW]
        CST: issuer_issue_u_ticket_to_herself()
        TODO: REQ: _issuer_recv_request() <- holder_send_request_to_issuer()
        CST: issuer_issue_u_ticket_to_holder() -> _holder_recv_u_ticket()
        RTN: _issuer_recv_r_ticket() <- holder_send_r_ticket_to_issuer()

        TODO: More complete Tx (with DID, etc.))
        TODO: Rollback (e.g., delete the temporary stored state and stored message) if fail
             execution only change state after success, but need pay attention to (SR)
        TODO: QoS Testing - Re-transmission, & Timeout
            (e.g., if RTicket is not returned, holder can request backup RTicket)
            (e.g., if CR or PS is Timeout, device can revert to the WAIT_FOR_UT state)
    */

    public void issuerIssueUTicketToHerself(String device_id, Map<String, String> arbitraryDict) {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();
        try {
            // [STAGE: (VL)]
            SimpleLogger.simpleLog("info", "deviceId = " + device_id + ", containing = " + (this.sharedData.getDeviceTable().containsKey(device_id) || "no_id".equals(device_id)));
            if (this.sharedData.getDeviceTable().containsKey(device_id) || "no_id".equals(device_id)) {
                // [STAGE: (G)]
                String generatedUTicketJson = this.msgGenerator.generateXxxUTicket(arbitraryDict);

                // [STAGE: (SG)]
                this.generatedMsgStorer.storeGeneratedXxxUTicket(generatedUTicketJson);

                // [STAGE: (O)]
                this.executor.executeUpdateTicketOrder("holderGenerateOrReceiveUTicket",UTicket.jsonStrToUTicket(generatedUTicketJson));
            }
        } catch (RuntimeException e) { // pragma: no cover -> Weird Ticket-Request (ValidationError)
            SimpleLogger.simpleLog("error", "FAILURE: (VUREQ)");
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        } finally {
            // End Process Measurement
            this.measureHelper.measureRecvCliPerfTime("issuerIssueUTicketToHerself");

            // Start Comm Measurement
            if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }
        }
    }

    public void issuerIssueUTicketToHolder(Map<String, String> arbitraryDict) {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();
        try {
            // [STAGE: (VL)]
            if (this.sharedData.getDeviceTable().containsKey(AdminAgentActivity.connectedDeviceId)) {
                // [STAGE: (G)]
                arbitraryDict.put("device_id", AdminAgentActivity.connectedDeviceId);
                String generatedUTicketJson = this.msgGenerator.generateXxxUTicket(arbitraryDict);
//                UTicket generatedUTicket = UTicket.jsonStrToUTicket(generatedUTicketJson);
//                generatedUTicket.setDeviceId(AdminAgentActivity.connectedDeviceId);
//                generatedUTicketJson = UTicket.uTicketToJsonStr(generatedUTicket);

                // [STAGE: (SG)]
                this.generatedMsgStorer.storeGeneratedXxxUTicket(generatedUTicketJson);

                // End Process Measurement
                this.measureHelper.measureRecvCliPerfTime("issuerIssueUTicketToHolder");

                // Start Comm Measurement
                if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                    this.measureHelper.measureCommPerfStart();
                }

                // [STAGE: (S)]
                this.msgSender.sendXxxMessageByNearby(
                        Message.MESSAGE_RECV_AND_STORE,
                        UTicket.MESSAGE_TYPE,
                        generatedUTicketJson
                );
            } else {
                SimpleLogger.simpleLog("info", "FlowIssueUTicket.issuerIssueUTicketToHolder: Device not in device table");
            }
        } catch (RuntimeException e) { // pragma: no cover -> Weird Ticket-Request (ValidationError)
            SimpleLogger.simpleLog("error", "FAILURE: (VUREQ)");
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
        // Manually Finish Simulated Comm
        SimpleLogger.simpleLog("debug", this.sharedData.getThisDevice().getDeviceName() + " manually finish UT-UT~~ (issuer)");
        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.completeSimulatedComm();
        }
    }

    public void _holderRecvUTicket(UTicket receivedUTicket) {
        try {
            /*
             [STAGE: (R)(VR)]
             But the actual device id & ticket order in device is still unknown
             -> TODO: test_fail_when_double_issuing_or_double_spending
            */

            // [STAGE: (SR)]
            VoterAgentActivity.connectedDeviceId = receivedUTicket.getDeviceId();
            this.receivedMsgStorer.storeReceivedXxxUTicket(receivedUTicket);

            // [STAGE: (O)]
            this.executor.executeUpdateTicketOrder("holderGenerateOrReceiveUTicket", receivedUTicket);
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        } finally {
            // [STAGE: (G)(S)]
            // Can optionally _generateXxxRTicket & _sendXxxMessage

            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("_holderRecvUTicket");
        }
        // Manually Finish Simulated Comm
        SimpleLogger.simpleLog("debug", sharedData.getThisDevice().getDeviceName() + " manually finish UT-UT~~ (holder)");
        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            msgSender.completeSimulatedComm();
        }
    }

    public void holderSendRTicketToIssuer(String device_id) {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();
        try {
            // [STAGE: (VL)(L)]
            String storedRTicketJson = this.sharedData.getDeviceTable().get(device_id).getDeviceRTicketForOwner();

            // End Process Measurement
            this.measureHelper.measureRecvCliPerfTime("holderSendRTicketToIssuer");

            // Start Comm Measurement
            if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(
                    Message.MESSAGE_RECV_AND_STORE,
                    RTicket.MESSAGE_TYPE,
                    storedRTicketJson
            );
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
        // Manually Finish Simulated Comm
        SimpleLogger.simpleLog("debug", this.sharedData.getThisDevice().getDeviceName() + " manually finish RT-RT~~ (holder)");
        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.completeSimulatedComm();
        }
    }

    public void _issuerRecvRTicket(RTicket receivedRTicket) {
        try {
            // [STAGE: (R)(VR)]
            // [STAGE: (SR)]
            this.receivedMsgStorer.storeReceivedXxxRTicket(receivedRTicket);
            String storedUTicketJson;
            if(receivedRTicket.getRTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET) ||
                    receivedRTicket.getRTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET) ||
                    receivedRTicket.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // Query Corresponding UTicket(s)
                //    Notice that even Initialization UTicket is copied in the deviceTable["device_id"]
                // [STAGE: (VL)(L)]
                if(receivedRTicket.getRTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)) {
                    storedUTicketJson = this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId()).getDeviceOwnershipUTicketForOthers();
                } else if (receivedRTicket.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                    storedUTicketJson = sharedData.getDeviceTable().get(receivedRTicket.getDeviceId()).getDeviceAccessUTicketForOthers();
                } else {
                    throw new RuntimeException("Shouldn't Reach Here");
                }

                SimpleLogger.simpleLog("debug", "Corresponding UTicket: " + storedUTicketJson);

                // [STAGE: (VR)]
                UTicket storedUTicket = this.msgVerifier._classifyUTicketIsDefinedType(storedUTicketJson);

                // [STAGE: (VRT)]
                this.msgVerifier.verifyUTicketHasExecutedThroughRTicket(receivedRTicket, storedUTicket, null);

                // [STAGE: (E)(O)]
                this.executor.executeXxxRTicket(receivedRTicket, "issuer");
                this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_HAS_EXECUTED");

                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT);
            } else { // pragma: no cover -> TODO: Revocation UTicket
                throw new RuntimeException("Not implemented yet");
            }
        } catch (RuntimeException e) { // pragma: no cover -> FAILURE: (VR)(VRT)
            // TODO: (VRT) test_fail_when_double_issuing_or_double_spending
            this.sharedData.setResultMessage("FAILURE: (VR)(VRT)");
            throw new RuntimeException("FAILURE: (VR)(VRT)", e);
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        } finally {
            SimpleLogger.simpleLog("debug", "result_message = " + this.sharedData.getResultMessage());
        }

        SimpleLogger.simpleLog("debug", this.sharedData.getThisDevice().getDeviceName() + " manually finish RT-RT~~ (issuer)");
        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.completeSimulatedComm();
        }
    }
}
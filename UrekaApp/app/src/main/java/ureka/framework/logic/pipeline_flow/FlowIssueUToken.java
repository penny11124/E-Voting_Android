package ureka.framework.logic.pipeline_flow;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import ureka.framework.Environment;
import ureka.framework.logic.stage_worker.Executor;
import ureka.framework.logic.stage_worker.GeneratedMsgStorer;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgGenerator;
import ureka.framework.logic.stage_worker.MsgSender;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.logic.stage_worker.ReceivedMsgStorer;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleLogger;

public class FlowIssueUToken {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private ReceivedMsgStorer receivedMsgStorer;
    private MsgVerifier msgVerifier;
    private Executor executor;
    private MsgGenerator msgGenerator;
    private GeneratedMsgStorer generatedMsgStorer;
    private MsgSender msgSender;
    private FlowApplyUTicket flowApplyUTicket;

    public FlowIssueUToken(
            SharedData shareData,
            MeasureHelper measureHelper,
            ReceivedMsgStorer receivedMsgStorer,
            MsgVerifier msgVerifier,
            Executor executor,
            MsgGenerator msgGenerator,
            GeneratedMsgStorer generatedMsgStorer,
            MsgSender msgSender,
            FlowApplyUTicket flowApplyUTicket
    ) {
        this.sharedData = shareData;
        this.measureHelper = measureHelper;
        this.receivedMsgStorer = receivedMsgStorer;
        this.msgVerifier = msgVerifier;
        this.executor = executor;
        this.msgGenerator = msgGenerator;
        this.generatedMsgStorer = generatedMsgStorer;
        this.msgSender = msgSender;
        this.flowApplyUTicket = flowApplyUTicket;
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

    public void setFlowApplyUTicket(FlowApplyUTicket flowApplyUTicket) {
        this.flowApplyUTicket = flowApplyUTicket;
    }

    public FlowApplyUTicket getFlowApplyUTicket() {
        return flowApplyUTicket;
    }

    /* [PIPELINE FLOW]

     APY (PS):
           holderSendCmd()   -> _deviceRecvCmd()
           _holderRecvData() <- _deviceSendData()
                               ...
                           ..repeated..
                               ...
           holderSendCmd(ACCESS_END) -> _deviceRecvCmd(ACCESS_END)
           _holderRecvRTicket() <- _deviceSendRTicket()
     */
    public void holderSendCmd(String device_id, String cmd, boolean accessEnd) {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();

        try {
            // Stage: (VL)
            if (this.sharedData.getDeviceTable().containsKey(device_id)) {
                // Stage: (E)
                this.executor.executePs("sendUToken", null, cmd, null);

                String u_ticket_type;
                if (!accessEnd) {
                    // Stage: (C)
                    this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_DATA);
                    // Stage: (G)
                    u_ticket_type = UTicket.TYPE_CMD_UTOKEN;
                } else {
                    // Stage: (C)
                    this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_RT);
                    // Stage: (G)
                    u_ticket_type = UTicket.TYPE_ACCESS_END_UTOKEN;
                }

                // Stage: (G)
                Map<String, String> generatedRequest = new HashMap<>();
                generatedRequest.put("device_id", this.sharedData.getCurrentSession().getCurrentDeviceId());
                generatedRequest.put("u_ticket_type", u_ticket_type);
                generatedRequest.put("associated_plaintext_cmd", this.sharedData.getCurrentSession().getAssociatedPlaintextCmd());
                generatedRequest.put("ciphertext_cmd", this.sharedData.getCurrentSession().getCiphertextCmd());
                generatedRequest.put("gcm_authentication_tag_cmd", this.sharedData.getCurrentSession().getGcmAuthenticationTagCmd());
                generatedRequest.put("iv_data", this.sharedData.getCurrentSession().getIvData());

                String generatedUTicketJson = this.msgGenerator.generateXxxUTicket(generatedRequest);

                // End Process Measurement
                if (!accessEnd) {
                    this.measureHelper.measureRecvCliPerfTime("holderSendCmd");
                } else {
                    this.measureHelper.measureRecvCliPerfTime("holderSendAccessEndCmd");
                }

                // Start Comm Measurement
                if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                    this.measureHelper.measureCommPerfStart();
                }

                // Stage: (S)
                this.msgSender.sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE, UTicket.MESSAGE_TYPE, generatedUTicketJson);

            } else {
                throw new NoSuchElementException("Device ID not found in device table: " + device_id);
            }

        } catch (NoSuchElementException e) {
            String error = "FAILURE: (VL)";
            this.sharedData.setResultMessage(error);
            throw new RuntimeException(error, e);
        } catch (RuntimeException e) {
            String failureMsg = "FAILURE: (VTKREQ)";
            SimpleLogger.simpleLog("error", failureMsg);
            this.sharedData.setResultMessage(failureMsg);
        } catch (Exception e) {
            throw new RuntimeException("Shouldn't Reach Here");
        }
    }

    public void _deviceRecvCmd(UTicket receivedUToken) {
        try {
            // [STAGE: (R)(VR)]

            // [STAGE: (VUT)]
            this.msgVerifier.verifyUTicketCanExecute(receivedUToken);

            // [STAGE: (VTK)(VTS)]
            // [STAGE: (E)]
            this.executor.executeXxxUTicket(receivedUToken);
            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_CAN_EXECUTE");

            if(receivedUToken.getUTicketType().equals(UTicket.TYPE_CMD_UTOKEN)) {
                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_CMD);
            } else if (receivedUToken.getUTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_UT);
            } else { // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("Shouldn't Reach Here");
            }
        } catch (RuntimeException error) {
            this.sharedData.setResultMessage(error.getMessage());
            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_CMD);
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here");
        } finally {
            // PS
            if (receivedUToken.getUTicketType().equals(UTicket.TYPE_CMD_UTOKEN)) {
                // [STAGE: (G)(S)]
                this._deviceSendData(this.sharedData.getResultMessage());
            } else if (receivedUToken.getUTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // [STAGE: (G)(S)]
                this.flowApplyUTicket._deviceSendRTicket(
                        receivedUToken.getUTicketType(),
                        receivedUToken.getUTicketId(),
                        this.sharedData.getResultMessage()
                );
            } else {
                throw new RuntimeException("Shouldn't Reach Here");
            }
        }
    }

    private void _deviceSendData(String resultMessage) {
        try {
            Map<String, String> generatedRequest = new HashMap<>();
            // // [STAGE: (G)]
            if (resultMessage.contains("SUCCESS")) {
                generatedRequest.put("r_ticket_type", RTicket.TYPE_DATA_RTOKEN);
                generatedRequest.put("device_id", this.sharedData.getThisDevice().getDevicePubKeyStr());
                generatedRequest.put("result", resultMessage);
                generatedRequest.put("audit_start", this.sharedData.getCurrentSession().getCurrentUTicketId());
                generatedRequest.put("associated_plaintext_data", this.sharedData.getCurrentSession().getAssociatedPlaintextData());
                generatedRequest.put("ciphertextData", this.sharedData.getCurrentSession().getCiphertextData());
                generatedRequest.put("gcmAuthenticationTagData", this.sharedData.getCurrentSession().getGcmAuthenticationTagData());
                generatedRequest.put("ivCmd", this.sharedData.getCurrentSession().getIvCmd());
            } else {
                generatedRequest.put("r_ticket_type", RTicket.TYPE_DATA_RTOKEN);
                generatedRequest.put("device_id", this.sharedData.getThisDevice().getDevicePubKeyStr());
                generatedRequest.put("result", resultMessage);
            }

            String generatedRTicketJson = this.msgGenerator.generateXxxRTicket(generatedRequest);

            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("deviceRecvCmdAndSendData");

            // Start Comm Measurement
            if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(
                    Message.MESSAGE_VERIFY_AND_EXECUTE,
                    RTicket.MESSAGE_TYPE,
                    generatedRTicketJson
            );
        } catch (Exception e) {
            // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }

        // Manually Finish Simulated Comm
        SimpleLogger.simpleLog("debug","+ " + this.sharedData.getThisDevice().getDeviceName() + " manually finish PS~~ (device)");

        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            msgSender.completeSimulatedComm();
        }
    }

    public void _holderRecvData(RTicket receivedRTicket) {
        try {
            // [STAGE: (R)(VR)]

            // Query Corresponding UTicket
            // [STAGE: (VL)(L)]
            String device_id = receivedRTicket.getDeviceId();
            String storedUTicketJson = this.sharedData.getDeviceTable().get(device_id).getDeviceUTicketForOwner();

            SimpleLogger.simpleLog("debug","Corresponding UTicket: " + storedUTicketJson);

            // [STAGE: (VR)]
            UTicket storedUTicket = this.msgVerifier._classifyUTicketIsDefinedType(storedUTicketJson);

            // [STAGE: (VRT)]
            this.msgVerifier.verifyUTicketHasExecutedThroughRTicket(receivedRTicket, storedUTicket, null);

            // [STAGE: (VTK)]
            // [STAGE: (E)]
            this.executor.executeXxxRTicket(receivedRTicket, "holderOrDevice");
            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_HAS_EXECUTED");

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_CMD);

            SimpleLogger.simpleLog("debug","resultMessage = " + this.sharedData.getResultMessage());

        } catch (NoSuchElementException e) {  // pragma: no cover -> FAILURE: (VL)
            String error = "FAILURE: (VL)";
            this.sharedData.setResultMessage(error);
            throw new RuntimeException(error, e);

        } catch (RuntimeException e) { // FAILURE: (VR)(VRT)(VTK)
            this.sharedData.setResultMessage(e.getMessage());

        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        } finally {
            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("holderRecvData");

            // Start Comm Measurement
            if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }
        }

        // Manually Finish Simulated/Bluetooth Comm
        SimpleLogger.simpleLog("debug","+ " + this.sharedData.getThisDevice().getDeviceName() + " manually finish PS~~ (holder)");

        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.completeSimulatedComm();
//        } else if (Environment.COMMUNICATION_CHANNEL.equals("BLUETOOTH")) {
//            this.msgSender.completeBluetoothComm();
        }
    }
}
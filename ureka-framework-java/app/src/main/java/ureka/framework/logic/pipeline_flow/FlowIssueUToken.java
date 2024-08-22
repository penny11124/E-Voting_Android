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

    /* [PIPELINE FLOW]

     APY (PS):
           holderSendCmd()   -> _deviceRecvCmd()
           _holderRecvData() <- _deviceSendData()
                               ...
                           ..repeated..
                               ...
           holderSendCmd(access_end) -> _deviceRecvCmd(access_end)
           _holderRecvRTicket() <- _deviceSendRTicket()
     */
    public void holderSendCmd(String deviceId, String cmd, boolean accessEnd) {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();

        try {
            // Stage: (VL)
            if (this.sharedData.getDeviceTable().containsKey(deviceId)) {
                // Stage: (E)
                this.executor.executePs("sendUToken", null, cmd, null);

                String uTicketType;
                if (!accessEnd) {
                    // Stage: (C)
                    this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_DATA);
                    // Stage: (G)
                    uTicketType = UTicket.TYPE_CMD_UTOKEN;
                } else {
                    // Stage: (C)
                    this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_RT);
                    // Stage: (G)
                    uTicketType = UTicket.TYPE_ACCESS_END_UTOKEN;
                }

                // Stage: (G)
                Map<String, String> generatedRequest = new HashMap<>();
                generatedRequest.put("deviceId", this.sharedData.getCurrentSession().getCurrentDeviceId());
                generatedRequest.put("uTicketType", uTicketType);
                generatedRequest.put("associatedPlaintextCmd", this.sharedData.getCurrentSession().getAssociatedPlaintextCmd());
                generatedRequest.put("ciphertextCmd", this.sharedData.getCurrentSession().getCiphertextCmd());
                generatedRequest.put("gcmAuthenticationTagCmd", this.sharedData.getCurrentSession().getGcmAuthenticationTagCmd());
                generatedRequest.put("ivData", this.sharedData.getCurrentSession().getIvData());

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
                throw new NoSuchElementException("Device ID not found in device table: " + deviceId);
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
            throw new RuntimeException("Shouldn't Reach Here", e);
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
            throw new RuntimeException("Shouldn't Reach Here", e);
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
                generatedRequest.put("rTicketType", RTicket.TYPE_DATA_RTOKEN);
                generatedRequest.put("deviceId", this.sharedData.getThisDevice().getDevicePubKeyStr());
                generatedRequest.put("result", resultMessage);
                generatedRequest.put("auditStart", this.sharedData.getCurrentSession().getCurrentUTicketId());
                generatedRequest.put("associatedPlaintextData", this.sharedData.getCurrentSession().getAssociatedPlaintextData());
                generatedRequest.put("ciphertextData", this.sharedData.getCurrentSession().getCiphertextData());
                generatedRequest.put("gcmAuthenticationTagData", this.sharedData.getCurrentSession().getGcmAuthenticationTagData());
                generatedRequest.put("ivCmd", this.sharedData.getCurrentSession().getIvCmd());
            } else {
                generatedRequest.put("rTicketType", RTicket.TYPE_DATA_RTOKEN);
                generatedRequest.put("deviceId", this.sharedData.getThisDevice().getDevicePubKeyStr());
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
            String deviceId = receivedRTicket.getDeviceId();
            String storedUTicketJson = this.sharedData.getDeviceTable().get(deviceId).getDeviceUTicketForOwner();

            SimpleLogger.simpleLog("debug","Corresponding UTicket: " + storedUTicketJson);

            // STAGE: (VR)]
            UTicket storedUTicket = this.msgVerifier._classify_u_ticket_is_defined_type(storedUTicketJson);

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
        } else if (Environment.COMMUNICATION_CHANNEL.equals("BLUETOOTH")) {
            this.msgSender.completeBluetoothComm();
        }
    }

}

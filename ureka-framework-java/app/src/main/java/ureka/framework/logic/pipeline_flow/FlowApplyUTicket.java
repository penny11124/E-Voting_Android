package ureka.framework.logic.pipeline_flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

public class FlowApplyUTicket {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private ReceivedMsgStorer receivedMsgStorer;
    private MsgVerifier msgVerifier;
    private Executor executor;
    private MsgGenerator msgGenerator;
    private GeneratedMsgStorer generatedMsgStorer;
    private MsgSender msgSender;
    private FlowOpenSession flowOpenSession;

    public FlowApplyUTicket(SharedData sharedData, MeasureHelper measureHelper, ReceivedMsgStorer receivedMsgStorer
        , MsgVerifier msgVerifier, Executor executor, MsgGenerator msgGenerator, GeneratedMsgStorer generatedMsgStorer
        , MsgSender msgSender, FlowOpenSession flowOpenSession) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
        this.receivedMsgStorer = receivedMsgStorer;
        this.msgVerifier = msgVerifier;
        this.executor = executor;
        this.msgGenerator = msgGenerator;
        this.generatedMsgStorer = generatedMsgStorer;
        this.msgSender = msgSender;
        this.flowOpenSession = flowOpenSession;
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

    public ReceivedMsgStorer getReceivedMsgStorer() {
        return receivedMsgStorer;
    }

    public void setReceivedMsgStorer(ReceivedMsgStorer receivedMsgStorer) {
        this.receivedMsgStorer = receivedMsgStorer;
    }

    public MsgVerifier getMsgVerifier() {
        return msgVerifier;
    }

    public void setMsgVerifier(MsgVerifier msgVerifier) {
        this.msgVerifier = msgVerifier;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public MsgGenerator getMsgGenerator() {
        return msgGenerator;
    }

    public void setMsgGenerator(MsgGenerator msgGenerator) {
        this.msgGenerator = msgGenerator;
    }

    public GeneratedMsgStorer getGeneratedMsgStorer() {
        return generatedMsgStorer;
    }

    public void setGeneratedMsgStorer(GeneratedMsgStorer generatedMsgStorer) {
        this.generatedMsgStorer = generatedMsgStorer;
    }

    public MsgSender getMsgSender() {
        return msgSender;
    }

    public void setMsgSender(MsgSender msgSender) {
        this.msgSender = msgSender;
    }

    public FlowOpenSession getFlowOpenSession() {
        return flowOpenSession;
    }

    public void setFlowOpenSession(FlowOpenSession flowOpenSession) {
        this.flowOpenSession = flowOpenSession;
    }

    public void holderApplyUTicket(String deviceId) {
        holderApplyUTicket(deviceId, "");
    }

    public void holderApplyUTicket(String deviceId, String cmd) {
        //////////////////////////////////////////////////////
        // Start Process Measurement
        //////////////////////////////////////////////////////
        this.measureHelper.measureProcessPerfStart();

        try {
            // [STAGE: (VL)(L)]
            String storedUTicketJson;
            if (this.sharedData.getDeviceTable().containsKey(deviceId)) {
                storedUTicketJson = this.sharedData.getDeviceTable().get(deviceId).getDeviceUTicketForOwner();
            } else {
                String error = "FAILURE: (VL)";
                this.sharedData.setResultMessage(error);
                throw new RuntimeException(error);
            }

            // [STAGE: (VR)]
            UTicket storedUTicket = this.msgVerifier._classifyUTicketIsDefinedType(storedUTicketJson);

            if (Objects.equals(storedUTicket.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
                || Objects.equals(storedUTicket.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_RT);
            } else if (Objects.equals(storedUTicket.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)
                || Objects.equals(storedUTicket.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
                // [STAGE: (E)]
                this.executor.executeCRKE(storedUTicket, "holder", cmd);
                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_CRKE1);
            } else {
                throw new RuntimeException("FlowApplyUTicket-holderApplyUTicket: storedUTicket type error.");
            }

            //////////////////////////////////////////////////////
            // End Process Measurement
            //////////////////////////////////////////////////////
            this.measureHelper.measureRecvCliPerfTime("holderApplyUTicket");

            ////////////////////////////////////////////////////////////////////////
            // Start Comm Measurement
            ////////////////////////////////////////////////////////////////////////
            if (!Objects.equals(this.sharedData.getThisDevice().getDeviceName(), "iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE
                , UTicket.MESSAGE_TYPE, storedUTicketJson);

        } catch (RuntimeException e) {
            this.sharedData.setResultMessage(e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("FlowApplyUTicket-holderApplyUTicket: Unexpected error.");
        }
    }

    public void _deviceRecvUTicket(UTicket receivedUTicket) {
        try {
            // [STAGE: (R)(VR)]

            // [STAGE: (SR)]
            // No need to optionally _store_received_xxx_u_ticket

            // [STAGE: (VUT)]
            this.msgVerifier.verifyUTicketCanExecute(receivedUTicket);
            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_CAN_EXECUTE");

            // UT-RT
            if (Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
                || Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // [STAGE: (EO)]
                this.executor.executeXxxUTicket(receivedUTicket);
                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_UT);
            }
            // CR-KE
            else if (Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)
                || Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
                // [STAGE: (E)]
                this.executor.executeXxxUTicket(receivedUTicket);
                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_CRKE2);
            } else {
                throw new RuntimeException("FlowApplyUTicket-_deviceRecvUTicket: receivedUTicket type error.");
            }

        } catch (RuntimeException e) {
            this.sharedData.setResultMessage("error");

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_UT);

            // UT-RT
            if (Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
                || Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // pass
            } else if (Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)
                || Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
                if (Environment.COMMUNICATION_CHANNEL == "SIMULATED") {
                    this.msgSender.completeSimulatedComm();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("FlowApplyUTicket-_deviceRecvUTicket: Unexpected error.");
        } finally {
            // UT-RT
            if (Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
                || Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // [STAGE: (G)(S)]
                this._deviceSendRTicket(receivedUTicket.getUTicketType()
                    , receivedUTicket.getUTicketId(), this.sharedData.getResultMessage());
            }
            // # CR-KE
            else if (Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)
                || Objects.equals(receivedUTicket.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
                // [STAGE: (G)(S)]
                this.flowOpenSession._deviceSendCrKe1(this.sharedData.getResultMessage());
            }
            // else {
            //     // pragma: no cover -> Shouldn't Reach Here
            //     throw new RuntimeException("FlowApplyUTicket-_deviceRecvUTicket: Unexpected error-2.");
            // }
        }
    }

    public void _deviceSendRTicket(String uTicketType, String uTicketId, String resultMessage) {
        try {
            Map<String, String> rTicketRequest = new HashMap<>();
            // [STAGE: (G)]
            if (Objects.equals(uTicketType, UTicket.TYPE_INITIALIZATION_UTICKET)
                || Objects.equals(uTicketType, UTicket.TYPE_OWNERSHIP_UTICKET)) {
                if (resultMessage.contains("SUCCESS")) {
                    rTicketRequest.put("rTicketType", uTicketType);
                    rTicketRequest.put("deviceId", this.sharedData.getThisDevice().getDevicePubKeyStr());
                    rTicketRequest.put("result", resultMessage);
                    rTicketRequest.put("auditStart", uTicketId);
                } else {
                    rTicketRequest.put("rTicketType", uTicketType);
                    rTicketRequest.put("deviceId", this.sharedData.getThisDevice().getDevicePubKeyStr());
                    rTicketRequest.put("result", resultMessage);
                }

            } else if (Objects.equals(uTicketType, UTicket.TYPE_ACCESS_END_UTOKEN)) {
                if (resultMessage.contains("SUCCESS")) {
                    // auditStart has already stored when receiving Access UTicket
                    rTicketRequest.put("rTicketType", uTicketType);
                    rTicketRequest.put("deviceId", this.sharedData.getThisDevice().getDevicePubKeyStr());
                    rTicketRequest.put("result", resultMessage);
                    rTicketRequest.put("auditStart", this.sharedData.getCurrentSession().getCurrentUTicketId());
                    rTicketRequest.put("auditEnd", "ACCESS END");
                } else {
                    // pragma: no cover -> Weird U-Token
                    rTicketRequest.put("rTicketType", uTicketType);
                    rTicketRequest.put("deviceId", this.sharedData.getThisDevice().getDevicePubKeyStr());
                    rTicketRequest.put("result", resultMessage);
                }
            } else {
                // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("FlowApplyUTicket-_deviceSendRTicket: Unexpected error.");
            }

            String generatedRTicketJson = this.msgGenerator.generateXxxRTicket(rTicketRequest);
            // [STAGE: (SG)]
            // Can optionally _stored_generated_xxx_r_ticket

            //////////////////////////////////////////////////////
            // End Process Measurement
            //////////////////////////////////////////////////////
            this.measureHelper.measureRecvMsgPerfTime("_deviceRecvUTicketAndSendRTicket");

            ////////////////////////////////////////////////////////////////////////
            // Start Comm Measurement
            ////////////////////////////////////////////////////////////////////////
            if (!Objects.equals(this.sharedData.getThisDevice().getDeviceName(), "iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE
                , RTicket.MESSAGE_TYPE, generatedRTicketJson);

        } catch (RuntimeException e) {
            throw new RuntimeException("FlowApplyUTicket-_deviceSendRTicket: Unexpected error-2.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Manually Finish Simulated Comm
        if (Environment.COMMUNICATION_CHANNEL == "SIMULATED") {
            this.msgSender.completeSimulatedComm();
        }
    }

    public void _holderRecvRTicket(RTicket receivedRTicket) {
        try {
            // [STAGE: (R)(VR)]

            // [STAGE: (SR)]
            this.receivedMsgStorer.storeReceivedXxxRTicket(receivedRTicket);

            if (Objects.equals(receivedRTicket.getRTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
                || Objects.equals(receivedRTicket.getRTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)
                || Objects.equals(receivedRTicket.getRTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // Query Corresponding UTicket(s)
                //   Notice that even Initialization UTicket is copied in the device_table["deviceId"]
                // [STAGE: (VL)(L)]
                String storedUTicketJson;
                if (this.sharedData.getDeviceTable().containsKey(receivedRTicket.getDeviceId())) {
                    storedUTicketJson = this.sharedData.getDeviceTable()
                        .get(receivedRTicket.getDeviceId()).getDeviceUTicketForOwner();
                } else {
                    String error = "FAILURE: (VL)";
                    this.sharedData.setResultMessage(error);
                    throw new RuntimeException(error);
                }
                // simpleLog("debug", "Corresponding UTicket: {storedUTicketJson}");
                // [STAGE: (VR)]
                UTicket storedUTicket = this.msgVerifier._classifyUTicketIsDefinedType(storedUTicketJson);

                // [STAGE: (VRT)]
                this.msgVerifier.verifyUTicketHasExecutedThroughRTicket(receivedRTicket, storedUTicket, null);

                //[STAGE: (E)(O)]
                this.executor.executeXxxRTicket(receivedRTicket, "holder-or-device");
                this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_HAS_EXECUTED");

                // [STAGE: (C)]
                this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT);
            } else {
                // pragma: no cover -> TODO: Revocation UTicket
                // Query Corresponding UTicket(s)
                throw new RuntimeException("FlowApplyUTicket-_holderRecvRTicket: Not implemented yet.");
            }
        } catch (RuntimeException e) {
            // FAILURE: (VR)(VRT)
            this.sharedData.setResultMessage(e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("FlowApplyUTicket-_holderRecvRTicket: Unexpected error.");
        } finally {
            //////////////////////////////////////////////////////
            // End Process Measurement
            //////////////////////////////////////////////////////
            this.measureHelper.measureRecvMsgPerfTime("_holderRecvRTicket");

            ////////////////////////////////////////////////////////////////////////
            // Start Comm Measurement
            ////////////////////////////////////////////////////////////////////////
            if (!Objects.equals(this.sharedData.getThisDevice().getDeviceName(), "iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }
        }

        // Manually Finish Simulated/Bluetooth Comm
        if (Environment.COMMUNICATION_CHANNEL == "SIMULATED") {
            this.msgSender.completeSimulatedComm();
//        } else if (Objects.equals(Environment.COMMUNICATION_CHANNEL, "BLUETOOTH")) {
//            this.msgSender.completeBluetoothComm();
        }
    }
}

package ureka.framework.logic.pipeline_flow;

import com.example.urekaapp.AdminAgentActivity;

import java.util.HashMap;
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
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.resource.logger.SimpleLogger;

public class FlowOpenSession {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private ReceivedMsgStorer receivedMsgStorer;
    private MsgVerifier msgVerifier;
    private Executor executor;
    private MsgGenerator msgGenerator;
    private GeneratedMsgStorer generatedMsgStorer;
    private MsgSender msgSender;

    public FlowOpenSession(SharedData sharedData, MeasureHelper measureHelper, ReceivedMsgStorer receivedMsgStorer,
            MsgVerifier msgVerifier, Executor executor, MsgGenerator msgGenerator,
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

    // [PIPELINE FLOW]
    // APY (CR-KE):
    //       holderApplyUTicket() -> _deviceRecvUTicket()
    //       _holderRecvCrKe1() <- _deviceSendCrKe1()
    //       _holderSendCrKe2() -> _deviceRecvCrKe2()
    //       _holderRecvCrKe3() <- _deviceSendCrKe3()
    public void _deviceSendCrKe1(String resultMessage) {
        try {
            // [STAGE: (G)]
            Map<String, String> rTicketRequest = new HashMap<>();
            if (resultMessage.contains("SUCCESS")) {
                rTicketRequest.put("r_ticket_type", RTicket.TYPE_CRKE1_RTICKET);
                rTicketRequest.put("device_id", this.sharedData.getThisDevice().getDevicePubKeyStr());
                rTicketRequest.put("result", resultMessage);
                rTicketRequest.put("audit_start", this.sharedData.getCurrentSession().getCurrentUTicketId());
                rTicketRequest.put("challenge1", this.sharedData.getCurrentSession().getChallenge1());
                rTicketRequest.put("keyExchangeSalt1", this.sharedData.getCurrentSession().getKeyExchangeSalt1());
                rTicketRequest.put("ivCmd", this.sharedData.getCurrentSession().getIvCmd());
            } else {
                rTicketRequest.put("r_ticket_type", RTicket.TYPE_CRKE1_RTICKET);
                rTicketRequest.put("device_id", this.sharedData.getThisDevice().getDevicePubKeyStr());
                rTicketRequest.put("result", resultMessage);
            }

            String generatedRTicketJson = this.msgGenerator.generateXxxRTicket(rTicketRequest);

            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("deviceRecvUTicketAndSendCrKe1");

            // Start Comm Measurement
            if(!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureProcessPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE, RTicket.MESSAGE_TYPE, generatedRTicketJson);
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
    }

    public void _holderRecvCrKe1(RTicket receivedRTicket) {
        try {
            // [STAGE: (R)(VR)]
            // [STAGE: (VRT)]
            this.msgVerifier.verifyUTicketHasExecutedThroughRTicket(receivedRTicket,null,null);

            // [STAGE: (E)]
            this.executor.executeXxxRTicket(receivedRTicket,"holderOrDevice");
            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_HAS_EXECUTED");

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_CRKE3);

            // [STAGE: (G)(S)]
            this._holderSendCrKe2(this.sharedData.getResultMessage());
        } catch (RuntimeException error) {
            this.sharedData.setResultMessage(error.getMessage());

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT);

            // Automatically Terminate Simulated/Bluetooth Comm
            SimpleLogger.simpleLog("debug", "+ " + this.sharedData.getThisDevice().getDeviceName() + " automatically terminate CR-KE-1~~ (holder)");

            if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
                msgSender.completeSimulatedComm();
            }
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }

        SimpleLogger.simpleLog("debug", "resultMessage = " + this.sharedData.getResultMessage());
    }

    public void _holderSendCrKe2(String resultMessage) {
        try {
            // [STAGE: (G)]
            Map<String, String> rTicketRequest = new HashMap<>();
            rTicketRequest.put("r_ticket_type", RTicket.TYPE_CRKE2_RTICKET);
            rTicketRequest.put("device_id", this.sharedData.getCurrentSession().getCurrentDeviceId());
            rTicketRequest.put("result", resultMessage);
            rTicketRequest.put("audit_start", this.sharedData.getCurrentSession().getCurrentUTicketId());
            rTicketRequest.put("challenge_1", this.sharedData.getCurrentSession().getChallenge1());
            rTicketRequest.put("challenge_2", this.sharedData.getCurrentSession().getChallenge2());
            rTicketRequest.put("key_exchange_salt_2", this.sharedData.getCurrentSession().getKeyExchangeSalt2());
            rTicketRequest.put("associated_plaintext_cmd", this.sharedData.getCurrentSession().getAssociatedPlaintextCmd());
            rTicketRequest.put("ciphertext_cmd", this.sharedData.getCurrentSession().getCiphertextCmd());
            rTicketRequest.put("gcm_authentication_tag_cmd", this.sharedData.getCurrentSession().getGcmAuthenticationTagCmd());
            rTicketRequest.put("iv_data", this.sharedData.getCurrentSession().getIvData());

            String generatedRTicketJson = this.msgGenerator.generateXxxRTicket(rTicketRequest);

            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("holderRecvCrKe1AndSendCrKe2");

            // Start Comm Measurement
            if(!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureProcessPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE, RTicket.MESSAGE_TYPE, generatedRTicketJson);
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
    }

    public void _deviceRecvCrKe2(RTicket receivedRTicket) {
        try {
            // [STAGE: (VRT)]
            this.msgVerifier.verifyUTicketHasExecutedThroughRTicket(receivedRTicket, null, null);

            // [STAGE: (VTK)(VTS)]
            // [STAGE: (E)]
            this.executor.executeXxxRTicket(receivedRTicket, "holderOrDevice");
            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_HAS_EXECUTED");

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_CMD);
        } catch (RuntimeException error) {
            this.sharedData.setResultMessage(error.getMessage());

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_UT);

            // Automatically Terminate Simulated Comm
            SimpleLogger.simpleLog("debug", "+ " + this.sharedData.getThisDevice().getDeviceName() + " automatically terminate CR-KE-2~~ (device)");
            // // Anyway, Finish CR-KE~~
        } catch (Exception e) {
            throw new RuntimeException("Shouldn't Reach Here", e);
        } finally {
            // [STAGE: (G)(S)]
            this._deviceSendCrKe3(this.sharedData.getResultMessage());
        }

        // Manually Finish Simulated Comm
        SimpleLogger.simpleLog("debug",  "+ " + this.sharedData.getThisDevice().getDeviceName() + " manually finish CR-KE~~ (device)");

        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.completeSimulatedComm();
        }
    }

    public void _deviceSendCrKe3(String resultMessage) {
        try {
            // [STAGE: (G)]
            Map<String, String> rTicketRequest = new HashMap<>();
            if (resultMessage.contains("SUCCESS")) {
                rTicketRequest.put("r_ticket_type", RTicket.TYPE_CRKE3_RTICKET);
                rTicketRequest.put("device_id", this.sharedData.getThisDevice().getDevicePubKeyStr());
                rTicketRequest.put("result", resultMessage);
                rTicketRequest.put("audit_start", this.sharedData.getCurrentSession().getCurrentUTicketId());
                rTicketRequest.put("challenge2", this.sharedData.getCurrentSession().getChallenge2());
                rTicketRequest.put("associated_plaintext_data", this.sharedData.getCurrentSession().getAssociatedPlaintextData());
                rTicketRequest.put("ciphertextData", this.sharedData.getCurrentSession().getCiphertextData());
                rTicketRequest.put("gcmAuthenticationTagData", this.sharedData.getCurrentSession().getGcmAuthenticationTagData());
                rTicketRequest.put("ivCmd", this.sharedData.getCurrentSession().getIvCmd());
            } else {
                rTicketRequest.put("r_ticket_type", RTicket.TYPE_CRKE3_RTICKET);
                rTicketRequest.put("device_id", this.sharedData.getThisDevice().getDevicePubKeyStr());
                rTicketRequest.put("result", resultMessage);
            }

            String generatedRTicketJson = this.msgGenerator.generateXxxRTicket(rTicketRequest);

            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("deviceRecvCrKe2AndSendCrKe3");

            // Start Comm Measurement
            if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }

            // [STAGE: (S)]
            this.msgSender.sendXxxMessage(Message.MESSAGE_VERIFY_AND_EXECUTE, RTicket.MESSAGE_TYPE, generatedRTicketJson);
        } catch (Exception e) {  // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
    }

    public void _holderRecvCrKe3(RTicket receivedRTicket) {
        try {
            // [STAGE: (R)(VR)]
            // [STAGE: (VRT)]
            this.msgVerifier.verifyUTicketHasExecutedThroughRTicket(receivedRTicket, null, null);

            // [STAGE: (VTK)]
            // [STAGE: (E)]
            this.executor.executeXxxRTicket(receivedRTicket, "holderOrDevice");
            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_UT_HAS_EXECUTED");

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_CMD);
        } catch (RuntimeException error) {
            this.sharedData.setResultMessage(error.getMessage());

            // [STAGE: (C)]
            this.executor.changeState(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT);

            // Automatically Terminate Simulated/Bluetooth Comm
            SimpleLogger.simpleLog("debug", this.sharedData.getThisDevice().getDeviceName() + " automatically terminate CR-KE-3~~ (holder)");
            // // Anyway, Finish CR-KE~~
        } catch (Exception e) {
            throw new RuntimeException("Shouldn't Reach Here", e);
        } finally {
            // End Process Measurement
            this.measureHelper.measureRecvMsgPerfTime("holderRecvCrKe3");

            // Start Comm Measurement
            if (!this.sharedData.getThisDevice().getDeviceName().equals("iotDevice")) {
                this.measureHelper.measureCommPerfStart();
            }
        }

        // Manually Finish Simulated/Bluetooth Comm
        SimpleLogger.simpleLog("debug",  "+ " + this.sharedData.getThisDevice().getDeviceName() + " manually finish CR-KE~~ (holder)");

        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.completeSimulatedComm();
        }

        SimpleLogger.simpleLog("debug", "resultMessage = " + this.sharedData.getResultMessage());
    }
}

package ureka.framework.logic.stage_worker;

import com.example.urekaapp.AdminAgentActivity;
import com.example.urekaapp.VoterAgentActivity;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.logic.pipeline_flow.FlowApplyUTicket;
import ureka.framework.logic.pipeline_flow.FlowIssueUTicket;
import ureka.framework.logic.pipeline_flow.FlowIssueUToken;
import ureka.framework.logic.pipeline_flow.FlowOpenSession;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.communication.bluetooth.BluetoothService;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MsgReceiver {
    private SharedData sharedData = null;
    private MeasureHelper measureHelper = null;
    private MsgVerifier msgVerifier = null;
    private Executor executor = null;
    private MsgSender msgSender = null;
    private FlowIssueUTicket flowIssueUTicket = null;
    private FlowApplyUTicket flowApplyUTicket = null;
    private FlowOpenSession flowOpenSession = null;
    private FlowIssueUToken flowIssueUToken = null;

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public SharedData getSharedData() {
        return this.sharedData;
    }

    public void setMeasureHelper(MeasureHelper measureHelper) {
        this.measureHelper = measureHelper;
    }

    public MeasureHelper getMeasureHelper() {
        return measureHelper;
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

    public void setMsgSender(MsgSender msgSender) {
        this.msgSender = msgSender;
    }

    public MsgSender getMsgSender() {
        return msgSender;
    }

    public void setFlowIssueUTicket(FlowIssueUTicket flowIssueUTicket) {
        this.flowIssueUTicket = flowIssueUTicket;
    }

    public FlowIssueUTicket getFlowIssueUTicket() {
        return flowIssueUTicket;
    }

    public void setFlowApplyUTicket(FlowApplyUTicket flowApplyUTicket) {
        this.flowApplyUTicket = flowApplyUTicket;
    }

    public FlowApplyUTicket getFlowApplyUTicket() {
        return flowApplyUTicket;
    }

    public void setFlowOpenSession(FlowOpenSession flowOpenSession) {
        this.flowOpenSession = flowOpenSession;
    }

    public FlowOpenSession getFlowOpenSession() {
        return flowOpenSession;
    }

    public void setFlowIssueUToken(FlowIssueUToken flowIssueUToken) {
        this.flowIssueUToken = flowIssueUToken;
    }

    public FlowIssueUToken getFlowIssueUToken() {
        return flowIssueUToken;
    }

    public MsgReceiver() {}
    public MsgReceiver(
        SharedData sharedData,
        MeasureHelper measureHelper,
        MsgVerifier msgVerifier,
        Executor executor,
        MsgSender msgSender,
        FlowIssueUTicket flowIssueUTicket,
        FlowApplyUTicket flowApplyUTicket,
        FlowOpenSession flowOpenSession,
        FlowIssueUToken flowIssueUToken
    ) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
        this.msgVerifier = msgVerifier;
        this.executor = executor;
        this.msgSender = msgSender;
        this.flowIssueUTicket = flowIssueUTicket;
        this.flowApplyUTicket = flowApplyUTicket;
        this.flowOpenSession = flowOpenSession;
        this.flowIssueUToken = flowIssueUToken;
    }

    /* Resource (Simulated Comm)
       Threading Issue:
           Pytest finishes this test when main thread is finished
               (& all daemon threads, e.g. all receiverThreads will also be terminated)
           In production, we may need Ctrl+C or other shutdown method to stop this loop program
     */
//    public void createSimulatedCommConnection(DeviceController end) {
//        SimpleLogger.simpleLog("info", this.sharedData.getThisDevice().getDeviceName() + " is connecting with {end.sharedData.thisDevice.deviceName}...");
//
//        // Set Sender (on Main Thread)
//        this.sharedData.getSimulatedCommChannel().setEnd(end);
//        this.sharedData.getSimulatedCommChannel().setSenderQueue(end.getSharedData().getSimulatedCommChannel().getReceiverQueue());
//
//        // Start Receiver Thread
//        Thread receiverThread = new Thread(this::_recvXxxMessage);
//        receiverThread.setDaemon(true);
//        receiverThread.start();
//    }

    // Resource (Bluetooth Comm)
//    public void acceptBluetoothComm() {
//        BluetoothService.AcceptSocket acceptSocket = new BluetoothService.AcceptSocket(
//                BluetoothService.SERVICE_UUID,
//                BluetoothService.SERVICE_NAME
//        );
//        this.sharedData.setAcceptSocket(acceptSocket);
//
//        BluetoothService.ConnectionSocket connectionSocket = acceptSocket.accept();
//        this.sharedData.setConnectionSocket(connectionSocket);
//    }
//
//    public void closeBluetoothConnection() throws Exception {
//        this.sharedData.getConnectionSocket().close();
//    }
//
//    public void closeBluetoothAcception() throws Exception {
//        this.sharedData.getAcceptSocket().close();
//    }

    // [STAGE: (R)] Receive Message
    public void _recvXxxMessage(String data) {
        SimpleLogger.simpleLog("info", "Received: " + data);

        String receivedMessageWithHeader = "";
        if (Objects.equals(Environment.DEPLOYMENT_ENV, "TEST")) {
            receivedMessageWithHeader = data;
        }
        // Message Size Measurement
        this.measureHelper.measureMessageSize("_recvMessage", receivedMessageWithHeader);
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();

        // [STAGE: (VR)]
        Object receivedMessage = this.msgVerifier._classifyMessageIsDefinedType(receivedMessageWithHeader);
        if (receivedMessage instanceof UTicket) {
            this.sharedData.setReceivedMessageJson(UTicket.uTicketToJsonStr((UTicket) receivedMessage));
        } else if (receivedMessage instanceof RTicket) {
            this.sharedData.setReceivedMessageJson(RTicket.rTicketToJsonStr((RTicket) receivedMessage));
        } else if (receivedMessage instanceof String) {
            // Handle Request UTicket
            String receivedMessageStr = (String) receivedMessage;
            SimpleLogger.simpleLog("info", "MsgReceiver: Message received = " + receivedMessageStr);
            this.sharedData.setReceivedMessageJson(receivedMessageStr);
            this.flowIssueUTicket.issuerIssueUTicketToHolder(SerializationUtil.jsonToMap(receivedMessageStr));
        }

        SimpleLogger.simpleLog("cli", "Received Message: " + this.sharedData.getReceivedMessageJson());

        try {
            // IOT_DEVICE
            String state = this.sharedData.getState();
            if (state.equals(ThisDevice.STATE_DEVICE_WAIT_FOR_UT)) {
                this.flowApplyUTicket._deviceRecvUTicket((UTicket) receivedMessage);
            } else if (state.equals(ThisDevice.STATE_DEVICE_WAIT_FOR_CRKE2)) {
                this.flowOpenSession._deviceRecvCrKe2((RTicket) receivedMessage);
                SimpleLogger.simpleLog("cli", "plaintextCmd in " + this.sharedData.getThisDevice().getDeviceName() + " = " + this.sharedData.getCurrentSession().getPlaintextCmd());
            } else if (state.equals(ThisDevice.STATE_DEVICE_WAIT_FOR_CMD)) {
                this.flowIssueUToken._deviceRecvCmd((UTicket) receivedMessage);
                SimpleLogger.simpleLog("cli", "plaintextCmd in " + this.sharedData.getThisDevice().getDeviceName() + " = " + this.sharedData.getCurrentSession().getPlaintextCmd());
            }
            // USER_AGENT_OR_CLOUD_SERVER
            else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT)) {
                if (receivedMessage instanceof UTicket) {
                    this.flowIssueUTicket._holderRecvUTicket((UTicket) receivedMessage);
                } else if (receivedMessage instanceof RTicket) {
                    this.flowIssueUTicket._issuerRecvRTicket((RTicket) receivedMessage);
                }
            } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_RT)) {
                this.flowApplyUTicket._holderRecvRTicket((RTicket) receivedMessage);
            } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_CRKE1)) {
                this.flowOpenSession._holderRecvCrKe1((RTicket) receivedMessage);
            } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_CRKE3)) {
                this.flowOpenSession._holderRecvCrKe3((RTicket) receivedMessage);
                SimpleLogger.simpleLog("cli", "plaintextData in " + this.sharedData.getThisDevice().getDeviceName() + " = " + this.sharedData.getCurrentSession().getPlaintextData());
                SimpleLogger.simpleLog("cli", "+++Session is Constructed+++");
                AdminAgentActivity.sendNextTicket = true;
                VoterAgentActivity.sendNextTicket = true;
                SimpleLogger.simpleLog("info", "Ready to send next ticket");
            } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_DATA)) {
                this.flowIssueUToken._holderRecvData((RTicket) receivedMessage);
                SimpleLogger.simpleLog("cli", "plaintextData in " + this.sharedData.getThisDevice().getDeviceName() + " = " + this.sharedData.getCurrentSession().getPlaintextData());
                AdminAgentActivity.sendNextTicket = true;
                VoterAgentActivity.sendNextTicket = true;
                SimpleLogger.simpleLog("info", "Ready to send next ticket");
            } else { // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("Shouldn't Reach Here");
            }
        } catch (RuntimeException error) {
            this.sharedData.setResultMessage(error.getMessage());
            throw error;
        } catch (Exception error) {
            throw new RuntimeException("Shouldn't Reach Here", error);
        }
    }
}

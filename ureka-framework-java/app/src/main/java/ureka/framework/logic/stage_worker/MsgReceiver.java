package ureka.framework.logic.stage_worker;

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
import ureka.framework.resource.logger.SimpleLogger;

public class MsgReceiver implements Runnable{
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private MsgVerifier msgVerifier;
    private Executor executor;
    private MsgSender msgSender;
    private FlowIssueUTicket flowIssueUTicket;
    private FlowApplyUTicket flowApplyUTicket;
    private FlowOpenSession flowOpenSession;
    private FlowIssueUToken flowIssueUToken;
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
    public void createSimulatedCommConnection(DeviceController end) {
        /* simpleLog("info",
             f"+ {self.sharedData.thisDevice.deviceName} is connecting with {end.sharedData.thisDevice.deviceName}...")
         */

        // Set Sender (on Main Thread)
        this.sharedData.getSimulatedCommChannel().setEnd(end);
        this.sharedData.getSimulatedCommChannel().setSenderQueue(end.getSharedData().getSimulated_comm_channel().getReceiver_queue());

        // Start Reciever Thread
        Thread receiverThread = new Thread(this::_recvXxxMessage);
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    // Resource (Bluetooth Comm)
    public void acceptBluetoothComm() {
        BluetoothService.AcceptSocket acceptSocket = new BluetoothService.AcceptSocket(
                BluetoothService.SERVICE_UUID,
                BluetoothService.SERVICE_NAME
        );
        this.sharedData.setAcceptSocket(acceptSocket);

        BluetoothService.ConnectionSocket connectionSocket = acceptSocket.accept();
        this.sharedData.setConnectionSocket(connectionSocket);
    }

    public void closeBluetoothConnection() throws Exception {
        this.sharedData.getConnectionSocket().close();
    }

    public void closeBluetoothAcception() throws Exception {
        this.sharedData.getAcceptSocket().close();
    }

    // [STAGE: (R)] Receive Message
    private void _recvXxxMessage() {

    }

    // _recvXxxMessage?
    @Override
    public void run() {
        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            this.msgSender.startSimulatedComm();
        } else if (Environment.COMMUNICATION_CHANNEL.equals("BLUETOOTH")) {
            this.msgSender.startBluetoothComm();
        }

        while (true) {
            try {
                // [STAGE: (R)]
                String receivedMessageWithHeader = null;

                if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
                    if (this.sharedData.getSimulatedCommCompletedFlag()) {
                        break;
                    }
                    BlockingQueue<String> receiverQueue = this.sharedData.getSimulatedCommChannel().getReceiverQueue();
                    if (Environment.DEPLOYMENT_ENV.equals("TEST")) {
                        // This will block until message is received
                        receivedMessageWithHeader = receiverQueue.take();
                    } else if (Environment.DEPLOYMENT_ENV.equals("PRODUCTION")) {
                        // This will block until message is received all until timeout
                        receivedMessageWithHeader = receiverQueue.poll((long) Environment.SIMULATED_COMM_TIME_OUT, TimeUnit.SECONDS);
                        if (receivedMessageWithHeader == null) {
                            throw new TimeoutException("Simulated communication timeout");
                        }
                    }

                    SimpleLogger.simpleLog("info",
                            "+ " + this.sharedData.getThisDevice().getDeviceName() + " is receiving message from "
                                    + this.sharedData.getSimulatedCommChannel().getEnd().getSharedData().getThisDevice().getDeviceName() + "...");
                } else if (Environment.COMMUNICATION_CHANNEL.equals("BLUETOOTH")) {// pragma: no cover -> PRODUCTION
                    // Start Comm Measurement
                    if (this.sharedData.getBluetoothCommCompletedFlag()) {
                        break;
                    }
                    receivedMessageWithHeader = this.sharedData.getConnectionSocket().recvMessage();
                    this.measureHelper.measureCommTime("_recvMessage");

                    SimpleLogger.simpleLog("info",
                            "+ " + this.sharedData.getThisDevice().getDeviceName() + " is receiving message from BT_address or BT_name...");
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
                }

                SimpleLogger.simpleLog("cli", "Received Message: " + this.sharedData.getReceivedMessageJson());

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
                        this.flowIssueUTicket._holder_recv_u_ticket(receivedMessage);
                    } else if (receivedMessage instanceof RTicket) {
                        this.flowIssueUTicket._issuer_recv_r_ticket(receivedMessage);
                    }
                } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_RT)) {
                    this.flowApplyUTicket._holderRecvRTicket(receivedMessage);
                } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_CRKE1)) {
                    this.flowOpenSession._holderRecvCrKe1(receivedMessage);
                } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_CRKE3)) {
                    this.flowOpenSession._holderRecvCrKe3(receivedMessage);
                    SimpleLogger.simpleLog("cli", "plaintextData in " + this.sharedData.getThisDevice().getDeviceName() + " = " + this.sharedData.getCurrentSession().getPlaintextData());
                    SimpleLogger.simpleLog("cli", "+++Session is Constructed+++");
                } else if (state.equals(ThisDevice.STATE_AGENT_WAIT_FOR_DATA)) {
                    this.flowIssueUToken._holderRecvData(receivedMessage);
                    SimpleLogger.simpleLog("cli", "plaintextData in " + this.sharedData.getThisDevice().getDeviceName() + " = " + this.sharedData.getCurrentSession().getPlaintextData());
                } else { // pragma: no cover -> Shouldn't Reach Here
                    throw new RuntimeException("Shouldn't Reach Here");
                }
            } catch (TimeoutException | InterruptedException error) { // Automatically Finish Simulated Comm
                SimpleLogger.simpleLog("debug", "+ " + this.sharedData.getThisDevice().getDeviceName() + " automatically terminate receiver thread (simulated comm) after Timeout (" + Environment.SIMULATED_COMM_TIME_OUT + " seconds)~~");
                if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
                    this.msgSender.completeSimulatedComm();
                }
                break;
            } catch (RuntimeException error) {
                this.sharedData.setResultMessage(error.getMessage());
                throw error;
            } catch (Exception error) {
                throw new RuntimeException("Shouldn't Reach Here", error);
            }
        }
    }
}

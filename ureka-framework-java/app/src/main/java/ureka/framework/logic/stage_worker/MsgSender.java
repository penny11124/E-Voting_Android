package ureka.framework.logic.stage_worker;

import ureka.framework.Environment;
import ureka.framework.model.SharedData;
import ureka.framework.model.message_model.Message;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.communication.bluetooth.BluetoothService;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

import java.util.HashMap;
import java.util.Map;

public class MsgSender {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    public MsgSender(SharedData sharedData, MeasureHelper measureHelper) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
    }

    // Resource (Simulated Comm)
    public void startSimulatedComm() {
        this.sharedData.setSimulatedCommCompletedFlag(false);
    }

    public void completeSimulatedComm() {
        this.sharedData.setSimulatedCommCompletedFlag(true);
    }

    public void waitSimulatedCommCompleted() throws InterruptedException {
        if (Environment.DEPLOYMENT_ENV.equals("TEST")) {
            while (!this.sharedData.getSimulatedCommCompletedFlag()) {
                Thread.sleep((long) Environment.SIMULATED_COMM_INTERRUPT_CYCLE_TIME);
            }
        } else if (Environment.DEPLOYMENT_ENV.equals("PRODUCTION")) {
            this.sharedData.getSimulatedCommReceiverThread().join();
        }
    }

    // Resource (Bluetooth Comm)
    public void startBluetoothComm() {
        this.sharedData.setBluetoothCommCompletedFlag(false);
    }

    public void completeBluetoothComm() {
        this.sharedData.setBluetoothCommCompletedFlag(true);
    }

    // something to fixed
    public void connectBluetoothComm() throws Exception {
        BluetoothService.ConnectingWorker connectingWorker = new BluetoothService.ConnectingWorker(
                BluetoothService.SERVICE_UUID,
                BluetoothService.SERVICE_NAME,
                BluetoothService.RECONNECT_TIMES,
                BluetoothService.RECONNECT_INTERVAL
        );
        this.sharedData.setConnectionSocket(connectingWorker.connect());
    }

    public void closeBluetoothConnection() throws Exception {
        this.sharedData.getConnectionSocket().close();
    }

    // [STAGE: (S)] Send Message
    public void sendXxxMessage(String messageOperation, String messageType, String sentMessageJson) {
        SimpleMeasurer.measureWorkerFunc(this::_sendXxxMessage, messageOperation, messageType, sentMessageJson); // something to fixed
    }
    public void _sendXxxMessage(String messageOperation, String messageType, String sentMessageJson) throws Exception {
        // Generate Message
        if ((messageOperation.equals(Message.MESSAGE_RECV_AND_STORE) ||
                messageOperation.equals(Message.MESSAGE_VERIFY_AND_EXECUTE)) &&
                (messageType.equals(UTicket.MESSAGE_TYPE) ||
                        messageType.equals(RTicket.MESSAGE_TYPE))) {

            Map<String, String> messageRequest = new HashMap<>();
            messageRequest.put("messageOperation", messageOperation);
            messageRequest.put("messageType", messageType);
            messageRequest.put("messageStr", sentMessageJson);

            try {
                Message newMessage = new Message(messageRequest);
                String newMessageJson = Message.messageToJsonstr(newMessage); // something to fixed
                // SimpleLogger.log("debug", "sentMessageJson: " + sentMessageJson);
            } catch (IllegalArgumentException error) { // pragma: no cover -> Weird M-Request
                throw new RuntimeException("Weird M-Request: " + error);
            }
        } else { // pragma: no cover -> Weird M-Request
            throw new RuntimeException("Weird M-Request");
        }

        if (Environment.COMMUNICATION_CHANNEL.equals("SIMULATED")) {
            SimpleLogger.simpleLog("info",
                    "+ " + this.sharedData.getThisDevice().getDeviceName() +
                            " is sending message to " +
                            this.sharedData.getSimulatedCommChannel().getEnd().getSharedData().getThisDevice().getDeviceName() + "...");

            // Simulate Network Delay
            for (int i = 0; i < Environment.SIMULATED_COMM_DELAY_COUNT; i++) {
                SimpleLogger.simpleLog("info", "+ network delay");
                SimpleLogger.simpleLog("info", "+ network delay");
                SimpleLogger.simpleLog("info", "+ network delay");
                if (Environment.DEPLOYMENT_ENV.equals("PRODUCTION")) { // pragma: no cover -> PRODUCTION
                    Thread.sleep((long) Environment.SIMULATED_COMM_DELAY_DURATION);
                }
            }

            this.sharedData.getSimulatedCommChannel().getSenderQueue().put(newMessageJson);
        } else { // pragma: no cover -> PRODUCTION
            SimpleLogger.simpleLog("info",
                    "+ " + this.sharedData.getThisDevice().getDeviceName() +
                            " is sending message to BT_address or BT_name...");

            this.sharedData.getConnectionSocket().sendMessage(newMessageJson);
        }
    }
}

package ureka.framework.model;

import java.util.HashMap;
import java.util.Map;

import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.resource.communication.simulated_comm.SimulatedCommChannel;

public class SharedData {
    // Data Model (Persistent)
    private ThisDevice thisDevice = null;
    private CurrentSession currentSession = null;
    // Data Model (Persistent: User Agent or Cloud Server only)
    private ThisPerson thisPerson = null;
    private Map<String, OtherDevice> deviceTable = new HashMap<>();
    // Data Model (RAM-only)
    private String state = null;

    // CLI Output
    private String receivedMessageJson = null;
    private String resultMessage = null;

    // Measurement Record
    private Map<String, Map<String, Long>> measureRec = null;

    // Resource (Simulated Comm)
    private SimulatedCommChannel simulatedCommChannel = null;
    private Thread simulatedCommReceiverThread = null;
    private Boolean simulatedCommCompletedFlag = null;

    // Resource (Bluetooth Comm)
    private AcceptSocket acceptSocket = null;
    private ConnectingWorker connectingWorker = null;
    private ConnectionSocket connectionSocket = null;
    private Boolean bluetoothCommCompletedFlag = null;

    public ThisDevice getThisDevice() {
        return thisDevice;
    }

    public void setThisDevice(ThisDevice thisDevice) {
        this.thisDevice = thisDevice;
    }

    public CurrentSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(CurrentSession currentSession) {
        this.currentSession = currentSession;
    }

    public ThisPerson getThisPerson() {
        return thisPerson;
    }

    public void setThisPerson(ThisPerson thisPerson) {
        this.thisPerson = thisPerson;
    }

    public Map<String, OtherDevice> getDeviceTable() {
        return deviceTable;
    }

    public void setDeviceTable(Map<String, OtherDevice> deviceTable) {
        this.deviceTable = deviceTable;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReceivedMessageJson() {
        return receivedMessageJson;
    }

    public void setReceivedMessageJson(String receivedMessageJson) {
        this.receivedMessageJson = receivedMessageJson;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Map<String, Map<String, Long>> getMeasureRec() {
        return measureRec;
    }

    public void setMeasureRec(Map<String, Map<String, Long>> measureRec) {
        this.measureRec = measureRec;
    }

    public SimulatedCommChannel getSimulatedCommChannel() {
        return simulatedCommChannel;
    }

    public void setSimulatedCommChannel(SimulatedCommChannel simulatedCommChannel) {
        this.simulatedCommChannel = simulatedCommChannel;
    }

    public Thread getSimulatedCommReceiverThread() {
        return simulatedCommReceiverThread;
    }

    public void setSimulatedCommReceiverThread(Thread simulatedCommReceiverThread) {
        this.simulatedCommReceiverThread = simulatedCommReceiverThread;
    }

    public AcceptSocket getAcceptSocket() {
        return acceptSocket;
    }

    public void setAcceptSocket(AcceptSocket acceptSocket) {
        this.acceptSocket = acceptSocket;
    }

    public Boolean getSimulatedCommCompletedFlag() {
        return simulatedCommCompletedFlag;
    }

    public void setSimulatedCommCompletedFlag(Boolean simulatedCommCompletedFlag) {
        this.simulatedCommCompletedFlag = simulatedCommCompletedFlag;
    }

    public ConnectingWorker getConnectingWorker() {
        return connectingWorker;
    }

    public void setConnectingWorker(ConnectingWorker connectingWorker) {
        this.connectingWorker = connectingWorker;
    }

    public ConnectionSocket getConnectionSocket() {
        return connectionSocket;
    }

    public void setConnectionSocket(ConnectionSocket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public Boolean getBluetoothCommCompletedFlag() {
        return bluetoothCommCompletedFlag;
    }

    public void setBluetoothCommCompletedFlag(Boolean bluetoothCommCompletedFlag) {
        this.bluetoothCommCompletedFlag = bluetoothCommCompletedFlag;
    }
}

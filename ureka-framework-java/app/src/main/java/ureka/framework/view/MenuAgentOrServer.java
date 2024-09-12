package ureka.framework.view;

import java.util.HashMap;
import java.util.Map;

import ureka.framework.Conftest;
import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MenuAgentOrServer {
    // Setting environment based on situation
    public static void setEnvironment(String situation) {
        if (situation.equals("cli")) {
            Environment.DEPLOYMENT_ENV = "PRODUCTION";
            Environment.DEBUG_LOG = "CLOSED";
            Environment.CLI_LOG = "OPEN";
            Environment.MEASURE_LOG = "CLOSED";
            Environment.MORE_MEASURE_WORKER_LOG = "CLOSED";
            Environment.MORE_MEASURE_RESOURCE_LOG = "CLOSED";
        } else if (situation.equals("measurement")) {
            Environment.DEPLOYMENT_ENV = "PRODUCTION";
            Environment.DEBUG_LOG = "CLOSED";
            Environment.CLI_LOG = "CLOSED";
            Environment.MEASURE_LOG = "CLOSED";
            Environment.MORE_MEASURE_WORKER_LOG = "CLOSED";
            Environment.MORE_MEASURE_RESOURCE_LOG = "CLOSED";
        }
    }
    private DeviceController agentOrServer;

    // Secure Mode
    public MenuAgentOrServer(String deviceName) {
        // GIVEN: Load UA or CS
        this.agentOrServer = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, deviceName);
    }

    public DeviceController getAgentOrServer() {
        return this.agentOrServer;
    }

    public String getTargetDeviceId() {
        // TODO: For complicated case, show a device list and let user choose
        // show_device_list()...
        // input()...

        // For simple case, just return the first device id
        Map<String, OtherDevice> deviceTable = this.agentOrServer.getSharedData().getDeviceTable();
        if (deviceTable.containsKey("noId")) {
            return (String) deviceTable.keySet().toArray()[1];
        } else {
            return (String) deviceTable.keySet().toArray()[0];
        }
    }

    public DeviceController initializeAgentOrServerThroughCLI() {
        // WHEN: Initialize UA or CS
        if (this.agentOrServer.getSharedData().getThisDevice().getTicketOrder() == 0) {
            this.agentOrServer.getExecutor()._executeOneTimeInitializeAgentOrServer();
        }
        return this.agentOrServer;
    }

    public DeviceController applyInitializationTicketThroughBluetooth() {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        try {
            this.agentOrServer.getMsgSender().connectBluetoothComm();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // WHEN: Issuer: DM's CS generate the intialization_u_ticket to herself
        String idForInitializationUTicket = "noId";
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", idForInitializationUTicket);
        generatedRequest.put("holderId", this.agentOrServer.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);

        this.agentOrServer.getFlowIssueUTicket().issuerIssueUTicketToHerself(idForInitializationUTicket, generatedRequest);

        // WHEN: Holder: DM's CS forward the intialization_u_ticket to Uninitialized IoTD
        this.agentOrServer.getFlowApplyUTicket().holderApplyUTicket(idForInitializationUTicket);

        // WHEN: Receive/Send Message in Connection
        this.agentOrServer.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish UT-RT~~)
        try {
            this.agentOrServer.getMsgSender().closeBluetoothConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this.agentOrServer;
    }

    public void issueOwnershipTicketThroughSimulatedComm(String targetDeviceId, DeviceController newOwner) {
        // WHEN: Issuer: Old owner generate & send the ownership_u_ticket to New Owner
        Environment.COMMUNICATION_CHANNEL = "SIMULATED";
        Conftest.createSimulatedCommConnection(this.agentOrServer, newOwner);

        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", newOwner.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET);

        this.agentOrServer.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        Conftest.waitSimulatedCommCompleted(newOwner, this.agentOrServer);
    }

    public DeviceController applyOwnershipTicketThroughBluetooth(String targetDeviceId) {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        this.agentOrServer.getMsgSender().connectBluetoothComm();

        // WHEN: Holder: EP's CS apply the ownership_u_ticket to IoTD
        this.agentOrServer.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId);
        // WHEN: Receive/Send Message in Connection
        this.agentOrServer.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish UT-RT~~)
        this.agentOrServer.getMsgSender().closeBluetoothConnection();
        return this.agentOrServer;
    }

    public DeviceController applySelfAccessTicketThroughBluetooth(String targetDeviceId) {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        this.agentOrServer.getMsgSender().connectBluetoothComm();

        // WHEN: Issuer: DM's CS generate the self_access_u_ticket to herself
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", this.agentOrServer.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_ACCESS_UTICKET);
        generatedRequest.put("taskScope", generatedTaskScope);

        this.agentOrServer.getFlowIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId,generatedRequest);

        // WHEN: Holder: DM's CS forward the self_access_u_ticket to Uninitialized IoTD
        String generatedCommand = "HELLO-1";
        this.agentOrServer.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);

        // WHEN: Receive/Send Message in Connection
        this.agentOrServer.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish CR-KE~~)
        this.agentOrServer.getMsgSender().closeBluetoothConnection();
        return this.agentOrServer;
    }

    public void issueAccessTicketThroughSimulatedComm(String targetDeviceId, DeviceController newAccessor) {
        // WHEN: Issuer: DO's UA generate & send the access_u_ticket to EP's CS
        Environment.COMMUNICATION_CHANNEL = "SIMULATED";
        Conftest.createSimulatedCommConnection(this.agentOrServer, newAccessor);

        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", newAccessor.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", generatedTaskScope);

        this.agentOrServer.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        Conftest.waitSimulatedCommCompleted(newAccessor, this.agentOrServer);
    }

    public DeviceController applyAccessTicketThroughBluetooth(String targetDeviceId) {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        this.agentOrServer.getMsgSender().connectBluetoothComm();

        // WHEN: Holder: EP's CS apply the access_u_ticket to IoTD
        String generatedCommand = "HELLO-1";
        this.agentOrServer.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId,generatedCommand);

        // WHEN: Receive/Send Message in Connection
        this.agentOrServer.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish CR-KE~~)
        this.agentOrServer.getMsgSender().closeBluetoothConnection();
        return this.agentOrServer;
    }

    public DeviceController applyCmdTokenThroughBluetooth(String targetDeviceId) {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        this.agentOrServer.getMsgSender().connectBluetoothComm();

        // WHEN: Holder: EP's CS apply the access_u_ticket to IoTD
        String generatedCommand = "HELLO-2";
        this.agentOrServer.getFlowIssueUToken().holderSendCmd(targetDeviceId,generatedCommand,false);

        // WHEN: Receive/Send Message in Connection
        this.agentOrServer.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish PS~~)
        this.agentOrServer.getMsgSender().closeBluetoothConnection();
        return this.agentOrServer;
    }

    public DeviceController applyAccessEndTokenThroughBluetooth(String targetDeviceId) {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        this.agentOrServer.getMsgSender().connectBluetoothComm();

        // WHEN: Holder: EP's CS apply the access_u_ticket to IoTD
        String generatedCommand = "ACCESS_END";
        this.agentOrServer.getFlowIssueUToken().holderSendCmd(targetDeviceId,generatedCommand,true);

        // WHEN: Receive/Send Message in Connection
        this.agentOrServer.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish PS~~)
        this.agentOrServer.getMsgSender().closeBluetoothConnection();
        return this.agentOrServer;
    }

    public void returnRTicketThroughSimulatedComm(String targetDeviceId, DeviceController originalIssuer) {
        // WHEN: Holder: Holder return the r_ticket to Issuer
        Environment.COMMUNICATION_CHANNEL = "SIMULATED";
        Conftest.createSimulatedCommConnection(originalIssuer, this.agentOrServer);
        this.agentOrServer.getFlowIssueUTicket().holderSendRTicketToIssuer(targetDeviceId);

        Conftest.waitSimulatedCommCompleted(originalIssuer, this.agentOrServer);
    }

    // Insecure Mode
    public DeviceController applyInsecureCmdThroughBluetooth(String option) {
        // WHEN: Connect bluetooth connection with IoTD
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
        this.agentOrServer.getMsgSender().connectBluetoothComm();

        // WHEN: UA or CS send the insecure_cmd to IoTD

        // Start Process Measurement
        this.agentOrServer.getMeasureHelper().measureProcessPerfStart();

        String insecureCmdJson;
        if (option.equals("shortest")) {
            insecureCmdJson = "HELLO";
        } else if (option.equals("withDeviceId")) {
            Map<String, String> insecureCmdDict = new HashMap<>();
            insecureCmdDict.put("protocolVersion", "UREKA-1.0");
            insecureCmdDict.put("deviceId", "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEuWt9xdWLXffJE-CydWYBTH05kv7xFmMGl-L3DT_7-YH2ocgHJWUUAPxQjjRBQGOeITMandJxLDye7jK8W26GmA==");
            insecureCmdDict.put("insecureCommand", "HELLO");

            insecureCmdJson = SerializationUtil.dictToJsonStr(insecureCmdDict);
        } else if (option.equals("uTicketSize")) {
            Map<String, String> insecureCmdDict = new HashMap<>();
            insecureCmdDict.put("protocolVersion", "UREKA-1.0");
            insecureCmdDict.put("deviceId", "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEuWt9xdWLXffJE-CydWYBTH05kv7xFmMGl-L3DT_7-YH2ocgHJWUUAPxQjjRBQGOeITMandJxLDye7jK8W26GmA==");
            insecureCmdDict.put("insecureCommand", "HELLO".repeat(150));

            insecureCmdJson = SerializationUtil.dictToJsonStr(insecureCmdDict);
        }

        this.agentOrServer.getSharedData().getConnectionSocket().sendMessage(insecureCmdJson);
        SimpleLogger.simpleLog("cli", "Sent Command: " + insecureCmdJson);

        // End Process Measurement
        this.agentOrServer.getMeasureHelper().measureRecvCliPerfTime("holderApplyInsecureCmd");
        // Start Comm Measurement
        if(!this.agentOrServer.getSharedData().getThisDevice().getDeviceName().equals("iotDevice")) {
            this.agentOrServer.getMeasureHelper().measureCommPerfStart();
        }

        // WHEN: UA or CS receive the insecure_data from IoTD
        try {
            // Start Comm Measurement
            this.agentOrServer.getMeasureHelper().measureCommPerfStart();

            // This will block until message is received
            String insecureDataJson = this.agentOrServer.getSharedData().getConnectionSocket().recvMessage();

            // End Comm Measurement
            this.agentOrServer.getMeasureHelper().measureCommTime("_recvMessage");

            // Message Size Measurement
            this.agentOrServer.getMeasureHelper().measureMessageSize("_recvMessage", insecureDataJson);

            SimpleLogger.simpleLog("cli", "Received Data: " + insecureDataJson);

            // Start Process Measurement
            this.agentOrServer.getMeasureHelper().measureProcessPerfStart();

            // WHEN: UA/CS do data processing
            // End Process Measurement
            this.agentOrServer.getMeasureHelper().measureRecvMsgPerfTime("_holderRecvInsecureData");

            // Start Comm Measurement
            if(!this.agentOrServer.getSharedData().getThisDevice().getDeviceName().equals("iotDevice")) {
                this.agentOrServer.getMeasureHelper().measureCommPerfStart();
                SimpleLogger.simpleLog("debug", "+ Finish CMD-DATA~~ (holder)");
            }
        } catch (Exception e) {
            SimpleLogger.simpleLog("cli", "");
            SimpleLogger.simpleLog("cli", "Connection is closed by peer.");
        }

        // RE-GIVEN: Close bluetooth connection with IoTD (Finish CMD-DATA~~)
        this.agentOrServer.getMsgSender().closeBluetoothConnection();
        return this.agentOrServer;
    }

}

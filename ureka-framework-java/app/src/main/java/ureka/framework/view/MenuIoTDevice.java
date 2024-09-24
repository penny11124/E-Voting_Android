package ureka.framework.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ureka.framework.Environment;
import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MenuIoTDevice {
    // Environment
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

    private DeviceController iotDevice;

    // Secure Mode
    public MenuIoTDevice(String deviceName) {
        // GIVEN: Load IoTD
        this.iotDevice = new DeviceController(ThisDevice.IOT_DEVICE,deviceName);
    }

    public DeviceController getIotDevice() {
        return this.iotDevice;
    }

    public DeviceController receiveUTicketThroughBluetooth() {
        // WHEN: Accept bluetooth connection from UA or CS
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
//        this.iotDevice.getMsgReceiver().acceptBluetoothComm();

        // WHEN: Receive/Send Message in Connection
        this.iotDevice.getMsgReceiver()._recvXxxMessage();

        // RE-GIVEN: Close bluetooth Connection with UA or CS
//        this.iotDevice.getMsgReceiver().closeBluetoothConnection();

        // RE-GIVEN: Stop Accepting New bluetooth Connections from UA or CS
//        this.iotDevice.getMsgReceiver().closeBluetoothAcception();
        return this.iotDevice;
    }

    // Insecure Mode
    public DeviceController receiveInsecureCmdThroughBluetooth(String option) {
        // WHEN: Accept bluetooth connection from UA or CS
        Environment.COMMUNICATION_CHANNEL = "BLUETOOTH";
//        this.iotDevice.getMsgReceiver().acceptBluetoothComm();

        // WHEN: IoTD receive the insecure_cmd from UA or CS
        while (true) {
            try {
                // This will block until message is received
                String insecureCmdJson = "";
//                insecureCmdJson = this.iotDevice.getSharedData().getConnectionSocket().recvMessage();

                // Message Size Measurement
                this.iotDevice.getMeasureHelper().measureMessageSize("_recvMessage",insecureCmdJson);
                SimpleLogger.simpleLog("cli", "Received Command: " + insecureCmdJson);

                // Start Process Measurement
                this.iotDevice.getMeasureHelper().measureProcessPerfStart();

                // WHEN: IoTD do data processing
                String insecureDataJson;
                if (option.equals("shortest")) {
                    insecureDataJson = "Data: " + insecureCmdJson;
                } else {
                    Map<String, String> insecureCmdDict = SerializationUtil.jsonStrToDict(insecureCmdJson);
                    Map<String, String> insecureDataDict = new HashMap<>();
                    insecureDataDict.put("protocolVersion", insecureCmdDict.get("protocolVersion"));
                    insecureDataDict.put("deviceId", insecureCmdDict.get("deviceId"));
                    insecureDataDict.put("insecureDataResponse", "Data: " + insecureCmdDict.get("insecureCommand"));

                    insecureDataJson = SerializationUtil.dictToJsonStr(insecureDataDict);
                }
                // WHEN: IoTD return the insecure_data to UA or CS
//                this.iotDevice.getSharedData().getConnectionSocket().sendMessage(insecureDataJson);
                SimpleLogger.simpleLog("cli","Sent Data: " + insecureDataJson);

                // End Process Measurement
                this.iotDevice.getMeasureHelper().measureRecvMsgPerfTime("_deviceRecvInsecureCmd");

                // Start Comm Measurement
                if(!this.iotDevice.getSharedData().getThisDevice().getDeviceName().equals("iotDevice")) {
                    this.iotDevice.getMeasureHelper().measureCommPerfStart();
                    SimpleLogger.simpleLog("debug", this.iotDevice.getSharedData().getThisDevice().getDeviceName() + " manually finish CMD-DATA~~ (device)");
                }
//            } catch (IOException e) {
//                SimpleLogger.simpleLog("cli", "");
//                SimpleLogger.simpleLog("cli", "+ Connection is closed by peer.");
//                break;
            } catch (RuntimeException e) {
                SimpleLogger.simpleLog("cli", "");
                SimpleLogger.simpleLog("cli", "+ Connection is closed by peer.");
                break;
            }
        }
        // RE-GIVEN: Close bluetooth connection with UA or CS
//        this.iotDevice.getMsgReceiver().closeBluetoothConnection();
        // RE-GIVEN: Stop Accepting New bluetooth Connections from UA or CS
//        this.iotDevice.getMsgReceiver().closeBluetoothAcception();
        return this.iotDevice;
    }
}

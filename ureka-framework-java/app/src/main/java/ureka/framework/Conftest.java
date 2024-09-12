package ureka.framework;
import java.util.HashMap;
import java.util.Map;

import ureka.framework.logic.DeviceController;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.Triple;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class Conftest {
    // Helper Functions

    // Get Current Class Name
    public static String getCurrentClassName() {
        try {
            return new Throwable().getStackTrace()[1].getClassName();
        } catch (Exception e) {
            SimpleLogger.simpleLog("warning","Failed to get class name.");
            return null;
        }
    }

    // Get Current Function Name called by the test
    public static String getCurrentFunctionName() {
        try {
            return new Throwable().getStackTrace()[1].getMethodName();
        } catch (Exception e) {
            SimpleLogger.simpleLog("warning","Failed to get function name.");
            return null;
        }
    }

    // Log
    public static void currentSetupLog() {
        SimpleLogger.simpleLog("info","");
        String className = getCurrentClassName();
        if (className != null) {
            SimpleLogger.simpleLog("info","*".repeat(100));
            SimpleLogger.simpleLog("info","Setup: " + className);
            SimpleLogger.simpleLog("info","*".repeat(100));
        }
    }

    // Log
    public static void currentTestGivenLog() {
        String functionName = getCurrentFunctionName();
        if (!"_hookexec".equals(functionName) && functionName != null) {
            SimpleLogger.simpleLog("info","*".repeat(50));
            SimpleLogger.simpleLog("info","Given: " + functionName);
            SimpleLogger.simpleLog("info","*".repeat(50));
        }
    }

    // Log
    public static void currentTestWhenAndThenLog() {
        String functionName = getCurrentFunctionName();
        if (!"_hookexec".equals(functionName) && functionName != null) {
            SimpleLogger.simpleLog("info","*".repeat(50));
            SimpleLogger.simpleLog("info","When & Then: " + functionName);
            SimpleLogger.simpleLog("info","*".repeat(50));
        }
    }

    // Log
    public static void currentTeardownLog() {
        String className = getCurrentClassName();
        if (className != null) {
            SimpleLogger.simpleLog("info","*".repeat(100));
            SimpleLogger.simpleLog("info","Teardown: " + className);
            SimpleLogger.simpleLog("info","*".repeat(100));
        }
    }

    // Helper Functions (Simulated Comm)
    public static void createSimulatedCommConnection(DeviceController end1, DeviceController end2) {
        // Set Sender (on Main Thread) & Start Receiver Thread
        end1.getMsgReceiver().createSimulatedCommConnection(end2);
        end2.getMsgReceiver().createSimulatedCommConnection(end1);

        SimpleLogger.simpleLog("info", String.format("+ Connection between %s and %s is started...",
                end1.getSharedData().getThisDevice().getDeviceName(),
                end2.getSharedData().getThisDevice().getDeviceName()));
    }

    // Wait for all sender/receiver to finish their works (block last 1st make log beautiful)
    public static void waitSimulatedCommCompleted(DeviceController end1, DeviceController end2) {
        try {
            end1.getMsgSender().waitSimulatedCommCompleted();
            end2.getMsgSender().waitSimulatedCommCompleted();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        SimpleLogger.simpleLog("info",String.format("+ Connection between %s and %s is completed...",
                end1.getSharedData().getThisDevice().getDeviceName(),
                end2.getSharedData().getThisDevice().getDeviceName()));
        SimpleLogger.simpleLog("info","");
        SimpleLogger.simpleLog("info","");
        SimpleLogger.simpleLog("info","");
    }

    // Helper Functions (Reusable Test Data)
    public static DeviceController deviceManufacturerServer() {
        DeviceController cloudServerDM = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "cloudServerDM");
        cloudServerDM.getExecutor()._executeOneTimeInitializeAgentOrServer();

        return cloudServerDM;
    }

    public static Pair deviceManufacturerServerAndHerDevice() {
        // GIVEN: Initialized DM's CS
        DeviceController cloudServerDM = deviceManufacturerServer();

        // GIVEN: Uninitialized IoTD
        DeviceController iotDevice = new DeviceController(ThisDevice.IOT_DEVICE, "iot_device");

        // WHEN: Issuer: DM's CS generate & send the initialization_u_ticket to Uninitialized IoTD
        createSimulatedCommConnection(cloudServerDM, iotDevice);
        String idForInitializationUTicket = "noId";
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", idForInitializationUTicket);
        generatedRequest.put("holderId", cloudServerDM.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_INITIALIZATION_UTICKET);

        cloudServerDM.getFlowIssueUTicket().issuerIssueUTicketToHerself(idForInitializationUTicket, generatedRequest);
        cloudServerDM.getFlowApplyUTicket().holderApplyUTicket(idForInitializationUTicket);

        waitSimulatedCommCompleted(cloudServerDM, iotDevice);

        return new Pair(cloudServerDM, iotDevice);
    }

    public static DeviceController deviceOwnerAgent() {
        // GIVEN: Initialized DM's CS
        DeviceController userAgentDO = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "userAgentDO");
        userAgentDO.getExecutor()._executeOneTimeInitializeAgentOrServer();

        return userAgentDO;
    }

    public static Pair deviceOwnerAgentAndHerDevice() {
        // GIVEN: Initialized DM's CS and DM's IoTD
        Pair cloudServerDMAndIoT = deviceManufacturerServerAndHerDevice();
        DeviceController cloudServerDM = (DeviceController) cloudServerDMAndIoT.getPairFirst();
        DeviceController iotDevice = (DeviceController) cloudServerDMAndIoT.getPairSecond();

        // GIVEN: Initialized DO's UA
        DeviceController userAgentDO = deviceOwnerAgent();

        // WHEN: Issuer: DM's CS generate & send the ownership_u_ticket to DO's UA
        createSimulatedCommConnection(cloudServerDM, userAgentDO);
        String targetDeviceId = iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_OWNERSHIP_UTICKET);

        cloudServerDM.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        waitSimulatedCommCompleted(userAgentDO, cloudServerDM);

        // WHEN: Holder: DO's UA forward the ownership_u_ticket
        createSimulatedCommConnection(userAgentDO, iotDevice);
        userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId);
        waitSimulatedCommCompleted(userAgentDO, iotDevice);

        return new Pair(userAgentDO, iotDevice);
    }

    public static Pair deviceOwnerAgentAndHerSession() {
        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair deviceTuple = deviceOwnerAgentAndHerDevice();
        DeviceController userAgentDO = (DeviceController) deviceTuple.getPairFirst();
        DeviceController iotDevice = (DeviceController) deviceTuple.getPairSecond();

        // WHEN:
        currentTestWhenAndThenLog();

        // WHEN: Issuer: DO's UA generate the self_access_u_ticket to herself
        String targetDeviceId = iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", userAgentDO.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_SELFACCESS_UTICKET);
        generatedRequest.put("taskScope", generatedTaskScope);

        userAgentDO.getFlowIssueUTicket().issuerIssueUTicketToHerself(targetDeviceId, generatedRequest);

        // WHEN: Holder: DO's UA forward the self_access_u_ticket
        createSimulatedCommConnection(userAgentDO, iotDevice);
        String generatedCommand = "HELLO-1";
        userAgentDO.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        waitSimulatedCommCompleted(userAgentDO, iotDevice);

        return new Pair(userAgentDO, iotDevice);
    }

    // GIVEN: Initialized EP's CS
    public static DeviceController enterpriseProviderServer() {
        DeviceController cloudServerEP = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "cloudServerEp");
        cloudServerEP.getExecutor()._executeOneTimeInitializeAgentOrServer();

        return cloudServerEP;
    }

    public static Triple enterpriseProviderServerAndHerSession() {
        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair deviceTuple = deviceOwnerAgentAndHerDevice();
        DeviceController userAgentDO = (DeviceController) deviceTuple.getPairFirst();
        DeviceController iotDevice = (DeviceController) deviceTuple.getPairSecond();

        // GIVEN: Initialized EP's CS
        DeviceController cloudServerEP = enterpriseProviderServer();

        // WHEN: Issuer: DO's UA generate & send the access_u_ticket to EP's CS
        createSimulatedCommConnection(userAgentDO, cloudServerEP);
        String targetDeviceId = iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of("ALL", "allow"));
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_ACCESS_UTICKET);
        generatedRequest.put("taskScope", generatedTaskScope);

        userAgentDO.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        waitSimulatedCommCompleted(cloudServerEP, userAgentDO);

        // WHEN: Holder: EP's CS forward the access_u_ticket
        createSimulatedCommConnection(cloudServerEP, iotDevice);
        String generatedCommand = "HELLO-1";
        cloudServerEP.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        waitSimulatedCommCompleted(cloudServerEP, iotDevice);

        return new Triple(userAgentDO, cloudServerEP, iotDevice);
    }

    public static Triple enterpriseProviderServerAndHerLimitedSession() {
        // GIVEN: Initialized DO's UA and DO's IoTD
        Pair deviceTuple = deviceOwnerAgentAndHerDevice();
        DeviceController userAgentDO = (DeviceController) deviceTuple.getPairFirst();
        DeviceController iotDevice = (DeviceController) deviceTuple.getPairSecond();

        // GIVEN: Initialized EP's CS
        DeviceController cloudServerEP = enterpriseProviderServer();

        // WHEN: Issuer: DO's UA generate & send the access_u_ticket to EP's CS
        createSimulatedCommConnection(userAgentDO, cloudServerEP);
        String targetDeviceId = iotDevice.getSharedData().getThisDevice().getDevicePubKeyStr();
        String generatedTaskScope = SerializationUtil.dictToJsonStr(Map.of(
                "SAY-HELLO-1", "allow",
                "SAY-HELLO-2", "allow",
                "SAY-HELLO-3", "forbid"
        ));
        Map<String, String> generatedRequest = new HashMap<>();
        generatedRequest.put("deviceId", targetDeviceId);
        generatedRequest.put("holderId", cloudServerEP.getSharedData().getThisPerson().getPersonPubKeyStr());
        generatedRequest.put("uTicketType", UTicket.TYPE_ACCESS_UTICKET);
        generatedRequest.put("taskScope", generatedTaskScope);

        userAgentDO.getFlowIssueUTicket().issuerIssueUTicketToHolder(targetDeviceId, generatedRequest);
        waitSimulatedCommCompleted(cloudServerEP, userAgentDO);

        // WHEN: Holder: EP's CS forward the access_u_ticket
        createSimulatedCommConnection(cloudServerEP, iotDevice);
        String generatedCommand = "HELLO-1";
        cloudServerEP.getFlowApplyUTicket().holderApplyUTicket(targetDeviceId, generatedCommand);
        waitSimulatedCommCompleted(cloudServerEP, iotDevice);

        return new Triple(userAgentDO, cloudServerEP, iotDevice);
    }

    public static DeviceController attackerServer() {
        // GIVEN: Initialized ATK's CS
        DeviceController cloudServerATK = new DeviceController(ThisDevice.USER_AGENT_OR_CLOUD_SERVER, "cloudServerATK");
        cloudServerATK.getExecutor()._executeOneTimeInitializeAgentOrServer();

        return cloudServerATK;
    }

    // Fixture
}

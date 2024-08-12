package ureka.framework.logic.pipeline_flow.stage_worker;

import java.util.HashMap;

import ureka.framework.Environment;
import ureka.framework.model.SharedData;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

public class MeasureHelper {
    private SharedData sharedData;

    public MeasureHelper(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public SharedData getSharedData() {
        return sharedData;
    }

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    //////////////////////////////////////////////////////
    // Measurement Helper:
    // Process Response Time
    //////////////////////////////////////////////////////
    public void measureProcessPerfStart() {
        SimpleMeasurer.measureWorkerFunc(this::_measureProcessPerfStart);
    }
    private void _measureProcessPerfStart() {
        SimpleMeasurer.startProcessTimer();
        SimpleMeasurer.startPerfTimer();
    }

    public void measureRecvCliPerfTime(String cliName) {
        SimpleMeasurer.measureWorkerFunc(this::_measureRecvCliPerfTime, cliName);
    }
    private void _measureRecvCliPerfTime(String cliName) {
        // Response Time
        long cliProcessTime = SimpleMeasurer.getProcessTime();
        long cliPerfTime = SimpleMeasurer.getPerfTime();
        long cliBlockedTime = Math.abs(cliPerfTime - cliProcessTime);

        // Collect Measurement Raw Data
        this.sharedData.getMeasureRec().computeIfAbsent(cliName, k -> new HashMap<>());

        // Record cliPerfTime
        this.sharedData.getMeasureRec().get(cliName).put("cliPerfTime", cliPerfTime);
        this.sharedData.getMeasureRec().get(cliName).put("cliBlockedTime", cliBlockedTime);
        // simpleLog("measure", f"measureRec = {self.sharedData.measureRec}")

        // Print Measurement Raw Data
        SimpleLogger.simpleLog("measure", "");
        SimpleLogger.simpleLog("measure", "+ Receive CLI Input: {" + cliName + "}");
        SimpleLogger.simpleLog("measure", "cliPerfTime = " + cliPerfTime + " nanoseconds");
        // "cliPerfTime = {cliPerfTime:{Environment.MEASUREMENT_TIME_PRECISION}} seconds"
        if (cliBlockedTime > Environment.IO_BLOCKING_TOLERANCE_TIME * 1e6 /*Conversion from millisecond to nanosecond*/) {
            SimpleLogger.simpleLog("warning", "+ PROC I/O MAYBE BLOCKED TOO LONG...");
            SimpleLogger.simpleLog(
                "warning",
                "cliPerfTime = " + cliBlockedTime + " nanoseconds"
            );
        }
    }

    public void measureRecvMsgPerfTime(String commName) {
        SimpleMeasurer.measureWorkerFunc(this::_measureRecvMsgPerfTime, commName);
    }
    private void _measureRecvMsgPerfTime(String commName) {
        // Response Time
        long msgProcessTime = SimpleMeasurer.getProcessTime();
        long msgPerfTime = SimpleMeasurer.getPerfTime();
        long msgBlockedTime = Math.abs(msgPerfTime - msgProcessTime);

        // Collect Measurement Raw Data
        this.sharedData.getMeasureRec().computeIfAbsent(commName, k -> new HashMap<>());

        // Record cliPerfTime
        this.sharedData.getMeasureRec().get(commName).put("msgPerfTime", msgPerfTime);
        this.sharedData.getMeasureRec().get(commName).put("msgBlockedTime", msgBlockedTime);

        // Record cached commTime & messageSize
        if (this.sharedData.getMeasureRec().get("_recvMessage") != null
            && this.sharedData.getMeasureRec().get("_recvMessage").get("commTime") != null) {
            this.sharedData.getMeasureRec().get(commName)
                .put("commTime", this.sharedData.getMeasureRec().get("_recvMessage").get("commTime"));
        }
        if (this.sharedData.getMeasureRec().get("_recvMessage") != null
            && this.sharedData.getMeasureRec().get("_recvMessage").get("messageSize") != null) {
            this.sharedData.getMeasureRec().get(commName)
                .put("messageSize", this.sharedData.getMeasureRec().get("_recvMessage").get("messageSize"));
        }
        // simpleLog("measure", f"measureRec = {self.sharedData.measureRec}")

        // Print Measurement Raw Data
        SimpleLogger.simpleLog("measure", "msgPerfTime = " + msgPerfTime + " nanoseconds");
        // "cliPerfTime = {cliPerfTime:{Environment.MEASUREMENT_TIME_PRECISION}} seconds"
        if (msgBlockedTime > Environment.IO_BLOCKING_TOLERANCE_TIME * 1e6 /*Conversion from millisecond to nanosecond*/) {
            SimpleLogger.simpleLog("warning", "+ PROC I/O MAYBE BLOCKED TOO LONG...");
            SimpleLogger.simpleLog("warning", "msgPerfTime = " + msgPerfTime + " nanoseconds");
        }
        SimpleLogger.simpleLog("measure", "+ Receive Message Input = " + commName);
        SimpleLogger.simpleLog("measure", "");
    }
    //////////////////////////////////////////////////////
    // Measurement Helper:
    // Comm Response Time
    //////////////////////////////////////////////////////
    public void measureCommPerfStart() {
        SimpleMeasurer.measureWorkerFunc(this::_measureCommPerfStart);
    }
    private void _measureCommPerfStart() {
        SimpleMeasurer.startCommTimer();
    }

    public void measureCommTime(String commName) {
        SimpleMeasurer.measureWorkerFunc(this::_measureCommTime, commName);
    }
    private void _measureCommTime(String commName) {
        // Response Time
        long commTime = SimpleMeasurer.getCommTime();

        // Collect Measurement Raw Data
        this.sharedData.getMeasureRec().computeIfAbsent(commName, k -> new HashMap<>());
        // Cache commTime
        this.sharedData.getMeasureRec().get(commName).put("commTime", commTime);
        // simpleLog("measure", f"measureRec = {self.sharedData.measureRec}")

        // Print Measurement Raw Data
        SimpleLogger.simpleLog("measure", "");
        SimpleLogger.simpleLog("measure", "+ Receive Comm Input: " + commName);
        SimpleLogger.simpleLog("measure", "commTime = " + commTime + " nanoseconds");
        if (commTime > Environment.COMM_BLOCKING_TOLERANCE_TIME * 1e6/*Conversion from millisecond to nanosecond*/) {
            SimpleLogger.simpleLog("warning", "+ COMM I/O MAYBE BLOCKED TOO LONG...");
            SimpleLogger.simpleLog("warning", "commTime = " + commTime + " nanoseconds");
        }
    }

    //////////////////////////////////////////////////////
    // Measurement Helper:
    // Data Size
    //////////////////////////////////////////////////////
    public void measureMessageSize(String commName, String receivedMessageWithHeader) {
        SimpleMeasurer.measureWorkerFunc(this::_measureMessageSize, commName, receivedMessageWithHeader);
    }
    private void _measureMessageSize(String commName, String receivedMessageWithHeader) {
        // Data Size
        int messageSize = SimpleMeasurer.simpleSizeCalculator(receivedMessageWithHeader);

        // Collect Measurement Raw Data
        this.sharedData.getMeasureRec().computeIfAbsent(commName, k -> new HashMap<>());
        // Cache messageSize
        this.sharedData.getMeasureRec().get(commName).put("messageSize", (long) messageSize);
        // simpleLog("measure", f"measureRec = {self.sharedData.measureRec}")

        // Print Measurement Raw Data
        SimpleLogger.simpleLog("measure", "messageSize = " + messageSize + " bytes");
    }
}

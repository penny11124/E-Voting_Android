package ureka.framework.logic;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.urekaapp.ble.BLEViewModel;
import com.example.urekaapp.ble.BLEManager;
import com.example.urekaapp.communication.NearbyManager;
import com.example.urekaapp.communication.NearbyViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Objects;

import ureka.framework.Environment;
import ureka.framework.logic.pipeline_flow.FlowApplyUTicket;
import ureka.framework.logic.pipeline_flow.FlowIssueUTicket;
import ureka.framework.logic.pipeline_flow.FlowIssueUToken;
import ureka.framework.logic.pipeline_flow.FlowOpenSession;
import ureka.framework.logic.stage_worker.Executor;
import ureka.framework.logic.stage_worker.GeneratedMsgStorer;
import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.logic.stage_worker.MsgGenerator;
import ureka.framework.logic.stage_worker.MsgReceiver;
import ureka.framework.logic.stage_worker.MsgSender;
import ureka.framework.logic.stage_worker.MsgVerifier;
import ureka.framework.logic.stage_worker.ReceivedMsgStorer;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.resource.communication.simulated_comm.SimulatedCommChannel;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class DeviceController {
    private SharedData sharedData;
    private SimpleStorage simpleStorage;
    private MeasureHelper measureHelper;
    private ReceivedMsgStorer receivedMsgStorer;
    private MsgVerifier msgVerifier;
    private Executor executor;
    private MsgGenerator msgGenerator;
    private GeneratedMsgStorer generatedMsgStorer;
    private MsgSender msgSender;
    private MsgReceiver msgReceiver;
    private BLEManager bleManager;
    private NearbyManager nearbyManager;
    // Flow
    private FlowIssueUTicket flowIssuerIssueUTicket;
    private FlowOpenSession flowOpenSession;
    private FlowApplyUTicket flowApplyUTicket;
    private FlowIssueUToken flowIssueUToken;

    public DeviceController(String deviceType, String deviceName) {
        this._initialize(deviceType, deviceName, Environment.applicationContext);
    }
    private void _initialize(String deviceType, String deviceName, Context context) {
        BLEViewModel bleViewModel = new ViewModelProvider((AppCompatActivity) context).get(BLEViewModel.class);
        NearbyViewModel nearbyViewModel = new ViewModelProvider((AppCompatActivity) context).get(NearbyViewModel.class);

        this.sharedData = new SharedData(new ThisDevice(), new CurrentSession(), new ThisPerson());
        this.simpleStorage = new SimpleStorage(deviceName);
        this.sharedData.setSimulatedCommChannel(new SimulatedCommChannel());
//        # Resource (Bluetooth Comm)
//        if HAS_PYBLUEZ == True:
//        self.shared_data.accept_socket: Optional[AcceptSocket] = None
//        self.shared_data.connecting_worker: Optional[ConnectingWorker] = None
//        self.shared_data.connection_socket: Optional[ConnectionSocket] = None
        this.sharedData.setMeasureRec(new HashMap<>());
        this.measureHelper = new MeasureHelper(this.sharedData);

        this.receivedMsgStorer = new ReceivedMsgStorer(this.sharedData, this.measureHelper, this.simpleStorage);
        this.msgVerifier = new MsgVerifier(this.sharedData, this.measureHelper);
        this.executor = new Executor(this.sharedData, this.measureHelper, this.simpleStorage, this.msgVerifier);
        this.msgGenerator = new MsgGenerator(this.sharedData, this.measureHelper);
        this.generatedMsgStorer = new GeneratedMsgStorer(this.sharedData, this.measureHelper, this.simpleStorage);
        this.bleManager = bleViewModel.getBLEManager(context);
        this.nearbyManager = nearbyViewModel.getNearbyManager(context, this.msgReceiver);
        this.msgSender = new MsgSender(this.sharedData, this.measureHelper, this.bleManager, this.nearbyManager);

        // Flow
        this.flowIssuerIssueUTicket = new FlowIssueUTicket(
            this.sharedData, this.measureHelper, this.receivedMsgStorer, this.msgVerifier
            , this.executor, this.msgGenerator, this.generatedMsgStorer, this.msgSender
        );
        this.flowOpenSession = new FlowOpenSession(
            this.sharedData, this.measureHelper, this.receivedMsgStorer, this.msgVerifier,
            this.executor, this.msgGenerator, this.generatedMsgStorer, this.msgSender
        );
        this.flowApplyUTicket = new FlowApplyUTicket(
            this.sharedData, this.measureHelper, this.receivedMsgStorer, this.msgVerifier,
            this.executor, this.msgGenerator, this.generatedMsgStorer, this.msgSender, this.flowOpenSession
        );
        this.flowIssueUToken = new FlowIssueUToken(
            this.sharedData, this.measureHelper, this.receivedMsgStorer, this.msgVerifier,
            this.executor, this.msgGenerator, this.generatedMsgStorer, this.msgSender, this.flowApplyUTicket
        );

        this.msgReceiver = new MsgReceiver(
            this.sharedData, this.measureHelper, this.msgVerifier, this.executor, this.msgSender,
            this.flowIssuerIssueUTicket, this.flowApplyUTicket, this.flowOpenSession, this.flowIssueUToken
        );

//        SimpleStorage.Tuple tuple = this.simpleStorage.loadStorage();
//        this.sharedData.setThisDevice(tuple.thisDevice);
//        this.sharedData.setDeviceTable(tuple.deviceTable);
//        this.sharedData.setThisPerson(tuple.thisPerson);
//        this.sharedData.setCurrentSession(tuple.currentSession);
//        # Always load Storage after Reboot
//            (
//                self.shared_data.this_device,
//                self.shared_data.device_table,
//                self.shared_data.this_person,
//                self.shared_data.current_session,
//                ) = self.simple_storage.load_storage()

        // Set Device Type (must after loading storage)
        if (!this.sharedData.getThisDevice().getHasDeviceType()) {
            this.executor._executeOneTimeSetTimeDeviceTypeAndName(deviceType, deviceName);
        }
        // Set Initialized State
        this.executor._initializeState();

        SimpleLogger.simpleLog("info", "Here is a " + this.sharedData.getThisDevice().getDeviceName() + "...");
    }

    //////////////////////////////////////////////////////
    // Device Activity Cycle
    //////////////////////////////////////////////////////
    public void rebootDevice() {
        SimpleLogger.simpleLog("info", "Reboot " + this.sharedData.getThisDevice().getDeviceName() + "...");
        this._initialize(this.sharedData.getThisDevice().getDeviceType(), this.sharedData.getThisDevice().getDeviceName(), Environment.applicationContext);
    }

    public SharedData getSharedData() {
        return this.sharedData;
    }
    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    public void setSimpleStorage(SimpleStorage simpleStorage) {
        this.simpleStorage = simpleStorage;
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

    public MsgReceiver getMsgReceiver() {
        return msgReceiver;
    }

    public void setMsgReceiver(MsgReceiver msgReceiver) {
        this.msgReceiver = msgReceiver;
    }

    public FlowIssueUTicket getFlowIssuerIssueUTicket() {
        return flowIssuerIssueUTicket;
    }

    public void setFlowIssuerIssueUTicket(FlowIssueUTicket flowIssuerIssueUTicket) {
        this.flowIssuerIssueUTicket = flowIssuerIssueUTicket;
    }

    public FlowOpenSession getFlowOpenSession() {
        return flowOpenSession;
    }

    public void setFlowOpenSession(FlowOpenSession flowOpenSession) {
        this.flowOpenSession = flowOpenSession;
    }

    public FlowApplyUTicket getFlowApplyUTicket() {
        return flowApplyUTicket;
    }

    public void setFlowApplyUTicket(FlowApplyUTicket flowApplyUTicket) {
        this.flowApplyUTicket = flowApplyUTicket;
    }

    public FlowIssueUToken getFlowIssueUToken() {
        return flowIssueUToken;
    }

    public void setFlowIssueUToken(FlowIssueUToken flowIssueUToken) {
        this.flowIssueUToken = flowIssueUToken;
    }

    public void connectToDevice(String deviceName, Runnable onConnected, Runnable onDisconnected, TextView textView) {
        new Thread(() -> {
            this.bleManager.startScan(deviceName, new BLEManager.BLECallback() {
                final StringBuilder jsonBuilder = new StringBuilder();
                @Override
                public void onConnected() {
                    SimpleLogger.simpleLog("info", "Device connected!");
                    new Handler(Looper.getMainLooper()).post(() ->
                            textView.setText("Device connected!")
                    );
                    onConnected.run();
                }

                @Override
                public void onDisconnected() {
                    SimpleLogger.simpleLog("info", "Device disconnected!");
                    new Handler(Looper.getMainLooper()).post(() ->
                            textView.setText("Device disconnected!")
                    );
                    onDisconnected.run();
                }

                @Override
                public void onDataReceived(String data) {
                    SimpleLogger.simpleLog("info", "Received data: " + data);
//                    new Handler(Looper.getMainLooper()).post(() ->
//                            textView.setText("Data received from voting machine.")
//                    );
                    jsonBuilder.append(data);

                    if (data.contains("freed")) {
                        SimpleLogger.simpleLog("info", "No hello thank you");
                        jsonBuilder.setLength(0);
                    } else if (data.contains("$")) {
                        Log.d("Bluetooth.onDataReceived", "Received complete JSON: starting process.");
                        jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
                        msgReceiver._recvXxxMessage(jsonBuilder.toString());
                        jsonBuilder.setLength(0);
                    } else {
                        Log.d("Bluetooth.onDataReceived", "Waiting for more data to complete JSON.");
                    }
                }
            });
        }).start();
    }
}

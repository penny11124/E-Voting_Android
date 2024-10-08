package ureka.framework.logic;

import java.util.HashMap;

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
    // Flow
    private FlowIssueUTicket flowIssuerIssueUTicket;
    private FlowOpenSession flowOpenSession;
    private FlowApplyUTicket flowApplyUTicket;
    private FlowIssueUToken flowIssueUToken;

    public DeviceController(String deviceType, String deviceName) {
        this._initialize(deviceType, deviceName);
    }
    private void _initialize(String deviceType, String deviceName) {
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
        this.msgSender = new MsgSender(this.sharedData, this.measureHelper);

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
        this._initialize(this.sharedData.getThisDevice().getDeviceType(), this.sharedData.getThisDevice().getDeviceName());
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
}
// TODO:
// 1. Handle with SimulatedCommChannel initialization at Line 42.
// 2. Handle with Bluetooth initialization at Line 43~47.

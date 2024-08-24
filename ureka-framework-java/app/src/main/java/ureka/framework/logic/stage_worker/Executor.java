package ureka.framework.logic.stage_worker;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.storage.SimpleStorage;

public class Executor {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private SimpleStorage simpleStorage;
    private MsgVerifier msgVerifier;
    public Executor(SharedData sharedData, MeasureHelper measureHelper, SimpleStorage simpleStorage, MsgVerifier msgVerifier) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
        this.simpleStorage = simpleStorage;
        this.msgVerifier = msgVerifier;
    }

//    // [STAGE: (E)] Execute
//    // Initialize state based on device type
    public boolean initializeState() {
        // [STAGE: (C)]
        if(this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.IOT_DEVICE)) {
            this._changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_UT);
        } else if (this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.USER_AGENT_OR_CLOUD_SERVER)) {
            this._changeState(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT);
        }
        return true;
    }

    // Execute Initialization (Update Keystore)
    public boolean executeOneTimeSetDeviceTypeAndName(String deviceType, String deviceName) {
        /* Determine device type name, but still be uninitialized
           Determine device name (for test)
         */
        this.sharedData.getThisDevice().setDeviceType(deviceType);
        this.sharedData.getThisDevice().setDeviceName(deviceName);
        this.sharedData.getThisDevice().setHasDeviceType(true);

        // Initial Order
        // [STAGE: (O)]
        this._executeUpdateTicketOrder("hasType");

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );
        return true;
    }

    public void executeOneTimeInitializeAgentOrServer() {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();

        SimpleLogger.simpleLog("info","+ " + this.sharedData.getThisDevice().getDeviceName() + " is initializing...");

        /*
        TODO: New way for _execute_one_time_intialize_agent_or_server()
                 + DM: Apply Initialization Ticket
                 + DM: Apply Personal Key Gen Ticket
                 + DO: Generate Personal Key
                 + DO: Request Ownership Ticket from DM
         */
        if(!this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.USER_AGENT_OR_CLOUD_SERVER)) {
            // FAILURE: (VRESET)
            String failureMsg = "-> FAILURE: ONLY USER-AGENT-OR-CLOUD-SERVER CAN DO THIS INITIALIZATION OPERATION";
            SimpleLogger.simpleLog("error",failureMsg);
            throw new RuntimeException(failureMsg);
        }
        if(this.sharedData.getThisDevice().getTicketOrder() != 0) {
            // FAILURE: (VUT)
            String failureMsg = "-> FAILURE: VERIFY_TICKET_ORDER: USER-AGENT-OR-CLOUD-SERVER ALREADY INITIALIZED";
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }

        // Initialize Device Id
        // CRYPTO
        KeyPair deviceKeyPair;
        try {
            deviceKeyPair = ECC.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        // RAM
        this.sharedData.getThisDevice().setDevicePrivKey(deviceKeyPair.getPrivate());
        this.sharedData.getThisDevice().setDevicePubKey(deviceKeyPair.getPublic());

        // Initialize Personal Id
        // CRYPTO
        KeyPair personKeyPair = null;
        try {
            personKeyPair = ECC.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        this.sharedData.getThisPerson().setPersonPrivKey(personKeyPair.getPrivate());
        this.sharedData.getThisPerson().setPersonPubKey(personKeyPair.getPublic());

        // Initialize Device Owner
        // RAM
        this.sharedData.getThisDevice().setOwnerPubKey(this.sharedData.getThisPerson().getPersonPubKey());

        // [STAGE: (O)]
        this._executeUpdateTicketOrder("agentInitialization");

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );

        // End Process Measurement
        this.measureHelper.measureRecvCliPerfTime("executeOneTimeInitializeAgentOrServer");
    }

    // Execute UTicket (Update Keystore, Session, & Ticket Order)
    public void executeUTicket(UTicket uTicketIn) {
        try {
            if(uTicketIn.getUTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)) {
                // [STAGE: (E)]
                this._executeOneTimeInitializeIOTDevice(uTicketIn);
                // [STAGE: (O)]
                this._executeUpdateTicketOrder("deviceVerifyUTicket", uTicketIn);
            } else if (uTicketIn.getUTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // [STAGE: (E)]
                this._executeOwnershipTransfer(uTicketIn);
                // [STAGE: (O)]
                this._executeUpdateTicketOrder("deviceVerifyUTicket", uTicketIn);
            } else if (uTicketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_UTICKET) ||
                    uTicketIn.getUTicketType().equals(UTicket.TYPE_SELFACCESS_UTICKET)) {
                // [STAGE: (E)]
                this._executeCRKE(uTicketIn,"device");
            }
        } catch (RuntimeException error) {
            SimpleLogger.simpleLog("error", error.getMessage());
            throw error;
        }

    }

    private void _executeOwnershipTransfer(UTicket uTicketIn) {
    }

    private void _executeOneTimeInitializeIOTDevice(UTicket uTicketIn) {
    }

    private void _executeUpdateTicketOrder(String hasType) {
    }

    private void _changeState(String stateDeviceWaitForUt) {
    }
}

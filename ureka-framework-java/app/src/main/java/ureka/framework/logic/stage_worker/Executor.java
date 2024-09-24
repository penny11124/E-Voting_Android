package ureka.framework.logic.stage_worker;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

import javax.crypto.AEADBadTagException;

import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.Pair;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;
import ureka.framework.resource.storage.SimpleStorage;

public class Executor {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private SimpleStorage simpleStorage;
    private MsgVerifier msgVerifier;

    private static final int IV_LENGTH = 12; // 96 bits
    public Executor(SharedData sharedData, MeasureHelper measureHelper, SimpleStorage simpleStorage, MsgVerifier msgVerifier) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
        this.simpleStorage = simpleStorage;
        this.msgVerifier = msgVerifier;
    }

    // [STAGE: (E)] Execute
    // Initialize state based on device type
    public boolean _initializeState() {
        // [STAGE: (C)]
        if(this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.IOT_DEVICE)) {
            this._changeState(ThisDevice.STATE_DEVICE_WAIT_FOR_UT);
        } else if (this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.USER_AGENT_OR_CLOUD_SERVER)) {
            this._changeState(ThisDevice.STATE_AGENT_WAIT_FOR_UREQ_UREJ_UT_RT);
        }
        return true;
    }

    // Execute Initialization (Update Keystore)
    public boolean _executeOneTimeSetTimeDeviceTypeAndName(String deviceType, String deviceName) {
        /* Determine device type name, but still be uninitialized
           Determine device name (for test)
         */
        this.sharedData.getThisDevice().setDeviceType(deviceType);
        this.sharedData.getThisDevice().setDeviceName(deviceName);
        this.sharedData.getThisDevice().setHasDeviceType(true);

        // Initial Order
        // [STAGE: (O)]
        this.executeUpdateTicketOrder("hasType",null);

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );
        return true;
    }

    public void _executeOneTimeInitializeAgentOrServer() {
        // Start Process Measurement
        this.measureHelper.measureProcessPerfStart();

        SimpleLogger.simpleLog("info","+ " + this.sharedData.getThisDevice().getDeviceName() + " is initializing...");

        /*
        TODO: New way for _execute_one_time_initialize_agent_or_server()
                 + DM: Apply Initialization Ticket
                 + DM: Apply Personal Key Gen Ticket
                 + DO: Generate Personal Key
                 + DO: Request Ownership Ticket from DM
         */
        if (!this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.USER_AGENT_OR_CLOUD_SERVER)) {
            // FAILURE: (VRESET)
            String failureMsg = "-> FAILURE: ONLY USER-AGENT-OR-CLOUD-SERVER CAN DO THIS INITIALIZATION OPERATION";
            SimpleLogger.simpleLog("error",failureMsg);
            throw new RuntimeException(failureMsg);
        }
        if (this.sharedData.getThisDevice().getTicketOrder() != 0) {
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
        this.sharedData.getThisDevice().setDevicePrivKey((ECPrivateKey) deviceKeyPair.getPrivate());
        this.sharedData.getThisDevice().setDevicePubKey((ECPublicKey) deviceKeyPair.getPublic());

        // Initialize Personal Id
        // CRYPTO
        KeyPair personKeyPair = null;
        try {
            personKeyPair = ECC.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        this.sharedData.getThisPerson().setPersonPrivKey((ECPrivateKey) personKeyPair.getPrivate());
        this.sharedData.getThisPerson().setPersonPubKey((ECPublicKey) personKeyPair.getPublic());

        // Initialize Device Owner
        // RAM
        this.sharedData.getThisDevice().setOwnerPubKey(this.sharedData.getThisPerson().getPersonPubKey());

        // [STAGE: (O)]
        this.executeUpdateTicketOrder("agentInitialization",null);

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );

        // End Process Measurement
        this.measureHelper.measureRecvCliPerfTime("_executeOneTimeInitializeAgentOrServer");
    }

    // Execute UTicket (Update Keystore, Session, & Ticket Order)
    public void executeXxxUTicket(UTicket uTicketIn) {
        SimpleMeasurer.measureWorkerFunc(this::_executeXxxUTicket, uTicketIn);
    }
    private void _executeXxxUTicket(UTicket uTicketIn) {
        try {
            if (uTicketIn.getUTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)) {
                // [STAGE: (E)]
                this._executeOneTimeInitializeIotDevice(uTicketIn);
                // [STAGE: (O)]
                this.executeUpdateTicketOrder("deviceVerifyUTicket", uTicketIn);
            } else if (uTicketIn.getUTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // [STAGE: (E)]
                this._executeOwnershipTransfer(uTicketIn);
                // [STAGE: (O)]
                this.executeUpdateTicketOrder("deviceVerifyUTicket", uTicketIn);
            } else if (uTicketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_UTICKET) ||
                    uTicketIn.getUTicketType().equals(UTicket.TYPE_SELFACCESS_UTICKET)) {
                // [STAGE: (E)]
                this.executeCRKE(uTicketIn,"device",null);
            } else if (uTicketIn.getUTicketType().equals(UTicket.TYPE_CMD_UTOKEN) ||
                    uTicketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // [STAGE: (VTK)(VTS)]
                // [STAGE: (E)]
                // Update Session: PS-Cmd
                this.executePs("recvUToken",uTicketIn,null,null);

                // Data Processing
                Pair dataProcessed = this._executeDataProcessing(
                        this.sharedData.getCurrentSession().getPlaintextCmd(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextCmd()
                );

                SimpleLogger.simpleLog("info", "data = " + dataProcessed.getPairFirst() + ", " + dataProcessed.getPairSecond());
                // Update Session: PS-Data
                this.executePs("sendRToken", uTicketIn, (String) dataProcessed.getPairFirst(), (String) dataProcessed.getPairSecond());

                if (uTicketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                    // [STAGE: (VTK)]
                    if (this.sharedData.getCurrentSession().getPlaintextCmd().equals("ACCESS_END")) {
                        this.sharedData.setResultMessage("-> SUCCESS: VERIFY_ACCESS_END");
                        SimpleLogger.simpleLog("info", this.sharedData.getResultMessage());

                        // [STAGE: (O)]
                        this.executeUpdateTicketOrder("deviceVerifyUTicket", uTicketIn);
                    } else { // pragma: no cover -> FAILURE: (VTK)
                        this.sharedData.setResultMessage("-> FAILURE: VERIFY_ACCESS_END");
                        SimpleLogger.simpleLog("error", this.sharedData.getResultMessage());
                        throw new RuntimeException(this.sharedData.getResultMessage());
                    }
                }
            } else { // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("Shouldn't Reach Here");
            }
        } catch (RuntimeException error) {
            SimpleLogger.simpleLog("error", error.getMessage());
            throw error;
        }

    }

    // Execute RTicket (Update Session, & Ticket Order)
    public void executeXxxRTicket(RTicket rTicketIn, String commEnd) {
        SimpleMeasurer.measureWorkerFunc(this::_executeXxxRTicket, rTicketIn, commEnd);
    }
    private void _executeXxxRTicket(RTicket rTicketIn, String commEnd) {
        if ("holderOrDevice".equals(commEnd)) {
            if (rTicketIn.getRTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET) ||
                rTicketIn.getRTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET) ||
                rTicketIn.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)
            ) { // [STAGE: (O)]
                this.executeUpdateTicketOrder("holderOrIssuerVerifyRTicket", rTicketIn);
            } else if (rTicketIn.getRTicketType().equals(RTicket.TYPE_CRKE1_RTICKET)) {
                // [STAGE: (E)]
                this.executeCRKE(rTicketIn, "holder",null);
            } else if (rTicketIn.getRTicketType().equals(RTicket.TYPE_CRKE2_RTICKET)) {
                // [STAGE: (E)]
                this.executeCRKE(rTicketIn, "device", null);
            } else if (rTicketIn.getRTicketType().equals(RTicket.TYPE_CRKE3_RTICKET)) {
                // [STAGE: (E)]
                this.executeCRKE(rTicketIn, "holder", null);
            } else if (rTicketIn.getRTicketType().equals(RTicket.TYPE_DATA_RTOKEN)) {
                // [STAGE: (E)]
                this.executePs("recvRToken", rTicketIn, null, null);
            } else {
                throw new RuntimeException("Shouldn't Reach Here");
            }
        } else if ("issuer".equals(commEnd)) {
            // Not owner anymore, delete this device in table
            if (rTicketIn.getRTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)) {
                this.sharedData.getDeviceTable().remove(rTicketIn.getDeviceId());

                // Storage
                this.simpleStorage.storeStorage(
                        this.sharedData.getThisDevice(),
                        this.sharedData.getDeviceTable(),
                        this.sharedData.getThisPerson(),
                        this.sharedData.getCurrentSession()
                );
            } else if (rTicketIn.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // Still owner, but keep/delete deviceAccessUTicketForOthers in table
                OtherDevice entry = this.sharedData.getDeviceTable().get(rTicketIn.getDeviceId());
                if (entry != null) {
                    entry.setDeviceAccessUTicketForOthers(null);
                    entry.setDeviceAccessEndRTicketForOthers(null);
                }
                // [STAGE: (O)]
                this.executeUpdateTicketOrder("holderOrIssuerVerifyRTicket", rTicketIn);

                // Storage
                this.simpleStorage.storeStorage(
                        this.sharedData.getThisDevice(),
                        this.sharedData.getDeviceTable(),
                        this.sharedData.getThisPerson(),
                        this.sharedData.getCurrentSession()
                );
            } else {
                throw new RuntimeException("Shouldn't Reach Here");
            }
        }
    }

    // Ownership
    public void _executeOneTimeInitializeIotDevice(UTicket uTicketIn) {
        SimpleLogger.simpleLog("info","+ " + sharedData.getThisDevice().getDeviceName() + " is initializing...");

        if (!this.sharedData.getThisDevice().getDeviceType().equals(ThisDevice.IOT_DEVICE)) {
            String failureMsg = "-> FAILURE: ONLY IOT_DEVICE CAN DO THIS INITIALIZATION OPERATION";
            SimpleLogger.simpleLog("error",failureMsg);
            throw new RuntimeException(failureMsg);
        }

        // Initialize Device Id
        // CRYPTO
        KeyPair keyPair;
        try {
            keyPair = ECC.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        // RAM
        this.sharedData.getThisDevice().setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());
        this.sharedData.getThisDevice().setDevicePubKey((ECPublicKey) keyPair.getPublic());

        // Initialize Device Owner
        // RAM
        ECPublicKey ownerPubKey = (ECPublicKey) SerializationUtil.strToKey(uTicketIn.getHolderId(), "eccPublicKey");
        this.sharedData.getThisDevice().setOwnerPubKey(ownerPubKey);

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );
    }

    public void _executeOwnershipTransfer(UTicket newUTicket) {
        SimpleLogger.simpleLog("info", "+ " + sharedData.getThisDevice().getDeviceName() + " is transferring ownership...");

        // Update Device Owner
        // RAM
        ECPublicKey ownerPubKey = (ECPublicKey) SerializationUtil.strToKey(newUTicket.getHolderId(), "eccPublicKey");
        this.sharedData.getThisDevice().setOwnerPubKey(ownerPubKey);

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );
    }

    // CR-KE
    public void executeCRKE(Object ticketIn, String commEnd, String cmd) {
        if (ticketIn instanceof UTicket) {
            SimpleMeasurer.measureWorkerFunc(this::_executeCRKEUTicket, (UTicket) ticketIn, commEnd, cmd);
        } else if (ticketIn instanceof RTicket) {
            SimpleMeasurer.measureWorkerFunc(this::_executeCRKERTicket, (RTicket) ticketIn, commEnd, cmd);
        } else {
            throw new IllegalArgumentException("Unsupported ticket type");
        }
    }
    private void _executeCRKEUTicket(UTicket ticketIn, String commEnd, String cmd) {
        SimpleLogger.simpleLog("info","+ " + sharedData.getThisDevice().getDeviceName() + " is updating current session...");

        if(ticketIn != null &&
                (ticketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_UTICKET) ||
                        ticketIn.getUTicketType().equals(UTicket.TYPE_SELFACCESS_UTICKET))) {

            if ("holder".equals(commEnd)) {
                // Update Session: Access UT
                this.sharedData.getCurrentSession().setCurrentUTicketId(ticketIn.getUTicketId());
                this.sharedData.getCurrentSession().setCurrentDeviceId(ticketIn.getDeviceId());
                this.sharedData.getCurrentSession().setCurrentHolderId(ticketIn.getHolderId());
                this.sharedData.getCurrentSession().setCurrentTaskScope(ticketIn.getTaskScope());
                // Update Session: PS-Cmd (Input: Plaintext, Associated-Plaintext)
                this.executePs("sendUt", null, cmd, null);
            } else if ("device".equals(commEnd)) {
                // Update Session: Access UT
                this.sharedData.getCurrentSession().setCurrentUTicketId(ticketIn.getUTicketId());
                this.sharedData.getCurrentSession().setCurrentDeviceId(ticketIn.getDeviceId());
                this.sharedData.getCurrentSession().setCurrentHolderId(ticketIn.getHolderId());
                this.sharedData.getCurrentSession().setCurrentTaskScope(ticketIn.getTaskScope());
                // Update Session: CR
                try {
                    this.sharedData.getCurrentSession().setChallenge1(ECDH.generateRandomStr(32));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // Update Session: KE
                try {
                    this.sharedData.getCurrentSession().setKeyExchangeSalt1(ECDH.generateRandomStr(32));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // Update Session: PS-Cmd
                this.executePs("recvUtAndSendCrke1", null, null, null);
            } else {
                throw new RuntimeException("Shouldn't Reach Here");
            }
        }
        // Storage (Persistent vs. RAM-only)
        // Uncomment the line below if you want to enable persistent storage
        // simpleStorage.storeStorage(sharedData.getThisDevice(), sharedData.getDeviceTable(), sharedData.getThisPerson(), sharedData.getCurrentSession());
    }
    private void _executeCRKERTicket(RTicket ticketIn, String commEnd, String cmd) {
        if (ticketIn != null && (ticketIn.getRTicketType().equals(RTicket.TYPE_CRKE1_RTICKET))) {
            // Update Session: CR
            this.sharedData.getCurrentSession().setChallenge1((ticketIn.getChallenge1()));
            try {
                this.sharedData.getCurrentSession().setChallenge2(ECDH.generateRandomStr(32));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Update Session: KE
            this.sharedData.getCurrentSession().setKeyExchangeSalt1(ticketIn.getKeyExchangeSalt1());
            try {
                this.sharedData.getCurrentSession().setKeyExchangeSalt2(ECDH.generateRandomStr(32));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Update Session: KE
            byte[] currentSessionKeyByte;
            try {
                currentSessionKeyByte = this._executeGenerateSessionKey(
                        this.sharedData.getCurrentSession().getKeyExchangeSalt1(),
                        this.sharedData.getCurrentSession().getKeyExchangeSalt2(),
                        this.sharedData.getThisPerson().getPersonPrivKey(),
                    (ECPublicKey) SerializationUtil.strToKey(this.sharedData.getCurrentSession().getCurrentDeviceId(), "eccPublicKey")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.sharedData.getCurrentSession().setCurrentSessionKeyStr(SerializationUtil.byteToBase64Str(currentSessionKeyByte));

            // Update Session: PS-Cmd
            this.executePs("recvCrke1AndSendCrke2", ticketIn, null, null);
        } else if (ticketIn != null && ticketIn.getRTicketType().equals(RTicket.TYPE_CRKE2_RTICKET)) {
            // Update Session: CR
            this.sharedData.getCurrentSession().setChallenge2(ticketIn.getChallenge2());
            // Update Session: KE
            this.sharedData.getCurrentSession().setKeyExchangeSalt2(ticketIn.getKeyExchangeSalt2());

            // Update Session: KE
            byte[] currentSessionKeyByte;
            try {
                currentSessionKeyByte = this._executeGenerateSessionKey(
                        this.sharedData.getCurrentSession().getKeyExchangeSalt1(),
                        ticketIn.getKeyExchangeSalt2(),
                        this.sharedData.getThisDevice().getDevicePrivKey(),
                    (ECPublicKey) SerializationUtil.strToKey(this.sharedData.getCurrentSession().getCurrentHolderId(), "eccPublicKey")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.sharedData.getCurrentSession().setCurrentSessionKeyStr(SerializationUtil.byteToBase64Str(currentSessionKeyByte));

            // Update Session: PS-Cmd
            this.executePs("recvCrke2", ticketIn, null, null);

            // Data Processing
            Pair dataProcessed = this._executeDataProcessing(
                    this.sharedData.getCurrentSession().getPlaintextCmd(),
                    this.sharedData.getCurrentSession().getAssociatedPlaintextCmd()
            );
            String plaintextData = (String) dataProcessed.getPairFirst();
            String associatedPlaintextData = (String) dataProcessed.getPairSecond();

            // Update Session: PS-Data
            this.executePs("sendCrke3", ticketIn, plaintextData, associatedPlaintextData);
        } else if (ticketIn != null && (ticketIn.getRTicketType().equals(RTicket.TYPE_CRKE3_RTICKET))) {
            // Update Session: PS-Data
            this.executePs("recvCrke3", ticketIn, null, null);
        } else {
            throw new RuntimeException("Shouldn't Reach Here");
        }
        // Storage (Persistent vs. RAM-only)
        // Uncomment the line below if you want to enable persistent storage
        // simpleStorage.storeStorage(sharedData.getThisDevice(), sharedData.getDeviceTable(), sharedData.getThisPerson(), sharedData.getCurrentSession());
    }

    private byte[] _executeGenerateSessionKey(String keyExchangeSalt1, String keyExchangeSalt2, ECPrivateKey serverPrivKey, ECPublicKey peerPubKey) throws Exception {
        // Convert Base64 encoded salts to byte arrays
        byte[] salt1Bytes = SerializationUtil.base64StrBackToByte(keyExchangeSalt1);
        byte[] salt2Bytes = SerializationUtil.base64StrBackToByte(keyExchangeSalt2);

        // Perform bitwise AND operation between salt1Bytes and salt2Bytes
        assert salt1Bytes != null && salt2Bytes != null;
        byte[] sharedSaltBytes = new byte[salt1Bytes.length];
        for (int i = 0; i < salt1Bytes.length; i++) {
            sharedSaltBytes[i] = (byte) (salt1Bytes[i] & salt2Bytes[i]);
        }

        // len(shared_salt)  # = 32 bytes
        // len(current_session_key)  # = 32 bytes
        return ECDH.generateEcdhKey(serverPrivKey, sharedSaltBytes, null, peerPubKey);
    }

    public void executePs(String executingCase, Object ticketIn, String plaintext, String associatedPlaintext) {
        if (ticketIn == null) {
            SimpleMeasurer.measureWorkerFunc(this::_executePsUTicket, executingCase, null, plaintext, associatedPlaintext);
        } else if (ticketIn instanceof UTicket) {
            SimpleMeasurer.measureWorkerFunc(this::_executePsUTicket, executingCase, (UTicket) ticketIn, plaintext, associatedPlaintext);
        } else if (ticketIn instanceof RTicket) {
            SimpleMeasurer.measureWorkerFunc(this::_executePsRTicket, executingCase, (RTicket) ticketIn, plaintext, associatedPlaintext);
        } else {
            throw new IllegalArgumentException("Unsupported ticket type");
        }
    }
    private void _executePsUTicket(String executingCase, UTicket ticketIn, String plaintext, String associatedPlaintext) {
        SimpleLogger.simpleLog("info","+ " + this.sharedData.getThisDevice().getDeviceName() + " is updating current session...");

        // CR-KE
        switch (executingCase) {
            case "sendUt" :
                // Update Session: PS-Cmd (Input: Plaintext, Associated-Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd("additional unencrypted cmd");
                break;
            case "recvUtAndSendCrke1":
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvCmd(this._generateNextIv());
                break;
            case "recvCrke2" :
                // Update Session: PS-Cmd (Input: Key)
                byte[] currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());

                // Update Session: PS-Cmd (Input: This-IV)
                // Update Session: PS-Cmd (Input: Ciphertext, Associated-Plaintext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextCmd(ticketIn.getCiphertextCmd());
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd(ticketIn.getAssociatedPlaintextCmd());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagCmd(ticketIn.getGcmAuthenticationTagCmd());

                // [STAGE: (VTK)(VTS)]
                // Update Session: PS-Cmd (Decryption)
                String plaintextCmd = this._executeDecryptCiphertext(
                        this.sharedData.getCurrentSession().getCiphertextCmd(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextCmd(),
                        this.sharedData.getCurrentSession().getGcmAuthenticationTagCmd(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvCmd()
                );

                this.msgVerifier.verifyCmdIsInTaskScope(plaintextCmd);
                // Update Session: PS-Cmd (Output: Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintextCmd);
                break;
            case "sendCrke3" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvData(ticketIn.getIvData());
                // Update Session: PS-Data (Input: Plaintext, Associated-Plaintext, Encryption)
                this.sharedData.getCurrentSession().setPlaintextData(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextData(associatedPlaintext);

                // Update Session: PS-Data (Encryption)
                Pair encResult = this._executeEncryptPlaintext(
                        this.sharedData.getCurrentSession().getPlaintextData(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextData(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Data (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextData((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagData((String) encResult.getPairSecond());
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvCmd(this._generateNextIv());
                break;
            // PS
            case "sendUToken" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                // Update Session: PS-Cmd (Input: Plaintext, Associated-Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd("additional unencrypted cmd");

                // Update Session: PS-Cmd (Encryption)
                encResult = this._executeEncryptPlaintext(
                        this.sharedData.getCurrentSession().getPlaintextCmd(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextCmd(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Cmd (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextCmd((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagCmd((String) encResult.getPairSecond());
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvData(this._generateNextIv());
                break;
            case "recvUToken" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                // Update Session: PS-Cmd (Input: Ciphertext, Associated-Plaintext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextCmd(ticketIn.getCiphertextCmd());
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd(ticketIn.getAssociatedPlaintextCmd());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagCmd(ticketIn.getGcmAuthenticationTagCmd());

                // [STAGE: (VTK)(VTS)]
                // Update Session: PS-Cmd (Decryption)
                plaintextCmd = this._executeDecryptCiphertext(
                        this.sharedData.getCurrentSession().getCiphertextCmd(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextCmd(),
                        this.sharedData.getCurrentSession().getGcmAuthenticationTagCmd(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvCmd()
                );

                if(!ticketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                    this.msgVerifier.verifyCmdIsInTaskScope(plaintextCmd);
                }
                // Update Session: PS-Cmd (Output: Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintextCmd);
                break;
            case "sendRToken" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvData(ticketIn.getIvData());
                // Update Session: PS-Data (Input: Plaintext, Associated-Plaintext, Encryption)
                this.sharedData.getCurrentSession().setPlaintextData(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextData(associatedPlaintext);

                // Update Session: PS-Data (Encryption)
                encResult = this._executeEncryptPlaintext(
                        plaintext,
                        associatedPlaintext,
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Data (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextData((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagData((String) encResult.getPairSecond());
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvCmd(this._generateNextIv());
                break;
            default: // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("Shouldn't Reach Here");
        }
    }
    private void _executePsRTicket(String executingCase, RTicket ticketIn, String plaintext, String associatedPlaintext) {
        SimpleLogger.simpleLog("info","+ " + this.sharedData.getThisDevice().getDeviceName() + " is updating current session...");

        // CR-KE
        switch (executingCase) {
            case "sendUt" :
                // Update Session: PS-Cmd (Input: Plaintext, Associated-Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd("additional unencrypted cmd");
                break;
            case "recvUtAndSendCrke1":
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvCmd(this._generateNextIv());
                break;
            case "recvCrke1AndSendCrke2" :
                // Update Session: PS-Cmd (Input: Key)
                byte[] currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvCmd(ticketIn.getIvCmd());
                // Update Session: PS-Cmd (Input: Plaintext, Associated-Plaintext)
                // Update Session: PS-Cmd (Encryption)
                String cmdPlaintext = this.sharedData.getCurrentSession().getPlaintextCmd();
                String cmdAssociatedPlaintext = this.sharedData.getCurrentSession().getAssociatedPlaintextCmd();
                String ivCmd = this.sharedData.getCurrentSession().getIvCmd();

                Pair encResult = this._executeEncryptPlaintext(
                        cmdPlaintext, cmdAssociatedPlaintext, currentSessionKeyBytes, ivCmd
                );

                // Update Session: PS-Cmd (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextCmd((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagCmd((String) encResult.getPairSecond());

                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvData(this._generateNextIv());
                break;
            case "recvCrke2" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());

                // Update Session: PS-Cmd (Input: This-IV)
                // Update Session: PS-Cmd (Input: Ciphertext, Associated-Plaintext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextCmd(ticketIn.getCiphertextCmd());
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd(ticketIn.getAssociatedPlaintextCmd());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagCmd(ticketIn.getGcmAuthenticationTagCmd());

                // [STAGE: (VTK)(VTS)]
                // Update Session: PS-Cmd (Decryption)
                String plaintextCmd = this._executeDecryptCiphertext(
                        this.sharedData.getCurrentSession().getCiphertextCmd(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextCmd(),
                        this.sharedData.getCurrentSession().getGcmAuthenticationTagCmd(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvCmd()
                );

                this.msgVerifier.verifyCmdIsInTaskScope(plaintextCmd);
                // Update Session: PS-Cmd (Output: Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintextCmd);
                break;
            case "sendCrke3" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvData(ticketIn.getIvData());
                // Update Session: PS-Data (Input: Plaintext, Associated-Plaintext, Encryption)
                this.sharedData.getCurrentSession().setPlaintextData(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextData(associatedPlaintext);

                // Update Session: PS-Data (Encryption)
                encResult = this._executeEncryptPlaintext(
                        this.sharedData.getCurrentSession().getPlaintextData(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextData(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Data (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextData((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagData((String) encResult.getPairSecond());
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvCmd(this._generateNextIv());
                break;
            case "recvCrke3":
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvCmd(ticketIn.getIvCmd());
                // Update Session: PS-Data (Input: Ciphertext, Associated-Plaintext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextData(ticketIn.getCiphertextData());
                this.sharedData.getCurrentSession().setAssociatedPlaintextData(ticketIn.getAssociatedPlaintextData());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagData(ticketIn.getGcmAuthenticationTagData());

                // [STAGE: (VTK)]
                // Update Session: PS-Data (Decryption)
                String plaintextData = this._executeDecryptCiphertext(
                        this.sharedData.getCurrentSession().getCiphertextData(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextData(),
                        this.sharedData.getCurrentSession().getGcmAuthenticationTagData(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Data (Output: Plaintext)
                this.sharedData.getCurrentSession().setPlaintextData(plaintextData);
                break;
            // PS
            case "sendUToken" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                // Update Session: PS-Cmd (Input: Plaintext, Associated-Plaintext)
                this.sharedData.getCurrentSession().setPlaintextCmd(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextCmd("additional unencrypted cmd");

                // Update Session: PS-Cmd (Encryption)
                encResult = this._executeEncryptPlaintext(
                        this.sharedData.getCurrentSession().getPlaintextCmd(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextCmd(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Cmd (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextCmd((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagCmd((String) encResult.getPairSecond());
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvData(this._generateNextIv());
                break;
            case "sendRToken" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvData(ticketIn.getIvData());
                // Update Session: PS-Data (Input: Plaintext, Associated-Plaintext, Encryption)
                this.sharedData.getCurrentSession().setPlaintextData(plaintext);
                this.sharedData.getCurrentSession().setAssociatedPlaintextData(associatedPlaintext);

                // Update Session: PS-Data (Encryption)
                encResult = this._executeEncryptPlaintext(
                        plaintext,
                        associatedPlaintext,
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Data (Output: Ciphertext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextData((String) encResult.getPairFirst());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagData((String) encResult.getPairSecond());
                // Update Session: Next-IV
                this.sharedData.getCurrentSession().setIvCmd(this._generateNextIv());
                break;
            case "recvRToken" :
                // Update Session: PS-Cmd (Input: Key)
                currentSessionKeyBytes = SerializationUtil.base64StrBackToByte(this.sharedData.getCurrentSession().getCurrentSessionKeyStr());
                // Update Session: PS-Cmd (Input: This-IV)
                this.sharedData.getCurrentSession().setIvCmd(ticketIn.getIvCmd());
                // Update Session: PS-Cmd (Input: Ciphertext, Associated-Plaintext, GCM-Authentication-Tag)
                this.sharedData.getCurrentSession().setCiphertextData(ticketIn.getCiphertextData());
                this.sharedData.getCurrentSession().setAssociatedPlaintextData(ticketIn.getAssociatedPlaintextData());
                this.sharedData.getCurrentSession().setGcmAuthenticationTagData(ticketIn.getGcmAuthenticationTagData());

                // [STAGE: (VTK)]
                // Update Session: PS-Data (Decryption)
                plaintextData = this._executeDecryptCiphertext(
                        this.sharedData.getCurrentSession().getCiphertextData(),
                        this.sharedData.getCurrentSession().getAssociatedPlaintextData(),
                        this.sharedData.getCurrentSession().getGcmAuthenticationTagData(),
                        currentSessionKeyBytes,
                        this.sharedData.getCurrentSession().getIvData()
                );

                // Update Session: PS-Data (Output: Plaintext)
                this.sharedData.getCurrentSession().setPlaintextData(plaintextData);
                break;
            default: // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("Shouldn't Reach Here");
        }
    }

    private String _generateNextIv() {
        try {
            return SerializationUtil.byteToBase64Str(ECDH.gcmGenIv());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Pair _executeEncryptPlaintext(String plaintext, String associatedPlaintext, byte[] sessionKey, String iv) {
        byte[] plaintextBytes = SerializationUtil.strToByte(plaintext);
        plaintextBytes = (plaintextBytes == null) ? new byte[0] : plaintextBytes;
        byte[] associatedPlaintextBytes = SerializationUtil.strToByte(associatedPlaintext);
        associatedPlaintextBytes = (associatedPlaintextBytes == null) ? new byte[0] : associatedPlaintextBytes;
        byte[] ivBytes = SerializationUtil.base64StrBackToByte(iv);

        try {
            byte[][] bytePair = ECDH.gcmEncrypt(plaintextBytes, associatedPlaintextBytes, sessionKey, ivBytes);
            byte[] ciphertextBytes = bytePair[0];
            byte[] gcmAuthenticationTagBytes = bytePair[2];

            String ciphertext = SerializationUtil.byteToBase64Str(ciphertextBytes);
            String gcmAuthenticationTag = SerializationUtil.byteToBase64Str(gcmAuthenticationTagBytes);

            return new Pair(ciphertext, gcmAuthenticationTag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String _executeDecryptCiphertext(String ciphertext, String associatedPlaintext, String gcmAuthenticationTag, byte[] sessionKey, String iv) {
        // [STAGE: (VTK)] Verify HMAC before Execution
        byte[] ciphertextBytes = SerializationUtil.base64StrBackToByte(ciphertext);
        byte[] associatedPlaintextBytes = SerializationUtil.strToByte(associatedPlaintext);
        byte[] gcmAuthenticationTagByte = SerializationUtil.base64StrBackToByte(gcmAuthenticationTag);
        byte[] ivBytes = SerializationUtil.base64StrBackToByte(iv);

        try {
            // verify_token_through_hmac
            byte[] plaintextByte = ECDH.gcmDecrypt(ciphertextBytes,associatedPlaintextBytes,gcmAuthenticationTagByte,sessionKey,ivBytes);

            String plaintext = SerializationUtil.byteBackToStr(plaintextByte);

            this.sharedData.setResultMessage("-> SUCCESS: VERIFY_IV_AND_HMAC");
            SimpleLogger.simpleLog("info", this.sharedData.getResultMessage());

            return plaintext;
        } catch (AEADBadTagException error) {
            this.sharedData.setResultMessage("-> FAILURE: VERIFY_IV_AND_HMAC");
            SimpleLogger.simpleLog("error", this.sharedData.getResultMessage());
            throw new RuntimeException(this.sharedData.getResultMessage());
        } catch (Exception e) { // pragma: no cover -> Shouldn't Reach Here
            throw new RuntimeException("Shouldn't Reach Here, " + e.getMessage());
        }
    }

    // Execute Application & Data Processing
    private Pair _executeDataProcessing(String plaintextCmd, String associatedPlaintextCmd) {
        SimpleLogger.simpleLog("debug", "+ " + this.sharedData.getThisDevice().getDeviceName() + " is executing application...");

        String plaintextData = "DATA: " + plaintextCmd;
        String associatedPlaintextData = "DATA: " + associatedPlaintextCmd;

        return new Pair(plaintextData,associatedPlaintextData);
    }

    /* [STAGE: (O)] Update Ticket Order
       Update Ticket Order after:
           "hasType": Initial Ticket Order = 0
           "agentInitialization": Ticket Order = 1 after device/agent is initialized
           "holderGenerateOrReceiveUTicket": Generate or Receive UTicket (expected ticket order)
           "deviceVerifyUTicket": Verify UTicket & End TX (actual ticket order)
           "holderOrIssuerVerifyRTicket": Verify RTicket (actual ticket order)
    */
    public void executeUpdateTicketOrder(String updatingCase, Object ticketIn) {
        SimpleMeasurer.measureWorkerFunc(this::_executeUpdateTicketOrder,updatingCase,ticketIn);
    }
    private void _executeUpdateTicketOrder(String updatingCase, Object ticketIn) {
        SimpleLogger.simpleLog("info", "+ " + this.sharedData.getThisDevice().getDeviceName() + " is updating ticket order...");

        switch (updatingCase) {
            case "hasType":
                this.sharedData.getThisDevice().setTicketOrder(0);
                break;
            case "agentInitialization":
                this.sharedData.getThisDevice().setTicketOrder(this.sharedData.getThisDevice().getTicketOrder() + 1);
                break;
            case "holderGenerateOrReceiveUTicket":
                if (ticketIn instanceof UTicket) {
                    UTicket uTicketIn = (UTicket) ticketIn;
                    if (uTicketIn.getUTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)
                            || uTicketIn.getUTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)
                            || uTicketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_UTICKET)
                            || uTicketIn.getUTicketType().equals(UTicket.TYPE_SELFACCESS_UTICKET)) {

                        this.sharedData.getDeviceTable().get(uTicketIn.getDeviceId()).setTicketOrder(uTicketIn.getTicketOrder());
                        SimpleLogger.simpleLog("debug",this.sharedData.getThisDevice().getDeviceName() + ": Predicted ticket_order=" +
                                this.sharedData.getDeviceTable().get(uTicketIn.getDeviceId()).getTicketOrder());
                    } else { // pragma: no cover -> Shouldn't Reach Here
                        throw new RuntimeException("Shouldn't Reach Here");
                    }
                }
                break;
            case "deviceVerifyUTicket":
                // Execute UTicket
                if (ticketIn instanceof UTicket) {
                    UTicket uTicketIn = (UTicket) ticketIn;
                    if (uTicketIn.getUTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)
                            || uTicketIn.getUTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)
                            || uTicketIn.getUTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {

                        this.sharedData.getThisDevice().setTicketOrder(this.sharedData.getThisDevice().getTicketOrder() + 1);
                        SimpleLogger.simpleLog("debug",this.sharedData.getThisDevice().getDeviceName() + ": Updated ticket_order=" +
                                this.sharedData.getThisDevice().getTicketOrder());
                    } else { // pragma: no cover -> Shouldn't Reach Here
                        throw new RuntimeException("Shouldn't Reach Here");
                    }
                }
                break;
            case "holderOrIssuerVerifyRTicket":
                // Execute UTicket
                if (ticketIn instanceof RTicket) {
                    RTicket rTicketIn = (RTicket) ticketIn;
                    if (rTicketIn.getRTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)
                            || rTicketIn.getRTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)
                            || rTicketIn.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {

                        this.sharedData.getDeviceTable().get(rTicketIn.getDeviceId())
                                .setTicketOrder(rTicketIn.getTicketOrder());
                        SimpleLogger.simpleLog("debug", this.sharedData.getThisDevice().getDeviceName() + ": Updated ticket_order=" +
                                this.sharedData.getDeviceTable().get(rTicketIn.getDeviceId()).getTicketOrder());
                    } else { // pragma: no cover -> Shouldn't Reach Here
                        throw new RuntimeException("Shouldn't Reach Here");
                    }
                }
                break;
            default: // pragma: no cover -> Shouldn't Reach Here
                throw new RuntimeException("Shouldn't Reach Here");
        }

        // Storage
        this.simpleStorage.storeStorage(
                this.sharedData.getThisDevice(),
                this.sharedData.getDeviceTable(),
                this.sharedData.getThisPerson(),
                this.sharedData.getCurrentSession()
        );
    }

    // [STAGE: (C)] Change Receiver State
    public void changeState(String newState) {
        _changeState(newState);
    }
    private void _changeState(String newState) {
        this.sharedData.setState(newState);
    }

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }
    public SharedData getSharedData() {
        return this.sharedData;
    }

    public void setMeasureHelper(MeasureHelper measureHelper) {
        this.measureHelper = measureHelper;
    }
    public MeasureHelper getMeasureHelper() {
        return measureHelper;
    }

    public void setSimpleStorage(SimpleStorage simpleStorage) {
        this.simpleStorage = simpleStorage;
    }
    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    public void setMsgVerifier(MsgVerifier msgVerifier) {
        this.msgVerifier = msgVerifier;
    }
    public MsgVerifier getMsgVerifier() {
        return msgVerifier;
    }
}

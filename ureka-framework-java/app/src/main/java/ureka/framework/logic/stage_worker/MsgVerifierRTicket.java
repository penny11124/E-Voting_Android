package ureka.framework.logic.stage_worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MsgVerifierRTicket {
    private ThisDevice thisDevice;
    private Map<String, OtherDevice> deviceTable;
    private UTicket auditStartTicket;
    private UTicket auditEndTicket;
    private CurrentSession currentSession;

    public MsgVerifierRTicket(Optional<ThisDevice> thisDevice, Optional<Map<String, OtherDevice>> deviceTable,
                              Optional<UTicket> auditStartTicket, Optional<UTicket> auditEndTicket,
                              Optional<CurrentSession> currentSession) {
        this.thisDevice = thisDevice.orElse(null);
        this.deviceTable = deviceTable.orElse(null);
        this.auditStartTicket = auditStartTicket.orElse(null);
        this.auditEndTicket = auditEndTicket.orElse(null);
        this.currentSession = currentSession.orElse(null);
    }

    // Message Verification Flow
    public RTicket verifyJsonSchema(String arbitraryJson) {
        String successMsg = "-> SUCCESS: VERIFY_JSON_SCHEMA";
        String failureMsg = "-> FAILURE: VERIFY_JSON_SCHEMA";

        try {
            RTicket rTicketIn = RTicket.jsonStrToRTicket(arbitraryJson);
            SimpleLogger.simpleLog("info", successMsg);
            return rTicketIn;
        } catch (RuntimeException error) { // pragma: no cover -> Weird R-Ticket
            SimpleLogger.simpleLog("error", failureMsg + ": " + error.getMessage());
            throw new RuntimeException(failureMsg + ": " + error.getMessage());
        }
    }

    public RTicket verifyProtocolVersion(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_PROTOCOL_VERSION = " + rTicketIn.getProtocolVersion();
        String failureMsg = "-> FAILURE: VERIFY_PROTOCOL_VERSION = " + rTicketIn.getProtocolVersion();

        if (rTicketIn.getProtocolVersion().equals(UTicket.PROTOCOL_VERSION)) {
            SimpleLogger.simpleLog("info", successMsg);
            return rTicketIn;
        } else { // pragma: no cover -> Weird R-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    public RTicket verifyRTicketId(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_RTICKET_ID";
        String failureMsg = "-> FAILURE: VERIFY_RTICKET_ID";

        // Verify UTicket Id (Hash-based)
        RTicket ticketWithoutIdAndSig;
        try {
            ticketWithoutIdAndSig = deepCopy(rTicketIn);
            ticketWithoutIdAndSig.setRTicketId(null);
            ticketWithoutIdAndSig.setDeviceSignature(null);
            String generatedHash;
            generatedHash = ECDH.generateSha256HashStr(RTicket.rTicketToJsonStr(ticketWithoutIdAndSig));

            if (generatedHash.equals(rTicketIn.getRTicketId())) {
                SimpleLogger.simpleLog("info", successMsg);
                return rTicketIn;
            } else {
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RTicket verifyRTicketType(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_RTICKET_TYPE = " + rTicketIn.getRTicketType();
        String failureMsg = "-> FAILURE: VERIFY_RTICKET_TYPE = " + rTicketIn.getRTicketType();

        if (Arrays.asList(RTicket.LEGAL_RTICKET_TYPES).contains(rTicketIn.getRTicketType())) {
            SimpleLogger.simpleLog("info", successMsg);
            return rTicketIn;
        } else { // pragma: no cover -> Weird R-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    // Because holder do not really know whether this device_id is correct before get RT
    public RTicket hasDeviceId(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: HAS_DEVICE_ID = " + rTicketIn.getDeviceId();
        String failureMsg = "-> FAILURE: HAS_DEVICE_ID = " + rTicketIn.getDeviceId();

        if (rTicketIn.getDeviceId() != null) {
            SimpleLogger.simpleLog("info", successMsg);
            return rTicketIn;
        } else { // pragma: no cover -> Weird R-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    // However, device can verify whether this device_id is correct
    public RTicket verifyDeviceId(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_DEVICE_ID = " + rTicketIn.getDeviceId();
        String failureMsg = "-> FAILURE: VERIFY_DEVICE_ID = " + rTicketIn.getDeviceId();

        switch (rTicketIn.getRTicketType()) {
            case UTicket.TYPE_INITIALIZATION_UTICKET:
                // Note that for TYPE_INITIALIZATION:
                // u_ticket_device_id = "no_id"
                // r_ticket_device_id = "newly-created device public key string"

                // NO Device ID
                SimpleLogger.simpleLog("info", successMsg);
                return rTicketIn;

            case UTicket.TYPE_OWNERSHIP_UTICKET:
            case RTicket.TYPE_DATA_RTOKEN:
            case UTicket.TYPE_ACCESS_END_UTOKEN:
                if (rTicketIn.getDeviceId().equals(this.auditStartTicket.getDeviceId())) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }

            default:
                if (Arrays.asList(RTicket.LEGAL_CRKE_TYPES).contains(rTicketIn.getRTicketType())) {
                    if (rTicketIn.getDeviceId().equals(this.currentSession.getCurrentDeviceId())) {
                        SimpleLogger.simpleLog("info", successMsg);
                        return rTicketIn;
                    } else { // pragma: no cover -> Weird R-Ticket
                        SimpleLogger.simpleLog("error", failureMsg);
                        throw new RuntimeException(failureMsg);
                    }
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }
        }
    }

    public RTicket verifyResult(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_RESULT";
        String failureMsg = "-> FAILURE: VERIFY_RESULT";

        if (rTicketIn.getResult().contains("SUCCESS")) {
            SimpleLogger.simpleLog("info", successMsg);
            return rTicketIn;
        } else { // pragma: no cover -> Weird R-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    public RTicket verifyTicketOrder(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_TICKET_ORDER";
        String failureMsg = "-> FAILURE: VERIFY_TICKET_ORDER";

        switch (rTicketIn.getRTicketType()) {
            // If TX is finished, the ticket_order++
            // "holder"
            case UTicket.TYPE_INITIALIZATION_UTICKET:
                if (rTicketIn.getTicketOrder() == 1) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }

            case UTicket.TYPE_OWNERSHIP_UTICKET:
            case UTicket.TYPE_ACCESS_END_UTOKEN:
                if (rTicketIn.getTicketOrder() == this.deviceTable.get(rTicketIn.getDeviceId()).getTicketOrder() + 1) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }

            // If TX is not finished, the ticket_order should be the same
            // "holder"
            case RTicket.TYPE_CRKE1_RTICKET:
            case RTicket.TYPE_CRKE3_RTICKET:
            case RTicket.TYPE_DATA_RTOKEN:
                if (rTicketIn.getTicketOrder().equals(this.deviceTable.get(rTicketIn.getDeviceId()).getTicketOrder())) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }

            // "device"
            case RTicket.TYPE_CRKE2_RTICKET:
                if (rTicketIn.getTicketOrder().equals(this.thisDevice.getTicketOrder())) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }

            default: // pragma: no cover -> Weird R-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
        }
    }

    public RTicket verifyAuditStart(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_AUDIT_START";
        String failureMsg = "-> FAILURE: VERIFY_AUDIT_START";

        switch (rTicketIn.getRTicketType()) {
            case UTicket.TYPE_INITIALIZATION_UTICKET:
            case UTicket.TYPE_OWNERSHIP_UTICKET:
            case RTicket.TYPE_DATA_RTOKEN:
            case UTicket.TYPE_ACCESS_END_UTOKEN:
                if (rTicketIn.getAuditStart().equals(this.auditStartTicket.getUTicketId())) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }

            default:
                if (Arrays.asList(RTicket.LEGAL_CRKE_TYPES).contains(rTicketIn.getRTicketType())) {
                    if (rTicketIn.getAuditStart().equals(this.currentSession.getCurrentUTicketId())) {
                        SimpleLogger.simpleLog("info", successMsg);
                        return rTicketIn;
                    } else { // pragma: no cover -> Weird R-Ticket
                        SimpleLogger.simpleLog("error", failureMsg);
                        throw new RuntimeException(failureMsg);
                    }
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }
        }
    }

    public RTicket verifyAuditEnd(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_AUDIT_END";
        String failureMsg = "-> FAILURE: VERIFY_AUDIT_END";

        // Audited by:
        //   Per-Use
        //   TX end UToken
        //   TODO: Revocation UTicket
        if (rTicketIn.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
            if (rTicketIn.getAuditEnd().equals("ACCESS_END")) {
                SimpleLogger.simpleLog("info", successMsg);
                return rTicketIn;
            } else { // pragma: no cover -> Weird R-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else {
            SimpleLogger.simpleLog("info", successMsg);
            return rTicketIn;
        }
    }

    public RTicket verifyCRKE(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_CR_KE";
        String failureMsg = "-> FAILURE: VERIFY_CR_KE";

        switch (rTicketIn.getRTicketType()) {
            case UTicket.TYPE_INITIALIZATION_UTICKET:
            case UTicket.TYPE_OWNERSHIP_UTICKET:
            case RTicket.TYPE_DATA_RTOKEN:
            case UTicket.TYPE_ACCESS_END_UTOKEN:
                // NO CR-KE
                SimpleLogger.simpleLog("info",successMsg);
                return rTicketIn;
            case RTicket.TYPE_CRKE1_RTICKET:
                if(rTicketIn.getChallenge1() != null && rTicketIn.getKeyExchangeSalt1() != null) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            case RTicket.TYPE_CRKE2_RTICKET:
                if(rTicketIn.getChallenge1() != null && rTicketIn.getChallenge2() != null && rTicketIn.getKeyExchangeSalt2() != null) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            case RTicket.TYPE_CRKE3_RTICKET:
                if(rTicketIn.getChallenge2() != null) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            default:
                SimpleLogger.simpleLog("error",failureMsg);
                throw new RuntimeException(failureMsg);
        }
    }

    public RTicket verifyPs(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_PS";
        String failureMsg = "-> FAILURE: VERIFY_PS";

        switch (rTicketIn.getRTicketType()) {
            case UTicket.TYPE_INITIALIZATION_UTICKET:
            case UTicket.TYPE_OWNERSHIP_UTICKET:
            case UTicket.TYPE_ACCESS_END_UTOKEN:
                // NO PS
                SimpleLogger.simpleLog("info",successMsg);
                return rTicketIn;
            case RTicket.TYPE_CRKE1_RTICKET:
                if(rTicketIn.getIvCmd() != null) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            case RTicket.TYPE_CRKE2_RTICKET:
                if(rTicketIn.getAssociatedPlaintextCmd() != null &&
                    rTicketIn.getCiphertextCmd() != null &&
                    rTicketIn.getIvData() != null &&
                    rTicketIn.getGcmAuthenticationTagCmd() != null) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            case RTicket.TYPE_CRKE3_RTICKET:
            case RTicket.TYPE_DATA_RTOKEN:
                if(rTicketIn.getAssociatedPlaintextData() != null &&
                        rTicketIn.getCiphertextData() != null &&
                        rTicketIn.getIvCmd() != null &&
                        rTicketIn.getGcmAuthenticationTagData() != null) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            default: // pragma: no cover -> Weird R-Ticket
                SimpleLogger.simpleLog("error",failureMsg);
                throw new RuntimeException(failureMsg);
        }
    }

    public RTicket verifyDeviceSignature(RTicket rTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_DEVICE_SIGNATURE on " + rTicketIn.getRTicketType() + " RTICKET";
        String failureMsg = "-> FAILURE: VERIFY_DEVICE_SIGNATURE on " + rTicketIn.getRTicketType() + " RTICKET";

        // Verify DEVICE_SIGNATURE through device_id
        switch (rTicketIn.getRTicketType()) {
            case UTicket.TYPE_INITIALIZATION_UTICKET:
            case UTicket.TYPE_OWNERSHIP_UTICKET:
            case RTicket.TYPE_CRKE1_RTICKET:
            case RTicket.TYPE_CRKE3_RTICKET:
            case UTicket.TYPE_ACCESS_END_UTOKEN:
                if(this._verifyDeviceSignatureOnRTicket(rTicketIn, SerializationUtil.strToKey(rTicketIn.getRTicketId(),"eccPublicKey"))) {
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            case RTicket.TYPE_CRKE2_RTICKET:
                if(this._verifyDeviceSignatureOnRTicket(rTicketIn, SerializationUtil.strToKey(this.currentSession.getCurrentHolderId(),"eccPublicKey"))) {
                    successMsg = "-> SUCCESS: VERIFY_HOLDER_SIGNATURE on " + rTicketIn.getRTicketType() + " RTICKET";
                    SimpleLogger.simpleLog("info",successMsg);
                    return rTicketIn;
                } else { // pragma: no cover -> Weird R-Ticket
                    failureMsg = "-> FAILURE: VERIFY_HOLDER_SIGNATURE on "+ rTicketIn.getRTicketType() + " RTICKET";
                    SimpleLogger.simpleLog("error",failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            case RTicket.TYPE_DATA_RTOKEN:
                // No DEVICE_SIGNATURE
                SimpleLogger.simpleLog("info",successMsg);
                return rTicketIn;
            default: // pragma: no cover -> Weird R-Ticket
                SimpleLogger.simpleLog("error",failureMsg);
                throw new RuntimeException(failureMsg);
        }
    }

    // Verify ECC Signature on RTicket
    private boolean _verifyDeviceSignatureOnRTicket(RTicket signedRTicket, ECPublicKey eccPublicKey) {
        // Get Signature on RTicket
        byte[] signatureByte = SerializationUtil.base64StrBackToByte(signedRTicket.getDeviceSignature());

        // Verify Signature on Signed RTicket, but Prevent side effect on Signed RTicket
        RTicket unsignedRTicket;
        try {
            unsignedRTicket = deepCopy(signedRTicket);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        unsignedRTicket.setDeviceSignature(null);

        String unsignedRTicketStr = RTicket.rTicketToJsonStr(unsignedRTicket);
        byte[] unsignedRTicketByte = SerializationUtil.strToByte(unsignedRTicketStr);

        // Verify Signature
        try {
            return ECC.verifySignature(signatureByte, unsignedRTicketByte, eccPublicKey);
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchProviderException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private RTicket deepCopy(RTicket originalRTicket) throws IOException, ClassNotFoundException {
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(originalRTicket);
        oos.flush();
        oos.close();
        bos.close();

        // Deserialize from byte array
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        RTicket copyRTicket = (RTicket) ois.readObject();
        ois.close();
        bis.close();

        return copyRTicket;
    }
}

package ureka.framework.logic.stage_worker;

import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MsgVerifierUTicket {
    private ThisDevice thisDevice;

    public MsgVerifierUTicket(ThisDevice thisDevice) {
        this.thisDevice = thisDevice;
    }

    //////////////////////////////////////////////////////
    // Message Verification Flow
    //////////////////////////////////////////////////////
    public static UTicket verifyJsonSchema(String arbitraryJson) {
        String success_msg = "-> SUCCESS: VERIFY_JSON_SCHEMA";
        String failure_msg = "-> FAILURE: VERIFY_JSON_SCHEMA: ";

        try {
            UTicket uTicketIn = UTicket.jsonStrToUTicket(arbitraryJson);
            SimpleLogger.simpleLog("info", success_msg);
            return uTicketIn;
        } catch (RuntimeException e) {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failure_msg + e.getMessage());
            throw e;
        }
    }

    public static UTicket verifyProtocolVersion(UTicket uTicketIn) {
        String success_msg = "-> SUCCESS: VERIFY_PROTOCOL_VERSION = " + uTicketIn.getProtocolVersion();
        String failure_msg = "-> FAILURE: VERIFY_PROTOCOL_VERSION = " + uTicketIn.getProtocolVersion();

        if (Objects.equals(uTicketIn.getProtocolVersion(), UTicket.PROTOCOL_VERSION)) {
            SimpleLogger.simpleLog("info", success_msg);
            return uTicketIn;
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }

    public static UTicket verifyUTicketId(UTicket uTicketIn) throws Exception {
        String success_msg = "-> SUCCESS: VERIFY_UTICKET_ID";
        String failure_msg = "-> FAILURE: VERIFY_UTICKET_ID";

        // Verify UTicket Id (Hash-based)
        UTicket ticketWithoutIdANdSig = new UTicket(uTicketIn); // We build another instance to achieve deep copy.
        ticketWithoutIdANdSig.setUTicketId(null);
        ticketWithoutIdANdSig.setIssuerSignature(null);
        String generatedHash = ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(ticketWithoutIdANdSig));

        if (Objects.equals(generatedHash, uTicketIn.getUTicketId())) {
            SimpleLogger.simpleLog("info", success_msg);
            return uTicketIn;
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }

    public static UTicket verifyUTicketType(UTicket uTicketIn) {
        String success_msg = "-> SUCCESS: VERIFY_UTICKET_TYPE = " + uTicketIn.getUTicketType();
        String failure_msg = "-> FAILURE: VERIFY_UTICKET_TYPE = " + uTicketIn.getUTicketType();

        for (String type : UTicket.LEGAL_UTICKET_TYPES) {
            if (Objects.equals(uTicketIn.getUTicketType(), type)) {
                SimpleLogger.simpleLog("info", success_msg);
                return uTicketIn;
            }
        }
        // pragma: no cover -> Weird U-Ticket
        SimpleLogger.simpleLog("error", failure_msg);
        throw new RuntimeException(failure_msg);
    }

    // Because holder do not really know whether this device_id is correct before get RT
    public static UTicket hasDeviceId(UTicket uTicketIn) {
        String success_msg = "-> SUCCESS: HAS_DEVICE_ID = " + uTicketIn.getDeviceId();
        String failure_msg = "-> FAILURE: HAS_DEVICE_ID = " + uTicketIn.getDeviceId();

        if (uTicketIn.getDeviceId() != null) {
            SimpleLogger.simpleLog("info", success_msg);
            return uTicketIn;
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }

    // However, device can verify whether this device_id is correct
    public UTicket verifyDeviceId(UTicket uticketIn) {
        String success_msg = "-> SUCCESS: VERIFY_DEVICE_ID = " + uticketIn.getDeviceId();
        String failure_msg = "-> FAILURE: VERIFY_DEVICE_ID = " + uticketIn.getDeviceId();

        if (Objects.equals(uticketIn.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)) {
            if (Objects.equals(uticketIn.getDeviceId(), "no_id")) {
                SimpleLogger.simpleLog("info", success_msg);
                return uticketIn;
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failure_msg);
                throw new RuntimeException(failure_msg);
            }
        } else if (Objects.equals(uticketIn.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)
            || Objects.equals(uticketIn.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)
            || Objects.equals(uticketIn.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)
            || Objects.equals(uticketIn.getUTicketType(), UTicket.TYPE_CMD_UTOKEN)
            || Objects.equals(uticketIn.getUTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            if (Objects.equals(uticketIn.getDeviceId(), this.thisDevice.getDevicePubKeyStr())) {
                SimpleLogger.simpleLog("info", success_msg);
                return uticketIn;
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failure_msg);
                throw new RuntimeException(failure_msg);
            }
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failure_msg);
            throw new RuntimeException(failure_msg);
        }
    }

    public UTicket verifyTicketOrder(UTicket uTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_TICKET_ORDER";
        String failureMsg = "-> FAILURE: VERIFY_TICKET_ORDER";

        if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)) {
            if (this.thisDevice.getTicketOrder() == 0) {
                if (uTicketIn.getTicketOrder() == 0) {
                    SimpleLogger.simpleLog("info", successMsg);
                    return uTicketIn;
                } else {
                    // pragma: no cover -> Weird U-Ticket
                    SimpleLogger.simpleLog("error", failureMsg);
                    throw new RuntimeException(failureMsg);
                }
            } else if (this.thisDevice.getTicketOrder() > 0) {
                failureMsg = "-> FAILURE: VERIFY_TICKET_ORDER: IOT_DEVICE ALREADY INITIALIZED";
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else {
            if (Objects.equals(uTicketIn.getTicketOrder(), this.thisDevice.getTicketOrder())) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        }
    }

    public UTicket verifyHolderId(UTicket uTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_HOLDER_ID";
        String failureMsg = "-> FAILURE: VERIFY_HOLDER_ID";

        if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)) {
            // With HOLDER_ID
            if (uTicketIn.getHolderId() != null) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
            // HOLDER_ID must be Device Owner
            if (Objects.equals(uTicketIn.getHolderId(), this.thisDevice.getOwnerPubKeyStr())) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_CMD_UTOKEN)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            // No HOLDER_ID
            SimpleLogger.simpleLog("info", successMsg);
            return uTicketIn;
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    public static UTicket verifyTaskScope(UTicket uTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_TASK_SCOPE";
        String failureMsg = "-> FAILURE: VERIFY_TASK_SCOPE";

        if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_CMD_UTOKEN)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            // No TASK_SCOPE
            SimpleLogger.simpleLog("info", successMsg);
            return uTicketIn;
        } else if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)) {
            if (uTicketIn.getTaskScope() != null) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
            Map<String, String> map = new HashMap<>();
            map.put("ALL", "allow");
            if (Objects.equals(uTicketIn.getTaskScope(), SerializationUtil.dictToJsonStr(map))) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    public static UTicket verifyPS(UTicket uTicketIn) {
        String successMsg = "-> SUCCESS: VERIFY_PS";
        String failureMsg = "-> FAILURE: VERIFY_PS";

        if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)) {
            // No PS
            SimpleLogger.simpleLog("info", successMsg);
            return uTicketIn;
        } else if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_CMD_UTOKEN)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            if (uTicketIn.getAssociatedPlaintextCmd() != null
                && uTicketIn.getCiphertextCmd() != null
                && uTicketIn.getIvData() != null
                && uTicketIn.getGcmAuthenticationTagCmd() != null) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                // pragma: no cover -> Weird U-Ticket
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    public UTicket verifyIssuerSignature(UTicket uTicketIn) throws Exception {
        String successMsg = "-> SUCCESS: VERIFY_ISSUER_SIGNATURE on " + uTicketIn.getUTicketType() + " UTICKET";
        String failureMsg = "-> FAILURE: VERIFY_ISSUER_SIGNATURE on " + uTicketIn.getUTicketType() + " UTICKET";

        // Verify ISSUER_SIGNATURE
        if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_CMD_UTOKEN)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            // No ISSUER_SIGNATURE
            SimpleLogger.simpleLog("info", successMsg);
            return uTicketIn;
        } else if (Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET)
            || Objects.equals(uTicketIn.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)) {
            if (_verifyIssuerSignatureOnUTicket(uTicketIn, this.thisDevice.getOwnerPubKey())) {
                SimpleLogger.simpleLog("info", successMsg);
                return uTicketIn;
            } else {
                SimpleLogger.simpleLog("error", failureMsg);
                throw new RuntimeException(failureMsg);
            }
        } else {
            // pragma: no cover -> Weird U-Ticket
            SimpleLogger.simpleLog("error", failureMsg);
            throw new RuntimeException(failureMsg);
        }
    }

    //////////////////////////////////////////////////////
    // Verify ECC Signature on UTicket
    //////////////////////////////////////////////////////
    private static boolean _verifyIssuerSignatureOnUTicket(UTicket signedUTicket, ECPublicKey publicKey) throws Exception {
        // Get signature on UTicket
        byte[] signatureByte = SerializationUtil.base64StrToSignature(signedUTicket.getIssuerSignature());

        // Verify Signature on Signed UTicket, but Prevent side effect on Signed UTicket
        UTicket unsignedUTicket = new UTicket(signedUTicket);
        unsignedUTicket.setIssuerSignature(null);

        String unsignedUTicketStr = UTicket.uTicketToJsonStr(unsignedUTicket);
        byte[] unsignedUTicketByte = SerializationUtil.strToByte(unsignedUTicketStr);

        // Verify signature
        return ECC.verifySignature(signatureByte, unsignedUTicketByte, publicKey);
    }
}

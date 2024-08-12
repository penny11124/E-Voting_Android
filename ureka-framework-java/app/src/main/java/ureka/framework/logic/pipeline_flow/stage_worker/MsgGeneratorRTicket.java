package ureka.framework.logic.pipeline_flow.stage_worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MsgGeneratorRTicket {
    private ThisDevice thisDevice;
    private ThisPerson thisPerson;
    private Map<String, OtherDevice> deviceTable;
    public MsgGeneratorRTicket(ThisDevice thisDevice, ThisPerson thisPerson, Map<String, OtherDevice> deviceTable) {
        this.thisDevice = thisDevice;
        this.thisPerson = thisPerson;
        this.deviceTable = deviceTable;
    }

    // Message Generation Flow
    public RTicket generateArbitraryRTicket(Map<String, String> arbitraryDict) {
        String successMsg = "-> SUCCESS: GENERATE_RTICKET";
        String failureMsg = "-> FAILURE: GENERATE_RTICKET";

        // Unsigned RTicket
        RTicket newRTicket;
        try {
            newRTicket = new RTicket(arbitraryDict);  // something to fixed
        } catch (Exception e) {
            // SimpleLogger.simpleLog("error", failureMsg + ": " + e.getMessage());
            throw new RuntimeException(failureMsg);
        }

        // "device"
        if(Objects.equals(newRTicket.getRTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_CRKE1_RTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_CRKE3_RTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_DATA_RTOKEN) ||
                Objects.equals(newRTicket.getRTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            newRTicket.setTicketOrder(this.thisDevice.getTicketOrder());
        }
        // "holder"
        else if (newRTicket.getRTicketType() == RTicket.TYPE_CRKE2_RTICKET) {
            newRTicket.setTicketOrder(this.deviceTable.get(newRTicket.getDeviceId()).getTicketOrder());
        }
        // pragma: no cover -> Shouldn't Reach Here
        else {
            throw new RuntimeException("Shouldn't Reach Here");
        }

        // Generate RTicket Id (Hash-based)
        try {
            newRTicket.setRTicketId(ECDH.generateSha256HashStr(RTicket.rTicketToJsonStr(newRTicket))); //something to fixed
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Signed RTicket
        // Generate Signature
        // "device"
        if(Objects.equals(newRTicket.getRTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_CRKE1_RTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_CRKE3_RTICKET) ||
                Objects.equals(newRTicket.getRTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            newRTicket = _addIssuerSignatureOnRTicket(newRTicket,this.thisDevice.getDevicePrivKey());
            SimpleLogger.simpleLog("info", successMsg);
        }
        // "holder"
        else if (Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_CRKE2_RTICKET)) {
            newRTicket = _addIssuerSignatureOnRTicket(newRTicket,this.thisPerson.getPersonPrivKey());
            SimpleLogger.simpleLog("info", successMsg);
        }
        // "device"
        else if (Objects.equals(newRTicket.getRTicketType(), RTicket.TYPE_DATA_RTOKEN)) {
            // NO Signature
            SimpleLogger.simpleLog("info", successMsg);
        }
        // pragma: no cover -> Shouldn't Reach Here
        else {
            throw new RuntimeException("Shouldn't Reach Here");
        }

        return newRTicket;
    }

    // something to fixed
    // Add ECC Signature on RTicket
    private RTicket _addIssuerSignatureOnRTicket(RTicket unsignedRTicket, ECPrivateKey privateKey) {
        // Message
        String unsignedRTicketStr = RTicket.rTicketToJsonStr(unsignedRTicket);  // something to fixed
        byte[] unsignedRTicketByte = SerializationUtil.strToByte(unsignedRTicketStr);

        // Sign Signature
        byte[] signatureByte;
        try {
            signatureByte = ECC.signSignature(unsignedRTicketByte, privateKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException |
                 SignatureException e) {
            throw new RuntimeException(e);
        }

        // Add Signature on New Signed RTicket, but Prevent side effect on Unsigned RTicket
        RTicket signedRTicket;
        try {
            signedRTicket = deepCopy(unsignedRTicket);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        signedRTicket.setDeviceSignature(SerializationUtil.byteToBase64Str(signatureByte));

        return signedRTicket;
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

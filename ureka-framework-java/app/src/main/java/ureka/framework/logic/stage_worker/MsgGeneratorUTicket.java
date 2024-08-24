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
import java.security.interfaces.ECPrivateKey;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class MsgGeneratorUTicket {
    private ThisDevice thisDevice;
    private ThisPerson thisPerson;
    private Map<String, OtherDevice> deviceTable;
    public MsgGeneratorUTicket(ThisDevice thisDevice, ThisPerson thisPerson, Map<String, OtherDevice> deviceTable) {
        this.thisDevice = thisDevice;
        this.thisPerson = thisPerson;
        this.deviceTable = deviceTable;
    }

    // Message Generation Flow
    public UTicket generateArbitraryUTicket(Map<String, String> arbitraryDict) {
        String successMsg = "-> SUCCESS: GENERATE_UTICKET";
        String failureMsg = "-> FAILURE: GENERATE_UTICKET";

        // Unsigned UTicket
        UTicket newUTicket;
        try {
            newUTicket = new UTicket(arbitraryDict);  // something to fixed
        } catch (Exception e) {
            // SimpleLogger.simpleLog("error", failureMsg + ": " + e.getMessage());
            throw new RuntimeException(failureMsg);
        }

        // Generate Ticket Order
        if (Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET)) {
            newUTicket.setTicketOrder(0);
        } else {
            newUTicket.setTicketOrder(this.deviceTable.get(newUTicket.getDeviceId()).getTicketOrder());
        }

        // Generate UTicket Id (Hash-based)
        try {
            newUTicket.setUTicketId(ECDH.generateSha256HashStr(UTicket.uTicketToJsonStr(newUTicket)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Signed UTicket
        if (Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_INITIALIZATION_UTICKET) ||
                Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_SELFACCESS_UTICKET) ||
                Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_CMD_UTOKEN) ||
                Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_ACCESS_END_UTOKEN)) {
            // No ISSUER_SIGNATURE
            SimpleLogger.simpleLog("info", successMsg);
        } else if (Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_OWNERSHIP_UTICKET) ||
                Objects.equals(newUTicket.getUTicketType(), UTicket.TYPE_ACCESS_UTICKET)) {
            newUTicket =this._addIssuerSignatureOnUTicket(newUTicket, this.thisPerson.getPersonPrivKey());
            SimpleLogger.simpleLog("info", successMsg);
        } else {
            throw new RuntimeException("Shouldn't Reach Here");
        }

        return newUTicket;
    }

    // something to fixed
    private UTicket _addIssuerSignatureOnUTicket(UTicket unsignedUTicket, ECPrivateKey privateKey) {
        // Message
        String uTicketToJsonstr = UTicket.uTicketToJsonStr(unsignedUTicket);
        byte[] unsignedUTicketByte = SerializationUtil.strToByte(uTicketToJsonstr);

        // Sign Signature
        byte[] signatureByte;
        try {
            signatureByte = ECC.signSignature(unsignedUTicketByte, privateKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException |
                 SignatureException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Add Signature on New Signed UTicket, but Prevent side effect on Unsigned UTicket
        UTicket signedUTicket;
        try {
            signedUTicket = deepCopy(unsignedUTicket);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        signedUTicket.setIssuerSignature(SerializationUtil.byteToBase64Str(signatureByte));

        return signedUTicket;
    }

    private UTicket deepCopy(UTicket originalUTicket) throws IOException, ClassNotFoundException {
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(originalUTicket);
        oos.flush();
        oos.close();
        bos.close();

        // Deserialize from byte array
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        UTicket copyUTicket = (UTicket) ois.readObject();
        ois.close();
        bis.close();

        return copyUTicket;
    }

}

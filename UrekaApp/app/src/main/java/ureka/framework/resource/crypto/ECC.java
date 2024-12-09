package ureka.framework.resource.crypto;

import android.util.Log;

//import org.bouncycastle.jce.provider.BouncyCastleProvider; // BouncyCastle
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.jce.provider.BouncyCastleProvider; // SpongyCastle -> More preferred on Android

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

public class ECC {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            // Security.addProvider(new BouncyCastleProvider());
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        }
    }

    //////////////////////////////////////////////////////
    // ECC Key Factory
    //////////////////////////////////////////////////////
    public static KeyPair generateKeyPair() throws Exception {
        try {
            return SimpleMeasurer.measureResourceFunc(ECC::_generateKeyPair);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static KeyPair _generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "SC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    //////////////////////////////////////////////////////
    // Sign ECC Signature
    //////////////////////////////////////////////////////
    public static byte[] signSignature(byte[] message, ECPrivateKey privateKey) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECC::_signSignature, message, privateKey);
    }
    private static byte[] _signSignature(byte[] message, ECPrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256withECDSA", "SC");
        signer.initSign(privateKey);
        signer.update(message);
        byte[] signatureBytes = signer.sign();

        ASN1Sequence sequence = (ASN1Sequence) ASN1Sequence.fromByteArray(signatureBytes);
        BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getValue();

        // Convert r and s to byte arrays (big-endian by default)
        byte[] rBytes = SerializationUtil.reverseBytes(r.toByteArray()); // Convert to small-endian
        byte[] sBytes = SerializationUtil.reverseBytes(s.toByteArray()); // Convert to small-endian

        // Convert r and s to Base64\
        String hexR = SerializationUtil.bytesToHex(rBytes);
        String hexS = SerializationUtil.bytesToHex(sBytes);
//        SimpleLogger.simpleLog("info", "MY R = " + hexR);
//        SimpleLogger.simpleLog("info", "MY S = " + hexS);

        return signatureBytes;
    }

    //////////////////////////////////////////////////////
    // Verify ECC Signature
    //////////////////////////////////////////////////////
    public static boolean verifySignature(byte[] signatureIn, byte[] messageIn, ECPublicKey publicKey) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECC::_verifySignature, signatureIn, messageIn, publicKey);
    }
    private static boolean _verifySignature(byte[] signatureIn, byte[] messageIn, ECPublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance("SHA256withECDSA", "SC");
        verifier.initVerify(publicKey);
        verifier.update(messageIn);
        return verifier.verify(signatureIn);
    }
}

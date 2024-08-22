package ureka.framework.resource.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import ureka.framework.resource.logger.SimpleMeasurer;

public class ECC {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    //////////////////////////////////////////////////////
    // ECC Key Factory
    //////////////////////////////////////////////////////
    public static KeyPair generateKeyPair()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        try {
            return SimpleMeasurer.measureResourceFunc(ECC::_generateKeyPair);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static KeyPair _generateKeyPair()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    //////////////////////////////////////////////////////
    // Sign ECC Signature
    //////////////////////////////////////////////////////
    public static byte[] signSignature(byte[] message, ECPrivateKey privateKey) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECC::_sign_signature, message, privateKey);
    }
    private static byte[] _sign_signature(byte[] message, ECPrivateKey privateKey)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
        signer.initSign(privateKey);
        signer.update(message);
        return signer.sign();
    }

    //////////////////////////////////////////////////////
    // Verify ECC Signature
    //////////////////////////////////////////////////////
    public static boolean verifySignature(byte[] signatureIn, byte[] messageIn, ECPublicKey publicKey)
        throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        try {
            return SimpleMeasurer.measureResourceFunc(ECC::_verifySignature, signatureIn, messageIn, publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean _verifySignature(byte[] signatureIn, byte[] messageIn, ECPublicKey publicKey)
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
        verifier.initVerify(publicKey);
        verifier.update(messageIn);
        return verifier.verify(signatureIn);
    }
}

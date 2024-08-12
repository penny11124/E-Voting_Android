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
    public static KeyPair generate_key_pair()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        try {
            return SimpleMeasurer.measureResourceFunc(ECC::_generate_key_pair);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static KeyPair _generate_key_pair()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyPairGenerator.initialize(ecSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    //////////////////////////////////////////////////////
    // Sign ECC Signature
    //////////////////////////////////////////////////////
    public static byte[] signSignature(byte[] message, ECPrivateKey privateKey)
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return SimpleMeasurer.measureWorkerFunc((message1, privateKey1) -> {
            try {
                _signSignature(message1, privateKey1);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }, message, privateKey);
    }
    private static byte[] _signSignature(byte[] message, ECPrivateKey privateKey)
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
        return SimpleMeasurer.measureWorkerFunc(ECC::_verifySignature, signatureIn, messageIn, publicKey);
    }
    private static boolean _verifySignature(byte[] signatureIn, byte[] messageIn, ECPublicKey publicKey)
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
        verifier.initVerify(publicKey);
        verifier.update(messageIn);
        return verifier.verify(signatureIn);
    }
}

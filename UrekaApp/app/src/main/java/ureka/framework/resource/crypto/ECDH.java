package ureka.framework.resource.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

public class ECDH {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    //////////////////////////////////////////////////////
    // Random Number Generation
    // pyca/cryptography recommends using operating system's provided random number generator
    //////////////////////////////////////////////////////
    public static byte[] generateRandomByte(int bytesNum) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_generateRandomByte, bytesNum);
    }
    private static byte[] _generateRandomByte(int bytesNum) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[bytesNum];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String generateRandomStr(int bytesNum) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_generateRandomStr, bytesNum);
    }
    private static String _generateRandomStr(int bytesNum) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[bytesNum];
        random.nextBytes(bytes);
        return SerializationUtil.byteToBase64Str(bytes);
    }

    //////////////////////////////////////////////////////
    // Hash Function
    //////////////////////////////////////////////////////
    public static byte[] generateSha256HashBytes(byte[] message) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_generateSha256HashBytes, message);
    }
    private static byte[] _generateSha256HashBytes(byte[] message)
            throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256", "SC");
        return digest.digest(message);
    }

    public static String generateSha256HashStr(String messageStr) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_generateSha256HashStr, messageStr);
    }
    private static String _generateSha256HashStr(String messageStr)
            throws Exception {
        byte[] messageBytes = SerializationUtil.strToByte(messageStr);
        byte[] hashBytes = _generateSha256HashBytes(messageBytes);
        return SerializationUtil.byteToBase64Str(hashBytes);
    }

    //////////////////////////////////////////////////////
    // ECDH Key Factory
    //   In a key agreement protocol deriving cryptographic keys from a Diffie-Hellman exchange
    //       can derive a salt value from public nonces exchanged and authenticated between communicating parties
    //       as part of the key agreement.
    //       <Ref> https://datatracker.ietf.org/doc/html/rfc5869.html#section-3.1
    //   Further recommend ECDHE (ECDH, ephemeral) if consider Forward Secrecy
    //       -> But need exchange ephemeral public keys & bring more overhead.
    //       -> A practical solution for ephemeral maybe periodically re-exchange a HKDF-derived session key.
    //       -> How to set the period depends on the trade-off between security and overhead.
    //////////////////////////////////////////////////////
    public static byte[] generateEcdhKey(ECPrivateKey serverPrivateKey, byte[] salt, byte[] info, ECPublicKey peerPublicKey) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_generateEcdhKey, serverPrivateKey, salt, info, peerPublicKey);
    }
    private static byte[] _generateEcdhKey(ECPrivateKey serverPrivateKey, byte[] salt, byte[] info, ECPublicKey peerPublicKey)
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        try {
            // Perform ECDH
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "SC");
            keyAgreement.init(serverPrivateKey);
            keyAgreement.doPhase(peerPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            // Perform Key Derivation [HKDF, HMAC-based Extract-and-Expand KDF]
            HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
            HKDFParameters hkdfParams = new HKDFParameters(sharedSecret, salt, info);
            hkdf.init(hkdfParams);
            byte[] derivedKey = new byte[32]; // Length of the derived key in bytes
            hkdf.generateBytes(derivedKey, 0, derivedKey.length);

            return derivedKey;
        } catch (NoSuchAlgorithmException e) {
            String failureMsg = "generateEcdhKey: NoSuchAlgorithmException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchProviderException e) {
            String failureMsg = "generateEcdhKey: NoSuchProviderException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidKeyException e) {
            String failureMsg = "generateEcdhKey: InvalidKeyException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        }

    }

    //////////////////////////////////////////////////////
    // ECDH AES Encryption (CBC Mode)
    //   Need Padding:
    //       https://cryptography.io/en/3.4.2/hazmat/primitives/padding.html
    //   Need HMAC:
    //       If ureka protocol applies Command UTicket/Data UTicket with signatures on them,
    //           the AES-CBC can be used and the HMAC can be omitted.
    //       However, AES+HMAC may provide better performance than Command UTicket/Data UTicket.
    //                   (Because HMAC verification may be cheaper than Signature verification.)
    //           In this case, AES-GCM (Galois Counter Mode) is better,
    //               which is a  AEAD (authenticated encryption with additional data) mode so that HMAC are not necessary.
    //////////////////////////////////////////////////////
    public static byte[][] cbcEncrypt(byte[] plaintext, byte[] key) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_cbcEncrypt, plaintext, key);
    }
    private static byte[][] _cbcEncrypt(byte[] plaintext, byte[] key)
        throws Exception {
        // Randomly generate IV (Initialization Vector)
        //   IV must be the same number of bytes as the blockSize of the cipher.
        //   IV must be random bytes.
        //   IV do not need to be kept secret and they can be included in a transmitted message.
        //   For better security, each time something is encrypted a new IV should be generated.
        //   Do not reuse an IV with a given key, and particularly do not use a constant IV, i.e.:
        //       iv = b"1234567890abcdef"  # Dangerous.
        //       iv = generateRandomByte(16) # Recommended, but need be transmitted & perform authenticity check every time.
        try {
            byte[] iv = generateRandomByte(16);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "SC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] paddedMessage = cipher.doFinal(plaintext);

            return new byte[][] { paddedMessage, iv };
        } catch (IllegalBlockSizeException e) {
            String failureMsg = "cbcEncrypt: IllegalBlockSizeException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidAlgorithmParameterException e) {
            String failureMsg = "cbcEncrypt: InvalidAlgorithmParameterException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidKeyException e) {
            String failureMsg = "cbcEncrypt: InvalidKeyException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (BadPaddingException e) {
            String failureMsg = "cbcEncrypt: BadPaddingException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchAlgorithmException e) {
            String failureMsg = "cbcEncrypt: NoSuchAlgorithmException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchPaddingException e) {
            String failureMsg = "cbcEncrypt: NoSuchPaddingException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchProviderException e) {
            String failureMsg = "cbcEncrypt: NoSuchProviderException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        }
    }

    //////////////////////////////////////////////////////
    // ECDH AES Decryption (CBC Mode)
    //////////////////////////////////////////////////////
    public static byte[] cbcDecrypt(byte[] ciphertext, byte[] key, byte[] iv) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_cbcDecrypt, ciphertext, key, iv);
    }
    private static byte[] _cbcDecrypt(byte[] ciphertext, byte[] key, byte[] iv)
        throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException
        , InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "SC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return cipher.doFinal(ciphertext);
        } catch (IllegalBlockSizeException e) {
            String failureMsg = "cbcDecrypt: IllegalBlockSizeException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidAlgorithmParameterException e) {
            String failureMsg = "cbcDecrypt: InvalidAlgorithmParameterException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidKeyException e) {
            String failureMsg = "cbcDecrypt: InvalidKeyException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (BadPaddingException e) {
            String failureMsg = "cbcDecrypt: BadPaddingException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchAlgorithmException e) {
            String failureMsg = "cbcDecrypt: NoSuchAlgorithmException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchPaddingException e) {
            String failureMsg = "cbcDecrypt: NoSuchPaddingException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchProviderException e) {
            String failureMsg = "cbcDecrypt: NoSuchProviderException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        }
    }

    //////////////////////////////////////////////////////
    // ECDH AES Encryption (GCM Mode)
    //   Need NOT Padding & Need NOT HMAC:
    //       But when decryption, it not only needs (ciphertext, key, iv), but also needs (associatedPlaintext, authenticationTag)
    //       <Ref> https://cryptography.io/en/3.4.2/hazmat/primitives/symmetric-encryption.html#cryptography.hazmat.primitives.ciphers.modes.GCM
    //   Can add Associated Plaintext (authenticated but not encrypted) in message:
    //       In ureka protocol, we assume all command & data are authenticated & encrypted,
    //           so associatedPlaintext can be set as None.
    //////////////////////////////////////////////////////
    public static byte[] gcmGenIv() throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_gcmGenIv);
    }
    private static byte[] _gcmGenIv() {
        // Generate a random 96-bit IV.
        return _generateRandomByte(12);
    }

    public static byte[][] gcmEncrypt(byte[] plaintext, byte[] associatePlaintext, byte[] key, byte[] iv) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_gcmEncrypt, plaintext, associatePlaintext, key, iv);
    }
    private static byte[][] _gcmEncrypt(byte[] plaintext, byte[] associatedPlaintext, byte[] key, byte[] iv)
        throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException
        , InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        try {
            if (iv == null) {
                iv = _generateRandomByte(12); // GCM standard recommends a 12-byte IV
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag length

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(associatedPlaintext);

            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] tag = new byte[16];
            System.arraycopy(ciphertext, ciphertext.length - 16, tag, 0, 16);
            byte[] finalCiphertext = new byte[ciphertext.length - 16];
            System.arraycopy(ciphertext, 0, finalCiphertext, 0, ciphertext.length - 16);

            return new byte[][] { finalCiphertext, iv, tag };
        } catch (Exception e) {
            SimpleLogger.simpleLog("error", e.getMessage());
            throw e;
        }
    }

    public static byte[] gcmDecrypt(byte[] ciphertext, byte[] associatedData, byte[] tag, byte[] key, byte[] iv) throws Exception {
        return SimpleMeasurer.measureResourceFunc(ECDH::_gcmDecrypt, ciphertext, associatedData, tag, key, iv);
    }
    private static byte[] _gcmDecrypt(byte[] ciphertext, byte[] associatedData, byte[] tag, byte[] key, byte[] iv)
        throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException
        , InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        try {
            byte[] combinedCiphertext = new byte[ciphertext.length + tag.length];
            System.arraycopy(ciphertext, 0, combinedCiphertext, 0, ciphertext.length);
            System.arraycopy(tag, 0, combinedCiphertext, ciphertext.length, tag.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag length

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(associatedData);

            return cipher.doFinal(combinedCiphertext);
        } catch (IllegalBlockSizeException e) {
            String failureMsg = "gcmDecrypt: IllegalBlockSizeException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidAlgorithmParameterException e) {
            String failureMsg = "gcmDecrypt: InvalidAlgorithmParameterException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (InvalidKeyException e) {
            String failureMsg = "gcmDecrypt: InvalidKeyException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (BadPaddingException e) {
            String failureMsg = "gcmDecrypt: BadPaddingException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchAlgorithmException e) {
            String failureMsg = "gcmDecrypt: NoSuchAlgorithmException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchPaddingException e) {
            String failureMsg = "gcmDecrypt: NoSuchPaddingException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        } catch (NoSuchProviderException e) {
            String failureMsg = "gcmDecrypt: NoSuchProviderException occurs.";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw e;
        }
    }
}

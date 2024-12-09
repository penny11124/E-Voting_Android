package ureka.framework.resource.crypto;

import com.google.gson.Gson;

import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;

import java.io.Serial;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import kotlin.jvm.JvmSerializableLambda;
import ureka.framework.resource.logger.SimpleLogger;

public class SerializationUtil {

//    static {
//        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
//            // Security.addProvider(new BouncyCastleProvider());
//            Security.insertProviderAt(new BouncyCastleProvider(), 1);
//        }
//    }

    ////////// String <-> Bytes //////////
    public static byte[] strToByte(String string) {
        if (string == null) {
            return null;
        }
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public static String byteBackToStr(byte[] byteArray) {
        try {
            return new String(byteArray, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("byteBackToStr: Decode failed.", e);
        }
    }

    ////////// Bytes <-> Base64Str //////////
    public static String byteToBase64Str(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    public static byte[] base64StrBackToByte(String base64String) {
        try {
            assert (base64String != null);
            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("base64StrBackToByte: Decode failed.", e);
        }
    }

    ////////// Dict <-> JSONStr //////////
    private static final Gson gson = new Gson();

    public static String dictToJsonStr(Map<String, String> dict) {
        return gson.toJson(dict);
    }

    public static Map<String, String> jsonStrToDict(String jsonStr) {
        try {
            return gson.fromJson(jsonStr, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("NOT VALID JSON", e);
        }
    }

    ////////// ECPublicKey/ECPrivateKey <-> Byte //////////
    public static byte[] _keyToByte(Object keyObj, String keyType) {
        try {
            if (keyType.equals("eccPublicKey") && keyObj instanceof ECPublicKey) {
                return ((ECPublicKey) keyObj).getEncoded();
            } else if (keyType.equals("eccPrivateKey") && keyObj instanceof ECPrivateKey) {
                return SerializationUtil.hexToBytes(SerializationUtil.ecPrivateKeyToHex((ECPrivateKey) keyObj));
            } else {
                throw new RuntimeException("_keyToByte: Key type not supported.");
            }
        } catch (Exception e) {
            throw new RuntimeException("_keyToByte: Conversion failed.", e);
        }
    }

    public static Object _byteToKey(byte[] keyByte, String keyType) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            if (keyType.equals("eccPublicKey")) {
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyByte);
                return keyFactory.generatePublic(keySpec);
            } else if (keyType.equals("eccPrivateKey")) {
                return SerializationUtil.hexToECPrivateKey(SerializationUtil.bytesToHex(keyByte));
            } else {
                throw new RuntimeException("_byteToKey: Key type not supported.");
            }
        } catch (Exception e) {
            throw new RuntimeException("_byteToKey: Conversion failed.", e);
        }
    }

    ////////// ECPublicKey/ECPrivateKey <-> String //////////
    public static String keyToStr(Object keyObj, String keyType) {
        if (keyObj instanceof ECPrivateKey) {
            return byteToBase64Str(_keyToByte(keyObj, keyType));
        } else if (keyObj instanceof ECPublicKey) {
            ECPublicKey publicKey = (ECPublicKey) keyObj;

            // Extract the public key's elliptic curve point
            BigInteger x = publicKey.getW().getAffineX();
            BigInteger y = publicKey.getW().getAffineY();

            byte[] xBytes = x.toByteArray();
            byte[] yBytes = y.toByteArray();
//            xBytes = SerializationUtil.reverseBytes(xBytes);
//            yBytes = SerializationUtil.reverseBytes(yBytes);
//            SimpleLogger.simpleLog("info", "xHex = " + SerializationUtil.bytesToHex(xBytes));
//            SimpleLogger.simpleLog("info", "yHex = " + SerializationUtil.bytesToHex(yBytes));

            // Encode the byte arrays to Base64 strings
            String xBase64 = Base64.getEncoder().encodeToString(xBytes);
            String yBase64 = Base64.getEncoder().encodeToString(yBytes);
            return xBase64 + "-" + yBase64;
        } else {
            throw new IllegalArgumentException("keyToStr: Illegal argument.");
        }
    }

    public static Object strToKey(String keyStr, String keyType) {
        if (Objects.equals(keyType, "eccPrivateKey")) {
            return _byteToKey(base64StrBackToByte(keyStr), keyType);
        } else if (Objects.equals(keyType, "eccPublicKey")) {
            try {
                String[] base64Int = keyStr.split("-");

                byte[] xBytes = Base64.getDecoder().decode(base64Int[0]);
                byte[] yBytes = Base64.getDecoder().decode(base64Int[1]);
//                xBytes = SerializationUtil.reverseBytes(xBytes);
//                yBytes = SerializationUtil.reverseBytes(yBytes);

                SimpleLogger.simpleLog("info", "strToKey: x = " + bytesToHex(xBytes));
                SimpleLogger.simpleLog("info", "strToKey: y = " + bytesToHex(yBytes));

                // Convert byte arrays to BigInteger
                BigInteger x = new BigInteger(1, xBytes);
                BigInteger y = new BigInteger(1, yBytes);

                // Create an ECPoint from x and y
                ECPoint ecPoint = new ECPoint(x, y);

                // Get the ECParameterSpec for the curve (e.g., secp256k1)
                // This example uses the standard named curve "secp256k1"
                ECGenParameterSpec ecGenSpec = new java.security.spec.ECGenParameterSpec("secp256k1");
                KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("EC", "SC");
                kpg.initialize(ecGenSpec);
                ECParameterSpec ecSpec = ((ECPublicKey) kpg.generateKeyPair().getPublic()).getParams();

                // Create the ECPublicKeySpec
                ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecSpec);

                // Generate the public key using KeyFactory
                KeyFactory keyFactory = KeyFactory.getInstance("EC", "SC");
                return keyFactory.generatePublic(ecPublicKeySpec);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("strToKey: Unknown key type.");
        }
    }

    ////////// Signature <-> Base64Str //////////
    public static String signatureToBase64Str(byte[] signature) {
        try {
            // Extract r and s from the ASN.1/DER-encoded signature
            ASN1Sequence sequence = (ASN1Sequence) ASN1Sequence.fromByteArray(signature);
            BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
            BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getValue();

            // Convert r and s to byte arrays (big-endian by default)
            byte[] rBytes = r.toByteArray();
            byte[] sBytes = s.toByteArray();
//            rBytes = reverseBytes(rBytes); // Convert to small-endian
//            sBytes = reverseBytes(sBytes); // Convert to small-endian

            // Convert r and s to Base64
            String rBase64 = Base64.getEncoder().encodeToString(rBytes);
            String sBase64 = Base64.getEncoder().encodeToString(sBytes);

            return rBase64 + "-" + sBase64;
        } catch (Exception e) {
            throw new RuntimeException("signatureToBase64Str: Decode failed.", e);
        }
    }

    public static byte[] base64StrToSignature(String base64Str) {
        try {
            String[] base64Strs = base64Str.split("-");
            // Decode Base64 strings to byte arrays
            byte[] rBytes = Base64.getDecoder().decode(base64Strs[0]);
            byte[] sBytes = Base64.getDecoder().decode(base64Strs[1]);
//            rBytes = SerializationUtil.reverseBytes(rBytes);
//            sBytes = SerializationUtil.reverseBytes(sBytes);
            SimpleLogger.simpleLog("info", "base64StrToSignature: r = " + SerializationUtil.bytesToHex(rBytes));
            SimpleLogger.simpleLog("info", "base64StrToSignature: s = " + SerializationUtil.bytesToHex(sBytes));

            // Convert byte arrays to BigInteger
            BigInteger r = new BigInteger(1, rBytes);
            BigInteger s = new BigInteger(1, sBytes);

            // Create ASN.1/DER-encoded sequence from r and s
            ASN1Integer rAsn1 = new ASN1Integer(r);
            ASN1Integer sAsn1 = new ASN1Integer(s);
            ASN1Sequence sequence = new DERSequence(new ASN1Integer[]{rAsn1, sAsn1});
            return sequence.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("base64StrToSignature: Decode failed.", e);
        }
    }

    ////////// byte[] <-> hex string //////////
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hexadecimal string");
        }

        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            String hexPair = hexString.substring(i, i + 2);
            byteArray[i / 2] = (byte) Integer.parseInt(hexPair, 16);
        }

        return byteArray;
    }

    ////////// ECPrivateKey <-> hex string //////////
    public static String ecPrivateKeyToHex(ECPrivateKey ecPrivateKey) {
        BigInteger privateKeyValue = ecPrivateKey.getS();

        byte[] privateKeyBytes = privateKeyValue.toByteArray();

        if (privateKeyBytes[0] == 0) {
            byte[] temp = new byte[privateKeyBytes.length - 1];
            System.arraycopy(privateKeyBytes, 1, temp, 0, temp.length);
            privateKeyBytes = temp;
        }
        return SerializationUtil.bytesToHex(privateKeyBytes);
    }

    public static ECPrivateKey hexToECPrivateKey(String hexString) throws Exception {
        byte[] privateKeyBytes = SerializationUtil.hexToBytes(hexString);

        // Create a BigInteger from the byte array (this is the 's' value of the private key)
        BigInteger privateKeyValue = new BigInteger(1, privateKeyBytes); // Ensure positive

        // Define the EC parameters for secp256k1
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ECNamedCurveTable.getParameterSpec("secp256k1"));

        // Use KeyFactory to generate the ECPrivateKey
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "SC");

        return (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    public static byte[] reverseBytes(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversed[i] = bytes[bytes.length - 1 - i];
        }
        return reversed;
    }
}

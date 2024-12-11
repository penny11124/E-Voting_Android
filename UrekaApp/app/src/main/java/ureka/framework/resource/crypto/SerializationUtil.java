package ureka.framework.resource.crypto;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
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

    //////////////////// Basic Serialization ////////////////////
    ////////// Bytes reversion //////////
    public static byte[] reverseBytes(byte[] byteArray) {
        if (byteArray == null) {
            throw new IllegalArgumentException("reverseBytes: ByteArray is null.");
        }
        byte[] reversed = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            reversed[i] = byteArray[byteArray.length - 1 - i];
        }
        return reversed;
    }

    ////////// String <-> Bytes //////////
    public static byte[] strToBytes(String string) {
        if (string == null) {
            throw new IllegalArgumentException("strToBytes: String is null.");
        }
        return string.getBytes(StandardCharsets.UTF_8);
    }
    public static String bytesToStr(byte[] byteArray) {
        if (byteArray == null) {
            throw new IllegalArgumentException("bytesToStr: ByteArray is null.");
        }
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    ////////// Hex <-> Bytes //////////
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException("hexToBytes: String is null.");
        } else if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("hexToBytes: Invalid hex string length.");
        }
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            String hexPair = hexString.substring(i, i + 2);
            byteArray[i / 2] = (byte) Integer.parseInt(hexPair, 16);
        }
        return byteArray;
    }
    public static String bytesToHex(byte[] byteArray) {
        if (byteArray == null) {
            throw new IllegalArgumentException("bytesToHex: ByteArray is null.");
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

    ////////// Base64Str <-> Bytes //////////
    public static byte[] base64ToBytes(String base64String) {
        if (base64String == null) {
            throw new IllegalArgumentException("base64ToBytes: String is null.");
        }
        return Base64.getDecoder().decode(base64String);
    }
    public static String bytesToBase64(byte[] byteArray) {
        if (byteArray == null) {
            throw new IllegalArgumentException("bytesToBase64: ByteArray is null.");
        }
        return Base64.getEncoder().encodeToString(byteArray);
    }

    ////////// Map <-> JSON //////////
    private static final Gson gson = new Gson();
    public static String mapToJson(Map<String, String> map) {
        if (map == null) {
            throw new IllegalArgumentException("mapToJson: Map is null.");
        }
        return gson.toJson(map);
    }
    public static Map<String, String> jsonToMap(String jsonString) {
        if (jsonString == null) {
            throw new IllegalArgumentException("jsonToMap: String is null.");
        }
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(jsonString, mapType);
    }

    //////////////////// Key Serialization ////////////////////
    ////////// PublicKey <-> Base64 //////////
    public static String publicKeyToBase64(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKeyToBase64: PublicKey is null.");
        }
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        BigInteger x = ecPublicKey.getW().getAffineX();
        BigInteger y = ecPublicKey.getW().getAffineY();
        byte[] xBytes = x.toByteArray();
        byte[] yBytes = y.toByteArray();
        if (xBytes.length > 32) {
            xBytes = Arrays.copyOfRange(xBytes, xBytes.length - 32, xBytes.length);
        }
        if (yBytes.length > 32) {
            yBytes = Arrays.copyOfRange(yBytes, yBytes.length - 32, yBytes.length);
        }
        String xBase64 = SerializationUtil.bytesToBase64(xBytes);
        String yBase64 = SerializationUtil.bytesToBase64(yBytes);
        return xBase64 + "-" + yBase64;
    }
    public static PublicKey base64ToPublicKey(String base64String) throws Exception {
        if (base64String == null) {
            throw new IllegalArgumentException("base64ToPublicKey: String is null.");
        } else if (!base64String.contains("-")) {
            throw new IllegalArgumentException("base64ToPublicKey: Invalid Base64 string.");
        }
        String[] parts = base64String.split("-");
        String xBase64 = parts[0];
        String yBase64 = parts[1];
        byte[] xBytes = SerializationUtil.base64ToBytes(xBase64);
        byte[] yBytes = SerializationUtil.base64ToBytes(yBase64);
        BigInteger x = new BigInteger(1, xBytes);
        BigInteger y = new BigInteger(1, yBytes);

        ECPoint ecPoint = new ECPoint(x, y);
        ECGenParameterSpec ecGenSpec = new java.security.spec.ECGenParameterSpec("secp256k1");
        KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("EC", "SC");
        kpg.initialize(ecGenSpec);
        ECParameterSpec ecSpec = ((ECPublicKey) kpg.generateKeyPair().getPublic()).getParams();
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "SC");
        return keyFactory.generatePublic(ecPublicKeySpec);
    }

    ////////// PrivateKey <-> Base64 //////////
    public static String privateKeyToBase64(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKeyToBase64: PrivateKey is null.");
        }
        byte[] privateKeyBytes = privateKey.getEncoded();
        return SerializationUtil.bytesToBase64(privateKeyBytes);
    }
    public static PrivateKey base64ToPrivateKey(String base64String) throws Exception {
        if (base64String == null) {
            throw new IllegalArgumentException("base64ToPrivateKey: String is null.");
        }
        byte[] privateKeyBytes = SerializationUtil.base64ToBytes(base64String);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    ////////// PublicKey <-> Hex //////////
    public static String[] publicKeyToHex(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKeyToHex: PublicKey is null.");
        }
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        BigInteger x = ecPublicKey.getW().getAffineX();
        BigInteger y = ecPublicKey.getW().getAffineY();
        byte[] xBytes = x.toByteArray();
        byte[] yBytes = y.toByteArray();
        String xHex = SerializationUtil.bytesToHex(xBytes);
        String yHex = SerializationUtil.bytesToHex(yBytes);
        return new String[]{xHex, yHex};
    }
    public static PublicKey hexToPublicKey(String xHex, String yHex) throws Exception {
        if (xHex == null) {
            throw new IllegalArgumentException("hexToPublicKey: xHex is null.");
        } else if (yHex == null) {
            throw new IllegalArgumentException("hexToPublicKey: yHex is null.");
        }
        byte[] xBytes = SerializationUtil.hexToBytes(xHex);
        byte[] yBytes = SerializationUtil.hexToBytes(yHex);
        BigInteger x = new BigInteger(1, xBytes);
        BigInteger y = new BigInteger(1, yBytes);

        ECPoint ecPoint = new ECPoint(x, y);
        ECGenParameterSpec ecGenSpec = new java.security.spec.ECGenParameterSpec("secp256k1");
        KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("EC", "SC");
        kpg.initialize(ecGenSpec);
        ECParameterSpec ecSpec = ((ECPublicKey) kpg.generateKeyPair().getPublic()).getParams();
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "SC");
        return keyFactory.generatePublic(ecPublicKeySpec);
    }

    ////////// PrivateKey <-> Hex //////////
    public static String privateKeyToHex(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKeyToHex: PrivateKey is null.");
        }
        ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
        byte[] privateKeyBytes = ecPrivateKey.getS().toByteArray();
        return SerializationUtil.bytesToHex(privateKeyBytes);
    }
    public static PrivateKey hexToPrivateKey(String hexString) throws Exception {
        if (hexString == null) {
            throw new IllegalArgumentException("hexToPrivateKey: String is null.");
        }
        byte[] privateKeyBytes = SerializationUtil.hexToBytes(hexString);
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(1, privateKeyBytes), spec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(privateKeySpec);
    }

    //////////////////// Signature Serialization ////////////////////
    ////////// Signature <-> Base64 //////////
    public static String signatureToBase64(byte[] signature) throws Exception {
        if (signature == null) {
            throw new IllegalArgumentException("signatureToBase64: Signature is null.");
        }
        ASN1Sequence sequence = (ASN1Sequence) ASN1Sequence.fromByteArray(signature);
        BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getValue();
        byte[] rBytes = r.toByteArray();
        byte[] sBytes = s.toByteArray();
        if (rBytes.length > 32) {
            rBytes = Arrays.copyOfRange(rBytes, rBytes.length - 32, rBytes.length);
        }
        if (sBytes.length > 32) {
            sBytes = Arrays.copyOfRange(sBytes, sBytes.length - 32, sBytes.length);
        }
        String rBase64 = SerializationUtil.bytesToBase64(rBytes);
        String sBase64 = SerializationUtil.bytesToBase64(sBytes);
        return rBase64 + "-" + sBase64;
    }
    public static byte[] base64ToSignature(String base64Str) throws Exception {
        if (base64Str == null) {
            throw new IllegalArgumentException("base64ToSignature: String is null.");
        } else if (!base64Str.contains("-")) {
            throw new IllegalArgumentException("base64ToSignature: Invalid Base64 string.");
        }
        String[] parts = base64Str.split("-");
        String rBase64 = parts[0];
        String sBase64 = parts[1];
        byte[] rBytes = SerializationUtil.base64ToBytes(rBase64);
        byte[] sBytes = SerializationUtil.base64ToBytes(sBase64);
        BigInteger r = new BigInteger(1, rBytes);
        BigInteger s = new BigInteger(1, sBytes);
        ASN1Integer rAsn1 = new ASN1Integer(r);
        ASN1Integer sAsn1 = new ASN1Integer(s);
        ASN1Sequence sequence = new DERSequence(new ASN1Integer[]{rAsn1, sAsn1});
        return sequence.getEncoded();
    }

    ////////// Signature <-> Hex //////////
    public static String[] signatureToHex(byte[] signature) throws IOException {
        if (signature == null) {
            throw new IllegalArgumentException("signatureToHex: Signature is null.");
        }
        ASN1Sequence sequence = (ASN1Sequence) ASN1Sequence.fromByteArray(signature);
        BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getValue();
        byte[] rBytes = r.toByteArray();
        byte[] sBytes = s.toByteArray();
        String rHex = SerializationUtil.bytesToHex(rBytes);
        String sHex = SerializationUtil.bytesToHex(sBytes);
        return new String[]{rHex, sHex};
    }
    public static byte[] hexToSignature(String rHex, String sHex) throws Exception {
        byte[] rBytes = SerializationUtil.hexToBytes(rHex);
        byte[] sBytes = SerializationUtil.hexToBytes(sHex);
        BigInteger r = new BigInteger(1, rBytes);
        BigInteger s = new BigInteger(1, sBytes);
        ASN1Integer rAsn1 = new ASN1Integer(r);
        ASN1Integer sAsn1 = new ASN1Integer(s);
        ASN1Sequence sequence = new DERSequence(new ASN1Integer[]{rAsn1, sAsn1});
        return sequence.getEncoded();
    }
}

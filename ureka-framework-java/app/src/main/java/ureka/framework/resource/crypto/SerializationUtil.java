package ureka.framework.resource.crypto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SerializationUtil {

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
            if (base64String == null) {
                return null;
            }
            return Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("base64StrBackToByte: Decode failed.", e);
        }
    }

    ////////// Dict <-> JSONStr //////////
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
                return ((ECPrivateKey) keyObj).getEncoded();
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
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyByte);
                return keyFactory.generatePrivate(keySpec);
            } else {
                throw new RuntimeException("_byteToKey: Key type not supported.");
            }
        } catch (Exception e) {
            throw new RuntimeException("_byteToKey: Conversion failed.", e);
        }
    }

    ////////// ECPublicKey/ECPrivateKey <-> String //////////
    public static String keyToStr(Object keyObj, String keyType) {
        if (keyObj instanceof ECPrivateKey || keyObj instanceof ECPublicKey) {
            return byteToBase64Str(_keyToByte(keyObj, keyType));
        } else {
            throw new IllegalArgumentException("keyToStr: Illegal argument.");
        }
    }
    public static String keyToStr(Object keyObj) {
        if (keyObj instanceof ECPublicKey) {
            return byteToBase64Str(_keyToByte(keyObj, "eccPublicKey"));
        } else if (keyObj instanceof ECPrivateKey) {
            return byteToBase64Str(_keyToByte(keyObj, "eccPrivateKey"));
        } else {
            throw new IllegalArgumentException("keyToStr: Illegal argument.");
        }
    }

    public static Object strToKey(String keyStr, String keyType) {
        return _byteToKey(base64StrBackToByte(keyStr), keyType);
    }
    public static Object strToKey(String keyStr) {
        return _byteToKey(base64StrBackToByte(keyStr), "eccPublicKey");
    }
}

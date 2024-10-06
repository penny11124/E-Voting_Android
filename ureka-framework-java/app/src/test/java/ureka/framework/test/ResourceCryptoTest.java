package ureka.framework.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.crypto.SerializationUtil;

public class ResourceCryptoTest {
    // auxiliary function
    public static byte[] hexStringToByteArray(String hex) {
        int length = hex.length();
        byte[] data = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i+1), 16));
        }

        return data;
    }

    // resource.crypto - COMPLETED
    @Test
    public void serializationUtilTest() throws Exception {
        String test = "foobar";
        assert (test.equals(SerializationUtil.byteBackToStr(SerializationUtil.strToByte(test))));

        byte[] test_byte = SerializationUtil.strToByte(test);
        assert (Arrays.equals(test_byte, SerializationUtil.base64StrBackToByte(SerializationUtil.byteToBase64Str(test_byte))));

        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        assert (map.equals(SerializationUtil.jsonStrToDict(SerializationUtil.dictToJsonStr(map))));

        KeyPair keyPair = ECC.generateKeyPair();
        byte[] publicKeyByte = SerializationUtil._keyToByte(keyPair.getPublic(), "eccPublicKey");
        byte[] newPublicKeyByte = SerializationUtil._keyToByte(
            SerializationUtil._byteToKey(publicKeyByte, "eccPublicKey"), "eccPublicKey");
        // Since keys might not override equals() functions, we compare the keys in byte[] form.
        assert (Arrays.equals(publicKeyByte, newPublicKeyByte));

        String privateKeyStr = SerializationUtil.keyToStr(keyPair.getPrivate(), "eccPrivateKey");
        String newPrivateKeyStr = SerializationUtil.keyToStr(
            SerializationUtil.strToKey(privateKeyStr, "eccPrivateKey"), "eccPrivateKey");
        assert (Objects.equals(privateKeyStr, newPrivateKeyStr));
    }

    @Test
    public void ECCTest() throws Exception {
        KeyPair keyPair = ECC.generateKeyPair();
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());

        byte[] test = SerializationUtil.strToByte("foobar");
        byte[] test_byte = ECC.signSignature(test, (ECPrivateKey) keyPair.getPrivate());
        assertTrue(ECC.verifySignature(test_byte, test, (ECPublicKey) keyPair.getPublic()));
    }

    @Test
    public void ECDHTest() throws Exception {
        byte[] random_byte = ECDH.generateRandomByte(16);
        // System.out.println(random_byte);
        assertNotNull(random_byte);

        String random_string = ECDH.generateRandomStr(12);
        // System.out.println(random_string);
        assertNotNull(random_string);

        String foobar = "foobar";
        byte[] bytes = foobar.getBytes();
        byte[] hashed_bytes = ECDH.generateSha256HashBytes(bytes);
        String expected_hash = "C3AB8FF13720E8AD9047DD39466B3C8974E592C2FA383D4A3960714CAEF0C4F2"; // Generated using online tools
        byte[] expected_hash_bytes = hexStringToByteArray(expected_hash);
        assertArrayEquals(expected_hash_bytes, hashed_bytes);

        String hashed_base64 = ECDH.generateSha256HashStr(foobar);
        // System.out.println(hashed_base64);
        String expected_base64 = "w6uP8Tcg6K2QR905Rms8iXTlksL6OD1KOWBxTK7wxPI=";
        assert (hashed_base64.equals(expected_base64));

        KeyPair keyPair1 = ECC.generateKeyPair(), keyPair2 = ECC.generateKeyPair();
        byte[] salt = ECDH.generateRandomByte(32);
        byte[] ecdh_key1 = ECDH.generateEcdhKey((ECPrivateKey) keyPair1.getPrivate(), salt, null, (ECPublicKey) keyPair2.getPublic());
        byte[] ecdh_key2 = ECDH.generateEcdhKey((ECPrivateKey) keyPair2.getPrivate(), salt, null, (ECPublicKey) keyPair1.getPublic());
        assert (Arrays.equals(ecdh_key1, ecdh_key2));

        byte[] plaintext = SerializationUtil.base64StrBackToByte(foobar);
        byte[][] cbc_result = ECDH.cbcEncrypt(plaintext, ecdh_key1);
        byte[] cbc_ciphertext = cbc_result[0];
        byte[] cbc_iv = cbc_result[1];
        byte[] cbc_plaintext = ECDH.cbcDecrypt(cbc_ciphertext, ecdh_key1, cbc_iv);
        assert (Arrays.equals(plaintext, cbc_plaintext));

        byte[] random_gcm_iv = ECDH.generateRandomByte(16);
        // System.out.println(random_gcm_iv);
        assertNotNull(random_gcm_iv);

        String associatedPlaintextString = "test";
        byte[] associatedPlaintext = associatedPlaintextString.getBytes();
        byte[][] gcm_result = ECDH.gcmEncrypt(plaintext, associatedPlaintext, ecdh_key1, null);
        byte[] gcm_ciphertext = gcm_result[0];
        byte[] gcm_iv = gcm_result[1];
        byte[] gcm_tag = gcm_result[2];
        byte[] gcm_plaintext = ECDH.gcmDecrypt(gcm_ciphertext, associatedPlaintext, gcm_tag, ecdh_key1, gcm_iv);
        assert (Arrays.equals(plaintext, gcm_plaintext));
    }
}

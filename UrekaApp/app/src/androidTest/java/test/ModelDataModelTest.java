package test;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.resource.crypto.ECC;
import ureka.framework.resource.logger.SimpleLogger;

public class ModelDataModelTest {
    // model.data_model - COMPLETED
    @Test
    public void dataModelMapConversionTest() throws IllegalAccessException, NoSuchFieldException {
        // Since the conversion of the data models is the same,
        // we only test one of the classes.
        OtherDevice otherDevice = new OtherDevice();
        otherDevice.setDeviceId("foo");

        Map<String, String> otherDeviceMap = OtherDevice._otherDeviceToMap(otherDevice);
        System.out.println(otherDeviceMap);
        OtherDevice newOtherDevice = OtherDevice._mapToOtherDevice(otherDeviceMap);
        assert (Objects.equals(otherDevice, newOtherDevice));
    }

    @Test
    public void dataModelSerialization() throws Exception {
        Map<String, OtherDevice> otherDeviceMap = new HashMap<>();
        OtherDevice otherDevice = new OtherDevice();
        otherDevice.setDeviceId("foo");
        otherDeviceMap.put("bar", otherDevice);

        String otherDeviceJson = OtherDevice.deviceTableToJsonStr(otherDeviceMap);
        Map<String, OtherDevice> newOtherDeviceMap = OtherDevice.jsonStrToDeviceTable(otherDeviceJson);
        assert (otherDeviceMap.equals(newOtherDeviceMap));

        // Since the serialization and deserialization of ThisDevice and ThisPerson are the same,
        // we only test one of the classes.
        ThisDevice thisDevice = new ThisDevice();
        thisDevice.setDeviceName("foo");
//        KeyPair keyPair = ECC.generateKeyPair();
//        thisDevice.setDevicePubKey((ECPublicKey) keyPair.getPublic());
//        thisDevice.setDevicePrivKey((ECPrivateKey) keyPair.getPrivate());

        String thisDeviceJson = ThisDevice.thisDeviceToJsonStr(thisDevice);
        System.out.println(thisDeviceJson);
        ThisDevice newThisDevice = ThisDevice.jsonStrToThisDevice(thisDeviceJson);
        assert (thisDevice.equals(newThisDevice));
    }
}

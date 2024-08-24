package ureka.framework.test;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.data_model.ThisDevice;

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
    public void dataModelSerialization() {
        Map<String, OtherDevice> otherDeviceMap = new HashMap<>();
        OtherDevice otherDevice = new OtherDevice();
        otherDevice.setDeviceId("foo");
        otherDeviceMap.put("bar", otherDevice);

        String otherDeviceJson = OtherDevice.deviceTableToJsonstr(otherDeviceMap);
        System.out.println(otherDeviceJson);
        Map<String, OtherDevice> newOtherDeviceMap = OtherDevice.jsonstrToDeviceTable(otherDeviceJson);
        assert (otherDeviceMap.equals(newOtherDeviceMap));

        // Since the serialization and deserialization of ThisDevice and ThisPerson are the same,
        // we only test one of the classes.
        ThisDevice thisDevice = new ThisDevice();
        thisDevice.setDeviceName("foo");

        String thisDeviceJson = ThisDevice.thisDeviceToJsonstr(thisDevice);
        System.out.println(thisDeviceJson);
        ThisDevice newThisDevice = ThisDevice.jsonStrToThisDevice(thisDeviceJson);
        assert (thisDevice.equals(newThisDevice));
    }
}

package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.Serial;
import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;

import ureka.framework.resource.crypto.SerializationUtil;
import ureka.framework.resource.logger.SimpleLogger;

public class ThisPerson {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private PrivateKey personPrivKey = null;
    private PublicKey personPubKey = null;

    public ThisPerson() {}

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Class<?> thisPersonClass = this.getClass();

        for (Field field : thisPersonClass.getDeclaredFields()) {
            try {
                Object value1 = field.get(this), value2 = field.get(obj);
                if (value1 == null && value2 != null) {
                    return false;
                } else if (value1 != null && value2 == null) {
                    return false;
                } else if (value1 != null && !value1.equals(value2)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return true;
    }

    public PrivateKey getPersonPrivKey() {
        return personPrivKey;
    }

    public void setPersonPrivKey(PrivateKey personPrivKey) {
        this.personPrivKey = personPrivKey;
    }

    public PublicKey getPersonPubKey() {
        return personPubKey;
    }

    // Adapted from @property in Python
    public String getPersonPubKeyStr() {
        return SerializationUtil.publicKeyToBase64(this.personPubKey);
    }

    public void setPersonPubKey(PublicKey personPubKey) {
        this.personPubKey = personPubKey;
    }

    public static Map<String, String> _thisPersonToMap(ThisPerson thisPerson) {
        Map<String, String> thisPersonMap = new HashMap<>();
        Class<?> thisPersonClass = thisPerson.getClass();

        for (Field field : thisPersonClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(thisPerson);

                if (value != null) {
                    if (field.getType().equals(Integer.class)) {
                        thisPersonMap.put(field.getName(), value.toString());
                    } else if (field.getType().equals(String.class)) {
                        thisPersonMap.put(field.getName(), (String) value);
                    } else if (field.getType().equals(PrivateKey.class)) {
                        thisPersonMap.put(field.getName(), SerializationUtil.privateKeyToBase64((PrivateKey) value));
                    } else if (field.getType().equals(PublicKey.class)) {
                        thisPersonMap.put(field.getName(), SerializationUtil.publicKeyToBase64((PublicKey) value));
                    }
                } else {
                    thisPersonMap.put(field.getName(), null);
                }
            } catch (Exception e) {
                String failureMsg = "ThisPerson._thisPersonToMap: " + e.getMessage();
                throw new RuntimeException(failureMsg);
            }
        }

        return thisPersonMap;
    }

    public static ThisPerson _mapToThisPerson(Map<String, String> thisPersonMap) {
        ThisPerson thisPerson = new ThisPerson();
        Class<?> thisPersonClass = thisPerson.getClass();

        for (Map.Entry<String, String> entry : thisPersonMap.entrySet()) {
            try {
                Field field = thisPersonClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);

                String value = entry.getValue();
                if (value == null) {
                    field.set(thisPerson, null);
                } else if (field.getType().equals(String.class)) {
                    field.set(thisPerson, value);
                } else if (field.getType().equals(Integer.class)) {
                    field.set(thisPerson, Integer.valueOf(value));
                } else if (field.getType().equals(PrivateKey.class)) {
                    field.set(thisPerson, SerializationUtil.base64ToPrivateKey(value));
                } else if (field.getType().equals(PublicKey.class)) {
                    field.set(thisPerson, SerializationUtil.base64ToPublicKey(value));
                }
            } catch (Exception e) {
                String failureMsg = "ThisPerson._mapToThisPerson: " + e.getMessage();
                throw new RuntimeException(failureMsg);
            }
        }

        return thisPerson;
    }

    public static String thisPersonToJsonStr(ThisPerson thisPerson) {
        Map<String, String> thisPersonMap = _thisPersonToMap(thisPerson);
        return gson.toJson(thisPersonMap);
    }

    public static ThisPerson jsonStrToThisPerson(String jsonStr) {
        Map<String, String> thisPersonMap = gson.fromJson(jsonStr, new TypeToken<Map<String, String>>() {}.getType());
        return _mapToThisPerson(thisPersonMap);
    }
}

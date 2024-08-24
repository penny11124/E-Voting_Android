package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Field;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;

import ureka.framework.resource.crypto.SerializationUtil;

public class ThisPerson {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private ECPrivateKey personPrivKey = null;
    private ECPublicKey personPubKey = null;

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

    public ECPrivateKey getPersonPrivKey() {
        return personPrivKey;
    }

    // Adapted from @property in Python
    public String getPersonPrivKeyStr() {
        if (this.personPrivKey == null) {
            return null;
        }
        return SerializationUtil.keyToStr(this.personPrivKey, "ecc-private-key");
    }

    public void setPersonPrivKey(ECPrivateKey personPrivKey) {
        this.personPrivKey = personPrivKey;
    }

    public ECPublicKey getPersonPubKey() {
        return personPubKey;
    }

    // Adapted from @property in Python
    public String getPersonPubKeyStr() {
        if (this.personPubKey == null) {
            return null;
        }
        return SerializationUtil.keyToStr(this.personPubKey, "ecc-public-key");
    }

    public void setPersonPubKey(ECPublicKey personPubKey) {
        this.personPubKey = personPubKey;
    }

    public static Map<String, String> _thisPersonToMap(ThisPerson thisPerson) throws IllegalAccessException {
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
                    } else if (field.getType().equals(ECPrivateKey.class)) {
                        thisPersonMap.put(field.getName(), SerializationUtil.keyToStr(value));
                    } else if (field.getType().equals(ECPublicKey.class)) {
                        thisPersonMap.put(field.getName(), SerializationUtil.keyToStr(value));
                    }
                } else {
                    thisPersonMap.put(field.getName(), null);
                }
            } catch (IllegalAccessException e) {
                String failureMsg = "ThisPerson._thisPersonToMap: IllegalAccessException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            }
        }

        return thisPersonMap;
    }

    public static ThisPerson _mapToThisPerson(Map<String, String> thisPersonMap)
        throws NoSuchFieldException, IllegalAccessException {
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
                } else if (field.getType().equals(ECPrivateKey.class)) {
                    field.set(thisPerson, SerializationUtil.strToKey(value, "ecc-private-key"));
                } else if (field.getType().equals(ECPublicKey.class)) {
                    field.set(thisPerson, SerializationUtil.strToKey(value, "ecc-public-key"));
                }
            } catch (NoSuchFieldException e) {
                String failureMsg = "thisPerson._mapToThisPerson: NoSuchFieldException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            } catch (IllegalAccessException e) {
                String failureMsg = "thisPerson._mapToThisPerson: IllegalAccessException occurs.";
                // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                throw e;
            }
        }

        return thisPerson;
    }

    public static String thisPersonToJsonstr(ThisPerson thisPerson) {
        // We don't need to apply _otherDeviceToMap since GSON will automatically handle it.
        return gson.toJson(thisPerson);
    }

    public static ThisPerson jsonStrToThisPerson(String jsonStr) {
        try {
            return gson.fromJson(jsonStr, ThisPerson.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID RTICKET SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

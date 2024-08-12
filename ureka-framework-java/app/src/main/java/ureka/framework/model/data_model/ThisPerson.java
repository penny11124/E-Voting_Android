package ureka.framework.model.data_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;

import ureka.framework.resource.crypto.SerializationUtil;

public class ThisPerson {
    private ECPrivateKey personPrivKey = null;
    private ECPublicKey personPubKey = null;

    public ThisPerson() {}

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

    private static Map<String, String> _thisPersonToMap(ThisPerson thisPerson) {
        Map<String, String> thisPersonDict = new HashMap<>();

        if (thisPerson.getPersonPrivKey() == null) {
            thisPersonDict.put("personPrivKey", null);
        } else {
            thisPersonDict.put("personPrivKey", SerializationUtil.keyToStr(thisPerson.getPersonPrivKey(), "ecc-private-key"));
        }
        if (thisPerson.getPersonPubKey() == null) {
            thisPersonDict.put("personPubKey", null);
        } else {
            thisPersonDict.put("personPubKey", SerializationUtil.keyToStr(thisPerson.getPersonPubKey(), "ecc-public-key"));
        }

        return thisPersonDict;
    }

    public static ThisPerson _mapToThisPerson(Map<String, String> thisPersonDict) {
        ThisPerson thisPerson = new ThisPerson();

        if (thisPersonDict.get("personPrivKey") == null) {
            thisPerson.setPersonPrivKey(null);
        } else {
            thisPerson.setPersonPrivKey((ECPrivateKey) SerializationUtil.strToKey(thisPersonDict.get("personPrivKey"), "ecc-private-key"));
        }
        if (thisPersonDict.get("personPubKey") == null) {
            thisPerson.setPersonPubKey(null);
        } else {
            thisPerson.setPersonPubKey((ECPublicKey) SerializationUtil.strToKey(thisPersonDict.get("personPubKey"), "ecc-public-key"));
        }

        return thisPerson;
    }

    public static String thisPersonToJsonstr(ThisPerson thisPerson) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ThisPerson.class, (JsonSerializer<ThisPerson>) (src, typeOfSrc, context) ->
                    context.serialize(_thisPersonToMap(src)))
            .create();

        return gson.toJson(thisPerson);
    }

    public static ThisPerson jsonStrToThisPerson(String jsonStr) {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(ThisPerson.class, (JsonDeserializer<ThisPerson>) (json, typeOfT, context) -> {
                Map<String, String> map = context.deserialize(json, Map.class);
                try {
                    return _mapToThisPerson(map);
                } catch (Exception e) {
                    String failureMsg = "NOT VALID JSON or VALID SCHEMA";
                    // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
                    throw new RuntimeException(failureMsg);
                }
            })
            .create();

        try {
            return gson.fromJson(jsonStr, ThisPerson.class);
        } catch (Exception e) {
            String failureMsg = "NOT VALID JSON or VALID SCHEMA";
            // SimpleLogger.simpleLog("error", "{" + failureMsg + "}: {" + e + "}");
            throw new RuntimeException(failureMsg);
        }
    }
}

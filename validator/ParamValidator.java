package knu.myhealthhub.dynamicconsent.validator;

import static knu.myhealthhub.common.JsonUtility.getJsonObject;
import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.settings.Configuration.TRUE;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import org.json.simple.JSONObject;

public class ParamValidator {
    public static String isValidKeyForJson(String body, String key) {
        JSONObject jsonObject = toJsonObject(body);
        if (null == jsonObject) {
            String reason = String.format("Fail to parse string to JSON - %s", body);
            return setErrorMessage(CDC_ERROR, reason);
        }
        JSONObject getJsonObjectResult = getJsonObject(jsonObject, key);
        if (null == getJsonObjectResult) {
            String reason = String.format("Fail to find key[%s] from %s", key, jsonObject.toJSONString());
            return setErrorMessage(CDC_ERROR, reason);
        }
        return TRUE;
    }
}

package knu.myhealthhub.dynamicconsent.authentication;

import static knu.myhealthhub.common.JsonUtility.getStringFromObject;
import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.enums.ERROR_CODE.XDS_REGISTRY_ERROR;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;
import static knu.myhealthhub.transactions.RestSender.createRest;
import static knu.myhealthhub.transactions.RestSender.getHeader;

import knu.myhealthhub.enums.USER_TYPE;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;

public class AccessToken {
    public static String getAccessToken(USER_TYPE userType) {
        String url = BLOCKCHAIN + AUTHENTICATION;
        String result = createRest(url, HttpMethod.POST, getHeader(null), getAuthenticationBody(userType));
        if (null == result) {
            return setErrorMessage(XDS_REGISTRY_ERROR, null);
        }
        JSONObject jsonObject = toJsonObject(result);
        if (null == jsonObject) {
            String reason = String.format("Fail to parse string to JSON - %s", result);
            return setErrorMessage(XDS_REGISTRY_ERROR, reason);
        }
        String accessToken = getStringFromObject(jsonObject, KEY_FOR_ACCESS_TOKEN);
        if (null == accessToken) {
            String reason = String.format("Fail to find key[accessToken] from %s", jsonObject.toJSONString());
            return setErrorMessage(XDS_REGISTRY_ERROR, reason);
        }
        return accessToken;
    }
    private static JSONObject getAuthenticationBody(USER_TYPE userType) {
        String clientKey = CLIENT_KEY_FOR_SUBJECT;
        String secretKey = SECRET_KEY_FOR_SUBJECT;
        if (userType.equals(USER_TYPE.DATA_CONSUMER)) {
            clientKey = CLIENT_KEY_FOR_CONSUMER;
            secretKey = SECRET_KEY_FOR_CONSUMER;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_FOR_CLIENT_KEY, clientKey);
        jsonObject.put(KEY_FOR_SECRET_KEY, secretKey);
        return jsonObject;
    }
}
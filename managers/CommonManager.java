package knu.myhealthhub.dynamicconsent.managers;

import static knu.myhealthhub.common.JsonUtility.isJsonObject;
import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.dynamicconsent.DynamicConsentApplication.logger;
import static knu.myhealthhub.dynamicconsent.authentication.AccessToken.getAccessToken;
import static knu.myhealthhub.settings.Configuration.FHIR_SERVER_ENDPOINT;
import static knu.myhealthhub.settings.Configuration.URL_HEADER;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.transactions.RestSender.*;

import knu.myhealthhub.enums.USER_TYPE;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;

public class CommonManager {
    public static String setResponse(String status, String key, String response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_FOR_STATUS, status);
        JSONObject responseJson = toJsonObject(response);
        if (null == responseJson) {
            jsonObject.put(key, response);
            return jsonObject.toJSONString();
        }
        jsonObject.put(KEY_FOR_REGISTRY_ERROR, responseJson);
        return jsonObject.toJSONString();
    }
    public static String sendRequest(String url, USER_TYPE type, HttpMethod method, JSONObject body) {
        String getAccessTokenResult = getAccessToken(type);
        if (isJsonObject(getAccessTokenResult)) {
            return getAccessTokenResult;
        }
        String result = createRest(url, method, getHeader(getAccessTokenResult), body);
        logger.debug("[REQUEST::]" + url);
        logger.debug("\t\t > " + body.toJSONString());
        logger.debug("\t\t < " + result);
        return result;
    }
    public static String requestQuery(String id, String base) {
        String url = String.format("%s%s%s?%s=%s", URL_HEADER, FHIR_SERVER_ENDPOINT, base, KEY_FOR_IDENTIFIER, id);
        return createRest(url, HttpMethod.GET, getFhirHeader(), new JSONObject());
    }
}
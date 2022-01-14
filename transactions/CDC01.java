package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.sendRequest;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.dynamicconsent.validator.ParamValidator.isValidKeyForJson;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_SUBJECT;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.ProfileBlock;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;

public class CDC01 {
    public static String registerProfile(ProfileBlock profile) {
        String profileJsonString = toJsonObjectFromJavaObject(profile);
        JSONObject profileJson = toJsonObject(profileJsonString);
        if (null == profileJson) {
            String reason = String.format("Fail to parse string to JSON - %s", profile);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_PROFILE_ID, registryError);
        }
        String requestRegisterProfileResult = requestRegisterProfile(profileJson);
        if (null == requestRegisterProfileResult) {
            String registryError = setErrorMessage(CDC_ERROR, null);
            return setResponse(FAILURE, KEY_FOR_PROFILE_ID, registryError);
        }
        String getProfileIdResult = getIdentifierFromResultJson(requestRegisterProfileResult);
        if (isJsonObject(getProfileIdResult)) {
            return setResponse(FAILURE, KEY_FOR_PROFILE_ID, getProfileIdResult);
        }
        return setResponse(SUCCESS, KEY_FOR_PROFILE_ID, getProfileIdResult);
    }
    private static String getIdentifierFromResultJson(String result) {
        String getJsonObjectsFromParametersResult = isValidKeyForJson(result, KEY_FOR_RESULT);
        if (!getJsonObjectsFromParametersResult.equals(TRUE)) {
            return getJsonObjectsFromParametersResult;
        }
        JSONObject jsonObject = toJsonObject(result);
        JSONObject resultJson = getJsonObject(jsonObject, KEY_FOR_RESULT);
        String resultCode = getStringFromObject(resultJson, KEY_FOR_RESULT_CODE);
        if (!resultCode.equalsIgnoreCase(SUCCESS)) {
            return setErrorMessage(CDC_ERROR, resultJson.toJSONString());
        }
        return getStringFromObject(jsonObject, KEY_FOR_PROFILE_ID);
    }
    private static String requestRegisterProfile(JSONObject jsonObject) {
        String url = BLOCKCHAIN + URL_DEFAULT + PROFILE;
        return sendRequest(url, DATA_SUBJECT, HttpMethod.POST, jsonObject);
    }
}
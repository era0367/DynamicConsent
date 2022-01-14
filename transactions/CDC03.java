package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.Common.getNow;
import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.sendRequest;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.ProfileBlock;
import knu.myhealthhub.enums.USER_TYPE;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;

public class CDC03 {
    public static String updateProfile(String profileId, String profileJsonString) {
        JSONObject profileJson = toJsonObject(profileJsonString);
        if (null == profileJson) {
            String reason = String.format("Fail to parse string to JSON - %s", profileJsonString);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_PROFILE_ID, registryError);
        }
        ProfileBlock profileBlock = toJavaObject(profileJson, ProfileBlock.class);
        profileBlock.setLastUpdated(getNow());
        String profileBlockString = toJsonObjectFromJavaObject(profileBlock);
        JSONObject profileBlockJson = toJsonObject(profileBlockString);
        String requestUpdateProfileResult = requestUpdateProfile(profileId, profileBlockJson);
        if (null == requestUpdateProfileResult) {
            String registryError = setErrorMessage(CDC_ERROR, null);
            return setResponse(FAILURE, KEY_FOR_PROFILE_ID, registryError);
        }
        JSONObject updateProfileResultJson = toJsonObject(requestUpdateProfileResult);
        JSONObject resultJson = getJsonObject(updateProfileResultJson, KEY_FOR_RESULT);
        String result = getStringFromObject(resultJson, KEY_FOR_RESULT_CODE);
        if (!result.equalsIgnoreCase(SUCCESS)) {
            return setResponse(FAILURE, KEY_FOR_PROFILE_ID, updateProfileResultJson.toJSONString());
        }
        return setResponse(SUCCESS, KEY_FOR_PROFILE_ID, profileId);
    }
    private static String requestUpdateProfile(String profileId, JSONObject profileJson) {
        String url = String.format("%s%s%s/%s", BLOCKCHAIN, URL_DEFAULT, PROFILE, profileId);
        return sendRequest(url, USER_TYPE.DATA_SUBJECT, HttpMethod.PUT, profileJson);
    }
}

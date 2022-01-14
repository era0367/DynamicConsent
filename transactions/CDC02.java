package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.dynamicconsent.managers.ProfileManager.getProfile;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.settings.Configuration.SUCCESS;
import static knu.myhealthhub.settings.KeyString.KEY_FOR_PROFILE;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import org.json.simple.JSONObject;

public class CDC02 {
    public static String retrieveProfile(String dataSubjectId) {
        String requestRetrieveProfileResult = getProfile(dataSubjectId);
        JSONObject profileJson = toJsonObject(requestRetrieveProfileResult);
        if (null == profileJson) {
            String reason = String.format("Fail to parse String to JSON - %s", requestRetrieveProfileResult);
            return setErrorMessage(CDC_ERROR, reason);
        }
        return setResponse(SUCCESS, KEY_FOR_PROFILE, profileJson.toJSONString());
    }
}

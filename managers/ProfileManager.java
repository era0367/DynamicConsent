package knu.myhealthhub.dynamicconsent.managers;

import static knu.myhealthhub.common.Common.toDateFromString;
import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.sendRequest;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_SUBJECT;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import java.time.LocalDate;
import knu.myhealthhub.datamodels.ProfileBlock;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

public class ProfileManager {
    private static String getProfileList(String dataSubjectId) {
        String getProfileListResult = requestRetrieveProfile(dataSubjectId);
        if (null == getProfileListResult) {
            return setErrorMessage(CDC_ERROR, null);
        }
        JSONObject profileListJson = toJsonObject(getProfileListResult);
        if (null == profileListJson) {
            String reason = String.format("Fail to parse string to JSON - %s", getProfileListResult);
            return setErrorMessage(CDC_ERROR, reason);
        }
        JSONArray profileList = getJsonArray(profileListJson, KEY_FOR_PROFILE_LIST);
        if (null == profileList) {
            String reason = String.format("Fail to find key[%s] from %s", KEY_FOR_PROFILE_LIST, profileListJson.toJSONString());
            return setErrorMessage(CDC_ERROR, reason);
        }
        return profileListJson.toJSONString();
    }
    private static String requestRetrieveProfile(String dataSubjectId) {
        String url = BLOCKCHAIN + URL_DEFAULT + PROFILE;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam(KEY_FOR_DATA_SUBJECT_ID, dataSubjectId);
        return sendRequest(builder.toUriString(), DATA_SUBJECT, HttpMethod.GET, new JSONObject());
    }
    private static int getLastProfileIndex(JSONArray profileList) {
        int lastUpdatedIndex = 0;
        for (int i = 1; i < profileList.size(); i++) {
            JSONObject lastUpdatedProfileJson = getJsonObjectFromArray(profileList, lastUpdatedIndex);
            JSONObject targetProfileJson = getJsonObjectFromArray(profileList, i);
            lastUpdatedProfileJson.remove(KEY_FOR_DOCTYPE);
            targetProfileJson.remove(KEY_FOR_DOCTYPE);
            ProfileBlock lastUpdatedProfile = toJavaObject(lastUpdatedProfileJson, ProfileBlock.class);
            ProfileBlock targetProfile = toJavaObject(targetProfileJson, ProfileBlock.class);
            String lastUpdated = lastUpdatedProfile.getLastUpdated();
            String targetProfileUpdated = targetProfile.getLastUpdated();
            if (null == lastUpdated) {
                lastUpdatedIndex = i;
                continue;
            }
            LocalDate lastUpdatedTime = toDateFromString(lastUpdated);
            if (null == targetProfileUpdated) {
                continue;
            }
            LocalDate targetUpdatedTime = toDateFromString(targetProfileUpdated);
            if (targetUpdatedTime.isAfter(lastUpdatedTime)) {
                lastUpdatedIndex = i;
            }
        }
        return lastUpdatedIndex;
    }
    public static String getProfile(String dataSubjectId) {
        String getProfileListResult = getProfileList(dataSubjectId);
        JSONObject profileListResultJson = toJsonObject(getProfileListResult);
        JSONArray profileListJson = getJsonArray(profileListResultJson, KEY_FOR_PROFILE_LIST);
        int lastUpdatedIndex = 0;
        if (1 != profileListJson.size()) {
            lastUpdatedIndex = getLastProfileIndex(profileListJson);
        }
        JSONObject profileJson = getJsonObjectFromArray(profileListJson, lastUpdatedIndex);
        profileJson.remove(KEY_FOR_DOCTYPE);
        return profileJson.toJSONString();
    }
}

package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.sendRequest;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_CONSUMER;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.Person;
import knu.myhealthhub.enums.USER_TYPE;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

public class CDC05 {
    public static String requestConsentHistory(String id, String type) {
        Person person = new Person();
        person.setIdentifier(id);
        person.setType(USER_TYPE.valueOf(type));
        person.setName("");
        String identifier = person.getIdentifier();
        String url = BLOCKCHAIN + URL_DEFAULT + CONSENT;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam(KEY_FOR_DATA_SUBJECT_ID, identifier);
        String getConsentHistoryResult = sendRequest(builder.toUriString(), DATA_CONSUMER, HttpMethod.GET, new JSONObject());
        if (null == getConsentHistoryResult) {
            String errorMessage = setErrorMessage(CDC_ERROR, null);
            return setResponse(FAILURE, KEY_FOR_CONSENT_LIST, errorMessage);
        }
        JSONObject getConsentHistoryResultJson = toJsonObject(getConsentHistoryResult);
        if (null == getConsentHistoryResultJson) {
            String reason = String.format("Fail to parse string to JSON - %s", getConsentHistoryResult);
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_LIST, errorMessage);
        }
        JSONObject resultJson = getJsonObject(getConsentHistoryResultJson, KEY_FOR_RESULT);
        if (null == resultJson) {
            String reason = String.format("Fail to find key[%s] from - %s", KEY_FOR_RESULT, getConsentHistoryResultJson.toJSONString());
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_RESULT, errorMessage);
        }
        String resultCode = getStringFromObject(resultJson, KEY_FOR_RESULT_CODE);
        if (null == resultCode) {
            String reason = String.format("Fail to find key[%s] from - %s", KEY_FOR_RESULT_CODE, resultJson.toJSONString());
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_RESULT_CODE, errorMessage);
        }
        if (!resultCode.equalsIgnoreCase(SUCCESS)) {
            String errorMessage = setErrorMessage(CDC_ERROR, getConsentHistoryResultJson.toJSONString());
            return setResponse(FAILURE, KEY_FOR_RESULT_CODE, errorMessage);
        }
        JSONArray consentListJson = getJsonArray(getConsentHistoryResultJson, KEY_FOR_CONSENT_LIST);
        if (null == consentListJson) {
            String reason = String.format("Fail to find key[%s] from - %s", KEY_FOR_CONSENT_LIST, getConsentHistoryResultJson.toJSONString());
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_LIST, errorMessage);
        }
        JSONObject consentListObject = new JSONObject();
        consentListObject.put(KEY_FOR_CONSENT_LIST, consentListJson);

        return setResponse(SUCCESS, KEY_FOR_CONSENT_LIST, consentListObject.toJSONString());
    }
}
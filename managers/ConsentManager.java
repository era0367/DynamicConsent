package knu.myhealthhub.dynamicconsent.managers;

import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.sendRequest;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_SUBJECT;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.Consent;
import knu.myhealthhub.enums.CONSENT_STATUS;
import knu.myhealthhub.enums.USER_TYPE;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;

public class ConsentManager {
    public static String getConsent(String consentId, USER_TYPE userType) {
        String url = String.format("%s%s%s/%s", BLOCKCHAIN, URL_DEFAULT, CONSENT, consentId);
        return sendRequest(url, userType, HttpMethod.GET, new JSONObject());
    }
    public static Consent setConsent(Consent consent, CONSENT_STATUS status) {
        if (null == consent.getConsentId()) {
            String consentId = setConsentId(consent);
            consent.setConsentId(consentId);
        }
        consent.setStatus(status);
        return consent;
    }
    public static String setConsentId(Consent consent) {
        String dataSubjectId = consent.getDataSubjectId();
        String dataConsumerId = consent.getDataConsumerId();
        String creationTime = consent.getCreationTime();
        return String.format("%s|%s|%s", dataSubjectId, dataConsumerId, creationTime);
    }
    public static String provideConsent(String consentId, String method) {
        String requestConsentResult = getConsent(consentId, DATA_SUBJECT);
        if (null == requestConsentResult) {
            return setErrorMessage(CDC_ERROR, null);
        }
        JSONObject requestConsentResultJson = toJsonObject(requestConsentResult);
        if (null == requestConsentResultJson) {
            String reason = String.format("Fail to parse string to JSON - %s", requestConsentResult);
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT, errorMessage);
        }
        JSONObject resultJson = getJsonObject(requestConsentResultJson, KEY_FOR_RESULT);
        if (null == resultJson) {
            String reason = String.format("Fail to find key[%s] from - %s", KEY_FOR_RESULT, requestConsentResultJson.toJSONString());
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
            String errorMessage = setErrorMessage(CDC_ERROR, requestConsentResultJson.toJSONString());
            return setResponse(FAILURE, KEY_FOR_RESULT_CODE, errorMessage);
        }
        JSONObject consentJson = getJsonObject(requestConsentResultJson, KEY_FOR_CONSENT);
        if (null == consentJson) {
            String reason = String.format("Fail to find key[%s] from - %s", KEY_FOR_CONSENT, requestConsentResultJson.toJSONString());
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_RESULT_CODE, errorMessage);
        }
        Consent consent = toJavaObject(consentJson, Consent.class);
        if (null == consent) {
            String reason = String.format("Fail to parse JSON to JavaObject[Consent] - %s", consentJson.toJSONString());
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT, errorMessage);
        }
        String requestAcceptResult = requestPatchConsent(consentId, method);
        if (!(requestAcceptResult.equalsIgnoreCase(TRUE) || requestAcceptResult.equalsIgnoreCase(SUCCESS))) {
            String errorMessage = setErrorMessage(CDC_ERROR, null);
            return setResponse(FAILURE, KEY_FOR_RESULT_CODE, errorMessage);
        }
        return consentJson.toJSONString();
    }
    public static String requestUpdateConsent(JSONObject jsonObject, USER_TYPE userType) {
        String consentId = getStringFromObject(jsonObject, KEY_FOR_CONSENT_ID);
        String url = String.format("%s%s%s/%s", BLOCKCHAIN, URL_DEFAULT, CONSENT, consentId);
        return sendRequest(url, userType, HttpMethod.PUT, jsonObject);
    }
    public static String requestPatchConsent(String consentId, String method) {
        String url = String.format("%s%s%s/%s%s", BLOCKCHAIN, URL_DEFAULT, CONSENT, consentId, method);
        return sendRequest(url, DATA_SUBJECT, HttpMethod.PATCH, new JSONObject());
    }
}

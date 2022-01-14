package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.getConsent;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_CONSUMER;
import static knu.myhealthhub.settings.Configuration.FAILURE;
import static knu.myhealthhub.settings.Configuration.SUCCESS;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.Consent;
import org.json.simple.JSONObject;

public class CDC09 {
    public static String retrieveConsent(String consentId) {
        String requestConsentResult = getConsent(consentId, DATA_CONSUMER);
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
        return setResponse(SUCCESS, KEY_FOR_CONSENT, consentJson.toJSONString());
    }
}

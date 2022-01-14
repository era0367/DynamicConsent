package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.toJavaObject;
import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.requestUpdateConsent;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_SUBJECT;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.KEY_FOR_CONSENT_ID;
import static knu.myhealthhub.settings.KeyString.KEY_FOR_RESULT_CODE;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.Consent;
import org.json.simple.JSONObject;

public class CDC07 {
    public static String updateConsent(String id, String type, String consentJsonString) {
        JSONObject consentJson = toJsonObject(consentJsonString);
        if (null == consentJson) {
            String reason = String.format("Fail to parse String to JSON - %s", consentJsonString);
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, errorMessage);
        }
        Consent consent = toJavaObject(consentJson, Consent.class);
        String validateResult = validateAuthority(id, type);
        String requestUpdateConsentResult = requestUpdateConsent(consentJson, DATA_SUBJECT);
        if (!(requestUpdateConsentResult.equalsIgnoreCase(TRUE) || requestUpdateConsentResult.equalsIgnoreCase(SUCCESS))) {
            String errorMessage = setErrorMessage(CDC_ERROR, requestUpdateConsentResult);
            return setResponse(FAILURE, KEY_FOR_RESULT_CODE, errorMessage);
        }
        return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consent.getConsentId());
    }
    private static String validateAuthority(String id, String type) {
        //@Todo
        return "";
    }
}
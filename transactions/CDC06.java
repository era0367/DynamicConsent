package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.Common.getNow;
import static knu.myhealthhub.common.JsonUtility.toJavaObject;
import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.provideConsent;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.requestUpdateConsent;
import static knu.myhealthhub.enums.CONSENT_STATUS.*;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_SUBJECT;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.KEY_FOR_CONSENT_ID;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import knu.myhealthhub.datamodels.Consent;
import org.json.simple.JSONObject;

public class CDC06 {
    public static String ProvideConsentForAccept(String consentId) {
        String provideConsentResult = provideConsent(consentId, ACCEPT);
        JSONObject provideConsentResultJson = toJsonObject(provideConsentResult);
        Consent consent = toJavaObject(provideConsentResultJson, Consent.class);
        if (null == consent) {
            return provideConsentResult;
        }
        consent.setStatus(ACCEPTED);
        consent.setResponseDate(getNow());
        consent.setLastUpdated(getNow());
        JSONObject updatedConsentJson = toJsonObject(consent.toString());
        if (null == updatedConsentJson) {
            String reason = String.format("Fail to parse JavaObject to JSON - %s", consent);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        String updateConsentResult = requestUpdateConsent(updatedConsentJson, DATA_SUBJECT);
        if (!updateConsentResult.equals(TRUE)) {
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, updateConsentResult);
        }
        return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consent.getConsentId());
    }
    public static String ProvideConsentForReject(String consentId) {
        String provideConsentResult = provideConsent(consentId, REJECT);
        JSONObject provideConsentResultJson = toJsonObject(provideConsentResult);
        Consent consent = toJavaObject(provideConsentResultJson, Consent.class);
        if (null == consent) {
            return provideConsentResult;
        }
        consent.setStatus(REJECTED);
        consent.setResponseDate(getNow());
        consent.setLastUpdated(getNow());
        JSONObject updatedConsentJson = toJsonObject(consent.toString());
        if (null == updatedConsentJson) {
            String reason = String.format("Fail to parse JavaObject to JSON - %s", consent);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        String updateConsentResult = requestUpdateConsent(updatedConsentJson, DATA_SUBJECT);
        if (!updateConsentResult.equals(TRUE)) {
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, updateConsentResult);
        }
        return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consent.getConsentId());
    }
    public static String ProvideConsentForExpire(String consentId) {
        String provideConsentResult = provideConsent(consentId, "/expire");
        JSONObject provideConsentResultJson = toJsonObject(provideConsentResult);
        Consent consent = toJavaObject(provideConsentResultJson, Consent.class);
        if (null == consent) {
            return provideConsentResult;
        }
        consent.setStatus(CANCELLED);
        consent.setResponseDate(getNow());
        consent.setLastUpdated(getNow());
        JSONObject updatedConsentJson = toJsonObject(consent.toString());
        if (null == updatedConsentJson) {
            String reason = String.format("Fail to parse JavaObject to JSON - %s", consent);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        String updateConsentResult = requestUpdateConsent(updatedConsentJson, DATA_SUBJECT);
        if (!updateConsentResult.equals(TRUE)) {
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, updateConsentResult);
        }
        return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consent.getConsentId());
    }
}

package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.getStringFromObject;
import static knu.myhealthhub.common.JsonUtility.toJsonObject;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.setResponse;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.getConsent;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_CONSUMER;
import static knu.myhealthhub.settings.Configuration.FAILURE;
import static knu.myhealthhub.settings.Configuration.SUCCESS;
import static knu.myhealthhub.settings.KeyString.KEY_FOR_CONSENT_ID;
import static knu.myhealthhub.settings.KeyString.KEY_FOR_TOKEN;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import org.json.simple.JSONObject;

public class CDC08 {
    public static String validateToken(String consentId, String token) {
        String getConsentResult = getConsent(consentId, DATA_CONSUMER);
        if (null == getConsentResult) {
            String errorMessage = setErrorMessage(CDC_ERROR, null);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, errorMessage);
        }
        JSONObject consentJson = toJsonObject(getConsentResult);
        if (null == consentJson) {
            String reason = String.format("Fail to parse string to JSON - %s", getConsentResult);
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, errorMessage);
        }
        String validToken = getStringFromObject(consentJson, KEY_FOR_TOKEN);
        if (!validToken.equals(token)) {
            String reason = "Invalid token";
            String errorMessage = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, errorMessage);
        }
        return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consentId);
    }
}

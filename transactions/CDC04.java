package knu.myhealthhub.dynamicconsent.transactions;

import static knu.myhealthhub.common.JsonUtility.*;
import static knu.myhealthhub.dynamicconsent.managers.CommonManager.*;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.requestUpdateConsent;
import static knu.myhealthhub.dynamicconsent.managers.ConsentManager.setConsent;
import static knu.myhealthhub.dynamicconsent.managers.ProfileManager.getProfile;
import static knu.myhealthhub.enums.CONSENT_STATUS.ACCEPTED;
import static knu.myhealthhub.enums.ERROR_CODE.CDC_ERROR;
import static knu.myhealthhub.enums.USER_TYPE.DATA_CONSUMER;
import static knu.myhealthhub.settings.Configuration.*;
import static knu.myhealthhub.settings.KeyString.*;
import static knu.myhealthhub.settings.errors.ErrorUtility.setErrorMessage;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import knu.myhealthhub.datamodels.*;
import knu.myhealthhub.enums.USER_TYPE;
import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpMethod;

public class CDC04 {
    public static String issueConsent(Consent consent) {
        String consentJsonString = toJsonObjectFromJavaObject(consent);
        JSONObject consentJson = toJsonObject(consentJsonString);
        String requestIssueConsentResult = requestIssueConsent(consentJson);
        if (null == requestIssueConsentResult) {
            String registryError = setErrorMessage(CDC_ERROR, null);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        JSONObject requestIssueConsentResultJson = toJsonObject(requestIssueConsentResult);
        if (null == requestIssueConsentResultJson) {
            String reason = String.format("Fail to parse string to JSON - %s", requestIssueConsentResult);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        String getConsentIdResult = getStringFromObject(requestIssueConsentResultJson, KEY_FOR_CONSENT_ID);
        if (isJsonObject(getConsentIdResult)) {
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, getConsentIdResult);
        }
        return checkAutomaticConsent(consent);
    }
    private static String checkAutomaticConsent(Consent consent) {
        String validateAutomaticConsentResult = isValidForAutomaticConsent(consent);
        if (!validateAutomaticConsentResult.equals(TRUE)) {
            return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consent.getConsentId());
        }
        Consent updatedConsent = setConsent(consent, ACCEPTED);
        String updatedConsentJsonString = toJsonObjectFromJavaObject(updatedConsent);
        JSONObject updatedConsentJson = toJsonObject(updatedConsentJsonString);
        if (null == updatedConsentJson) {
            String reason = String.format("Fail to parse JavaObject to JSON - %s", updatedConsent);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        String updateConsentResult = requestUpdateConsent(updatedConsentJson, USER_TYPE.DATA_SUBJECT);
        JSONObject updateConsentResultJson = toJsonObject(updateConsentResult);
        if (null == updateConsentResultJson) {
            String reason = String.format("Fail to parse JavaObject to JSON - %s", updateConsentResult);
            String registryError = setErrorMessage(CDC_ERROR, reason);
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, registryError);
        }
        JSONObject resultJson = getJsonObject(updateConsentResultJson, KEY_FOR_RESULT);
        String resultString = getStringFromObject(resultJson, KEY_FOR_RESULT_CODE);
        if (!resultString.equalsIgnoreCase(SUCCESS)) {
            return setResponse(FAILURE, KEY_FOR_CONSENT_ID, updateConsentResultJson.toJSONString());
        }
        return setResponse(SUCCESS, KEY_FOR_CONSENT_ID, consent.getConsentId());
    }
    private static String isValidForAutomaticConsent(Consent consent) {
        String dataSubjectId = consent.getDataSubjectId();
        String getProfileResult = getProfile(dataSubjectId);
        JSONObject getProfileResultJson = toJsonObject(getProfileResult);
        ProfileBlock profile = toJavaObject(getProfileResultJson, ProfileBlock.class);
        Preference preference = profile.getPreference();
        String decodedString = new String(Base64.decodeBase64(preference.getAutomaticConsent()));
        JSONObject automaticConsentJson = toJsonObject(decodedString);
        String metadataId = consent.getMetadataId();
        String getMetadataResult = getMetadata(metadataId);
        JSONObject getMetadataResultJson = toJsonObject(getMetadataResult);
        JSONObject metadataJson = getJsonObject(getMetadataResultJson, "registryObjectList");
        String classCode = getStringFromObject(metadataJson, "classCode").toLowerCase();
        String dataType = String.format("%s_data", classCode);
        JSONObject automaticConsentTarget = getJsonObject(automaticConsentJson, dataType);
        String dataConsumerId = consent.getDataConsumerId();
        String institutionIdentifier = getInstitutionIdentifierByDataConsumer(dataConsumerId);
        String institutionType = getInstitutionType(institutionIdentifier);
        return getStringFromObject(automaticConsentTarget, institutionType);
    }
    private static String getMetadata(String metadataId) {
        String url = String.format("%s%s%s%s/%s", URL_HEADER, BLOCKCHAIN_REGISTRY_ENDPOINT, URL_DEFAULT, METADATA, metadataId);
        return sendRequest(url, DATA_CONSUMER, HttpMethod.GET, new JSONObject());
    }
    private static String getInstitutionIdentifierByDataConsumer(String identifier) {
        String requestQueryResult = requestQuery(identifier, "Practitioner");
        JSONObject requestQueryResultJson = toJsonObject(requestQueryResult);
        JSONArray entryList = getJsonArray(requestQueryResultJson, KEY_FOR_RESOURCE_ENTRY);
        JSONObject entryJson = getJsonObjectFromArray(entryList, 0);
        String resource = getStringFromObject(entryJson, KEY_FOR_RESOURCE);
        FhirContext fhirContext = FhirContext.forR4();
        IParser parser = fhirContext.newJsonParser();
        Practitioner practitioner = parser.parseResource(Practitioner.class, resource);
        return practitioner.getQualification().get(0).getIssuer().getIdentifier().getValue();
    }
    private static String getInstitutionType(String identifier) {
        String requestQueryResult = requestQuery(identifier, "Organization");
        JSONObject requestQueryResultJson = toJsonObject(requestQueryResult);
        JSONArray entryList = getJsonArray(requestQueryResultJson, KEY_FOR_RESOURCE_ENTRY);
        JSONObject entryJson = getJsonObjectFromArray(entryList, 0);
        String resource = getStringFromObject(entryJson, KEY_FOR_RESOURCE);
        FhirContext fhirContext = FhirContext.forR4();
        IParser parser = fhirContext.newJsonParser();
        Organization organization = parser.parseResource(Organization.class, resource);
        return organization.getType().get(0).getText().toLowerCase();
    }
    private static String requestIssueConsent(JSONObject jsonObject) {
        String url = BLOCKCHAIN + URL_DEFAULT + CONSENT;
        return sendRequest(url, DATA_CONSUMER, HttpMethod.POST, jsonObject);
    }
}
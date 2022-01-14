package knu.myhealthhub.dynamicconsent.controller;

import static knu.myhealthhub.dynamicconsent.transactions.CDC01.registerProfile;
import static knu.myhealthhub.dynamicconsent.transactions.CDC02.retrieveProfile;
import static knu.myhealthhub.dynamicconsent.transactions.CDC03.updateProfile;
import static knu.myhealthhub.dynamicconsent.transactions.CDC04.issueConsent;
import static knu.myhealthhub.dynamicconsent.transactions.CDC05.requestConsentHistory;
import static knu.myhealthhub.dynamicconsent.transactions.CDC06.*;
import static knu.myhealthhub.dynamicconsent.transactions.CDC07.updateConsent;
import static knu.myhealthhub.dynamicconsent.transactions.CDC08.validateToken;
import static knu.myhealthhub.dynamicconsent.transactions.CDC09.retrieveConsent;
import static knu.myhealthhub.settings.Configuration.*;

import knu.myhealthhub.datamodels.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConsentController {
    @PostMapping(URL_DEFAULT + PROFILE)
    public String cdcRegisterProfile(@RequestBody @Validated Profile profile) {
        ProfileBlock profileBlock = new ProfileBlock(profile);
        return registerProfile(profileBlock);
    }
    @GetMapping(URL_DEFAULT + PROFILE + DATA_SUBJECT_ID)
    public String cdcGetProfile(@PathVariable("dataSubjectId") String dataSubjectId) {
        return retrieveProfile(dataSubjectId);
    }
    @PutMapping(URL_DEFAULT + PROFILE + ID)
    public String cdcUpdateProfile(@PathVariable("id") String profileId, @RequestBody String profileJsonString) {
        return updateProfile(profileId, profileJsonString);
    }
    @PostMapping(URL_DEFAULT + CONSENT)
    public String cdcIssueConsent(@RequestBody @Validated Consent consent) {
        return issueConsent(consent);
    }
    @GetMapping(URL_DEFAULT + CONSENT + ID + TYPE)
    public String cdcRequestConsentHistory(@PathVariable("id") String id, @PathVariable("type") String type) {
        return requestConsentHistory(id, type);
    }
    @PatchMapping(URL_DEFAULT + CONSENT + CONSENT_ID + ACCEPT)
    public String cdcProvideConsentForAccept(@PathVariable("consentId") String consentId) {
        return ProvideConsentForAccept(consentId);
    }
    @PatchMapping(URL_DEFAULT + CONSENT + CONSENT_ID + REJECT)
    public String cdcProvideConsentForReject(@PathVariable("consentId") String consentId) {
        return ProvideConsentForReject(consentId);
    }
    @PatchMapping(URL_DEFAULT + CONSENT + CONSENT_ID + "/expire")
    public String cdcProvideConsentForExpire(@PathVariable("consentId") String consentId) {
        return ProvideConsentForExpire(consentId);
    }
    @PutMapping(URL_DEFAULT + CONSENT)
    public String cdcUpdateConsent(@RequestBody @Validated String id, @RequestBody @Validated String type, @RequestBody @Validated String consentJsonString) {
        return updateConsent(id, type, consentJsonString);
    }
    @GetMapping(URL_DEFAULT + CONSENT + VALIDATE + CONSENT_ID + TOKEN)
    public String cdcValidateToken(@PathVariable("consentId") String consentId, @PathVariable("token") String token) {
        return validateToken(consentId, token);
    }
    @GetMapping(URL_DEFAULT + CONSENT + CONSENT_ID)
    public String cdcRetrieveConsent(@PathVariable("consentId") String consentId) {
        return retrieveConsent(consentId);
    }
}

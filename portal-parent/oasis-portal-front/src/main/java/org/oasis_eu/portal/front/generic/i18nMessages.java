package org.oasis_eu.portal.front.generic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;

import com.fasterxml.jackson.core.JsonProcessingException;

public final class i18nMessages {
    private i18nMessages(){}; //this class can't be an instance (all members are static)

    private static final List<String> i18nkeys = Arrays.asList("create-org", "modify-org", "find-or-create-organization", "information",
            "leave", "invite", "admin", "user", "email", "yes-i-want-to-leave", "confirm-leave",
            "organization-type.PUBLIC_BODY", "organization-type.COMPANY", "organization-name", "organization-type", "create",
            "by", "will-be-deleted", "confirm-trash.title", "confirm-trash.body", "confirm-untrash.title", "confirm-untrash.body",
            "organization.pending-invitation");

    private static final List<String> generickeys = Arrays.asList("save", "cancel", "close", "appstore", "confirm", "delete",
            "loading", "go", "general-error", "edit", "remove", "location", "unexpected_error", "something_went_wrong_msg",
            "something_went_wrong_title", "error_detail_title", "search", "next", "previous");

    private static final List<String> searchOrganization = Arrays.asList("title","country","legal-name", "business-id.fr", "business-id.it",
            "business-id.es", "business-id.bg", "business-id.tr", "business-id.en", "sector-type", "sector-type.PUBLIC_BODY","sector-type.COMPANY",
            "selection.new", "selection.existing", "cannot-be-used");

    private static final List<String> searchContact = Arrays.asList("title","address.title","name", "lastname","email");

    private static final List<String> createOrModifyOrganization = Arrays.asList("legal_name","in_activity","tab1.general_information",
            "alt_name","org_type", "org_type.placeholder", "tax_reg_num.bg", "tax_reg_num.es", "tax_reg_num.fr", "tax_reg_num.it", "tax_reg_num.tr",
            "tax_reg_num.en", "tax_reg_official_id.fr","tax_reg_official_id.tr","tax_reg_activity.bg","tax_reg_activity.es","tax_reg_activity.fr",
            "tax_reg_activity.it","tax_reg_activity.tr","tax_reg_activity.en","jurisdiction",
            "jurisdiction.placeholder","phone_number","web_site", "email","email.placeholder","change-icon","tab2.address_geolocation",
            "street_and_number","additional_address_field","po_box","city","zip", "cedex","country");

    private static final List<String> storekeys = Arrays.asList("citizens", "publicbodies", "companies", "free", "paid",
            "languages-supported-by-applications", "look-for-an-application", "keywords","installed", "tos", "privacy", "by",
            "agree-to-tos", "install", "install_this_app", "confirm-install-this-app", "confirm-install-this-app-paid",
            "for_myself", "on_behalf_of", "create-new-org", "modify-org","buying", "sorry", "could-not-install-app", "already-rated", "launch");

    private static final List<String> storeInstallkeys = Arrays.asList("type.ORG", "type.PERSONAL", "success-msg-1", "success-msg-2", "success-msg-3",
            "success-msg-4");

    private static final List<String> languagekeys = Arrays.asList("all", "en", "fr", "it", "es", "ca", "tr", "bg"); // OZWILLO locales

    private static final List<String> networkkeys = Arrays.asList("organization-name", "organization-type", "organization-type.PUBLIC_BODY",
            "organization-type.COMPANY", "create");

    private static final List<String> myApps = Arrays.asList(
            "none", "manage_users", "users", "push-to-dashboard-existing-user", "settings", "name", "actions",
            "settings-add-a-user", "description", "icon", "upload", "published", "notpublished", "services",
            "restricted-service", "geographical-area-of-interest",
            "by", "will-be-deleted", "confirm-trash.title", "confirm-trash.body", "confirm-untrash.title", "confirm-untrash.body",
            "apps-for-organization", "apps-for-personal-use");

    private static final List<String> errors = Arrays.asList("datacore.forbidden");


    /* Messages Handlers  */
    public static Map<String, String> getI18n_all(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        Map<String, String> i18n = new HashMap<>();

        i18n.putAll(i18nkeys.stream().collect(Collectors.toMap(k -> "my.network." + k,
                k -> messageSource.getMessage("my.network." + k, new Object[]{}, locale))));
        i18n.putAll(generickeys.stream().collect(Collectors.toMap(k -> "ui." + k,
                k -> messageSource.getMessage("ui." + k, new Object[]{}, locale))));
        i18n.putAll(searchOrganization.stream().collect(Collectors.toMap(k -> "search.organization." + k,
                k -> messageSource.getMessage("search.organization." + k, new Object[]{}, locale))));
        i18n.putAll(searchContact.stream().collect(Collectors.toMap(k -> "search.contact." + k,
                k -> messageSource.getMessage("search.contact." + k, new Object[]{}, locale))));
        i18n.putAll(createOrModifyOrganization.stream().collect(Collectors.toMap(k -> "my.network.organization." + k,
                k -> messageSource.getMessage("my.network.organization." + k, new Object[]{}, locale))));

        i18n.putAll(networkkeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.network." + k, new Object[0], locale))));
        i18n.putAll(storekeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store." + k, new Object[0], locale))));
        i18n.putAll(storeInstallkeys.stream().collect(Collectors.toMap(k -> "install.org."+k,
                k -> messageSource.getMessage("install.org." + k, new Object[0], locale))));
        i18n.putAll(languagekeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store.language." + k, new Object[0], locale))));
        i18n.putAll(myApps.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.apps." + k, new Object[0], locale))));
        i18n.putAll(errors.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("error." + k, new Object[0], locale))));

        return i18n;
    }

    public static Map<String, String> getI18n_i18keys(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (i18nkeys.stream().collect(Collectors.toMap(k -> "my.network." + k,
                k -> messageSource.getMessage("my.network." + k, new Object[]{}, locale))));
    }
    public static Map<String, String> getI18n_generickeys(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (generickeys.stream().collect(Collectors.toMap(k -> "ui." + k, k -> messageSource.getMessage("ui." + k, new Object[]{}, locale))));
    }
    public static Map<String, String> getI18n_searchOrganization(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(searchOrganization.stream().collect(Collectors.toMap(k -> "search.organization." + k,
                k -> messageSource.getMessage("search.organization." + k, new Object[]{}, locale))));
        i18n.putAll(searchContact.stream().collect(Collectors.toMap(k -> "search.contact." + k,
                k -> messageSource.getMessage("search.contact." + k, new Object[]{}, locale))));
        return i18n;
    }
    public static Map<String, String> getI18n_createOrModifyOrganization(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (createOrModifyOrganization.stream().collect(Collectors.toMap(k -> "my.network.organization." + k,
                k -> messageSource.getMessage("my.network.organization." + k, new Object[]{}, locale))));
    }
    public static Map<String, String> getI18n_networkkeys(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (networkkeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.network." + k, new Object[0], locale))));
    }
    public static Map<String, String> getI18n_storekeys(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(storekeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store." + k, new Object[0], locale))));
        i18n.putAll(storeInstallkeys.stream().collect(Collectors.toMap(k -> "install.org."+k,
                k -> messageSource.getMessage("install.org." + k, new Object[0], locale))));
        return i18n;
    }
    public static Map<String, String> getI18n_errors(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (errors.stream().collect(Collectors.toMap(k -> "error." + k, k -> messageSource.getMessage("error." + k, new Object[]{}, locale))));
    }
    public static Map<String, String> getI18n_myApps(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (myApps.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.apps." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_languagekeys(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        return (languagekeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store.language." + k, new Object[0], locale))));
    }
}

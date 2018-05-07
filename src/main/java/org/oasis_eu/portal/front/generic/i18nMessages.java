package org.oasis_eu.portal.front.generic;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.MessageSource;

import java.util.*;
import java.util.stream.Collectors;

public final class i18nMessages {
    private i18nMessages() {
    }

    //this class can't be an instance (all members are static)

    private static final List<String> i18nkeys = Arrays.asList("create-org", "modify-org", "add-organization", "information",
        "leave", "invite", "admin", "user", "email", "yes-i-want-to-leave", "confirm-leave", "no-information-available",
        "organization-type.PUBLIC_BODY", "organization-type.COMPANY", "organization-name", "organization-type", "create",
        "by", "will-be-deleted", "confirm-trash.title", "confirm-trash.body", "confirm-untrash.title", "confirm-untrash.body",
        "organization.pending-invitation");

    private static final List<String> generickeys = Arrays.asList("save", "create", "cancel", "close", "appstore", "confirm", "delete",
        "loading", "go", "general-error", "edit", "remove", "location", "unexpected_error", "something_went_wrong_msg",
        "something_went_wrong_title", "error_detail_title", "search", "next", "previous", "welcome", "send", "add", "searching",
        "no-matches-found", "yes", "datastore", "login", "logout", "notifications", "message.will-be-deleted",
        "message.will-be-deleted-plural", "or", "request.send", "error", "ok");

    private static final List<String> dashboardKeys = Arrays.asList("dashboard", "profile", "network", "apps", "create", "confirm-delete-dash",
        "confirm-delete-dash-long", "confirm-remove-app", "confirm-remove-app-long", "name", "click-to-add", "drop-to-remove");

    private static final List<String> contactKeys = Arrays.asList("title", "form.copy-to-sender",
        "form.motive", "form.motive.question", "form.motive.feedback", "form.motive.application-problem",
        "form.motive.other-problem", "form.motive.other", "form.subject", "form.body", "form-sent", "technical-problem",
        "form.captcha");

    private static final List<String> searchOrganization = Arrays.asList("search-organization", "title", "country", "legal-name", "business-id.fr", "business-id.it",
        "business-id.es", "business-id.bg", "business-id.tr", "business-id.en", "sector-type", "sector-type.PUBLIC_BODY", "sector-type.COMPANY",
        "selection.new", "selection.existing", "cannot-be-used", "technical-problem");

    private static final List<String> searchContact = Arrays.asList("title", "address.title", "name", "lastname", "email");

    private static final List<String> createOrModifyOrganization = Arrays.asList("profile_information", "contact_lastname", "contact_name", "contact_information", "legal_name", "in_activity",
        "alt_name", "org_type", "org_type.placeholder", "tax_reg_num.bg", "tax_reg_num.es", "tax_reg_num.fr", "tax_reg_num.it", "tax_reg_num.tr",
        "tax_reg_num.en", "tax_reg_num.already_used", "tax_reg_official_id.fr", "tax_reg_official_id.tr", "tax_reg_official_id.bg",
            "tax_reg_official_id.es", "tax_reg_official_id.it", "tax_reg_official_id.en",
            "tax_reg_activity.bg", "tax_reg_activity.es", "tax_reg_activity.fr",
        "tax_reg_activity.it", "tax_reg_activity.tr", "tax_reg_activity.en", "jurisdiction",
        "jurisdiction.placeholder", "phone_number", "web_site", "email", "email.placeholder", "change-icon",
        "street_and_number", "po_box", "city", "zip", "cedex", "country", "invalid_fields", "step",
        "additional_information", "sector_type", "sector_type.PUBLIC_BODY", "sector_type.COMPANY", "sector_type.Public", "sector_type.Private");

    private static final List<String> storekeys = Arrays.asList("audience", "mode", "citizens", "publicbodies", "companies", "free", "paid",
        "languages-supported-by-applications", "geoarea", "keywords", "installed", "tos", "privacy", "by",
        "agree-to-tos", "install", "install_this_app", "confirm-install-this-app", "confirm-install-this-app-paid",
        "for_myself", "on_behalf_of", "create-new-org", "modify-org", "buying", "sorry", "could-not-install-app", "already-rated", "launch", "load-more", "choose-install-type",
        "rating.half-star", "rating.one-star", "rating.one-half-star", "rating.two-stars", "rating.two-half-stars", "rating.three-stars",
        "rating.three-half-stars", "rating.four-stars", "rating.four-half-stars", "rating.five-stars",
        "install.orgType.title", "ad.description", "ad.title.part1", "ad.title.part2", "ad.joinUs");

    private static final List<String> storeInstallkeys = Arrays.asList("type.ORG", "type.PERSONAL", "success-msg-1", "success-msg-2", "success-msg-3",
        "success-msg-4");

    private static final List<String> languagekeys = Arrays.asList("all", "en", "fr", "it", "es", "ca", "tr", "bg"); // OZWILLO locales

    private static final List<String> networkkeys = Arrays.asList("organization-name", "organization-type", "organization-type.PUBLIC_BODY",
        "organization-type.COMPANY", "create");

    private static final List<String> myApps = Arrays.asList(
        "none", "manage_users", "users", "settings", "name", "status", "actions",
        "settings-add-a-user", "setting-add-from-organization", "settings-invite-by-email", "settings-invite-by-email-title",
        "settings.status.to-validate", "settings.status.pending", "settings.status.member",
        "description", "icon", "upload", "published", "services",
        "restricted-service", "geographical-area-of-interest",
        "by", "will-be-deleted", "confirm-trash.title", "confirm-trash.body", "confirm-untrash.title", "confirm-untrash.body",
        "apps-for-organization", "apps-for-personal-use", "pending-install");

    private static final List<String> profilekeys = Arrays.asList("errormsg.formatNoMatches", "errormsg.formatAjaxError",
        "title.account", "personal.nickname", "account.email", "account.changepassword", "account.createpassword", "account.language", "account.language.en",
        "account.language.fr", "account.language.it", "account.language.es", "account.language.ca",
        "account.language.bg", "account.language.tr", "account.password", "personal.address", "personal.identity", "personal.firstname", "personal.lastname",
        "personal.birthdate", "personal.phonenumber", "personal.gender", "personal.gender.male", "personal.gender.female", "personal.postalcode",
        "personal.streetaddress", "personal.country", "personal.locality", "personal.middlename", "account.update");

    private static final List<String> franceconnectKeys = Arrays.asList("name", "form.your-profile", "form.synchronise", "form.desynchronize",
            "form.desynchronize-without-pwd");


    private static final List<String> organizationSearchKeys = Arrays.asList("title", "new", "no-apps-installed");

    private static final List<String> organizationFormKeys = Arrays.asList("invite-new-collaborator", "email", "create",
            "title", "contact-information", "admin");

    private static final List<String> organizationDescKeys = Arrays.asList("applications", "members", "admin",
            "add-application", "add-in-organization", "send-invitation", "form.success");

    private static final List<String> errors = Arrays.asList("datacore.forbidden");

    private static final List<String> tooltipKeys = Arrays.asList("instances", "members", "admin", "config", "pending",
            "delete.organization", "delete.instance", "delete.member", "remove.right", "remove.icon",
            "remove.right.admin", "add.right.admin", "add.icon", "remove.instance", "remove.member");

    private static final List<String> notificationKeys = Arrays.asList("date", "app", "message", "archive", "manage",
            "no-notification", "unread", "read", "any", "all-apps");


    /* Messages Handlers  */
    public static Map<String, String> getI18n_all(Locale locale, MessageSource messageSource) throws JsonProcessingException {
        Map<String, String> i18n = new HashMap<>();

        i18n.putAll(i18nMessages.getI18n_i18keys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18nContactKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18nDashboardKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_searchOrganization(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_createOrModifyOrganization(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_networkkeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_storekeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_errors(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_myApps(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_profilekeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_languagekeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_franceconnectKeys(locale, messageSource));

        i18n.putAll(i18nMessages.getI18n_organizationSearchKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_organizationFormKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_organizationDescKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_tooltipKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_notificationKeys(locale, messageSource));

        return i18n;
    }

    public static Map<String, String> getI18n_i18keys(Locale locale, MessageSource messageSource) {
        return (i18nkeys.stream().collect(Collectors.toMap(k -> "my.network." + k,
            k -> messageSource.getMessage("my.network." + k, new Object[]{}, locale))));
    }

    public static Map<String, String> getI18n_generickeys(Locale locale, MessageSource messageSource) {
        return (generickeys.stream().collect(Collectors.toMap(k -> "ui." + k, k -> messageSource.getMessage("ui." + k, new Object[]{}, locale))));
    }

    public static Map<String, String> getI18nContactKeys(Locale locale, MessageSource messageSource) {
        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(contactKeys.stream().collect(Collectors.toMap(k -> "contact." + k,
            k -> messageSource.getMessage("contact." + k, new Object[]{}, locale))));
        // because there is some magic done with keys prefixes, manually add footer.contact key
        i18n.put("footer.contact", messageSource.getMessage("footer.contact", new Object[]{}, locale));
        return i18n;
    }

    public static Map<String, String> getI18nDashboardKeys(Locale locale, MessageSource messageSource) {
        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(dashboardKeys.stream().collect(Collectors.toMap(k -> "my." + k,
            k -> messageSource.getMessage("my." + k, new Object[]{}, locale))));
        return i18n;
    }

    public static Map<String, String> getI18n_searchOrganization(Locale locale, MessageSource messageSource) {
        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(searchOrganization.stream().collect(Collectors.toMap(k -> "search.organization." + k,
            k -> messageSource.getMessage("search.organization." + k, new Object[]{}, locale))));
        i18n.putAll(searchContact.stream().collect(Collectors.toMap(k -> "search.contact." + k,
            k -> messageSource.getMessage("search.contact." + k, new Object[]{}, locale))));
        return i18n;
    }

    public static Map<String, String> getI18n_createOrModifyOrganization(Locale locale, MessageSource messageSource) {
        return (createOrModifyOrganization.stream().collect(Collectors.toMap(k -> "my.network.organization." + k,
            k -> messageSource.getMessage("my.network.organization." + k, new Object[]{}, locale))));
    }

    public static Map<String, String> getI18n_networkkeys(Locale locale, MessageSource messageSource) {
        return (networkkeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.network." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_storekeys(Locale locale, MessageSource messageSource) {
        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(storekeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store." + k, new Object[0], locale))));
        i18n.putAll(storeInstallkeys.stream().collect(Collectors.toMap(k -> "install.org." + k,
            k -> messageSource.getMessage("install.org." + k, new Object[0], locale))));
        return i18n;
    }

    public static Map<String, String> getI18n_errors(Locale locale, MessageSource messageSource) {
        return (errors.stream().collect(Collectors.toMap(k -> "error." + k, k -> messageSource.getMessage("error." + k, new Object[]{}, locale))));
    }

    public static Map<String, String> getI18n_myApps(Locale locale, MessageSource messageSource) {
        return (myApps.stream().collect(Collectors.toMap(k -> "my.apps." + k, k -> messageSource.getMessage("my.apps." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_profilekeys(Locale locale, MessageSource messageSource) {
        return (profilekeys.stream().collect(Collectors.toMap(k -> "my.profile." + k, k -> messageSource.getMessage("my.profile." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_languagekeys(Locale locale, MessageSource messageSource) {
        return (languagekeys.stream().collect(Collectors.toMap(k -> "store.language." + k, k -> messageSource.getMessage("store.language." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_franceconnectKeys(Locale locale, MessageSource messageSource) {
        return (franceconnectKeys.stream().collect(Collectors.toMap(k -> "franceconnect." +k, k -> messageSource.getMessage("franceconnect." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_organizationSearchKeys(Locale locale, MessageSource messageSource) {
        return (organizationSearchKeys.stream().collect(Collectors.toMap(k -> "organization.search." +k,
                k -> messageSource.getMessage("organization.search." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_organizationFormKeys(Locale locale, MessageSource messageSource) {
        return (organizationFormKeys.stream().collect(Collectors.toMap(k -> "organization.form." +k,
                k -> messageSource.getMessage("organization.form." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_organizationDescKeys(Locale locale, MessageSource messageSource) {
        return (organizationDescKeys.stream().collect(Collectors.toMap(k -> "organization.desc." +k,
                k -> messageSource.getMessage("organization.desc." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_tooltipKeys(Locale locale, MessageSource messageSource) {
        return (tooltipKeys.stream().collect(Collectors.toMap(k -> "tooltip." +k,
                k -> messageSource.getMessage("tooltip." + k, new Object[0], locale))));
    }

    public static Map<String, String> getI18n_notificationKeys(Locale locale, MessageSource messageSource) {
        return (notificationKeys.stream().collect(Collectors.toMap(k -> "notif." +k,
                k -> messageSource.getMessage("notif." + k, new Object[0], locale))));
    }
}

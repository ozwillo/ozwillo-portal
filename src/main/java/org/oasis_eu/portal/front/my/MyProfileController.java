package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.model.Languages;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.model.FormLayout;
import org.oasis_eu.portal.model.FormLayoutMode;
import org.oasis_eu.portal.model.FormWidgetSelect;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.service.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author mkalamalami
 */
//@Controller
//@RequestMapping("/my/profile")
public class MyProfileController extends PortalController {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory
        .getLogger(MyProfileController.class);

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired(required = false)
    private MyProfileState myProfileState;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private MessageSource messageSource;

    @ModelAttribute("modelObject")
    UserAccount getCurrentUserAccount() {
        return new UserAccount(user());
    }

    @ModelAttribute("i18n")
    public Map<String, String> i18n(HttpServletRequest request) throws JsonProcessingException {
        Locale locale = RequestContextUtils.getLocale(request);
        Map<String, String> i18n = new HashMap<>();

        i18n.putAll(i18nMessages.getI18n_profilekeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18nContactKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));

        return i18n;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {

        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {

            @Override
            public void setAsText(String value) {
                try {
                    //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(new Locale(currentLanguage().getLanguage())); // Languages.locale renvoie en pour locale en-GB
                    //setValue(LocalDate.parse(value, dateTimeFormatter));
                    setValue(LocalDate.parse(value));
                } catch (DateTimeParseException e) {

                    setValue(null);
                }
            }

            @Override
            public String getAsText() {
                return getValue() != null ? getValue().toString() : "1970-01-01";
            }

        });
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public String profile(Model model) {
        if (requiresLogout()) {
            return "redirect:/logout";
        }


        initProfileModel(model);
        return "profile/my-profile";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/fragment/layout/{id}")
    public String profileLayoutFragment(@PathVariable("id") String layoutId,
        Model model, RedirectAttributes redirectAttributes) {

        initProfileModel(model);
        model.addAttribute("layout", myProfileState.getLayout(layoutId));

        return "profile/my-profile-fragments :: layout";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/mode")
    public String toggleProfileLayout(@RequestParam("mode") String mode,
        @RequestParam("id") String layoutId, Model model, RedirectAttributes redirectAttributes) {
        FormLayout formLayout = myProfileState.getLayout(layoutId);
        if (formLayout != null) {
            formLayout.setMode(FormLayoutMode.valueOf(mode));
        }
        initProfileModel(model);
        model.addAttribute("layout", formLayout);

//		return "redirect:/my/profile/fragment/layout/" + layoutId;
        return "profile/my-profile-fragments::layout";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save/{layoutId}")
    public String saveLayout(@PathVariable("layoutId") String layoutId,
        @ModelAttribute("modelObject") @Valid UserAccount currentUser, BindingResult result, Model model,
        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            // check that on-errors attributes are fields from the layout
            List<FieldError> fieldsErrors = result.getFieldErrors();
            Iterator<FieldError> itFieldsErrors = fieldsErrors.iterator();
            int layoutFieldsErrorsCount = 0;
            while (itFieldsErrors.hasNext()) {
                FieldError fieldError = itFieldsErrors.next();
                if (myProfileState.getLayout(layoutId).getWidget(fieldError.getField()) != null) {
                    layoutFieldsErrorsCount++;
                }
            }
            if (layoutFieldsErrorsCount > 0) {

                initProfileModel(model);
                model.addAttribute("layout", myProfileState.getLayout(layoutId));
//				return "includes/my-profile-fragments :: layout";
                return "profile/my-profile";
            }
        }

        currentUser.setName(currentUser.getNickname()); // force name = nickname
        userAccountService.saveUserAccount(currentUser);

        FormLayout layout = myProfileState.getLayout(layoutId);
        layout.setMode(FormLayoutMode.VIEW);
        initProfileModel(model);
        model.addAttribute("layout", layout);

//		return "redirect:/my/profile/fragment/layout/" + layoutId;
//		return "includes/my-profile-fragments::layout";
        return "redirect:/my/profile";
    }

    protected void initProfileModel(Model model) {
        model.addAttribute("navigation",
            myNavigationService.getNavigation("profile"));
        model.addAttribute("layouts", myProfileState.getLayouts());

        FormWidgetSelect localeSelect = (FormWidgetSelect) myProfileState.getLayout("account").getWidget("locale");
        for (Languages language : languages()) {
            localeSelect.addOption(language.getLocale().getLanguage(), "language.name." + language.getLocale().getLanguage());
        }
    }

};

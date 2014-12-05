package org.oasis_eu.portal.services;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * User: schambon
 * Date: 8/14/14
 */
@Service
public class NameDefaults {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HttpServletRequest request;

    public UserInfo complete(UserInfo userInfo) {

        UserInfo ui = UserInfo.from(userInfo);


        // normalizes locale (en if included country/was en-GB in database)
        if (ui.getLocale() != null) {

            ui.setLocale(Locale.forLanguageTag(ui.getLocale()).getLanguage());
        } else {
            Locale currentLocale = RequestContextUtils.getLocale(request);
            ui.setLocale(currentLocale.getLanguage());
        }

        return ui;
    }

}

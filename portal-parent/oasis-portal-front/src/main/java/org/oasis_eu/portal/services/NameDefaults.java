package org.oasis_eu.portal.services;

import java.util.Locale;

import com.google.common.base.Strings;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

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
        if (Strings.isNullOrEmpty(userInfo.getName())) {

            StringBuilder name = new StringBuilder("");

            if (! Strings.isNullOrEmpty(userInfo.getGivenName()) ) {
                name.append(userInfo.getGivenName());
                name.append(" ");
            }
            if (!Strings.isNullOrEmpty(userInfo.getFamilyName())) {
                name.append(userInfo.getFamilyName());
            }

            String sName = name.toString();
            if (Strings.isNullOrEmpty(sName)) {
                if (!Strings.isNullOrEmpty(userInfo.getEmail())) {
                    sName = userInfo.getEmail();
                } else {
                    sName = messageSource.getMessage("ui.default_username", new Object[0], RequestContextUtils.getLocale(request));
                }
            }

            userInfo.setName(sName);
        }
        
        // normalizes locale (en if included country/was en-GB in database)
        if(userInfo.getLocale()!=null) {
			
        	userInfo.setLocale(Locale.forLanguageTag(userInfo.getLocale()).getLanguage());
		} else {
			Locale currentLocale = RequestContextUtils.getLocale(request);
			userInfo.setLocale(currentLocale.getLanguage());
		}

        return userInfo;
    }

}

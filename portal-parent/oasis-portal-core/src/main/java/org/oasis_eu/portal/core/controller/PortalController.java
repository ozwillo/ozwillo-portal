package org.oasis_eu.portal.core.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * User: schambon
 * Date: 6/11/14
 */
abstract public class PortalController {
    
    @ModelAttribute("languages")
    public Languages[] languages() {
        return Languages.values();
    }

    @ModelAttribute("currentLanguage")
    public Languages currentLanguage(HttpServletRequest request) {
        Locale current = RequestContextUtils.getLocale(request);
        for (Languages l : Languages.values()) {
            if (l.getLanguage().equals(current.getLanguage())) {
                return l;
            }
        }
        return Languages.ENGLISH;
    }

}

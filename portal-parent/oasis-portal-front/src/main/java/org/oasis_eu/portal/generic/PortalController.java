package org.oasis_eu.portal.generic;

import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * User: schambon
 * Date: 6/11/14
 */
abstract public class PortalController  {

    @ModelAttribute("languages")
    public Languages[] languages() {
        return Languages.values();
    }

}

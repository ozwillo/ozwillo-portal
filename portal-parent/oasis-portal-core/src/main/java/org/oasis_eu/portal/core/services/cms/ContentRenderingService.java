package org.oasis_eu.portal.core.services.cms;

import java.util.Locale;

/**
 * User: schambon
 * Date: 6/11/14
 */
public interface ContentRenderingService {

    public String render(String contentId, Locale language);

}

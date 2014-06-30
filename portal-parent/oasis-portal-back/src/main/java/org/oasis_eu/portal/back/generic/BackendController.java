package org.oasis_eu.portal.back.generic;

import org.oasis_eu.portal.core.controller.PortalController;
import org.oasis_eu.portal.services.ErrorMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
public class BackendController extends PortalController {
    
	@Autowired
	protected ResourceBundleMessageSource messageSource;
	
    @Autowired
    protected ErrorMessageService errorMessageService;

    @ModelAttribute("errorMessageService")
    public ErrorMessageService getErrorMessageService() {
		return errorMessageService;
	}
    
}

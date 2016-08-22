package org.oasis_eu.portal.front.generic;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * User: ilucatero Date: 20/05/15
 */
public abstract class BaseAJAXServices {

	@Autowired
	protected MessageSource messageSource;

	public String getErrorMessage(String key, HttpServletRequest request) {
		Locale locale = RequestContextUtils.getLocale(request);
		return messageSource.getMessage("error.msg." + key, new Object[] {}, locale);
	}

	// was used in MyAppsAJAXServices
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public void accessDenied() { }
	
	//was used in  DashBoardAJAXServices
	@ExceptionHandler(AuthenticationRequiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public void hande401(HttpServletRequest request, HttpServletResponse response) throws IOException {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
	}
	
	// was used in NetworkAJAXSQervices
	@ExceptionHandler(WrongQueryException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handle400(HttpServletRequest request, WrongQueryException wqex) throws IOException {
		// public String handle400(HttpServletResponse httpRes,
		// WrongQueryException wqex) throws IOException {
		if (wqex.getTranslatedBusinessMessage() == null || wqex.getTranslatedBusinessMessage().isEmpty()) {

			String translatedBusinessMessage = getErrorMessage("action-cant-be-done", request);
			wqex.setTranslatedBusinessMessage(translatedBusinessMessage);
		}
		return wqex.getTranslatedBusinessMessage();
	}

	// new
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handle404(HttpServletRequest request, EntityNotFoundException enfex) throws IOException {
		if (enfex.getTranslatedBusinessMessage() == null || enfex.getTranslatedBusinessMessage().isEmpty()) {
			String translatedBusinessMessage = getErrorMessage("not-found", request);
			enfex.setTranslatedBusinessMessage(translatedBusinessMessage);
		}
		return enfex.getTranslatedBusinessMessage();
	}

	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ResponseBody
	public String handle403(HttpServletRequest request, ForbiddenException fex) throws IOException {
		if (fex.getTranslatedBusinessMessage() == null || fex.getTranslatedBusinessMessage().isEmpty()) {
			String translatedBusinessMessage = getErrorMessage("action-forbidden", request);
			fex.setTranslatedBusinessMessage(translatedBusinessMessage);
		}
		return fex.getTranslatedBusinessMessage();
	}
  
	// was used in StoreAJAXServices
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // rather than 400, because is developer error
	public void handle500() { }


}

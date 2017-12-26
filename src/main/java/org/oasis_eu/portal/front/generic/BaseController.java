package org.oasis_eu.portal.front.generic;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;

/**
 * User: ilucatero Date: 20/05/15
 */
public abstract class BaseController {

    @Autowired
    protected MessageSource messageSource;

    public String getErrorMessage(String key, HttpServletRequest request) {
        Locale locale = RequestContextUtils.getLocale(request);
        return messageSource.getMessage("error.msg." + key, new Object[]{}, locale);
    }

    // was used in MyAppsController
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void accessDenied() { }

    //was used in  DashBoardAJAXServices
    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void hande401(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // was used in NetworkAJAXSServices
    @ExceptionHandler(WrongQueryException.class)
    @ResponseBody
    public ResponseEntity<String> handle400(HttpServletRequest request, WrongQueryException wqex) {
        // public String handle400(HttpServletResponse httpRes,
        // WrongQueryException wqex) throws IOException {
        if (wqex.getTranslatedBusinessMessage() == null || wqex.getTranslatedBusinessMessage().isEmpty()) {

            String translatedBusinessMessage = getErrorMessage("action-cant-be-done", request);
            wqex.setTranslatedBusinessMessage(translatedBusinessMessage);
        }

        return ResponseEntity
                .status(HttpStatus.valueOf(wqex.getStatusCode()))
                .body(wqex.getTranslatedBusinessMessage());
    }

    // new
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handle404(HttpServletRequest request, EntityNotFoundException enfex) {
        if (enfex.getTranslatedBusinessMessage() == null || enfex.getTranslatedBusinessMessage().isEmpty()) {
            String translatedBusinessMessage = getErrorMessage("not-found", request);
            enfex.setTranslatedBusinessMessage(translatedBusinessMessage);
        }
        return ResponseEntity
                .status(HttpStatus.valueOf(enfex.getStatusCode()))
                .body(enfex.getTranslatedBusinessMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseBody
    public ResponseEntity<String>  handle403(HttpServletRequest request, ForbiddenException fex) {
        if (fex.getTranslatedBusinessMessage() == null || fex.getTranslatedBusinessMessage().isEmpty()) {
            String translatedBusinessMessage = getErrorMessage("action-forbidden", request);
            fex.setTranslatedBusinessMessage(translatedBusinessMessage);
        }
        return ResponseEntity
                .status(HttpStatus.valueOf(fex.getStatusCode()))
                .body(fex.getTranslatedBusinessMessage());
    }

    // was used in StoreController
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // rather than 400, because is developer error
    public void handle500() { }

}

package org.oasis_eu.portal.controller;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.security.RefreshTokenNeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

@ControllerAdvice
public class PortalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    protected MessageSource messageSource;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String getErrorMessage(String key, HttpServletRequest request) {
        Locale locale = RequestContextUtils.getLocale(request);
        return messageSource.getMessage("error.msg." + key, new Object[]{}, locale);
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public void handleAuthRequired(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("/my");
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleAccessDenied() {
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseBody
    public ResponseEntity<String> handle403(HttpServletRequest request, ForbiddenException fex) {
        if (fex.getTranslatedBusinessMessage() == null || fex.getTranslatedBusinessMessage().isEmpty()) {
            String translatedBusinessMessage = getErrorMessage("action-forbidden", request);
            fex.setTranslatedBusinessMessage(translatedBusinessMessage);
        }
        return ResponseEntity
                .status(HttpStatus.valueOf(fex.getStatusCode()))
                .body(fex.getTranslatedBusinessMessage());
    }

    @ExceptionHandler(WrongQueryException.class)
    @ResponseBody
    public ResponseEntity<String> handle400(HttpServletRequest request, WrongQueryException wqex) {
        if (wqex.getTranslatedBusinessMessage() == null || wqex.getTranslatedBusinessMessage().isEmpty()) {

            String translatedBusinessMessage = getErrorMessage("action-cant-be-done", request);
            wqex.setTranslatedBusinessMessage(translatedBusinessMessage);
        }

        return ResponseEntity
                .status(HttpStatus.valueOf(wqex.getStatusCode()))
                .body(wqex.getTranslatedBusinessMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handle404(HttpServletRequest request, EntityNotFoundException enfex) {
        if (enfex.getTranslatedBusinessMessage() == null || enfex.getTranslatedBusinessMessage().isEmpty()) {
            String translatedBusinessMessage = getErrorMessage("not-found", request);
            enfex.setTranslatedBusinessMessage(translatedBusinessMessage);
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(enfex.getTranslatedBusinessMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleErrors(Exception e) {

        if (e instanceof RefreshTokenNeedException) {
            throw (RefreshTokenNeedException) e;
        }

        logger.error("Cannot process request: ", e);

        return e.getMessage();
    }
}

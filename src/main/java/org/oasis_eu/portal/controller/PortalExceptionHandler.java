package org.oasis_eu.portal.controller;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.security.RefreshTokenNeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@ControllerAdvice
public class PortalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("error.msg.action-forbidden");
    }

    @ExceptionHandler(WrongQueryException.class)
    @ResponseBody
    public ResponseEntity<String> handle400(HttpServletRequest request, WrongQueryException wqex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(wqex.getMessage() != null ? wqex.getMessage() : "error.msg.action-cant-be-done");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handle404(HttpServletRequest request, EntityNotFoundException enfex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("error.msg.not-found");
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

package org.oasis_eu.portal.front.generic;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: ilucatero Date: 20/05/15
 */
public abstract class BaseAJAXServices {
		
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
    public void handleErrors() { }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void hande403() { }
  
    // was used in StoreAJAXServices
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException() { }
    
    
}

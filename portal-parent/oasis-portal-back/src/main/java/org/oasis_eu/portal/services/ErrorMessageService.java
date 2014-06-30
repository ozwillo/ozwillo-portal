package org.oasis_eu.portal.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 
 * Provides a way to store and retrieve errors to be displayed in the UI.
 * 
 * @author mkalamalami
 *
 */
@Service
public class ErrorMessageService {

    private List<String> errorMessages = new ArrayList<String>();

    public List<String> getErrorMessages() {
    	List<String> errorsToDisplay = errorMessages;
    	errorMessages = new ArrayList<String>();
    	return errorsToDisplay;
    }
    
    public void addErrorMessage(String message) {
    	errorMessages.add(message);
    }
    

}

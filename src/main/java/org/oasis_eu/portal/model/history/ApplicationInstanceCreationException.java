package org.oasis_eu.portal.model.store;

import org.oasis_eu.portal.model.kernel.ApplicationInstantiationRequest;


/**
 * User: schambon
 * Date: 9/12/14
 */
public class ApplicationInstanceCreationException extends RuntimeException {

    private String applicationId;
    private ApplicationInstantiationRequest requested;
    private ApplicationInstanceErrorType type;
    private Integer httpStatus;


    public ApplicationInstanceCreationException(String appId, Integer httpStatus, ApplicationInstantiationRequest requested, ApplicationInstanceErrorType type) {
        this.applicationId = appId;
        this.requested = requested;
        this.type = type;
        this.httpStatus = httpStatus;
    }


    public ApplicationInstantiationRequest getRequested() {
        return requested;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public ApplicationInstanceErrorType getType() {
        return type;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public enum ApplicationInstanceErrorType {
        TECHNICAL_ERROR,
        INVALID_REQUEST
    }
}

package org.oasis_eu.portal.core.model.appstore;

/**
 * User: schambon
 * Date: 9/12/14
 */
public class ApplicationInstanceCreationException extends RuntimeException {

    private String applicationId;
    private ApplicationInstantiationRequest requested;
    private ApplicationInstanceErrorType type;

    public ApplicationInstanceCreationException(String appId, ApplicationInstantiationRequest requested, ApplicationInstanceErrorType type) {
        this.applicationId = appId;
        this.requested = requested;
        this.type = type;
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


    public enum ApplicationInstanceErrorType {
        TECHNICAL_ERROR,
        INVALID_REQUEST
    }
}

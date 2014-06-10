package org.oasis_eu.portal.core.exception;

/**
 * User: schambon
 * Date: 5/28/14
 */
public class EntityNotFoundException extends RuntimeException {


    public EntityNotFoundException(String appstoreCategoryId) {
        super("App store category not found: " + appstoreCategoryId);
    }

}

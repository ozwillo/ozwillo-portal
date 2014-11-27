package org.oasis_eu.portal.core.model.subscription;

/**
 * This enum controls the type of subscription.
 * <ul>
 * <li>personnal subscription: as a private person, add an application or local service to my dashboard</li>
 * <li>manager: buy an application for use in my company/public institution</li>
 * <li>employee: use an application in a professional context, that has been bought and assigned to me by someone else</li>
 * </ul>
 * <p>
 * User: schambon
 * Date: 6/12/14
 */
public enum SubscriptionType {
    PERSONAL,
    ORGANIZATION
}

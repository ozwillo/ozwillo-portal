package org.oasis_eu.portal.mockserver.subscription;

import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.mockserver.repo.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * todo manage contexts here
 *
 * User: schambon
 * Date: 6/12/14
 */
@RestController
@RequestMapping("/subs")
public class SubscriptionEndpoint {

    @Autowired
    private TestData testData;


    @RequestMapping(method = RequestMethod.GET, value = "/{user_id}")
    public List<Subscription> getSubscriptions(@PathVariable("user_id") String userId) {
        return testData.getSubscriptions(userId);
    }

}

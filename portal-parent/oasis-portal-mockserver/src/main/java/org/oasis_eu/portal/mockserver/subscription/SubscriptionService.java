package org.oasis_eu.portal.mockserver.subscription;

import org.oasis_eu.portal.mockserver.repo.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * User: schambon
 * Date: 6/12/14
 */
@RestController
@RequestMapping("/subs")
public class SubscriptionService {

    @Autowired
    private TestData testData;

    @RequestMapping(value = "/apps/{userId}", method = RequestMethod.GET)
    public Set<String> getAppSubscriptions(@PathVariable String userId) {
        return testData.getAppSubscriptions(userId);
    }

    @RequestMapping(value = "/services/{userId}", method = RequestMethod.GET)
    public Set<String> getLocalServiceSubscriptions(@PathVariable String userId) {
        return testData.getServiceSubscriptions(userId);
    }

    @RequestMapping(value = "/apps/{userId}/{appId}", method = RequestMethod.POST)
    public void subscribeApplication(String userId, String appId) {
        testData.subscribeApplication(userId, appId);
    }

    @RequestMapping(value = "/apps/{userId}/{appId}", method = RequestMethod.DELETE)
    public void unSubscribeApplication(String userId, String appId) {
        testData.unsubscribeApplication(userId, appId);
    }

    @RequestMapping(value = "/services/{userId}/{serviceId}", method = RequestMethod.POST)
    public void subscribeLocalService(String userId, String serviceId) {
        testData.subscribeApplication(userId, serviceId);
    }

    @RequestMapping(value = "/services/{userId}/{serviceId}", method = RequestMethod.DELETE)
    public void unSubscribeLocalService(String userId, String serviceId) {
        testData.subscribeApplication(userId, serviceId);
    }
}

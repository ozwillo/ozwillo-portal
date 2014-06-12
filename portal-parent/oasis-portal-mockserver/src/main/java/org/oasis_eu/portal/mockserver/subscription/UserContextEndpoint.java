package org.oasis_eu.portal.mockserver.subscription;

import org.oasis_eu.portal.core.model.subscription.UserContext;
import org.oasis_eu.portal.mockserver.repo.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * User: schambon
 * Date: 6/12/14
 */
@RestController
@RequestMapping("/ctx")
public class UserContextEndpoint {

    @Autowired
    private TestData testData;

    @RequestMapping(method = RequestMethod.GET, value = "/{user_id}")
    public List<UserContext> getUserContexts(@PathVariable("user_id") String userId) {
        return testData.getUserContexts(userId);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/{user_id}")
    public UserContext push(@RequestBody UserContext context, @PathVariable("user_id") String userId) {
        context.setId(UUID.randomUUID().toString());

        testData.getUserContexts(userId).add(context);

        return context;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{user_id}")
    public UserContext update(@RequestBody UserContext context, @PathVariable("user_id") String userId) {
        remove(userId, context.getId());
        testData.getUserContexts(userId).add(context);
        return context;
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/{user_id}/{context_id}")
    public void remove(@PathVariable("user_id") String userId, @PathVariable("context_id") String contextId) {
        for (Iterator<UserContext> it = testData.getUserContexts(userId).iterator(); it.hasNext(); ) {
            UserContext context = it.next();
            if (context.getId().equals(contextId)) {
                it.remove();
                return;
            }
        }
        throw new NoSuchElementException(contextId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{user_id}/{context_id}")
    public UserContext get(@PathVariable("user_id") String userId, @PathVariable("context_id") String contextId) {
        return testData.getUserContexts(userId).stream().filter(c -> c.getId().equals(contextId)).findAny().get();
    }



    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoSuch() {
    }

}

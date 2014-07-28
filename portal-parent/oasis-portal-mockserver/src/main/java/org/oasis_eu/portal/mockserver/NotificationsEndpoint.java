package org.oasis_eu.portal.mockserver;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 6/25/14
 */
@RestController
@RequestMapping("/n")
@Deprecated
public class NotificationsEndpoint {

    @RequestMapping("/{userId}/messages")
    public List<Object> notifications(@PathVariable String userId) {

        return new ArrayList<>();
    }
}

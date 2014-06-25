package org.oasis_eu.portal.mockserver;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: schambon
 * Date: 6/25/14
 */
@RestController
@RequestMapping("/d")
public class OrganizationEndpoint {

    @RequestMapping("/org/{orgId}")
    public String getOrg(@PathVariable String orgId) {
        return "{\n" +
                "  \"id\": \"6dccdb8d-ec46-4675-9965-806ea37b73e1\",\n" +
                "  \"name\": \"openwide-ck\",\n" +
                "  \"modified\": 1386859649613\n" +
                "}";
    }
}

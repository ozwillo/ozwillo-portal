package org.oasis_eu.portal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Utility endpoint used to get the CSRF token required by Spring Security when modifying resources.
 *
 * The token is retrieved from the response headers, see {@link org.oasis_eu.portal.config.CsrfTokenGeneratorFilter}
 */
@RestController
@RequestMapping("/api/csrf-token")
public class CsrfTokenController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> fakeCallToGetCsrfToken() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

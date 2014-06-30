package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.BackendNavigation;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/18/14
 */
@Service
public class BackendNavigationService {

    private final List<String> pages = Arrays.asList("contents");

    public List<BackendNavigation> getNavigation(String pagename) {
        return pages.stream()
        		.map(id -> new BackendNavigation().setId(id).setActive(id.equals(pagename)))
        		.collect(Collectors.toList());
    }
    
    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }

}

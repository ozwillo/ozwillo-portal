package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.MyNavigation;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/18/14
 */
@Service
public class MyNavigationService {

    private List<String> pages = Arrays.asList("dashboard", "profile", "network", "apps-management");

    public List<MyNavigation> getNavigation(String pagename) {
        return pages.stream().map(id -> new MyNavigation().setId(id).setActive(id.equals(pagename))).collect(Collectors.toList());
    }

}

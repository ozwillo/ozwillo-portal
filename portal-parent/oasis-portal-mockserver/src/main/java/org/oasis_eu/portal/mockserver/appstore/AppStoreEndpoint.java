package org.oasis_eu.portal.mockserver.appstore;

import org.oasis_eu.portal.core.model.appstore.*;
import org.oasis_eu.portal.mockserver.repo.TestData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/12/14
 */
@RestController
@RequestMapping("/store")
public class AppStoreEndpoint {

    @Autowired
    private TestData testData;

    @RequestMapping(method = RequestMethod.GET, value = "/app/{applicationId}")
    public Application getApplication(@PathVariable String applicationId) {
        return testData.getApplications().get(applicationId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/app")
    public List<Application> getApplications(@RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "limit", defaultValue = "25") int limit) {
        return new ArrayList<>(testData.getApplications().values());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/app/search")
    public List<Application> findApplication(@RequestParam String query, @RequestParam(value = "controls", required = false) SearchControls searchControls) {
        String queryLanguage = searchControls(searchControls).getLanguage();

        if (queryLanguage == null) {
            return testData.getApplications().values().stream().filter(app -> app.getDefaultName().toLowerCase().contains(query.toLowerCase()) || app.getDefaultDescription().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
        } else {
            Locale locale = new Locale(queryLanguage);
            return testData.getApplications().values().stream().filter(app -> app.getName(locale).toLowerCase().contains(query.toLowerCase()) || app.getDescription(locale).toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/service")
    public List<LocalService> getServices(@RequestParam List<String> territories, @RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "limit", defaultValue = "25") int limit) {
        return testData.getLocalServices().values().stream().filter(s -> territories.contains(s.getTerritoryId())).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/service/search")
    public List<LocalService> findServices(@RequestParam String query, @RequestParam List<String> territories, @RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "limit", defaultValue = "25") int limit) {
        return testData.getLocalServices().values().stream().filter(s -> territories.contains(s.getTerritoryId()) && s.getDefaultName().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value="/service/{serviceId}")
    public LocalService getLocalService(@PathVariable String serviceId) {
        return testData.getLocalServices().get(serviceId);
    }

    private SearchControls defaultSearchControls;
    {
        defaultSearchControls = new SearchControls("en");
        defaultSearchControls.setAudience(Audience.CITIZENS);
    }

    private SearchControls searchControls(SearchControls in) {
        if (in != null) return in;

        return defaultSearchControls;
    }


}

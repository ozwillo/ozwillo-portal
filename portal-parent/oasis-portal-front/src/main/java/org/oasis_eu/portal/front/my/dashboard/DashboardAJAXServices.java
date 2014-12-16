package org.oasis_eu.portal.front.my.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.model.dashboard.DashboardApp;
import org.oasis_eu.portal.model.dashboard.DashboardPendingApp;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * User: schambon
 * Date: 11/17/14
 */
@RestController
@RequestMapping("/my/api/dashboard")
public class DashboardAJAXServices {

    @Autowired
    private PortalDashboardService portalDashboardService;

    @Autowired
    private PortalNotificationService portalNotificationService;

    @RequestMapping(value = "/dashboards", method = GET)
    public List<UserContext> getContexts() {
        return portalDashboardService.getUserContexts();
    }

    @RequestMapping(value = "/apps", method = GET)
    private List<DashboardApp> getAppsForMainContext() {
        return portalDashboardService.getMainDashboardApps();
    }

    @RequestMapping(value = "/apps/{contextId}", method = GET)
    public List<DashboardApp> getAppsForContext(@PathVariable String contextId) {
        return portalDashboardService.getDashboardApps(contextId);
    }

    @RequestMapping(value = "/apps/{contextId}", method = PUT)
    public void updateApps(@PathVariable String contextId, @RequestBody List<DashboardApp> apps) {
        portalDashboardService.setAppsInContext(contextId, apps);
    }

    @RequestMapping(value = "/apps/move/{appId}/to/{contextId}", method = POST)
    public void moveAppTo(@PathVariable String appId, @PathVariable String contextId) {
        portalDashboardService.moveAppTo(appId, contextId);
    }

    @RequestMapping(value = "/apps/remove/{appId}", method = DELETE)
    public void removeApp(@PathVariable String appId) {
        portalDashboardService.unsubscribeApp(appId);
    }

    @RequestMapping(value = "/pending-apps", method = GET)
    public List<DashboardPendingApp> pendingApps() {
        return portalDashboardService.getPendingApps();
    }


    @RequestMapping(value = "/dashboards", method = POST)
    public UserContext createDashboard(@RequestBody CreateDashRequest request) {
        return portalDashboardService.createContext(request.name);
    }

    public static class CreateDashRequest {
        @JsonProperty String name;
    }

    @RequestMapping(value = "/dashboard/{contextId}", method = PUT)
    public void renameContext(@PathVariable String contextId, @RequestBody UserContext userContext) {
        portalDashboardService.renameContext(contextId, userContext.getName());
    }

    @RequestMapping(value = "/dashboard/{contextId}", method = DELETE)
    public void deleteContext(@PathVariable String contextId) {
        portalDashboardService.deleteContext(contextId);
    }


    @RequestMapping(value = "/notifications", method = GET)
    public Map<String, Integer> getAppNotificationCounts() {
        return portalNotificationService.getAppNotificationCounts();
    }
}

package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.ui.DashboardApp;
import org.oasis_eu.portal.ui.DashboardPendingApp;
import org.oasis_eu.portal.services.DashboardService;
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
public class DashboardController extends BaseController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private PortalNotificationService portalNotificationService;

    @RequestMapping(value = "/dashboards", method = GET)
    public List<UserContext> getContexts() {
        return dashboardService.getUserContexts();
    }

    @RequestMapping(value = "/apps", method = GET)
    private List<DashboardApp> getAppsForMainContext() {
        return dashboardService.getMainDashboardApps();
    }

    @RequestMapping(value = "/apps/{contextId}", method = GET)
    public List<DashboardApp> getAppsForContext(@PathVariable String contextId) {
        return dashboardService.getDashboardApps(contextId);
    }

    @RequestMapping(value = "/apps/{contextId}", method = PUT)
    public void updateApps(@PathVariable String contextId, @RequestBody List<DashboardApp> apps) {
        dashboardService.setAppsInContext(contextId, apps);
    }

    @RequestMapping(value = "/apps/move/{appId}/to/{contextId}", method = POST)
    public void moveAppTo(@PathVariable String appId, @PathVariable String contextId) {
        dashboardService.moveAppTo(appId, contextId);
    }

    @RequestMapping(value = "/apps/remove/{appId}", method = DELETE)
    public void removeApp(@PathVariable String appId) {
        dashboardService.unsubscribeApp(appId);
    }

    @RequestMapping(value = "/pending-apps/{appId}", method = DELETE)
    public void removePendingApp(@PathVariable String appId) {
        dashboardService.removePendingApp(appId);
    }

    @RequestMapping(value = "/pending-apps", method = GET)
    public List<DashboardPendingApp> pendingApps() {
        return dashboardService.getPendingApps();
    }


    @RequestMapping(value = "/dashboards", method = POST)
    public UserContext createDashboard(@RequestBody CreateDashRequest request) {
        return dashboardService.createContext(request.name);
    }

    public static class CreateDashRequest {
        @JsonProperty
        String name;
    }

    @RequestMapping(value = "/dashboard/{contextId}", method = PUT)
    public void renameContext(@PathVariable String contextId, @RequestBody UserContext userContext) {
        dashboardService.renameContext(contextId, userContext.getName());
    }

    @RequestMapping(value = "/dashboard/{contextId}", method = DELETE)
    public void deleteContext(@PathVariable String contextId) {
        dashboardService.deleteContext(contextId);
    }


    @RequestMapping(value = "/notifications", method = GET)
    public Map<String, Integer> getAppNotificationCounts() {
        return portalNotificationService.getAppNotificationCounts();
    }

}

package org.oasis_eu.portal.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.model.dashboard.DashboardApp;
import org.oasis_eu.portal.model.dashboard.DashboardPendingApp;
import org.oasis_eu.portal.model.dashboard.UserContext;
import org.oasis_eu.portal.services.DashboardService;
import org.oasis_eu.portal.services.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/my/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    private final NotificationService notificationService;

    public DashboardController(DashboardService dashboardService, NotificationService notificationService) {
        this.dashboardService = dashboardService;
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/dashboards")
    public List<UserContext> getContexts() {
        return dashboardService.getUserContexts();
    }

    @GetMapping(value = "/apps")
    private List<DashboardApp> getAppsForMainContext() {
        return dashboardService.getMainDashboardApps();
    }

    @GetMapping(value = "/apps/{contextId}")
    public List<DashboardApp> getAppsForContext(@PathVariable String contextId) {
        return dashboardService.getDashboardApps(contextId);
    }

    @PutMapping(value = "/apps/{contextId}")
    public void updateApps(@PathVariable String contextId, @RequestBody List<DashboardApp> apps) {
        dashboardService.setAppsInContext(contextId, apps);
    }

    @PostMapping(value = "/apps/move/{appId}/to/{contextId}")
    public void moveAppTo(@PathVariable String appId, @PathVariable String contextId) {
        dashboardService.moveAppTo(appId, contextId);
    }

    @DeleteMapping(value = "/apps/remove/{appId}")
    public void removeApp(@PathVariable String appId) {
        dashboardService.unsubscribeApp(appId);
    }

    @DeleteMapping(value = "/pending-apps/{appId}")
    public void removePendingApp(@PathVariable String appId) {
        dashboardService.removePendingApp(appId);
    }

    @GetMapping(value = "/pending-apps")
    public List<DashboardPendingApp> pendingApps() {
        return dashboardService.getPendingApps();
    }


    @PostMapping(value = "/dashboards")
    public UserContext createDashboard(@RequestBody CreateDashRequest request) {
        return dashboardService.createContext(request.name);
    }

    public static class CreateDashRequest {
        @JsonProperty
        String name;
    }

    @PutMapping(value = "/dashboard/{contextId}")
    public void renameContext(@PathVariable String contextId, @RequestBody UserContext userContext) {
        dashboardService.renameContext(contextId, userContext.getName());
    }

    @DeleteMapping(value = "/dashboard/{contextId}")
    public void deleteContext(@PathVariable String contextId) {
        dashboardService.deleteContext(contextId);
    }


    @GetMapping(value = "/notifications")
    public Map<String, Integer> getNotificationsCountByService() {
        return notificationService.getNotificationsCountByService();
    }
}

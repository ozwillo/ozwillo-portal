package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.ServiceEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.dao.my.HiddenPendingAppsRepository;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.mongo.model.my.Dashboard;
import org.oasis_eu.portal.core.mongo.model.my.HiddenPendingApps;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.core.mongo.model.my.UserSubscription;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.ui.DashboardApp;
import org.oasis_eu.portal.ui.DashboardPendingApp;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private HiddenPendingAppsRepository hiddenPendingAppsRepository;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private ApplicationInstanceStore applicationInstanceStore;

    @Autowired
    private UserInfoService userInfoHelper;

    @Autowired
    private ImageService imageService;

    UserContext getPrimaryUserContext() {
        return getUserContexts().stream().filter(UserContext::isPrimary).findFirst().get();
    }

    public List<UserContext> getUserContexts() {
        return getDash().getContexts();
    }


    private List<Subscription> orphanSubscriptions(List<Subscription> allSubscriptions) {
        List<UserContext> ctxs = getDash().getContexts();

        Set<String> configured = ctxs
            .stream()
            .flatMap(c -> c.getSubscriptions().stream())
            .map(UserSubscription::getId)
            .collect(Collectors.toSet());

        return allSubscriptions
            .stream()
            .filter(s -> s.getId() != null)
            .filter(s -> !configured.contains(s.getId()))
            .collect(Collectors.toList());
    }

    public List<DashboardApp> getMainDashboardApps() {
        return getDashboardApps(getPrimaryUserContext().getId());
    }

    public List<DashboardApp> getDashboardApps(String userContextId) {
        Dashboard dash = getDash();

        UserContext userContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().orElse(null);
        if (userContext == null) {
            logger.warn("Cannot get apps for non-existing dashboard {}, user={}", userContextId, userInfoHelper.currentUser().getUserId());
            return Collections.emptyList();
        }

        List<Subscription> actualSubscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId());

        Map<String, Subscription> subscriptionById = actualSubscriptions.stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        // apps in mongodb and kernel
        List<DashboardApp> apps = userContext.getSubscriptions()
            .stream()
            .filter(us -> subscriptionById.containsKey(us.getId()))
            .map(us -> this.toDashboardApp(subscriptionById.get(us.getId())))
            .filter(app -> app != null)
            .collect(Collectors.toList());

        apps.addAll(orphanSubscriptions(actualSubscriptions)
            .stream()
            .map(this::toDashboardApp)
            .filter(app -> app != null)
            .collect(Collectors.toList()));

        // update the dashboard
        userContext.setSubscriptions(apps.stream().map(this::toUserSubscription).collect(Collectors.toList()));
        dashboardRepository.save(dash);

        return apps;
    }

    private DashboardApp toDashboardApp(Subscription sub) {
        try {
            ServiceEntry service = catalogStore.findService(sub.getServiceId());
            if (service == null) {
                return null;
            }

            DashboardApp app = new DashboardApp();
            app.setId(sub.getId());
            app.setServiceId(sub.getServiceId());
            app.setName(service.getName(RequestContextUtils.getLocale(request)));
            app.setIcon(imageService.getImageForURL(service.getIcon(RequestContextUtils.getLocale(request)), ImageFormat.PNG_64BY64, false));
            app.setUrl(service.getUrl());

            if (service.getNotificationUrl() != null) {
                app.setNotificationUrl(service.getNotificationUrl());
            }

            return app;

        } catch (WrongQueryException e) {
            logger.debug(e.getMessage());
            return null;
        }
    }

    private UserSubscription toUserSubscription(DashboardApp app) {
        UserSubscription result = new UserSubscription();
        result.setId(app.getId());
        result.setServiceId(app.getServiceId());
        return result;
    }

    public void setAppsInContext(String contextId, List<DashboardApp> apps) {
        logger.debug("Updating apps in context {} with list {}", contextId, apps);

        Dashboard dash = getDash();

        UserContext userContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst().orElse(null);
        if (userContext != null) {
            userContext.setSubscriptions(apps.stream().map(this::toUserSubscription).collect(Collectors.toList()));
            dashboardRepository.save(dash);
        } else {
            logger.error("Usercontext {} does not exist for userid {}", contextId, userInfoHelper.currentUser().getUserId());
        }
    }


    public UserContext createContext(String name) {
        Dashboard dash = getDash();
        List<UserContext> userContexts = dash.getContexts();
        Optional<UserContext> existingContext = userContexts.stream().filter(ctx -> ctx.getName().equals(name)).findAny();
        if (!existingContext.isPresent()) {
            UserContext ctx = new UserContext().setId(UUID.randomUUID().toString()).setName(name);
            dash.getContexts().add(ctx);
            dashboardRepository.save(dash);
            return ctx;
        } else {
            return existingContext.get();
        }
    }

    public UserContext renameContext(String id, String name) {
        Dashboard dash = getDash();
        List<UserContext> userContexts = dash.getContexts();
        UserContext userContext = userContexts.stream().filter(ctx -> ctx.getId().equals(id)).findFirst().get();
        boolean nameTaken = userContexts.stream().filter(ctx -> ctx.getName().equals(name) && !ctx.getId().equals(id)).findAny().isPresent();
        if (!nameTaken) {
            userContext.setName(name);
            dashboardRepository.save(dash);
        }

        return userContext;
    }

    public UserContext deleteContext(String id) {
        Dashboard dash = getDash();

        List<UserContext> userContexts = dash.getContexts();
        UserContext userContextToDelete = userContexts.stream().filter(ctx -> ctx.getId().equals(id)).findFirst().get();
        if (!userContextToDelete.isPrimary()) { // Prevent from deleting the primary context
            List<UserContext> remainingContexts = userContexts.stream().filter(ctx -> !ctx.getId().equals(id)).collect(Collectors.toList());
            dash.setContexts(remainingContexts);
            dashboardRepository.save(dash);
            return remainingContexts.get(0);
        } else {
            return userContextToDelete;
        }

    }

    public void moveAppTo(String subjectId, String targetContextId) {
        Dashboard dash = getDash();

        logger.debug("Moving app {} to {}", subjectId, targetContextId);

        UserContext targetContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(targetContextId)).findFirst().get();
        UserContext sourceContext = dash.getContexts().stream()
            .filter(context -> context.getSubscriptions().stream().anyMatch(sub -> sub.getId().equals(subjectId)))
            .findFirst().orElse(null);

        if (sourceContext != null) {

            UserSubscription userSubscription = sourceContext.getSubscriptions().stream().filter(s -> s.getId().equals(subjectId)).findFirst().get();

            sourceContext.setSubscriptions(sourceContext.getSubscriptions().stream().filter(s -> !s.getId().equals(subjectId)).collect(Collectors.toList()));
            targetContext.getSubscriptions().add(userSubscription);

            dashboardRepository.save(dash);
        }

    }

    public void unsubscribeApp(String subscriptionId) {
        Dashboard dash = getDash();

        UserContext userContext = dash.getContexts().stream().filter(context -> context.getSubscriptions().stream().anyMatch(sub -> sub.getId().equals(subscriptionId))).findFirst().orElse(null);
        if (userContext == null) {
            logger.error("Subscription {} does not exist for user {}", subscriptionId, userInfoHelper.currentUser().getUserId());
        } else {
            // try to delete...
            subscriptionStore.unsubscribe(subscriptionId);

            // if that works, then save the dash
            userContext.setSubscriptions(userContext.getSubscriptions().stream()
                .filter(sub -> !sub.getId().equals(subscriptionId))
                .collect(Collectors.toList())
            );

            dashboardRepository.save(dash);
        }
    }


    public Dashboard getDash() {
        UserInfo user = userInfoHelper.currentUser();
        Dashboard dashboard = dashboardRepository.findOne(user.getUserId());
        if (dashboard != null) {

            // at one point we didn't store the service id in the subscription object. Now that we do, maybe some migration is needed
            boolean needsMigrate = dashboard.getContexts().stream().flatMap(uc -> uc.getSubscriptions().stream()).anyMatch(us -> us.getServiceId() == null);
            if (needsMigrate) {

                Map<String, String> serviceBySubId = subscriptionStore.findByUserId(user.getUserId()).stream().collect(Collectors.toMap(sub -> sub.getId(), sub -> sub.getServiceId()));
                for (UserContext cx : dashboard.getContexts()) {
                    cx.getSubscriptions().forEach(us -> us.setServiceId(serviceBySubId.get(us.getId())));
                }
                dashboardRepository.save(dashboard);
            }

            return dashboard;
        } else {
            logger.info("Creating dashboard for user {} ({}) using locale: {}", user.getName(), user.getUserId(), RequestContextUtils.getLocale(request));

            dashboard = new Dashboard();
            dashboard.setUserId(user.getUserId());
            dashboard.getContexts().add(new UserContext().setId(UUID.randomUUID().toString()).setName(messageSource.getMessage("my.default-dashboard-name", new Object[]{}, RequestContextUtils.getLocale(request))).setPrimary(true));
            return dashboardRepository.save(dashboard);
        }
    }

    public List<DashboardPendingApp> getPendingApps() {

        HiddenPendingApps hidden = hiddenPendingAppsRepository.findOne(userInfoHelper.currentUser().getUserId());
        List<ApplicationInstance> pendingInstances = applicationInstanceStore.findPendingInstances(userInfoHelper.currentUser().getUserId());

        List<DashboardPendingApp> DashboardPendingAppLst = pendingInstances.stream()
            // filter on "deleted" a.k.a portal-side hidden app : (#156 Possibility to delete "pending app instances" icons)
            .filter(instance -> (hidden == null || !hidden.getHiddenApps().contains(instance.getInstanceId())))
            .map(this::toPendingApp)
            .filter(app -> app != null)
            .collect(Collectors.toList());

        return DashboardPendingAppLst;
    }

    public void removePendingApp(String appId) {
        HiddenPendingApps hidden = hiddenPendingAppsRepository.findOne(userInfoHelper.currentUser().getUserId());
        if (hidden == null) {
            hidden = new HiddenPendingApps();
            hidden.setUserId(userInfoHelper.currentUser().getUserId());
        }
        hidden.hideApp(appId);
        hiddenPendingAppsRepository.save(hidden);
    }

    private DashboardPendingApp toPendingApp(ApplicationInstance instance) {
        try {

            CatalogEntry appCatalog = catalogStore.findApplication(instance.getApplicationId());

            String imageUrl = "";
            if (appCatalog != null) {
                // No icons are set in the appInstance, so we have to take it directly from the app
                // TODO remove once kernel #121 is done
                imageUrl = appCatalog.getIcon(RequestContextUtils.getLocale(request));
            } // #220 The app could be deleted or stopped

            DashboardPendingApp dashPendingApp = new DashboardPendingApp();
            dashPendingApp.setId(instance.getInstanceId());
            dashPendingApp.setIcon(imageService.getImageForURL(imageUrl, ImageFormat.PNG_64BY64, false));
            //dashPendingApp.setIcon(imageService.getImageForURL(instance.getIcon(
            //		RequestContextUtils.getLocale(request)), ImageFormat.PNG_64BY64, false)); // TODO once kernel #121 is done
            dashPendingApp.setName(instance.getName(RequestContextUtils.getLocale(request)));


            return dashPendingApp;
        } catch(HttpClientErrorException e) {
            logger.debug(e.getMessage());
            return null;
        }
    }
}

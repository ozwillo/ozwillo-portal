package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.dao.LocalServiceStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.oasis_eu.portal.core.model.subscription.ApplicationType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.model.my.Dashboard;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.model.DashboardEntry;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS) // create a new instance for each request!
public class PortalDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(PortalDashboardService.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private ApplicationStore applicationStore;

    @Autowired
    private LocalServiceStore localServiceStore;

    @Autowired
    private UserInfoHelper userInfoHelper;

    @Autowired
    private PortalNotificationService notificationService;

    public UserContext getPrimaryUserContext() {
        return getUserContexts().stream().filter(UserContext::isPrimary).findFirst().get();
    }

    public List<UserContext> getUserContexts() {
        return getDash().getContexts();
    }


    private List<Subscription> orphanSubscriptions(List<Subscription> allSubscriptions) {
        List<UserContext> ctxs = getDash().getContexts();

        // note: this is supposed to be correct, not fast. We should rewrite it faster at some point!
        return allSubscriptions
                .stream()
                .filter(s -> ctxs.stream().flatMap(c -> c.getSubscriptions().stream()).noneMatch(sid -> sid.equals(s.getId())))
                .collect(Collectors.toList());
    }

    public List<String> getApplicationIds(String userContextId) {
//        if (logger.isDebugEnabled()) {
//            logger.debug("-> get application ids for context: " + userContextId);
//
//        }
        Map<String, Subscription> subs = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId()).stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        return getDash().getContexts()
                .stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().get().getSubscriptions()
                .stream().map(subs::get).filter(s -> s != null).map(Subscription::getApplicationId).collect(Collectors.toList());

    }

    public List<DashboardEntry> getDashboardEntries(String userContextId) {
        Locale displayLocale = RequestContextUtils.getLocale(request);
        Dashboard dash = getDash();

        UserContext userContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().get();
        List<Subscription> actualSubscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId());

        Map<String, Subscription> subscriptionById = actualSubscriptions.stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        List<DashboardEntry> entries = userContext.getSubscriptions().stream()
                .filter(subscriptionById::containsKey)
                .map(sid -> getDashboardEntry(subscriptionById.get(sid)))
                .collect(Collectors.toList());


        if (userContext.isPrimary()) {
            entries.addAll(orphanSubscriptions(actualSubscriptions).stream()
                    .map(s -> getDashboardEntry(s))
                    .collect(Collectors.toList()));
        }

        userContext.setSubscriptions(entries.stream().map(e -> e.getSubscription().getId()).collect(Collectors.toList()));

        // Right now we save all the time -- just in case we have made any modifications -- but this should be optimized someday
        dashboardRepository.save(dash);

        return entries;
    }

    private Dashboard getDash() {
        UserInfo user = userInfoHelper.currentUser();
        Dashboard dashboard = dashboardRepository.findOne(user.getUserId());
        if (dashboard != null) {
            return dashboard;
        } else {
            logger.info("Creating dashboard for user {} ({}) using locale: {}", user.getName(), user.getUserId(), RequestContextUtils.getLocale(request));

            dashboard = new Dashboard();
            dashboard.setUserId(user.getUserId());
            dashboard.getContexts().add(new UserContext().setId(UUID.randomUUID().toString()).setName(messageSource.getMessage("my.default-dashboard-name", new Object[]{}, RequestContextUtils.getLocale(request))).setPrimary(true));
            return dashboardRepository.save(dashboard);
        }
    }

    private DashboardEntry getDashboardEntry(Subscription s) {
        DashboardEntry entry = new DashboardEntry();
        entry.setDisplayLocale(RequestContextUtils.getLocale(request));
        entry.setSubscription(s);
        if (s.getApplicationType().equals(ApplicationType.APPLICATION)) {
            Application application = applicationStore.find(s.getApplicationId());
            if (application == null) {
                logger.warn("Application {} not found in app store", s.getApplicationId());
            }
            entry.setApplication(application);
            entry.setNotificationsCount((int) notificationService.countAppNotifications(s.getApplicationId()));
        } else {

            LocalService localService = localServiceStore.find(s.getApplicationId());
            if (localService == null) {
                logger.warn("Local service {} not found in app store", s.getApplicationId());
            }
            entry.setLocalService(localService);
        }
        return entry;
    }


}

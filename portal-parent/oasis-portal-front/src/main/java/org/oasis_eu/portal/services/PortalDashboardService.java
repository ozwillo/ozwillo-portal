package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.mongo.model.my.Dashboard;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.core.mongo.model.my.UserSubscription;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.dashboard.AppNotificationData;
import org.oasis_eu.portal.model.dashboard.DashboardApp;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Service
//@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS) // create a new instance for each request!
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
    private CatalogStore catalogStore;

    @Autowired
    private UserInfoService userInfoHelper;

    @Autowired
    private PortalNotificationService notificationService;

    @Autowired
    private ImageService imageService;

    public UserContext getPrimaryUserContext() {
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
                .map(us -> us.getId())
                .collect(Collectors.toSet());

        return allSubscriptions
                .stream()
                .filter(s -> s.getId() != null)
                .filter(s -> ! configured.contains(s.getId()))
                .collect(Collectors.toList());

    }

    public List<String> getServicesIds(String userContextId) {

        UserContext context = getDash().getContexts().stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().orElse(null);
        if (context == null) {
            return Collections.emptyList();
        } else {
            return context.getSubscriptions().stream().map(us -> us.getServiceId()).collect(Collectors.toList());
        }

    }

    public List<DashboardApp> getMainDashboardApps() {
        return getDashboardApps(getPrimaryUserContext().getId());
    }

    public List<DashboardApp> getDashboardApps(String userContextId) {
        Dashboard dash = getDash();

        UserContext userContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().orElse(null);
        if (userContext == null) {
            logger.info("Cannot get apps for non-existant dashboard {}, user={}", userContextId, userInfoHelper.currentUser().getUserId());
            return Collections.emptyList();
        }

        List<Subscription> actualSubscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId());

        Map<String, Subscription> subscriptionById = actualSubscriptions.stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        List<DashboardApp> apps = userContext.getSubscriptions()
                .stream()
                .filter(us -> subscriptionById.containsKey(us.getId()))
                .map(us -> toDashboardApp(subscriptionById.get(us.getId()), null))
                .filter(app -> app != null)
                .collect(Collectors.toList());

        apps.addAll(orphanSubscriptions(actualSubscriptions)
                        .stream()
                        .map(sub -> toDashboardApp(sub, null))
                        .filter(app -> app != null)
                        .collect(Collectors.toList())
        );

        // update the dashboard
        userContext.setSubscriptions(apps.stream().map(this::toUserSubscription).collect(Collectors.toList()));
        dashboardRepository.save(dash);

        return apps;
    }

    private DashboardApp toDashboardApp(Subscription sub, List<AppNotificationData> appNotificationCounts) {
        try {
            CatalogEntry service = catalogStore.findService(sub.getServiceId());
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
        } catch (HttpServerErrorException kernelException) {
            logger.error("Cannot load service from the Kernel for subscription {}, skipping.", sub);
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
		}
		else {
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
    	}
    	else {
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

            // at one point we didn't store the service id in the subscription object. Now that we do, maybe some
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
}

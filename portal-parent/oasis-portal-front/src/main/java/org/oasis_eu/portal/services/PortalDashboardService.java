package org.oasis_eu.portal.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.mongo.model.my.Dashboard;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.dashboard.DashboardEntry;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

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

//    @Autowired
//    private ApplicationStore applicationStore;
//
//    @Autowired
//    private LocalServiceStore localServiceStore;

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

        // note: this is supposed to be correct, not fast. We should rewrite it faster at some point!
        return allSubscriptions
                .stream()
                .filter(s -> ctxs.stream().flatMap(c -> c.getSubscriptions().stream()).noneMatch(sid -> sid.equals(s.getId())))
                .collect(Collectors.toList());
    }

    public List<String> getServicesIds(String userContextId) {
//        if (logger.isDebugEnabled()) {
//            logger.debug("-> get application ids for context: " + userContextId);
//
//        }
        Map<String, Subscription> subs = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId()).stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        Optional<UserContext> optUserContext = getDash().getContexts()
                .stream().filter(uc -> uc.getId().equals(userContextId)).findFirst();
        if (optUserContext.isPresent()) {
			return optUserContext.get().getSubscriptions()
	                .stream().map(subs::get).filter(s -> s != null).map(Subscription::getServiceId).collect(Collectors.toList());
        }
        else {
        	return Collections.emptyList();
        }

    }

    public List<DashboardEntry> getDashboardEntries(String userContextId) {
        Dashboard dash = getDash();

        UserContext userContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().get();
        List<Subscription> actualSubscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId());

        Map<String, Subscription> subscriptionById = actualSubscriptions.stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        List<DashboardEntry> entries = userContext.getSubscriptions().stream()
                .filter(subscriptionById::containsKey)
                .map(sid -> getDashboardEntry(subscriptionById.get(sid)))
                .filter(e -> e != null)
                .collect(Collectors.toList());


        if (userContext.isPrimary()) {
            entries.addAll(orphanSubscriptions(actualSubscriptions).stream()
                    .map(this::getDashboardEntry)
                    .filter(e -> e != null)
                    .collect(Collectors.toList()));
        }

        userContext.setSubscriptions(entries.stream().map(e -> e.getSubscription().getId()).collect(Collectors.toList()));

        // Right now we save all the time -- just in case we have made any modifications -- but this should be optimized someday
        dashboardRepository.save(dash);

        return entries;
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
    
    public void moveBefore(String userContextId, String subjectId, String objectId) {
        move(userContextId, subjectId, objectId, (p1, p2) -> Stream.of(p2, p1));
    }

    public void moveAfter(String userContextId, String subjectId, String objectId) {
        move(userContextId, subjectId, objectId, (p1, p2) -> Stream.of(p1, p2));
    }

    public void moveAppToContext(String sourceContextId, String subjectId, String targetContextId) {
        Dashboard dash = getDash();

        UserContext sourceContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(sourceContextId)).findFirst().orElse(null);
        UserContext targetContext = dash.getContexts().stream().filter(uc -> uc.getId().equals(targetContextId)).findFirst().orElse(null);
        List<Subscription> actualSubscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId());
        Map<String, Subscription> subscriptionById = actualSubscriptions.stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

        String subscriptionId = sourceContext.getSubscriptions().stream().filter(s -> subscriptionById.get(s).getServiceId().equals(subjectId)).findFirst().orElse(null);

        if (subscriptionById != null) {
            sourceContext.setSubscriptions(sourceContext.getSubscriptions().stream().filter(s -> !s.equals(subscriptionId)).collect(Collectors.toList()));
            targetContext.getSubscriptions().add(subscriptionId);
        }

        dashboardRepository.save(dash);
    }

    private static interface Orderer {
        Stream<Pair<String, String>> apply(Pair<String, String> a, Pair<String, String> b);
    }

    private void move(String userContextId, String subjectId, String objectId, Orderer orderer) {
        Dashboard dash = getDash();
        UserContext context = dash.getContexts().stream().filter(uc -> uc.getId().equals(userContextId)).findFirst().orElse(null);
        if (context != null) {
            // get rid of app- prefix
            String subjectId_ = subjectId.substring("app-".length());
            String objectId_ = objectId.substring("app-".length());

            List<Subscription> actualSubscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId());
            Map<String, Subscription> subscriptionById = actualSubscriptions.stream().collect(Collectors.toMap(GenericEntity::getId, s -> s));

            List<Pair<String, String>> subsAppPairs = context.getSubscriptions().stream().map(sid -> new Pair<>(sid, subscriptionById.get(sid).getServiceId())).collect(Collectors.toList());

            Pair<String, String> object = subsAppPairs.stream().filter(p -> p.getB().equals(subjectId_)).findFirst().get();

            context.setSubscriptions(subsAppPairs.stream().filter(p -> !p.getB().equals(subjectId_)).flatMap(p -> p.getB().equals(objectId_) ? orderer.apply(p, object) : Stream.of(p)).map(p -> p.getA()).collect(Collectors.toList()));

            dashboardRepository.save(dash);
        }

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
        CatalogEntry catalogEntry = catalogStore.findService(s.getServiceId());
        if (catalogEntry == null) {
            logger.warn("User {} - cannot find service for id {}", userInfoHelper.currentUser().getUserId(), s.getServiceId());

            return null;
        }
        entry.setCatalogEntry(catalogEntry);
        entry.setNotificationsCount((int) notificationService.countAppNotifications(s.getServiceId()));
        entry.setIconUrl(imageService.getImageForURL(catalogEntry.getIcon(RequestContextUtils.getLocale(request)), ImageFormat.PNG_64BY64));

        return entry;
    }

    private static class Pair<A, B> {
        A a;
        B b;

        private Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }
    }
}

package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.dao.LocalServiceStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.dao.UserContextStore;
import org.oasis_eu.portal.core.model.subscription.ApplicationType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.UserContext;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardOrderingRepository;
import org.oasis_eu.portal.core.mongo.model.my.DashboardOrdering;
import org.oasis_eu.portal.model.DashboardEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Service
public class DashboardService {

    @Autowired
    private DashboardOrderingRepository orderingRepository;

    @Autowired
    private UserContextStore userContextStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private ApplicationStore applicationStore;

    @Autowired
    private LocalServiceStore localServiceStore;


    @Autowired
    private UserInfoHelper userInfoHelper;


    public UserContext getPrimaryUserContext() {

        return userContextStore.getUserContexts(userInfoHelper.currentUser().getUserId()).stream().filter(c -> c.isPrimary()).findFirst().get();
    }

    public List<UserContext> getUserContexts() {
        return userContextStore.getUserContexts(userInfoHelper.currentUser().getUserId());
    }

    private DashboardOrdering getOrCreateOrdering(UserContext userContext) {
        DashboardOrdering ords = orderingRepository.findByUserIdAndUserContextId(userInfoHelper.currentUser().getUserId(), userContext.getId());
        if (ords == null) {
            ords = new DashboardOrdering();
            ords.setUserContextId(userContext.getId());
            ords.setUserId(userInfoHelper.currentUser().getUserId());
            ords = orderingRepository.save(ords);
        }
        return ords;
    }

    public List<DashboardEntry> getDashboardEntries(UserContext userContext) {
        List<Subscription> subscriptions = subscriptionStore.findByUserIdAndUserContextId(userInfoHelper.currentUser().getUserId(), userContext.getId());

        DashboardOrdering ords = getOrCreateOrdering(userContext);

        List<DashboardEntry> entries = new ArrayList<>(subscriptions.size());

        for (Subscription s : subscriptions) {
            DashboardEntry entry = new DashboardEntry();
            entry.setSubscription(s);
            if (s.getApplicationType().equals(ApplicationType.APPLICATION)) {
                entry.setApplication(applicationStore.find(s.getApplicationId()));
            } else {
                entry.setLocalService(localServiceStore.find(s.getApplicationId()));
            }
            entries.add(entry);
        }

        entries.sort((e1, e2) -> {
            Integer o1 = ords.getOrderings().get(e1.getSubscription().getId());
            Integer o2 = ords.getOrderings().get(e2.getSubscription().getId());

            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else return o1 - o2;

        });


        // TODO detect if a reordering sanitization is required (ie a subscription doesn't have an ordering, a sub has _more_ than one ordering, two subs have the same ordering, some orderings don't have any associated subscriptions…) and persist
        // TODO now we'll just reorder every time
        ords.getOrderings().clear();
        for (int i = 0; i < entries.size(); i++) {
            ords.getOrderings().put(entries.get(i).getSubscription().getId(), i);
        }
        orderingRepository.save(ords);


        return entries;
    }


}

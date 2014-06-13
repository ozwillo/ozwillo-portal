package org.oasis_eu.portal.mockserver.repo;

import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.ApplicationType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.model.subscription.UserContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;

/**
 * User: schambon
 * Date: 6/12/14
 */

@Service
public class TestData {

    Map<String, Application> applications = new HashMap<>();
    Map<String, LocalService> localServices = new HashMap<>();


    Map<String, List<UserContext>> userContexts = new HashMap<>();
    Map<String, Map<String, List<Subscription>>> subscriptions = new HashMap<>();


    public Map<String, Application> getApplications() {
        return applications;
    }

    public Map<String, LocalService> getLocalServices() {
        return localServices;
    }


    public List<UserContext> getUserContexts(String userId) {
        List<UserContext> ctxs = userContexts.get(userId);
        if (ctxs == null) {

            ctxs = new LinkedList<>();
            // auto-create default user context
            UserContext primaryUserContext = new UserContext();
            primaryUserContext.setId(UUID.randomUUID().toString());
            primaryUserContext.setName("Primary");
            primaryUserContext.setPrimary(true);
            ctxs.add(primaryUserContext);

            userContexts.put(userId, ctxs);
        }
        return ctxs;
    }

    public UserContext getPrimaryUserContext(String userId) {

        return getUserContexts(userId).stream().filter(c -> c.isPrimary()).findAny().get();
    }

    public UserContext createUserContext(String userId, String name) {
        UserContext context = new UserContext();
        context.setPrimary(false);
        context.setName(name);
        context.setId(UUID.randomUUID().toString());

        getUserContexts(userId).add(context);
        return context;
    }

    public Map<String, List<Subscription>> getSubscriptionsByContext(String userId) {
        return subscriptions.get(userId);
    }

    public List<Subscription> getSubscriptions(String userId, String contextId) {
        Map<String, List<Subscription>> byCtx = subscriptions.get(userId);

        return byCtx != null ? byCtx.get(contextId) : null;
    }

    public boolean subscribe(Subscription subscription) {
        Map<String, List<Subscription>> byCtx = subscriptions.get(subscription.getUserId());
        if (byCtx == null) {
            byCtx = new HashMap<>();
            for (UserContext ctx : getUserContexts(subscription.getUserId())) {
                byCtx.put(ctx.getId(), new ArrayList<>());
            }
            subscriptions.put(subscription.getUserId(), byCtx);
        }

        List<Subscription> subs = byCtx.get(subscription.getUserContextId());
        if (subs == null) {

            subs = new ArrayList<>();
            byCtx.put(subscription.getUserContextId(), subs);
        }

        if (subs.stream().anyMatch(s -> s.getApplicationId().equals(subscription.getApplicationId()) && s.getSubscriptionType().equals(subscription.getSubscriptionType()))) {
            // there is already a subscription for that user / context / application / substype triple
            return false;
        } else {
            subs.add(subscription);
            return true;
        }
    }

    public boolean subscribeApplication(String userId, String applicationId, SubscriptionType subscriptionType) {
        Subscription s = new Subscription();
        s.setId(UUID.randomUUID().toString());
        s.setUserId(userId);
        s.setApplicationId(applicationId);
        s.setApplicationType(ApplicationType.APPLICATION);
        s.setCreated(Instant.now());
        s.setUserContextId(getPrimaryUserContext(userId).getId());
        s.setSubscriptionType(subscriptionType);

        return subscribe(s);

    }

    public boolean subscribeLocalService(String userId, String applicationId) {
        Subscription s = new Subscription();
        s.setId(UUID.randomUUID().toString());
        s.setUserId(userId);
        s.setApplicationId(applicationId);
        s.setApplicationType(ApplicationType.LOCAL_SERVICE);
        s.setCreated(Instant.now());
        s.setUserContextId(getPrimaryUserContext(userId).getId());
        s.setSubscriptionType(SubscriptionType.PERSONAL);

        return subscribe(s);

    }

    @PostConstruct
    public void initializeTestData() throws Exception {

        Application citizenkin = new Application();
        citizenkin.setId("citizenkin_back");
        citizenkin.setDefaultLocale(Locale.FRENCH);
        citizenkin.setDefaultDescription("Portail de relations citoyennes");
        Set<String> nocategories = Collections.emptySet();
        citizenkin.setCategoryIds(nocategories);
        citizenkin.setIcon(new URL("http://localhost:8081/app_icon/citizenkin.png"));
        citizenkin.setDefaultName("Citizen Kin");
        citizenkin.setPaymentOption(PaymentOption.PAID);
        citizenkin.setUrl(new URL("http://srv3-polenum.fingerprint-technologies.net/back"));

        citizenkin.setTargetAudience(new HashMap<Audience, Boolean>() {{
            put(Audience.CITIZENS, false);
            put(Audience.COMPANIES, false);
            put(Audience.PUBLIC_BODIES, true);
        }});

        citizenkin.setTranslatedNames(new HashMap<String, String>() {{
            put("bg", "Ситизен Кин");
        }});

        citizenkin.setTranslatedDescriptions(new HashMap<String, String>() {{
            put("en", "Citizen Relationship Management");
            put("fr", "Portail de relations citoyennes");
            put("it", "Portale rapporto cittadino");
            put("ca", "Portal relació ciutadà");
            put("tr", "Vatandaş ilişki portalı");
            put("bg", "Портал гражданин отношения");
            put("es", "portal relación ciudadano");

        }});

        applications.put("citizenkin_back", citizenkin);


        Application ckValence = new Application();
        ckValence.setId("0a046fde-a20f-46eb-8252-48b78d89a9a2");
        ckValence.setDefaultName("Citizen Kin Valence");
        ckValence.setDefaultDescription("Portal de relations citoyennes de la ville de Valence");
        ckValence.setPaymentOption(PaymentOption.FREE);
        ckValence.setTargetAudience(new HashMap<Audience, Boolean>() {{
            put(Audience.COMPANIES, false);
            put(Audience.PUBLIC_BODIES, false);
            put(Audience.CITIZENS, true);
        }});
        ckValence.setDefaultLocale(Locale.FRENCH);
        ckValence.setIcon(new URL("http://whatever.com/test.png"));
        ckValence.setUrl(new URL("http://srv3-polenum.fingerprint-technologies.net/front/valence"));
        applications.put(ckValence.getId(), ckValence);

        LocalService elecRoll = new LocalService();
        elecRoll.setId("elecRollValence");
        elecRoll.setDefaultName("Inscription sur liste électorale, Valence");
        elecRoll.setDefaultDescription("S'inscrire sur les listes électorales");
        elecRoll.setApplicationId("ckValence");
        elecRoll.setUrl(new URL("http://srv3-polenum.fingerprint-technologies.net/front/valence/form/electoral_roll_registration/init"));
        elecRoll.setTerritoryId("26000");

        localServices.put("elecRollValence", elecRoll);

        LocalService other = new LocalService();
        other.setId("11107e06-d34e-4241-8d21-2bba7bf479b3");
        other.setDefaultName("Bibliothèque de Lyon");
        other.setTerritoryId("69000");
        other.setUrl(new URL("http://lyon.fr"));
        localServices.put("11107e06-d34e-4241-8d21-2bba7bf479b3", other);





        // some subscriptions...
        subscribeApplication("bb2c6f76-362f-46aa-982c-1fc60d54b8ef", ckValence.getId(), SubscriptionType.PERSONAL);
        subscribeLocalService("bb2c6f76-362f-46aa-982c-1fc60d54b8ef", "elecRollValence");
        subscribeApplication("a399684b-4ea3-49c3-800b-b8a0bf1131cb", "citizenkin_back", SubscriptionType.EMPLOYEE);
    }
}

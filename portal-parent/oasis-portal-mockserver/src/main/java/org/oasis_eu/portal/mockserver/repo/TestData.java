package org.oasis_eu.portal.mockserver.repo;

import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;
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

    Map<String, Set<String>> appSubscriptions = new HashMap<>();
    Map<String, Set<String>> localServiceSubscriptions = new HashMap<>();


    public Map<String, Application> getApplications() {
        return applications;
    }

    public Map<String, LocalService> getLocalServices() {
        return localServices;
    }

    public Set<String> getAppSubscriptions(String userId) {
        return appSubscriptions.get(userId);
    }

    public Set<String> getServiceSubscriptions(String userId) {
        return localServiceSubscriptions.get(userId);
    }

    public void subscribeApplication(String userId, String appId) {
        Set<String> appSubs = appSubscriptions.get(userId);
        if (appSubs == null) {
            appSubs = new HashSet<>();
            appSubscriptions.put(userId, appSubs);
        }
        appSubs.add(appId);
    }


    public void subscribeLocalService(String userId, String serviceId) {
        Set<String> serviceSubs = localServiceSubscriptions.get(userId);
        if (serviceSubs == null) {
            serviceSubs = new HashSet<>();
            localServiceSubscriptions.put(userId, serviceSubs);
        }
        serviceSubs.add(serviceId);
    }


    public void unsubscribeApplication(String userId, String appId) {
        Set<String> appSubs = appSubscriptions.get(userId);
        if (appSubs == null) {
            appSubs = new HashSet<>();
            appSubscriptions.put(userId, appSubs);
        }
        appSubs.remove(appId);
    }

    public void unsubscribeLocalService(String userId, String serviceId) {
        Set<String> serviceSubs = localServiceSubscriptions.get(userId);
        if (serviceSubs == null) {
            serviceSubs = new HashSet<>();
            localServiceSubscriptions.put(userId, serviceSubs);
        }
        serviceSubs.remove(serviceId);
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
        ckValence.setId("ckValence");
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
        applications.put("ckValence", ckValence);

        LocalService elecRoll = new LocalService();
        elecRoll.setId("elecRollValence");
        elecRoll.setDefaultName("Inscription sur liste électorale, Valence");
        elecRoll.setApplicationId("ckValence");
        elecRoll.setUrl(new URL("http://srv3-polenum.fingerprint-technologies.net/front/valence/electoral_roll_registration/init"));
        elecRoll.setTerritoryId("26000");

        localServices.put("elecRollValence", elecRoll);

        LocalService other = new LocalService();
        other.setId("11107e06-d34e-4241-8d21-2bba7bf479b3");
        other.setDefaultName("Bibliothèque de Lyon");
        other.setTerritoryId("69000");
        other.setUrl(new URL("http://lyon.fr"));
        localServices.put("11107e06-d34e-4241-8d21-2bba7bf479b3", other);


        // some subscriptions...
        subscribeApplication("bb2c6f76-362f-46aa-982c-1fc60d54b8ef", "ckValence");
        subscribeLocalService("bb2c6f76-362f-46aa-982c-1fc60d54b8ef", "elecRollValence");
        subscribeApplication("a399684b-4ea3-49c3-800b-b8a0bf1131cb", "citizekin");
    }
}

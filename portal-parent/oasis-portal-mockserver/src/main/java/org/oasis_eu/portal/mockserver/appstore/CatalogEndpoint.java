package org.oasis_eu.portal.mockserver.appstore;

import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.CatalogEntryType;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.mockserver.main.Catalog;
import org.oasis_eu.portal.mockserver.main.Subscriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: schambon
 * Date: 6/24/14
 */

@RestController
@RequestMapping("/catalog")
public class CatalogEndpoint {

    @Autowired
    private Catalog catalog;

    @Autowired
    private Subscriptions subscriptions;

    @RequestMapping(method = POST, value="/testdata")
    public void resetTestData() {

        catalog.deleteAll();
        subscriptions.deleteAll();

        CatalogEntry ckArchetype = new CatalogEntry();
        ckArchetype.setType(CatalogEntryType.ARCHETYPE);
        ckArchetype.setId("__citizenkin__");
        ckArchetype.setDefaultName("Citizen Kin");
        ckArchetype.setDefaultDescription("Citizen relationship management application");
        ckArchetype.setTargetAudience(Arrays.asList(Audience.PUBLIC_BODIES));
        ckArchetype.setPaymentOption(PaymentOption.PAID);
        ckArchetype.setVisible(true);

        catalog.save(ckArchetype);
    }


    @RequestMapping(method = GET, value = "/{id}")
    public CatalogEntry find(@PathVariable String id) {
        return catalog.findOne(id);
    }

    @RequestMapping(method = GET, value = "")
    public List<CatalogEntry> findAllVisible() {
        return catalog.findByVisible(true);
    }

    @RequestMapping(method = POST, value = "/buy/{id}/{userId}")
    public CatalogEntry subscribe(@PathVariable String userId, @PathVariable String id) {

        CatalogEntry base = catalog.findOne(id);
        if (base != null) {

            if (base.getType().equals(CatalogEntryType.SERVICE)) {
                // easy case: just add a subscription to this very service
                Subscription s = new Subscription();
                s.setSubscriptionType(SubscriptionType.PERSONAL);
                s.setId(UUID.randomUUID().toString());
                s.setCreated(Instant.now());
                s.setCatalogId(id);
                s.setUserId(userId);

                subscriptions.save(s);
                return base;

            } else {
                // hard case: create a new instance and afferent services
                // this is a mock, so no worries with hardcoded stuff in there
                if (base.getId().equals("__citizenkin__")) {

                    CatalogEntry backOffice = new CatalogEntry();
                    backOffice.setType(CatalogEntryType.SERVICE);
                    backOffice.setVisible(false);
                    backOffice.setDefaultName("Citizen forms");
                    backOffice.setDefaultDescription("forms for the citizens");
                    backOffice.setLocalizedNames(new HashMap<String, String>() {{
                        put("fr", "Formulaires citoyens");
                        put("bg", "Форми на гражданите");
                    }});
                    backOffice.setLocalizedDescriptions(new HashMap<String, String>() {{
                        put("en", "Citizen Relationship Management");
                        put("fr", "Portail de relations citoyennes");
                        put("it", "Portale rapporto cittadino");
                        put("ca", "Portal relació ciutadà");
                        put("tr", "Vatandaş ilişki portalı");
                        put("bg", "Портал гражданин отношения");
                        put("es", "portal relación ciudadano");
                    }});
                    backOffice.setDefaultLocale(Locale.ENGLISH);

                    backOffice.setId(UUID.randomUUID().toString());
                    backOffice.setParentId(base.getId());
                    backOffice.setUrl("http://31.172.165.220/back/valence");

                    catalog.save(backOffice);

                    CatalogEntry frontOffice = new CatalogEntry();
                    frontOffice.setType(CatalogEntryType.SERVICE);
                    frontOffice.setVisible(true);
                    frontOffice.setDefaultName("Valence Forms");
                    frontOffice.setDefaultDescription("Citizen forms for Valence");
                    frontOffice.setLocalizedNames(new HashMap<String, String>() {{
                        put("fr", "Formalités à Valence");
                        put("bg", "Форми на гражданите на град Валенсия");
                    }});
                    frontOffice.setLocalizedDescriptions(new HashMap<String, String>() {{
                        put("fr", "Portail administratif de la ville de Valence");
                        put("bg", "Форми на гражданите на град Валенсия");
                    }});
                    frontOffice.setDefaultLocale(Locale.ENGLISH);
                    frontOffice.setId(UUID.randomUUID().toString());
                    frontOffice.setParentId(base.getId());
                    frontOffice.setUrl("http://31.172.165.220/front/valence");
                    frontOffice.setTerritoryId("26000");

                    catalog.save(frontOffice);

                    CatalogEntry electoralRoll = new CatalogEntry();
                    electoralRoll.setType(CatalogEntryType.SERVICE);
                    electoralRoll.setVisible(true);
                    electoralRoll.setDefaultName("Register as a voter");
                    electoralRoll.setDefaultDescription("Preregister on the electoral rolls of the city of Valence");
                    electoralRoll.setLocalizedNames(new HashMap<String, String>() {{
                        put("fr", "Inscription sur liste électorale");
                    }});
                    electoralRoll.setLocalizedDescriptions(new HashMap<String, String>() {{
                        put("fr", "Faire une préinscription en ligne sur les listes électorales de la ville de Valence");
                    }});
                    electoralRoll.setDefaultLocale(Locale.ENGLISH);
                    electoralRoll.setId(UUID.randomUUID().toString());
                    electoralRoll.setParentId(base.getId());
                    electoralRoll.setUrl("http://31.172.165.220/front/valence/form/electoral_roll_registration");

                    catalog.save(electoralRoll);

                    Subscription s = new Subscription();
                    s.setSubscriptionType(SubscriptionType.MANAGER);
                    s.setCatalogId(backOffice.getId());
                    s.setUserId(userId);
                    s.setCreated(Instant.now());
                    s.setId(UUID.randomUUID().toString());
                    subscriptions.save(s);

                    return backOffice;

                } else if (base.getId().equals("__openelec__")) {
                    return base;
                } else {
                    throw new NotFoundException();
                }

            }

        } else {
            throw new NotFoundException();
        }

    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private void handleException() {}
}

class NotFoundException extends RuntimeException {

}
package org.oasis_eu.portal.core.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.constants.PortalConstants;
import org.oasis_eu.portal.core.model.*;
import org.oasis_eu.portal.core.services.test.InMemoryEntityStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * User: schambon
 * Date: 5/14/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore // Not implemented
public class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;


    @Before
    public void setUpTestData() {
        InMemoryEntityStore<Application> applicationStore = new InMemoryEntityStore();
        InMemoryEntityStore<GeoEntity> geoEntityStore = new InMemoryEntityStore<>();
        InMemoryEntityStore<AppstoreCategory> categoryStore = new InMemoryEntityStore<>();

        ReflectionTestUtils.setField(applicationService, "store", applicationStore);
        ReflectionTestUtils.setField(applicationService, "geoEntityStore", geoEntityStore);
        ReflectionTestUtils.setField(applicationService, "categoryStore", categoryStore);


        Application ushahidi = new Application();
        ushahidi.setDefaultName("Ushaidi");
        ushahidi.setDefaultDescription("Édition participative de données cartographiques");
        ushahidi.setDefaultLocale(OasisLocales.FRENCH);

        Application ckValence = new Application();
        ckValence.setDefaultName("Portail de procédures citoyennes de Valence");
        ckValence.setDefaultDescription("Dématérialisation de formulaires");
        ckValence.setDefaultLocale(OasisLocales.FRENCH);

        Application ckVienne = new Application();
        ckVienne.setDefaultLocale(OasisLocales.FRENCH);
        ckVienne.setDefaultName("Portail de procédures citoyennes de Vienne");
        ckVienne.setDefaultDescription("Dématérialisation de formulaires");

        Application ckAggloSudRhoneAlpes = new Application();
        ckAggloSudRhoneAlpes.setDefaultLocale(OasisLocales.FRENCH);
        ckAggloSudRhoneAlpes.setDefaultName("Portail de procédures citoyennes de la communauté d'agglomération Sud Rhône Alpes");
        ckAggloSudRhoneAlpes.setDefaultDescription("Dématérialisation de formulaires");

        Application pastell = new Application();
        pastell.setDefaultLocale(OasisLocales.FRENCH);
        pastell.setDefaultName("Pastell");
        pastell.setDefaultDescription("Gestionnaire de flux de documents");

        for (Application application : new Application[]{ushahidi, ckValence, ckVienne, ckAggloSudRhoneAlpes}) {
            applicationService.create(application);
        }

        GeoEntity drome = new GeoEntity();
        drome.addKeywords("drome", "26");
        drome = geoEntityStore.create(drome);

        GeoEntity aggloSudRhoneAlpes = new GeoEntity();
        aggloSudRhoneAlpes.addKeywords("agglo sud rhone alpes", "sud rha");
        aggloSudRhoneAlpes.addContainingEntities(drome);
        aggloSudRhoneAlpes = geoEntityStore.create(aggloSudRhoneAlpes);

        GeoEntity valence = new GeoEntity();
        valence.addKeywords("valence", "26000");
        valence.addContainingEntities(aggloSudRhoneAlpes);
        geoEntityStore.create(valence);


        GeoEntity vienne = new GeoEntity();
        vienne.addKeywords("vienne");
        vienne = geoEntityStore.create(vienne);

        GeoEntity lyon = new GeoEntity();
        lyon.addKeywords("lyon", "69000", "69001", "69002", "69003", "69004", "69005", "69006","69007","69008","69009");
        lyon = geoEntityStore.create(lyon);




//        applicationStore.addService("Inscription liste électorale Valence", "Portail de procédures citoyennes de Valence", valence, ckValence);
//        applicationStore.addService("Enlèvement objet encombrant Valence", "Portail de procédures citoyennes de la communauté d'agglomération Sud Rhône Alpes", aggloSudRhoneAlpes, ckAggloSudRhoneAlpes);
//        applicationStore.addService("Inscription liste électorale Vienne", "Portail de procédures citoyennes de Vienne", vienne, ckVienne);
//        applicationStore.addService("Inscription cantine scolaire Valence", "Portail de procédures citoyennes de Valence", valence, ckValence);
//        applicationStore.addService("Déclaration d'incident", "Déclaration d'incident de voirie", lyon, null); // services don't need to be provided by an app
//        applicationStore.addService("Rendez-vous MDPH", "Maison départementales des personnes handicapées", drome, null);
    }

    @Test
    public void categories() {
        // expected behaviour: get the list of categories
        // the API contains provisions for subcategories but we won't support them straightaway (UI-wise)

    }

    @Test
    public void browseRootCategory() {
        // expected behaviour: browsing retrieves application, but no services
    }

    @Test
    public void searchI18n() {
        // expected: search through name/description in the provided language with fallback to default language

    }

    @Test
    public void searchByNameCitizen() {
        /*
        Citizens search through applications and services by name and description
        Services are displayed first
         */

        SearchControls controls = new SearchControls(PortalConstants.PORTAL_DEFAULT_LOCALE);
        controls.setAudience(Audience.CITIZENS);

        List<AppStoreHit> hits = applicationService.search("procédures", controls);
        assertEquals(7, hits.size());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(0).getType());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(1).getType());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(2).getType());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(3).getType());
        assertEquals(AppStoreHit.HitType.APPLICATION, hits.get(4).getType());
        assertEquals(AppStoreHit.HitType.APPLICATION, hits.get(5).getType());
        assertEquals(AppStoreHit.HitType.APPLICATION, hits.get(6).getType());

    }

    @Test
    public void searchByLocationCitizen() {
        /*
        When a search query matches a location in the geo entity database, then that query is expanded
         */
        SearchControls controls = new SearchControls(PortalConstants.PORTAL_DEFAULT_LOCALE);
        controls.setAudience(Audience.CITIZENS);

        List<AppStoreHit> hits = applicationService.search("valence", controls);
        // gets all the services for the Valence area and "above" plus all the applications containing Valence in name / description
        assertEquals(5, hits.size());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(0).getType());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(1).getType());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(2).getType());
        assertEquals(AppStoreHit.HitType.LOCAL_SERVICE, hits.get(3).getType());
        assertEquals(AppStoreHit.HitType.APPLICATION, hits.get(4).getType());

        assertEquals("Rendez-vous MDPH", hits.get(3).getLocalService().getName(PortalConstants.PORTAL_DEFAULT_LOCALE));

    }

    @Test
    public void searchByTextAndLocationCitizen() {
        /*
        When part of a search query matches a location in the geo entity database, then that part of the query is expanded
         */
        SearchControls controls = new SearchControls(PortalConstants.PORTAL_DEFAULT_LOCALE);
        controls.setAudience(Audience.CITIZENS);

        List<AppStoreHit> hits = applicationService.search("liste électorale 26000", controls);
        // gets all the services for valence and above that match "liste électorale", plus all applications matching "liste électorale 26000" plainly
        assertEquals(1, hits.size());
    }


}

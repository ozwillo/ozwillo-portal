package org.oasis_eu.portal.core.services;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.config.PortalCoreConfiguration;
import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.constants.PortalConstants;
import org.oasis_eu.portal.core.exception.EntityNotFoundException;
import org.oasis_eu.portal.core.exception.InvalidEntityException;
import org.oasis_eu.portal.core.model.AppstoreCategory;
import org.oasis_eu.portal.core.services.test.InMemoryEntityStore;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PortalCoreConfiguration.class, loader = AnnotationConfigContextLoader.class)
@Ignore // Not implemented
public class AppstoreCategoryServiceTest {

    @Autowired
    private AppstoreCategoryService categoryService;

    @Before
    public void setUpTestData() {
        InMemoryEntityStore<AppstoreCategory> dao = new InMemoryEntityStore<>();
        dao.addEntity(new AppstoreCategory("d31093dd-bf55-4e4b-9994-af128087005a", new HashMap() {{
            put(OasisLocales.ENGLISH.getLanguage(), "DocumentManagement");
            put(OasisLocales.FRENCH.getLanguage(), "Gestion des documents");
            put(OasisLocales.BULGARIAN.getLanguage(), "управление на ресурсите");
        }}));
        dao.addEntity(new AppstoreCategory("3a34ce46-5d3a-4b39-9021-5dbe00f5c7bb", new HashMap() {{
            put(OasisLocales.ENGLISH.getLanguage(), "Resource Management");
            put(OasisLocales.FRENCH.getLanguage(), "Gestion des ressources");
        }}));
        dao.addEntity(new AppstoreCategory("7790e616-9185-4339-ab7c-9f51c1169ac9", new HashMap() {{
            put(OasisLocales.ENGLISH.getLanguage(), "Environment");
        }}));
        dao.addEntity(new AppstoreCategory("c8f8835c-52df-4c65-a536-68eeeb9aa375", new HashMap() {{
            put(OasisLocales.ENGLISH.getLanguage(), "Transport");
        }}));

        ReflectionTestUtils.setField(categoryService, "store", dao);
    }

    @Test
    public void testCategoryI18n() {
        // setup log appender
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AppstoreCategory.class);
        logger.addAppender(appender);

        AppstoreCategory cat = new AppstoreCategory();
        cat.setId("KatId");
        cat.setNames(new HashMap() {{
            put(OasisLocales.FRENCH.getLanguage(), "Catégorie de test");
        }});


        appender.start();
        assertNull(cat.getLocalizedName(OasisLocales.TURKISH));

        cat.setLocalizedName(OasisLocales.ENGLISH, "Hello world");

        assertEquals("Hello world", cat.getLocalizedName(OasisLocales.BULGARIAN));
        appender.stop();

        // check that we got our warnings and error
        assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("Cannot find translation for category KatId in language bg")));
        assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("Cannot find translation for category KatId in language tr")));
        assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("Cannot find translation for category KatId in default language (en)")));
    }


    @Test
    public void fetchCategories() {
        List<AppstoreCategory> categories = categoryService.find();

        assertEquals(4, categories.size());
        // test ordering
        assertEquals("d31093dd-bf55-4e4b-9994-af128087005a", categories.get(0).getId());
        assertEquals("3a34ce46-5d3a-4b39-9021-5dbe00f5c7bb", categories.get(1).getId());

        categories = categoryService.find(2, 10);
        assertEquals(2, categories.size());
        assertEquals("7790e616-9185-4339-ab7c-9f51c1169ac9", categories.get(0).getId());
    }

    @Test
    public void reorderCategories() {
        List<AppstoreCategory> categories = categoryService.find();
        categoryService.moveBefore(categories.get(1), categories.get(0));
        // reload categories
        categories = categoryService.find();
        assertEquals("3a34ce46-5d3a-4b39-9021-5dbe00f5c7bb", categories.get(0).getId());
        assertEquals("d31093dd-bf55-4e4b-9994-af128087005a", categories.get(1).getId());
    }

    // TODO: move the following tests to a generic test

    @Test
    public void crud() {
        AppstoreCategory appstoreCategory = new AppstoreCategory();
        appstoreCategory.setLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE, "Test Category");
        AppstoreCategory savedCategory = categoryService.create(appstoreCategory);
        assertNotNull(savedCategory.getId());
        assertEquals("Test Category", savedCategory.getLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE));
        assertEquals("Test Category", savedCategory.getLocalizedName(OasisLocales.TURKISH)); // fallback to default

        savedCategory.setLocalizedName(OasisLocales.FRENCH, "Catégorie de test");
        categoryService.update(savedCategory);

        AppstoreCategory reloaded = categoryService.find(savedCategory.getId());
        assertEquals("Catégorie de test", reloaded.getLocalizedName(OasisLocales.FRENCH));

        categoryService.delete(savedCategory);

        reloaded = categoryService.find(savedCategory.getId());
        assertNull(reloaded);

    }


    @Test(expected = InvalidEntityException.class)
    public void createCategoryRequiresAtLeastOneLanguage() {
        AppstoreCategory appstoreCategory = new AppstoreCategory();
        categoryService.create(appstoreCategory);
    }

    @Test(expected = InvalidEntityException.class)
    public void createCategoryRequiresPortalDefaultLanguage() {
        AppstoreCategory appstoreCategory = new AppstoreCategory();
        appstoreCategory.setLocalizedName(OasisLocales.TURKISH, "Türkçe kategorisinde isim");
        categoryService.create(appstoreCategory);
    }

    @Test(expected = InvalidEntityException.class)
    public void cannotCreateCategoryWithId() {
        AppstoreCategory appstoreCategory = new AppstoreCategory();
        appstoreCategory.setId(UUID.randomUUID().toString());
        appstoreCategory.setLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE, "Test Category");
        categoryService.create(appstoreCategory);
    }


    @Test(expected = EntityNotFoundException.class)
    public void updateInexisting() {
        AppstoreCategory doesntExist = new AppstoreCategory();
        doesntExist.setId("tagada pouet pouet");
        doesntExist.setLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE, "hello world");
        categoryService.update(doesntExist);

    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteInexisting() {
        AppstoreCategory category = new AppstoreCategory();
        category.setId("tagada pouet pouet");

        categoryService.delete(category);
    }

    @Test(expected = EntityNotFoundException.class)
    public void moveInexisting1() {
        AppstoreCategory doesntExist = new AppstoreCategory();
        doesntExist.setId("ho ho ho");
        doesntExist.setLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE, "hello world");

        AppstoreCategory found = categoryService.find("d31093dd-bf55-4e4b-9994-af128087005a");

        categoryService.moveBefore(doesntExist, found);
    }

    @Test(expected = EntityNotFoundException.class)
    public void moveInexisting2() {
        AppstoreCategory doesntExist = new AppstoreCategory();
        doesntExist.setId("ho ho ho");
        doesntExist.setLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE, "hello world");

        AppstoreCategory found = categoryService.find("d31093dd-bf55-4e4b-9994-af128087005a");

        categoryService.moveBefore(found, doesntExist);
    }

    @Test(expected = EntityNotFoundException.class)
    public void moveInexisting3() {
        AppstoreCategory doesntExist = new AppstoreCategory();
        doesntExist.setId("ho ho ho");
        doesntExist.setLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE, "hello world");

        categoryService.pushToEnd(doesntExist);
    }
}
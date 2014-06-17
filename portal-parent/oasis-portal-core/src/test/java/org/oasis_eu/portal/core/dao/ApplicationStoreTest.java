package org.oasis_eu.portal.core.dao;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.dao.impl.DAOTestConfiguration;
import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.spring.test.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DAOTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
@Category(IntegrationTest.class)
public class ApplicationStoreTest {

    @Autowired
    private ApplicationStore store;


    @Test
    public void testFind() {
        Application app = store.find("54456301-8c8c-40dc-b36a-da5ccf2b9148");

        assertNotNull(app);
        assertEquals("ОпенЕлец", app.getName(OasisLocales.BULGARIAN));
    }
}
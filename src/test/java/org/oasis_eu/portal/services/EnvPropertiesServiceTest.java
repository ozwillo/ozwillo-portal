package org.oasis_eu.portal.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.OzwilloPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class, MockServletContext.class})
public class EnvPropertiesServiceTest {

    @Autowired
    private EnvPropertiesService envPropertiesService;



    @Test
    public void defaultConfTest() {
        String defaultConf = this.envPropertiesService.extractDefaultConfKey();
        assertNotNull(defaultConf);
        assertEquals("ozwillo", defaultConf);
    }

}

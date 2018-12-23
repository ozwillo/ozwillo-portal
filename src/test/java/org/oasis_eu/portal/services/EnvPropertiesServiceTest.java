package org.oasis_eu.portal.services;

import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.OzwilloPortal;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class, MockServletContext.class})
public class EnvPropertiesServiceTest {

    @Autowired
    private EnvPropertiesService envPropertiesService;

    @Test
    public void defaultConfTest() {
        EnvConfig defaultConf = this.envPropertiesService.getDefaultConfig();
        assertNotNull(defaultConf);
        assertEquals("https://portal.ozwillo.com", defaultConf.getBaseUrl());
    }
}

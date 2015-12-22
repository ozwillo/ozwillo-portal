package org.oasis_eu.portal.main;

import org.oasis_eu.portal.OasisPortal;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * User: schambon
 * Date: 5/19/14
 */
public class OasisPortalWebapp extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(OasisPortal.class);
	}
}

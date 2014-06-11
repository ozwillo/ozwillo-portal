package org.oasis_eu.portal.core.services.cms.impl;

import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.constants.PortalConstants;
import org.oasis_eu.portal.core.mongo.dao.cms.ContentItemRepository;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.oasis_eu.portal.core.services.cms.ContentRenderingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Locale;

/**
 * TODO: cache the renderings!
 *
 * User: schambon
 * Date: 6/11/14
 */
@Service
public class ContentRenderingServiceImpl implements ContentRenderingService {

    private static Logger logger = LoggerFactory.getLogger(ContentRenderingServiceImpl.class);

    @Autowired
    private ContentItemRepository repository;

    @Override
    public String render(String contentId, Locale locale) {
        ContentItem item = repository.findOne(contentId);
        if (item == null) {
            logger.warn("No content item found for id: {}", contentId);
            return "";
        }

        String markdown = item.getContent().get(locale.getLanguage());
        if (markdown == null) {
            logger.warn("Content item {} found, but no translation in language {}", contentId, locale);
            markdown = item.getContent().get(PortalConstants.PORTAL_DEFAULT_LOCALE.getLanguage());
        }

        try {
            return new Markdown4jProcessor().process(markdown);
        } catch (IOException e) {
            logger.error("Exception caught while processing Markdown", e);
            return "";
        }
    }

}

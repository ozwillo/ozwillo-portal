package org.oasis_eu.portal.back.content;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.oasis_eu.portal.back.generic.Languages;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;

/**
 * 
 * XXX Refactor into ContentItem?
 * 
 * @author mkalamalami
 *
 */
public class ContentItemInfo {
	
	private ContentItem item;

	public ContentItemInfo(ContentItem item) {
		this.item = item;
	}

    public List<String> getMissingTranslations() {
    	return Arrays.asList(Languages.values()).stream()
    				.map(language -> language.getLocale().getLanguage())
    				.filter(languageId -> (item.getContent().get(languageId) == null || "".equals(item.getContent().get(languageId).trim())))
    				.collect(Collectors.toList());
	}
    
    public ContentItem getContentItem() {
		return item;
	}
    
}

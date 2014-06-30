package org.oasis_eu.portal.back.content;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.core.controller.PortalController;
import org.oasis_eu.portal.core.mongo.dao.cms.ContentItemRepository;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.oasis_eu.portal.model.ContentItemInfo;
import org.oasis_eu.portal.services.BackendNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * XXX hardcoded content/home edition references
 * 
 * @author mkalamalami
 *
 */
@Controller
public class ContentController extends PortalController {
	
    public static final String CONTENTS_PATH = "/contents";
    
    public static final String DEFAULT_CONTENT = "home";

    @Autowired
    private BackendNavigationService backendNavigationService;

    @Autowired
    private ContentItemRepository repository;
    
    /**
     * The language that will be displayed by default in the editor.
     */
    private Languages defaultLanguage = null;
    
    @RequestMapping(CONTENTS_PATH)
    public String edit(Model model, HttpServletRequest request) {
        return showContentsEditor(null, model, request);
    }
    
    @RequestMapping(CONTENTS_PATH + "/edit/{contentId}")
    public String edit(@PathVariable String contentId, Model model, HttpServletRequest request) {
        return showContentsEditor(contentId, model, request);
    }
    
    @RequestMapping(value = CONTENTS_PATH + "/create", method = RequestMethod.POST)
    public String create(@RequestParam String contentId, Model model) {
        if (!StringUtils.isEmpty(contentId)) {
            ContentItem newContentItem = new ContentItem();
            newContentItem.setId(contentId);
            repository.save(newContentItem);
            return "redirect:/contents/edit/" + contentId;
        }
        return "redirect:/contents";
    }
    
    @RequestMapping(value = CONTENTS_PATH + "/save", method = RequestMethod.POST)
    public String save(@RequestBody ContentItem contentItem, Model model) {
        repository.save(contentItem);
        return "redirect:/contents/edit/" + contentItem.getId();
    }

    @RequestMapping(value = CONTENTS_PATH + "/delete/{contentId}", method = RequestMethod.POST)
    public String delete(@PathVariable String contentId, Model model) {
        if (!DEFAULT_CONTENT.equals(contentId)) {
            repository.delete(contentId);
        }
        return "redirect:/contents/edit/" + DEFAULT_CONTENT;
    }

    @RequestMapping(value = CONTENTS_PATH + "/defaultlanguage", method = RequestMethod.POST)
    public String changeDefaultLanguage(@RequestBody String defaultLanguage, Model model) {
        for (Languages language : Languages.values()) {
            if (language.getLocale().getLanguage().equals(defaultLanguage)) {
                this.defaultLanguage = language;
                break;
            }
        }
        return "empty"; // XXX Cleaner way to return nothing?
    }
    
    public String showContentsEditor(String contentId, Model model, HttpServletRequest request) {
        contentId = ((contentId != null) ? contentId : DEFAULT_CONTENT);
        ContentItemInfo contentItemStats = new ContentItemInfo(repository.findOne(contentId));
        
        model.addAttribute("navigation", backendNavigationService.getNavigation("contents"));
        model.addAttribute("pageTemplate", "contents");
        model.addAttribute("contentList", getContentList());
		model.addAttribute("currentContent", contentItemStats.getContentItem());
        model.addAttribute("missingTranslations", contentItemStats.getMissingTranslations());
        model.addAttribute("languagesCount", Languages.values().length);
        model.addAttribute("defaultLanguage", defaultLanguage != null ? defaultLanguage : currentLanguage(request));
        
        return "backend";
    }

    private List<ContentItemInfo> getContentList() {
        // FIXME Bad performance
        // Use clever mongo query, or simply store missing translations count in content item?
        return repository.findAll()
                .stream()
                .sorted()
                .map(item -> new ContentItemInfo(item))
                .collect(Collectors.toList());
    }
    
}

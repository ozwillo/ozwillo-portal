package org.oasis_eu.portal.back.content;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.back.generic.BackendController;
import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.core.mongo.dao.cms.ContentItemRepository;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.oasis_eu.portal.model.ContentItemInfo;
import org.oasis_eu.portal.services.BackendNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
public class ContentController extends BackendController {
    
    public static final String DEFAULT_CONTENT = "home";

    @Autowired
    private BackendNavigationService backendNavigationService;

    @Autowired
    private ContentItemRepository repository;
    
    @Autowired
    private HttpServletRequest request;
    
    private Languages defaultEditorLanguage = null;
    
    @RequestMapping("/contents")
    public String edit(Model model, HttpServletRequest request) {
        return "redirect:/contents/edit/" + DEFAULT_CONTENT;
    }
    
    @RequestMapping("/contents/edit/{contentId}")
    public String edit(@PathVariable String contentId, Model model) {
        contentId = ((contentId != null) ? contentId : DEFAULT_CONTENT);
        ContentItemInfo contentItemStats = new ContentItemInfo(repository.findOne(contentId));
        
        model.addAttribute("navigation", backendNavigationService.getNavigation("contents"));
        model.addAttribute("pageTemplate", "contents");
        model.addAttribute("contentList", getContentList());
        
        if (contentItemStats.getContentItem() != null) {
			model.addAttribute("currentContent", contentItemStats.getContentItem());
	        model.addAttribute("missingTranslations", contentItemStats.getMissingTranslations());
        }
        
        return "backend";
    }
    
    @RequestMapping(value = "/contents/save", method = RequestMethod.POST)
	public String save(@RequestBody ContentItem contentItem) {
	    repository.save(contentItem);
	    return "redirect:/contents/edit/" + contentItem.getId();
	}

	@RequestMapping(value = "/contents/create", method = RequestMethod.POST)
    public String create(@RequestParam String contentId) {
        if (!StringUtils.isEmpty(contentId)) {
            ContentItem newContentItem = new ContentItem();
            newContentItem.setId(contentId);
            repository.save(newContentItem);
            return "redirect:/contents/edit/" + contentId;
        }
        return "redirect:/contents";
    }
    
    @RequestMapping(value = "/contents/rename", method = RequestMethod.POST)
    public String rename(@RequestParam("oldName") String oldName,
    		@RequestParam("newName") String newName) {
        if (!DEFAULT_CONTENT.equals(oldName)) {
        	if (!newName.equals(oldName)) {
    	        ContentItem item = repository.findOne(oldName);
    	        ContentItem existingItem = repository.findOne(newName);
    	        if (existingItem == null || newName.equals(oldName)) {
    		        repository.delete(item);
    		        item.setId(newName);
    		        repository.save(item);
    	        }
    	        else {
    	        	errorMessageService.addErrorMessage(
    	        			messageSource.getMessage("backend.contents.rename.error.exists",
    	        					new String[]{ newName },
    	        					currentLanguage(request).getLocale()));
    		        return "redirect:/contents/edit/" + oldName;
    	        }
        	}
		    return "redirect:/contents/edit/" + newName;
	        
        }
        else {
            return "redirect:/contents";
        }
    }
    
    @RequestMapping(value = "/contents/delete/{contentId}", method = RequestMethod.POST)
    public String delete(@PathVariable String contentId) {
        if (!DEFAULT_CONTENT.equals(contentId)) {
            repository.delete(contentId);
        }
        return "redirect:/contents";
    }

    @RequestMapping(value = "/contents/defaultlanguage", method = RequestMethod.POST)
    @ResponseBody
    public void changeDefaultEditorLanguage(@RequestBody String defaultEditorLanguageName) {
        for (Languages language : Languages.values()) {
            if (language.getLocale().getLanguage().equals(defaultEditorLanguageName)) {
                this.defaultEditorLanguage = language;
                break;
            }
        }
    }

    @ModelAttribute("defaultEditorLanguage")
    public Languages getDefaultEditorLanguage() {
    	return defaultEditorLanguage != null ? defaultEditorLanguage : currentLanguage(request);
	}
    
    @ModelAttribute("languagesCount")
    public int getLanguagesCount() {
    	return Languages.values().length;
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

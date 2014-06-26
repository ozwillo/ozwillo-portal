package org.oasis_eu.portal.back.content;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.back.generic.Languages;
import org.oasis_eu.portal.back.generic.PortalController;
import org.oasis_eu.portal.core.mongo.dao.cms.ContentItemRepository;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.oasis_eu.portal.services.BackendNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    private static final String DEFAULT_CONTENT = "home";

    @Autowired
    private BackendNavigationService backendNavigationService;

    @Autowired
    private ContentItemRepository repository;
    
    /**
     * The language that will be displayed by default in the editor.
     */
    private Languages defaultLanguage = null;
    
    @RequestMapping("/")
    public String home(Model model, HttpServletRequest request) {
        return showContentsEditor(null, model, request);
    }
    
    @RequestMapping("/contents")
    public String edit(Model model, HttpServletRequest request) {
        return showContentsEditor(null, model, request);
    }
    
    @RequestMapping("/contents/edit/{contentId}")
    public String edit(@PathVariable String contentId, Model model, HttpServletRequest request) {
        return showContentsEditor(contentId, model, request);
    }
    
    @RequestMapping(value = "/contents/create", method = RequestMethod.POST)
    public String create(@RequestParam String contentId, Model model) {
        ContentItem newContentItem = new ContentItem();
        newContentItem.setId(contentId);
        repository.save(newContentItem);
        return "redirect:/contents/edit/" + contentId;
    }
    
    @RequestMapping(value = "/contents/save", method = RequestMethod.POST)
    public String save(@RequestBody ContentItem contentItem, Model model) {
        repository.save(contentItem);
        return "redirect:/contents/edit/" + contentItem.getId();
    }

    @RequestMapping(value = "/contents/delete/{contentId}", method = RequestMethod.POST)
    public String delete(@PathVariable String contentId, Model model) {
        if (!DEFAULT_CONTENT.equals(contentId)) {
            repository.delete(contentId);
        }
        return "redirect:/contents/edit/" + DEFAULT_CONTENT;
    }

    @RequestMapping(value = "/contents/defaultlanguage", method = RequestMethod.POST)
    public String save(@RequestBody String defaultLanguage, Model model) {
        for (Languages language : Languages.values()) {
            if (language.getName().equals(defaultLanguage)) {
                this.defaultLanguage = language;
                break;
            }
        }
        return "redirect:/contents/edit/" + DEFAULT_CONTENT;
    }
    
    public String showContentsEditor(String contentId, Model model, HttpServletRequest request) {
        String page = "contents";
        contentId = ((contentId != null) ? contentId : DEFAULT_CONTENT);
        
        model.addAttribute("navigation", backendNavigationService.getNavigation(page));
        model.addAttribute("page", page);
        model.addAttribute("contentIdList", getContentIdList());
        model.addAttribute("contentItem", repository.findOne(contentId));
        model.addAttribute("defaultLanguage", defaultLanguage != null ? defaultLanguage : currentLanguage(request));
        
        return "backend";
    }

    private List<String> getContentIdList() {
        // TODO only query IDs through projection : db.content_item.find({}, ["_id"])
        return repository.findAll().stream()
            .map(item -> item.getId())
            .sorted()
            .collect(Collectors.toList());
    }

}

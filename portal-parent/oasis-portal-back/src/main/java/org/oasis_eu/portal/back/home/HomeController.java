package org.oasis_eu.portal.back.home;

import net.minidev.json.JSONObject;

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

/**
 * XXX hardcoded content/home edition references
 * 
 * @author mkalamalami
 *
 */
@Controller
public class HomeController extends PortalController {

    @Autowired
    private BackendNavigationService backendNavigationService;

    @Autowired
    private ContentItemRepository repository;
    
    @RequestMapping("/")
    public String home(Model model) {
        return showContentsEditor(null, model);
    }
    
    @RequestMapping("/contents")
    public String backend(Model model) {
        return showContentsEditor(null, model);
    }
    
    @RequestMapping("/contents/{contentId}")
    public String backend(@PathVariable String contentId, Model model) {
        return showContentsEditor(contentId, model);
    }

    @RequestMapping(value = "/contents/{contentId}", method = RequestMethod.POST)
    public String postContents(@PathVariable String contentId, @RequestBody ContentItem contentItem, Model model) {
        repository.save(contentItem);
        return showContentsEditor(contentId, model);
    }
    
    public String showContentsEditor(String contentId, Model model) {
        String page = "contents";
        contentId = ((contentId != null) ? contentId : "home");
        
        model.addAttribute("navigation", backendNavigationService.getNavigation(page));
        model.addAttribute("page", page);
        model.addAttribute("contentId", contentId);
        model.addAttribute("content", repository.findOne(contentId).getContent().get("fr"));
        
        return "home";
    }

}

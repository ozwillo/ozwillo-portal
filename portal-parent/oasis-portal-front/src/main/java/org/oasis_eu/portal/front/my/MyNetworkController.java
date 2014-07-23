package org.oasis_eu.portal.front.my;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.model.directory.AgentInfo;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
@RequestMapping("/my/network")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyNetworkController extends PortalController {

	private static final Logger logger = LoggerFactory.getLogger(MyNetworkController.class);

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private OrganizationStore organizationStore;
    
    @Autowired
    private UserDirectory userDirectory;

    // TODO Decide cache policies
    private Cache<String, String> organizationNamesCache = CacheBuilder.newBuilder()
    		.expireAfterWrite(10, TimeUnit.MINUTES)
    		.build();
    
    @RequestMapping(method = RequestMethod.GET, value="")
    public String profile(Model model) throws ExecutionException {
    	initModel(model);
        return "my-network";
    }
    
    @RequestMapping(method = RequestMethod.GET, value="/fragment/{fragmentId}")
    public String getFragment(@PathVariable("fragmentId") String fragmentId, Model model) throws ExecutionException {
    	initModel(model);
        return "my-network :: " + fragmentId;
    }

    @RequestMapping(method = RequestMethod.POST, value="/relationships/save/{agentId}")
    public String saveRelationship(@PathVariable("agentId") String agentId, Model model) throws ExecutionException {
    	initModel(model);
    	// TODO Ability to change administrator status
        return "redirect:/my/network/fragment/relationships";
    }

    @RequestMapping(method = RequestMethod.POST, value="/relationships/create")
    public String addRelationship(@RequestParam("emails") String emails, Model model) throws ExecutionException {
    	initModel(model);
    	// TODO Field validation
		for (String email : emails.split(",")) {
	    	if (!StringUtils.isEmpty(email)) {
				UserInfo newAgent = new UserInfo();
				newAgent.setEmail(email.trim());
		    	userDirectory.createAgent(user().getOrganizationId(), newAgent);
	    	}
		}
        return "redirect:/my/network/fragment/relationships";
    }

    @RequestMapping(method = RequestMethod.POST, value="/relationships/delete/{agentId}")
    public String removeRelationship(@PathVariable("agentId") String agentId, Model model) throws ExecutionException {
    	initModel(model);
    	userDirectory.deleteAgent(userDirectory.getAgent(agentId));
        return "redirect:/my/network/fragment/relationships";
    }
    
	private void initModel(Model model) throws ExecutionException {
		String organizationId = user().getOrganizationId();
        model.addAttribute("navigation", myNavigationService.getNavigation("network"));
		model.addAttribute("isAgent", organizationId != null);
		if (organizationId != null) {
			List<AgentInfo> agents = userDirectory.getAgents(organizationId, 0, 25);
			model.addAttribute("agents", agents); // TODO Pagination
			model.addAttribute("organizationNames", fetchOrganizationNames(agents));
		}
	}


    private Map<String, String> fetchOrganizationNames(List<AgentInfo> agents) throws ExecutionException {
    	 return agents.stream()
			.map(agent -> agent.getOrganizationId())
			.distinct()
			.collect(Collectors.toMap(
					orgId -> orgId,
					orgId -> fetchOrganizationName(orgId)
				));
    }
    
    private String fetchOrganizationName(String orgId) {
    	try {
			return organizationNamesCache.get(orgId, new OrganizationNameCallable(orgId));
		} catch (ExecutionException e) {
			logger.error("Failed to fetch organization name", e);
			return orgId;
		}
    }
    
    private class OrganizationNameCallable implements Callable<String> {

    	private String orgId;

		public OrganizationNameCallable(String orgId) {
			this.orgId = orgId;
		}
    	
		@Override
		public String call() throws Exception {
			return organizationStore.find(orgId).getName();
		}
    	
    }

}
package org.oasis_eu.portal.front.my.network;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.NetworkService.UserGeneralInfo;
import org.oasis_eu.portal.services.dc.organization.DCOrganization;
import org.oasis_eu.portal.services.dc.organization.OrganizationService;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon Date: 10/24/14
 */
@RestController
@RequestMapping("/my/api/network")
public class NetworkAJAXServices extends BaseAJAXServices {

	private static final Logger logger = LoggerFactory.getLogger(NetworkAJAXServices.class);

	@Value("${application.devmode:false}")
	private boolean devmode;

	@Autowired
	private NetworkService networkService;
	@Autowired
	private OrganizationService organizationService;

	@RequestMapping(value = "/organizations", method = GET)
	public List<UIOrganization> organizations() {
		List<UIOrganization> organizations = networkService.getMyOrganizations();

		logger.debug("Found organizations: {}", organizations);

		return organizations;
	}

	@RequestMapping(value = "/organization/{organizationId}", method = POST)
	public void updateOrganization(@RequestBody @Valid UIOrganization organization, Errors errors) {
		logger.debug("Updating organization {}", organization);

		networkService.updateOrganization(organization);
	}

	@RequestMapping(value = "/invite/{organizationId}", method = POST)
	public void invite(@PathVariable String organizationId, @RequestBody @Valid InvitationRequest invitation,
			Errors errors) {
		logger.debug("Inviting {} to organization {}", invitation.email, organizationId);

		if (errors.hasErrors()) {
			throw new WrongQueryException();
		}

		networkService.invite(invitation.email, organizationId);

	}

	@RequestMapping(value = "/remove-invitation/{organizationId}", method = POST)
	public void removeInvitation(@PathVariable String organizationId,
			@RequestBody @Valid RemoveInvitationRequest invitation, Errors errors) {
		logger.debug("Removing invitation {} to organization {}", invitation.email, organizationId);

		if (errors.hasErrors()) {
			throw new WrongQueryException();
		}

		networkService.removeInvitation(organizationId, invitation.id, invitation.etag);

	}

	@RequestMapping(value = "/leave", method = POST)
	public void leave(@RequestBody @Valid LeaveRequest request) {
		logger.debug("Leaving {}", request.organization);

		networkService.leave(request.organization);
	}

	@RequestMapping(value = "/search-organization", method = GET)
	public DCOrganization searchOrganization(
			@RequestParam(required=true) String country,
			@RequestParam(required=true) String country_uri,
			@RequestParam(required=true) String legal_name,
			@RequestParam(required=true) String tax_reg_num,
			@RequestParam(required=true) String sector_type
	) {
		logger.debug("Searching for organization {} from {} of type {}", legal_name, country+"["+country_uri+"]", sector_type);

		return organizationService.findOrganization(country,country_uri, sector_type, legal_name, tax_reg_num);
	}

	@RequestMapping(value = "/search-organizations", method = GET)
	public List<DCOrganization> searchOrganizations(
			@RequestParam(required=true) String country_uri,
			@RequestParam(required=true) String query) {
		logger.debug("Searching for organization matching {} in country {}", query, country_uri);

		return organizationService.findOrganizations(country_uri, query);
	}

	@RequestMapping(value = "/kernel-organization", method = HEAD)
	public ResponseEntity<String> checkKernelOrganization(@RequestParam(required=true) String dc_id) {
		logger.debug("Checking existence of organization {} in kernel", dc_id);
		if (networkService.searchOrganizationByDCId(dc_id) == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<>(HttpStatus.FOUND);
	}

	@RequestMapping(value = "/search-organization-by-id", method = GET)
	public DCOrganization searchOrganizationByID(@RequestParam(required=true) String dc_id) {
		logger.debug("Searching for organization with id : {} ", dc_id);

		return dc_id != null && !dc_id.isEmpty() ? organizationService.findOrganizationById(dc_id) : null;
	}

	@RequestMapping(value = "/create-dc-organization", method = POST)
	public UIOrganization createDCOrganization(@RequestBody DCOrganization dcOrganization) {
		return organizationService.create(dcOrganization);
	}
	@RequestMapping(value = "/update-dc-organization", method = POST)
	public UIOrganization updateDCOrganization(@RequestBody DCOrganization dcOrganization) {
		return organizationService.update(dcOrganization);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/general-user-info")
	public UserGeneralInfo getUserGeneralInformation() {
		UserGeneralInfo userGeneralInfo  = networkService.getCurrentUser();// new UserGeneralInfo();
		return userGeneralInfo;
	}

	@RequestMapping(value = "/organization/{organizationId}/set-status", method = POST)
	public String setOrganizationStatus(@RequestBody @Valid UIOrganization organization, Errors errors) {
		logger.debug("Updating organization {}", organization);

		return networkService.setOrganizationStatus(organization);
	}


	public static class InvitationRequest {
		@JsonProperty
		@NotNull
		@NotEmpty
		String email;
	}

	public static class RemoveInvitationRequest {
		@JsonProperty
		@NotNull
		@NotEmpty
		String id;

		@JsonProperty
		@NotNull
		@NotEmpty
		String email;

		@JsonProperty
		@NotNull
		@NotEmpty
		String etag;
	}

	public static class LeaveRequest {
		@JsonProperty
		@NotNull
		@NotEmpty
		String organization;
	}

}

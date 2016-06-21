package org.oasis_eu.portal.front.my.network;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.model.network.UIOrganizationMember;
import org.oasis_eu.portal.model.network.UIPendingOrganizationMember;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.NetworkService.UserGeneralInfo;
import org.oasis_eu.portal.services.dc.organization.DCOrganization;
import org.oasis_eu.portal.services.dc.organization.DCOrganizationService;
import org.oasis_eu.portal.services.dc.organization.OrganizationService;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * User: schambon Date: 10/24/14
 */
@RestController
@RequestMapping("/my/api/network")
public class NetworkAJAXServices extends BaseAJAXServices {

	private static final Logger logger = LoggerFactory.getLogger(NetworkAJAXServices.class);

	@Autowired
	private NetworkService networkService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private DCOrganizationService dcOrganizationService;

	@RequestMapping(value = "/organizations", method = GET)
	public List<UIOrganization> organizations() {
		List<UIOrganization> organizations = networkService.getMyOrganizations();

		logger.debug("Found organizations: {}", organizations);

		return organizations;
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

	@RequestMapping(value = "/organization/{organizationId}/membership/{accountId}", method = DELETE)
	public void removeMember(@PathVariable String organizationId, @PathVariable String accountId) {
		networkService.removeMember(organizationId, accountId);
	}

	@RequestMapping(value = "/organization/{organizationId}/membership/{accountId}", method = POST)
	public void updateMember(@PathVariable String organizationId, @PathVariable String accountId,
							 @RequestParam String admin) {
		networkService.updateMember(organizationId, accountId, Boolean.valueOf(admin));
	}

	@RequestMapping(value = "/leave", method = POST)
	public void leave(@RequestBody @Valid LeaveRequest request) {
		logger.debug("Leaving {}", request.organization);

		networkService.leave(request.organization);
	}

	@RequestMapping(value = "/search-organization", method = GET)
	public DCOrganization searchOrganization(
			@RequestParam String country,
			@RequestParam String country_uri,
			@RequestParam String legal_name,
			@RequestParam String tax_reg_num,
			@RequestParam String sector_type) {
		logger.debug("Searching for organization {} from {} of type {}", legal_name, country+" ["+country_uri+"]", sector_type);

		return organizationService.findOrBootstrapOrganization(country, country_uri, sector_type, legal_name, tax_reg_num);
	}

	@RequestMapping(value = "/search-organizations", method = GET)
	public List<DCOrganization> searchOrganizations(
			@RequestParam(required=true) String country_uri,
			@RequestParam(required=true) String query) {
		logger.debug("Searching for organization matching {} in country {}", query, country_uri);

		return organizationService.findOrganizations(country_uri, query);
	}

	@RequestMapping(value = "/kernel-organization", method = HEAD)
	public ResponseEntity<String> checkKernelOrganization(@RequestParam String dc_id) {
		logger.debug("Checking existence of organization {} and its aliases in kernel", dc_id);
		if (!organizationService.existsOrganizationOrAliasesInKernel(dc_id))
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<>(HttpStatus.FOUND);
	}

	@RequestMapping(value = "/organization", method = GET)
	public DCOrganization getOrganization(@RequestParam String dc_id) {
		logger.debug("Loading organization {}", dc_id);

		return organizationService.getOrganization(dc_id);
	}

	/**
	 * Checks whether the given registration number is available :
	 * <li>
	 *     <ul>It does not exist in the DC</ul>
	 *     <ul>It exists but is one of the past registration numbers of the given organization</ul>
	 * </li>
	 */
	@RequestMapping(value = "/check-regnumber-availability", method = HEAD)
	public ResponseEntity<Object> checkRegnumberAvailability(@RequestParam String country_uri,
			@RequestParam String reg_number, @RequestParam String dc_id) {
		logger.debug("Searching for organization with reg number {} and country {}", reg_number, country_uri);

		Optional<DCResource> resource = dcOrganizationService.findOrganizationByCountryAndRegNumber(country_uri, reg_number);
		if (!resource.isPresent() || resource.get().getUri().equals(dc_id))
			return new ResponseEntity<>(HttpStatus.OK);
		else
			return new ResponseEntity<>(HttpStatus.FOUND);
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
		return networkService.getCurrentUser();
	}

	@RequestMapping(value = "/organization/{organizationId}/set-status", method = POST)
	public String setOrganizationStatus(@RequestBody @Valid UIOrganization organization, Errors errors) {
		logger.debug("Updating organization {}", organization);

		return networkService.setOrganizationStatus(organization);
	}

	@RequestMapping(value = "/organization/{organizationId}/members", method = GET)
	public List<UIOrganizationMember> getOrganizationMembers(@PathVariable String organizationId) {
		return networkService.getOrganizationMembers(organizationId);
	}

	@RequestMapping(value = "/organization/{organizationId}/pending-members", method = GET)
	public List<UIPendingOrganizationMember> getOrganizationPendingMembers(@PathVariable String organizationId) {
		return networkService.getOrganizationPendingMembers(organizationId);
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

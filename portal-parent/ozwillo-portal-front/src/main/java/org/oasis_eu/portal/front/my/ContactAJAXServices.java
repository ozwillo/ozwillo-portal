package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/my/api/contact")
public class ContactAJAXServices extends BaseAJAXServices {

	private static final Logger logger = LoggerFactory.getLogger(ContactAJAXServices.class);

	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public void send(@RequestBody @Valid ContactRequest contactRequest) {
		logger.debug("send {}'s contact type with subject {} and body {}", contactRequest.motive,
						contactRequest.subject, contactRequest.body);


	}

	public static class ContactRequest {
		@JsonProperty
		@NotNull
		@NotEmpty
		String motive;

		@JsonProperty
		@NotNull
		@NotEmpty
		String subject;

		@JsonProperty
		@NotNull
		@NotEmpty
		String body;
	}

}

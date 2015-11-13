package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.services.MailService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/my/api/contact")
public class ContactAJAXServices extends BaseAJAXServices {

	private static final Logger logger = LoggerFactory.getLogger(ContactAJAXServices.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private UserInfoService userInfoService;

	@Value("${mail.contact}")
	private String mailContact;

	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public void send(@RequestBody @Valid ContactRequest contactRequest) throws MessagingException {
		logger.debug("send {}'s contact type with subject {}, body {}", contactRequest.motive, contactRequest.subject, contactRequest.body);

		String senderEmail = userInfoService.currentUser().getEmail();
		String carbonCopy = null;
		if (contactRequest.copyToSender){
			carbonCopy = senderEmail;
		}

		String subject = String.format("[%s by %s]%s",contactRequest.motive, userInfoService.currentUser().getNickname(), contactRequest.subject);

		mailService.sendMail(mailContact, carbonCopy, senderEmail, subject, contactRequest.body);
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

		@JsonProperty
		boolean copyToSender;
	}
}

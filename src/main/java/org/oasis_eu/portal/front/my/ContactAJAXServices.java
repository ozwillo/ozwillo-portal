package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.services.MailService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Locale;

@RestController
@RequestMapping("/my/api/contact")
public class ContactAJAXServices extends BaseAJAXServices {

	private static final Logger logger = LoggerFactory.getLogger(ContactAJAXServices.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private MessageSource messageSource;

	@Value("${mail.contact}")
	private String mailContact;

	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public void send(@RequestBody @Valid ContactRequest contactRequest, Locale locale) throws MessagingException {
		logger.debug("send {}'s contact type with subject {}, body {}", contactRequest.motive, contactRequest.subject, contactRequest.body);

		UserInfo userInfo = userInfoService.currentUser();

		String userEmail = userInfo.getEmail();
		String carbonCopy = null;
		if (contactRequest.copyToSender){
			carbonCopy = userEmail;
		}

		String motive = messageSource.getMessage(contactRequest.motive, new Object[] {}, locale);
		String subject = String.format("[%s] %s", motive, contactRequest.subject);

		String requesterLabel = messageSource.getMessage("contact.form.requester", new Object[] {}, locale);
		String bodyLabel = messageSource.getMessage("contact.form.body", new Object[] {}, locale);
		String userName;
		if (!Strings.isNullOrEmpty(userInfo.getGivenName()) || !Strings.isNullOrEmpty(userInfo.getFamilyName()))
			userName = userInfo.getNickname()
				+ " (" + Strings.nullToEmpty(userInfo.getGivenName()) + " " + Strings.nullToEmpty(userInfo.getFamilyName()) + ")";
		else
			userName = userInfo.getNickname();
		StringBuilder bodyBuilder = new StringBuilder()
			.append(requesterLabel).append(" : ").append(userName).append("\n\n")
			.append(bodyLabel).append(" : ").append("\n\n").append(contactRequest.body);

		mailService.sendMail(mailContact, carbonCopy, userEmail, subject, bodyBuilder.toString());
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

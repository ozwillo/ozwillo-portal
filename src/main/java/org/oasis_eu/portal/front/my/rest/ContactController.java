package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@RestController
@RequestMapping("/my/api/contact")
public class ContactController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private MailSender mailSender;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private MessageSource messageSource;

    @Value("${mail.contact.from}")
    private String mailContactFrom;

    @Value("${mail.contact.to}")
    private String mailContactTo;

    @Value("${captcha.secret}")
    private String captchaSecret;

    @Value("${captcha.sitekey}")
    private String sitekey;

    @Value("${captcha.url}")
    private String captchaUrl;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity<String> send(@RequestBody @Valid ContactRequest contactRequest, Locale locale) {
        logger.debug("send {}'s contact type with subject {}, body {}", contactRequest.motive, contactRequest.subject, contactRequest.body);

        HttpHeaders headers = new HttpHeaders();
        RestTemplate rest = new RestTemplate();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        UriComponents uriComponents = UriComponentsBuilder
                        .fromUriString(captchaUrl)
                        .queryParam("secret", captchaSecret)
                        .queryParam("response", contactRequest.captchaToken)
                        .build()
                        .encode();
        ResponseEntity<CaptchaResponse> responseEntity = rest.exchange(uriComponents.toUri(), HttpMethod.POST, requestEntity, CaptchaResponse.class);

        CaptchaResponse captchaResponse = responseEntity.getBody();
        logger.debug("Verification of recaptcha is '{}', the hostname of the site where the reCAPTCHA was solved {}", captchaResponse.success, captchaResponse.hostname);

        if(captchaResponse.success) {
            SimpleMailMessage message = new SimpleMailMessage();

            UserInfo userInfo = userInfoService.currentUser();
            String userEmail = userInfo.getEmail();

            message.setTo(mailContactTo);
            message.setFrom(mailContactFrom);
            message.setReplyTo(userEmail);
            if (contactRequest.copyToSender) {
                message.setCc(userEmail);
            }

            String motive = messageSource.getMessage(contactRequest.motive, new Object[]{}, locale);
            String subject = String.format("[%s] %s", motive, contactRequest.subject);
            message.setSubject(subject);

            String requesterLabel = messageSource.getMessage("contact.form.requester", new Object[]{}, locale);
            String bodyLabel = messageSource.getMessage("contact.form.body", new Object[]{}, locale);
            String userName;
            if (!Strings.isNullOrEmpty(userInfo.getGivenName()) || !Strings.isNullOrEmpty(userInfo.getFamilyName()))
                userName = userInfo.getNickname()
                        + " (" + Strings.nullToEmpty(userInfo.getGivenName()) + " " + Strings.nullToEmpty(userInfo.getFamilyName()) + ")";
            else
                userName = userInfo.getNickname();
            StringBuilder bodyBuilder = new StringBuilder()
                    .append(requesterLabel).append(" : ").append(userName).append("\n\n")
                    .append(bodyLabel).append(" : ").append("\n\n").append(contactRequest.body);

            message.setText(String.valueOf(bodyBuilder));

            this.mailSender.send(message);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/captchasitekey")
    public ResponseEntity<String> notifications() {
        if(StringUtils.isEmpty(sitekey)) {
            logger.info("Captcha sitekey is empty, you must add it to the config file");
            return new ResponseEntity<String>(" Captcha sitekey is empty", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sitekey, HttpStatus.OK);
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
        @NotNull
        @NotEmpty
        String captchaToken;

        @JsonProperty
        boolean copyToSender;
    }

    public static class CaptchaResponse {
        @JsonProperty
        @NotNull
        @NotEmpty
        boolean success;

        @JsonProperty
        @NotNull
        @NotEmpty
        Date challenge_ts;

        @JsonProperty
        @NotNull
        @NotEmpty
        String hostname;

        @JsonProperty("error-codes")
        ArrayList errorCodes;
    }
}

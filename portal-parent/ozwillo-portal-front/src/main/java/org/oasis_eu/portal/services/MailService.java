package org.oasis_eu.portal.services;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Service
public class MailService {

	private final Properties connexionProperties;
	private final String login;
	private final String password;

	@Autowired
	MailService(@Value("${mail.host}") String host,
				@Value("${mail.port}") String port,
				@Value("${mail.starttls}") boolean starttls,
				@Value("${mail.login}") String login,
				@Value("${mail.password}") String password) {
		connexionProperties = new Properties();
		connexionProperties.setProperty("mail.smtp.auth", "true");
		connexionProperties.setProperty("mail.smtp.starttls.enable", String.valueOf(starttls));
		connexionProperties.setProperty("mail.smtp.host", host);
		connexionProperties.setProperty("mail.smtp.port", port);

		if (log.isDebugEnabled()) {
			connexionProperties.setProperty("mail.debug", "true");
		}

		 this.login = login;
		 this.password = password;
	}

	private Session authenticateSession() {
		log.debug("Authentication for SMTP");
		return Session.getInstance(connexionProperties,
			new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(login, password);
				}
			});
	}

	public void sendMail(String to, String subject, String text) throws MessagingException {
		sendMail(to, null, subject, text);
	}

	public void sendMail(String to, String cc, String subject, String text) throws MessagingException {
		sendMail(to, cc, null, subject, text);
	}

	public void sendMail(String to, String cc, String replyTo, String subject, String text) throws MessagingException {
		sendMail(to, cc, null, replyTo, subject, text);
	}

	public void sendMail(String to, String cc, String bcc, String replyTo, String subject, String text) throws MessagingException {
		try {
			Message message = new MimeMessage(authenticateSession());
			message.setFrom(new InternetAddress(login));
			// message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("matthieu.lecoupeau@openwide.fr"));

			if (!Strings.isNullOrEmpty(cc)) {
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
			}
			if (!Strings.isNullOrEmpty(bcc)) {
				message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
			}

			if (!Strings.isNullOrEmpty(replyTo)){
				message.setReplyTo(InternetAddress.parse(replyTo));
			}

			message.setSubject(subject);
			message.setText(text);
			message.setSentDate(DateTime.now().toDate());
			message.setHeader("Content-Type", "text/plain; charset=UTF-8");

			log.debug("Sending email, from : {}\tto : {}\tsubject : {}", login, to, subject);
			Transport.send(message);
			log.debug("Mail send !");

		} catch (MessagingException e) {
			log.error("Error during sending mail !",e);
			throw  e;
		}
	}
}

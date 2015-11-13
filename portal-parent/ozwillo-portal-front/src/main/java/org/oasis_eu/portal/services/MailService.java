package org.oasis_eu.portal.services;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailService.class);

	private final Properties connexionProperties;
	private final String login;
	private final String password;
	private final String protocol;

	@Autowired
	MailService(@Value("${mail.host}") String host,
				@Value("${mail.port}") String port,
				@Value("${mail.smtps}") boolean smtps,
				@Value("${mail.starttls}") boolean starttls,
				@Value("${mail.login}") String login,
				@Value("${mail.password}") String password) {

		protocol = smtps ? "smtps" : "smtp";
		String prefix = "mail." + protocol;

		connexionProperties = new Properties();
		connexionProperties.setProperty(prefix + ".auth", "true");
		connexionProperties.setProperty(prefix + ".starttls.enable", String.valueOf(starttls));
		connexionProperties.setProperty(prefix + ".host", host);

		if (Integer.valueOf(port) > 0) {
			connexionProperties.setProperty(prefix + ".port", port);
		}

		if (logger.isDebugEnabled()) {
			connexionProperties.setProperty("mail.debug", "true");
		}

		 this.login = login;
		 this.password = password;
	}

	private Session authenticateSession() {
		logger.debug("Authentication for SMTP");
		Session session = Session.getInstance(connexionProperties, getAuthenticator());
		session.setProtocolForAddress("rfc822", protocol);
		return session;
	}

	private Authenticator getAuthenticator(){
		return new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(login, password);
			}
		};
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
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

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

			logger.debug("Sending email, from : {}\tto : {}\tsubject : {}", login, to, subject);
			Transport.send(message);
			logger.debug("Mail sent !");

		} catch (MessagingException e) {
			logger.error("Error during sending mail !",e);
			throw  e;
		}
	}
}

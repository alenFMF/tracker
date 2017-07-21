package com.tracker.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailService {
	
	@Value("#{generalProperties.emailConfigPath}")
	private String emailConfigPath;
	
	private String IAMUserName = null;
	private String SmtpUsername = null;
	private String SmtpPassword = null;
	private String host;
	private int port;
	private String protocol;
	private boolean smtpsAuth;
	private boolean smtpsStartTLSEnable;
	private String from;
	
	private JavaMailSender mailSender;
	
	private SimpleMailMessage templateMessage;
	
	@PostConstruct
	public void postConstruct() {
		 InputStream input = null;
		 try {
				input = new FileInputStream(emailConfigPath);
				Properties properties = new Properties();
				// load a properties file
				properties.load(input);
				this.IAMUserName = properties.getProperty("mail.IAMUserName");
				properties.remove("mail.IAMUserName");
				this.SmtpUsername = properties.getProperty("mail.SmtpUsername");
				properties.remove("mail.SmtpUsername");
				this.SmtpPassword = properties.getProperty("mail.SmtpPassword");
				properties.remove("mail.SmtpPassword");
				this.from = properties.getProperty("mail.from");	
				properties.remove("mail.from");
				
				
				
				this.host = properties.getProperty("mail.host");
				properties.remove("mail.host");
				this.port = Integer.parseInt(properties.getProperty("mail.port"));
				properties.remove("mail.port");
				
				this.protocol = properties.getProperty("mail.transport.protocol");				
				this.smtpsAuth = Boolean.parseBoolean(properties.getProperty("mail.smtps.auth"));
				this.smtpsStartTLSEnable = Boolean.parseBoolean(properties.getProperty("mail.smtps.starttls.enable"));
				
				
				JavaMailSenderImpl sender = new JavaMailSenderImpl();
				sender.setJavaMailProperties(properties);
				sender.setHost(this.host);
				sender.setPort(this.port);
//				sender.setProtocol(this.protocol);
				sender.setUsername(this.SmtpUsername);
				sender.setPassword(this.SmtpPassword);
				mailSender = sender;
				
				templateMessage = new SimpleMailMessage();
				templateMessage.setFrom(this.from);				
		 } catch (Exception ex) {
			 System.err.println("ERROR: Problem with email configuration.");
			 ex.printStackTrace();
		 } finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		 }				 
	} 		
	
	public void send(String to, String subject, String body, String contentType) {
		SimpleMailMessage message = new SimpleMailMessage(this.templateMessage);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);		
		mailSender.send(message);
	}
}

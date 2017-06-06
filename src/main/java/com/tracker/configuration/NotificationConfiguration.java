package com.tracker.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tracker.engine.NotificationService;

@Configuration
public class NotificationConfiguration {
	
	@Value("#{notificationProperties}")
	private Properties notificationProperties;
	
	@Bean
	public NotificationService notificationService() {
		NotificationService service = new NotificationService(notificationProperties);
		return service;
	}
	
}

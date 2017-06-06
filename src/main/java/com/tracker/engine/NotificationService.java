package com.tracker.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class NotificationService {
	private String gcmApiKey = null; 
	private String apnsCertificatePath = null; 
	private String apnsPassword = null; 
	
	private Sender gcmService;
	private ApnsService apnsService;
	
	public NotificationService(Properties properties) {
		 apnsCertificatePath = properties.getProperty("notification.apnsCertificatePath");
		 InputStream input = null;
		 try {
				input = new FileInputStream(properties.getProperty("notification.configurationPath"));
				Properties prop2 = new Properties();
				// load a properties file
				prop2.load(input);
				gcmApiKey = prop2.getProperty("gcmApiKey");
				apnsPassword = prop2.getProperty("apnsPassword");
				this.gcmService = new Sender(this.gcmApiKey);
				this.apnsService = APNS.newService()
				    	 .withCert(apnsCertificatePath, apnsPassword)
				    	 .withSandboxDestination()
				    	 .build();			
//		    	 cleanup devices ios
//		    	 Map<String,Date> getInactiveDevices()
//		                 throws NetworkIOException
				
		 } catch (Exception ex) {
			 System.err.println("ERROR: Problem with Notification server configuration.");
//			 throw new RuntimeException(ex);
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
	
	public Sender getGcmService() {
		return gcmService;
	}

	public ApnsService getApnsService() {
		return apnsService;
	}
}

package com.tracker.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;

public class NotificationService {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationService.class);
	private final int retries = 3; 
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
		 } catch (Exception ex) {
			 System.err.println("ERROR: Problem with Notification server configuration. Notification server disabled.");
			 ex.printStackTrace();
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
	
    public String pushGCM(String token, String title, String message){
        Message msg = new Message.Builder()
        		.addData("title", title == null ? "" : title)
        		.addData("message", message == null ? "" : message)
        		.addData("largeIcon", "push_notification_icon")
        		.addData("appName", "com.gooptidriverproject") // for receiving confirmation that push notification was delivered
        		.addData("urlPath", "response_push_notification_link")  // for receiving confirmation that push notification was delivered
        		.build();
        try {
                Result result = gcmService.send(msg, token, retries);
                return result.getMessageId();
        } catch (IOException e) {

        } 
        return null;
    }
    
    public String pushAPNS(String token, String title, String message){
    	try {
	    	 String payload = APNS.newPayload()
		    	 .alertBody(message)
		    	 .alertTitle(title).build();
	    	 @SuppressWarnings("unchecked")
	    	 ApnsNotification notification = (ApnsNotification)apnsService.push(token, payload);    
	    	 return Integer.toString(notification.getIdentifier());
	    	 
    	} catch (Exception e) {
        } 
    	return null;
    }	
    
    public String push(String token, String title, String message, String platform) {
    	if(platform.equals("iOS") || platform.equals("iPhone OS")) return pushAPNS(token, title, message);
    	if(platform.equals("Android")) return pushGCM(token, title, message);
    	return null;
    }
	
}

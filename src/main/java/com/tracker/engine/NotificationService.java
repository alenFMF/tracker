package com.tracker.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;

public class NotificationService {
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
	
    public boolean pushGCM(String token, String title, String message){
        Message msg = new Message.Builder()
        		.addData("title", title == null ? "" : title)
        		.addData("message", message == null ? "" : message)
        		.build();
        try {
                Result result = gcmService.send(msg, token, retries);
                return result.getMessageId() != null;
        } catch (IOException e) {

        } 
        return false;
    }
    
    public boolean pushAPNS(String token, String title, String message){
    	try {
	    	 String payload = APNS.newPayload()
		    	 .alertBody(message)
		    	 .alertTitle(title).build();
	    	 @SuppressWarnings("unchecked")
	    	 ApnsNotification notification = (ApnsNotification)apnsService.push(token, payload);    
	    	 return notification.getIdentifier() > 0;
    	} catch (Exception e) {
        } 
    	return false;
    }	
    
    public boolean push(String token, String title, String message, String platform) {
    	if(platform.equals("iOS")) return pushAPNS(token, title, message);
    	if(platform.equals("Android")) return pushGCM(token, title, message);
    	return false;
    }
	
}

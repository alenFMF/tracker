package com.tracker.apientities.notifications;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APINotificationsResponse extends APIBaseResponse {
	public List<APINotificationMessage> sent;
	public List<APINotificationMessage> received;
	
	public APINotificationsResponse() {
		super();
	}
	
	public APINotificationsResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
}

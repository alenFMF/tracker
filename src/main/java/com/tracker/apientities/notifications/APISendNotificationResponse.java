package com.tracker.apientities.notifications;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APISendNotificationResponse extends APIBaseResponse {
	public List<APINotificationStatus> notificationStatus;

	public APISendNotificationResponse() {
		super();
	}
	
	public APISendNotificationResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
}

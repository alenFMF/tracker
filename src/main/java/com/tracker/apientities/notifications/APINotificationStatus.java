package com.tracker.apientities.notifications;

import com.tracker.db.TrackingUser;

public class APINotificationStatus {
	public Integer messageId;
	public String userId;
	public String status;
	public String errorMessage;
	
	
	public APINotificationStatus() {}
	public APINotificationStatus(TrackingUser user, String status, String errorMessage) {
		this.userId = (user == null) ? null : user.getUserId();
		this.status = status;
		this.errorMessage = errorMessage;
	}
}

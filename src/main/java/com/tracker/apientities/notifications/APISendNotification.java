package com.tracker.apientities.notifications;

import java.util.List;

public class APISendNotification {
	public String token;
	public List<APIUsers> recipients;   // for type==NOTIFICATION only
	public String to;  // for type EMAIL only
	public String toGroup;  // relevant only if type==GROUP_NOTIFICATION
	public String fromGroup;  // sender must be admin of a group
	public String message;
	public String messageType;
	public String title;   // subject for email, title for notification
	public String type; // see NotificationEngine
	public Integer travelOrderId;
	public Integer taskGoalId;
}

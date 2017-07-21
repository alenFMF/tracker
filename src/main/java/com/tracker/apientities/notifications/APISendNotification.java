package com.tracker.apientities.notifications;

import java.util.List;

public class APISendNotification {
	public String token;
	public List<APIUsers> recipients; 
	public String to;  // in case of an EMAIL
	public String group;
	public String message;
	public String messageType;
	public String title;   // subject for email
	public String type;
	public Integer travelOrderId;
	public Integer taskGoalId;
}

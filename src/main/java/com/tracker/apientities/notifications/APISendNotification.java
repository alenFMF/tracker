package com.tracker.apientities.notifications;

import java.util.List;

public class APISendNotification {
	public String token;
	public List<APIUsers> recipients;  
	public String message;
	public String messageType;
	public String title;
	public String type;
	public Integer travelOrderId;
	public Integer taskGoalId;
}

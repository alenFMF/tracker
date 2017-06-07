package com.tracker.apientities.notifications;

import java.util.Date;

public class APINotificationMessage {
	public Integer messageId;
	public Date timestamp;
	public Date timeRecorded;
	public String title;
	public String message;
	public String messageType;   // text or html
	public String senderId;
	public String receiverId;
	public String travelOrderid;
	public Boolean sent;
}

package com.tracker.apientities.notifications;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class APISendSmsResponse {
	public static class SmsContent
	{
		public String msisdn = null;
		public String messageId = null;
		public String report = null;
		public String status = null;
		public String timeDelivered = null;
		public String timeSent = null;
		public String timeQueue = null;
		public String gateway = null;
		public String message = null;
		public String extMessageId = null;
		
		public SmsContent() {
			
		}
	}

	public String packetId = null;
	public Integer status = null;
	public String statusDescription = null;
	public ArrayList<SmsContent> messages;
	public String error = null;
	public String errorDescription = null;
	public String errorCode = null;
	
	@JsonIgnore
	public boolean isSent() {
		return status != null && status.intValue() == 1;
	}

	public String getPacketId() {
		return packetId;
	}

	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}
	
}

package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class MessageBody extends BaseEntity {
	public String message;
	public String messageType;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
		
}

package com.tracker.apientities.notifications;

import java.util.Date;

import com.tracker.db.EventMessage;

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
	
	public APINotificationMessage() {}
	
	public APINotificationMessage(EventMessage em) {
		this.messageId = em.getId();
		this.timestamp = em.getTimestamp();
		this.timeRecorded = em.getTimeRecorded();
		this.title = em.getTitle();
		if(em.getBody() != null) {
			this.message = em.getBody().getMessage();
			this.messageType = em.getBody().getMessage();
		}
		this.senderId = em.getSender().getUserId();
		if(em.getReceiver() != null) {
			this.receiverId = em.getReceiver().getUserId();
		}
		if(em.getTravelOrder() != null) {
			this.travelOrderid = em.getTravelOrder().getTravelOrderId();
		}
		this.sent = em.getSent();
	}
}

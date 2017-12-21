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
	public String link;
	public String type;
	public String sentTo;
	public String senderGroup;
	public String receiverGroup;
	public Integer pushNotificationId;
	public Boolean markMessageAsRead;
	
	public APINotificationMessage() {}
	
	public APINotificationMessage(EventMessage em, String linkRoot) {
		this.messageId = em.getId();
		this.timestamp = em.getTimestamp();
		this.timeRecorded = em.getTimeRecorded();
		this.title = em.getTitle();
		if(em.getBody() != null) {
			this.message = em.getBody().getMessage();
			this.messageType = em.getBody().getMessageType();
		}
		
		this.senderId = em.getSender() != null ? em.getSender().getUserId() : null;
		if(em.getReceiver() != null) {
			this.receiverId = em.getReceiver().getUserId();
		}
		if(em.getSenderGroup() != null) {
			this.senderGroup = em.getSenderGroup().getGroupId();
		}
		if(em.getReceiverGroup() != null) {
			this.receiverGroup = em.getReceiverGroup().getGroupId();
		}
		if(em.getTravelOrder() != null) {
			this.travelOrderid = em.getTravelOrder().getTravelOrderId();
		}
		this.sent = em.getSent();
		if(em.getBody() != null && em.getBody().getLink() != null && linkRoot != null) {
			this.link = linkRoot + em.getBody().getLink();
		}
		this.type = em.getType();
		if(this.type.equals("EMAIL_TEXT")) {
			this.sentTo = em.getEmailTo();
		}
		if (em.getPushNotificationMessage() != null) {
			this.pushNotificationId = em.getPushNotificationMessage().getId();
			this.markMessageAsRead = em.getPushNotificationMessage().getMarkMessageAsRead();
		}
	}
}

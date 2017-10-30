package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class EventMessage extends BaseEntity {
	public Date timestamp; // time at which event-message is actualized
	public Date timeRecorded;
	public Date receivedOnDevice; // time at which push notifications was received on device
	
	public String type;   
	public String senderNameToBeDisplayed;
	
	@ManyToOne
	public TrackingUser sender;
	@ManyToOne
	public TrackingUser receiver;
	@ManyToOne
	public OrganizationGroup senderGroup;
	@ManyToOne
	public OrganizationGroup receiverGroup;
	
	@ManyToOne
	public TravelOrder travelOrder;
	public Integer travelOrderId = null;
	
	public Boolean sent;    // true if message is of type that is communicated

	public String title;
	
//	public String contextGroupId;
	
	public String emailTo;
	
	public String serviceMessageId = null; // message id from GCM or APNS
	
	public Boolean markMessageAsRead;
	
	@ManyToOne
	public MessageBody body;
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public TrackingUser getSender() {
		return sender;
	}
	public void setSender(TrackingUser sender) {
		this.sender = sender;
	}
	public TrackingUser getReceiver() {
		return receiver;
	}
	public void setReceiver(TrackingUser receiver) {
		this.receiver = receiver;
	}
	public Boolean getSent() {
		return sent;
	}
	public void setSent(Boolean sent) {
		this.sent = sent;
	}
	public MessageBody getBody() {
		return body;
	}
	public void setBody(MessageBody body) {
		this.body = body;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public TravelOrder getTravelOrder() {
		return travelOrder;
	}
	public void setTravelOrder(TravelOrder travelOrder) {
		this.travelOrder = travelOrder;
	}
	public Date getTimeRecorded() {
		return timeRecorded;
	}
	public void setTimeRecorded(Date timeRecorded) {
		this.timeRecorded = timeRecorded;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
//	public String getContextGroupId() {
//		return contextGroupId;
//	}
//	public void setContextGroupId(String contextGroupId) {
//		this.contextGroupId = contextGroupId;
//	}
	public OrganizationGroup getSenderGroup() {
		return senderGroup;
	}
	public void setSenderGroup(OrganizationGroup senderGroup) {
		this.senderGroup = senderGroup;
	}
	public OrganizationGroup getReceiverGroup() {
		return receiverGroup;
	}
	public void setReceiverGroup(OrganizationGroup receiverGroup) {
		this.receiverGroup = receiverGroup;
	}
	public String getEmailTo() {
		return emailTo;
	}
	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}	
	public String getSenderNameToBeDisplayed() {
		return senderNameToBeDisplayed;
	}
	public void setSenderNameToBeDisplayed(String senderNameToBeDisplayed) {
		this.senderNameToBeDisplayed = senderNameToBeDisplayed;
	}
	public Date getReceivedOnDevice() {
		return receivedOnDevice;
	}
	public void setReceivedOnDevice(Date receivedOnDevice) {
		this.receivedOnDevice = receivedOnDevice;
	}
	public Integer getTravelOrderId() {
		return travelOrderId;
	}
	public void setTravelOrderId(Integer travelOrderId) {
		this.travelOrderId = travelOrderId;
	}
	public String getserviceMessageId() {
		return serviceMessageId;
	}
	public void setServiceMessageId(String serviceMessageId) {
		this.serviceMessageId = serviceMessageId;
	}
	public Boolean getMarkMessageAsRead() {
		return markMessageAsRead;
	}
	public void setMarkMessageAsRead(Boolean markMessageAsRead) {
		this.markMessageAsRead = markMessageAsRead;
	}
}

/* Possible message types:
 * NOTIFICATION - notification message 
 * START - start/resume tracking
 * STOP - stop/pause tracking
 * GROUP_NOTIFICATION - comunication is in group context. Receiver can be empty
 * MOBILE_LOG - mobile log (stored on S3).
 */ 

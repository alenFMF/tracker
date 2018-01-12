package com.tracker.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import com.tracker.types.PushNotificationStatus;
import com.tracker.types.GoOptiDriverAssignmentStatus;


@Entity
public class PushNotificationMessage extends BaseEntity {

	public String servicePushNotificationId; // auto generated PN id
	public String sender; // for sms - SmsSenderType or uid of the user who sent it.
	public Date timeDelievered;
	public Date timeSent;
	public Boolean markMessageAsRead;
	public String receipient; // for sms
	public String franchiseUids; // for sms
	public String smsType; // for sms
	public Boolean smsSuccessfullySentSimulatneously;
	public Integer driverAssignmentId; // DriverAssignment id received from GoOpti Back
	public String senderNameToBeDisplayed;
	public String title;
	public String number;
	public Integer travelOrderId; // TravelOrder id received from GoOpti Back
	
	@Column(length=3000)
	public String content; // for sms

	@Enumerated(EnumType.STRING)
	@Column(length=30)
	PushNotificationStatus pushNotificationStatus;
	
//	@ManyToOne
//	EventMessage eventMessage;
//	
	@Enumerated(EnumType.STRING)
	@Column(length=30)
	GoOptiDriverAssignmentStatus driverAssignmentStatus;
	
	public String getServicePushNotificationId() {
		return this.servicePushNotificationId;
	}
	
	public void setServicePushNotificationId(String servicePushNotificationId) {
		this.servicePushNotificationId = servicePushNotificationId;
	}
	
	public String getSender() {
		return this.sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public Date getTimeDelievered() {
		return this.timeDelievered;
	}
	
	public void setTimeDelievered(Date timeDelievered) {
		this.timeDelievered = timeDelievered;
	}
	
	public Date getTimeSent() {
		return this.timeSent;
	}
	
	public void setTimeSent(Date timeSent) {
		this.timeSent = timeSent;
	}
	
	public Boolean getMarkMessageAsRead() {
		return this.markMessageAsRead;
	}
	
	public void setMarkMessageAsRead(Boolean markMessageAsRead) {
		this.markMessageAsRead = markMessageAsRead;
	}
	
	public String getReceipient() {
		return this.receipient;
	}
	
	public void setReceipient(String receipient) {
		this.receipient = receipient;
	}
	
	public String getFranchiseUids() {
		return this.franchiseUids;
	}
	
	public void setFranchiseUids(String franchiseUids) {
		this.franchiseUids = franchiseUids;
	}

	public String getContent() {
		return this.content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public String getSmsType() {
		return this.smsType;
	}
	
	public void setSmsType(String smsType) {
		this.smsType = smsType;
	}
	
	public Boolean getSmsSuccessfullySentSimulatneously() {
		return this.smsSuccessfullySentSimulatneously;
	}
	
	public void setSmsSuccessfullySentSimulatneously(Boolean smsSuccessfullySentSimulatneously) {
		this.smsSuccessfullySentSimulatneously = smsSuccessfullySentSimulatneously;
	}

//	public EventMessage getEventMessage() {
//		return this.eventMessage;
//	}
//	
//	public void setEventMessage(EventMessage eventMessage) {
//		this.eventMessage = eventMessage;
//	}
	
	public Integer getDriverAssignmentId() {
		return this.driverAssignmentId;
	}
	
	public void setDriverAssignmentId(Integer driverAssignmentId) {
		this.driverAssignmentId = driverAssignmentId;
	}

	public String getSenderNameToBeDisplayed() {
		return this.senderNameToBeDisplayed;
	}
	
	public void setSenderNameToBeDisplayed(String senderNameToBeDisplayed) {
		this.senderNameToBeDisplayed = senderNameToBeDisplayed;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public GoOptiDriverAssignmentStatus getGoOptiDriverAssignmentStatus() {
		return this.driverAssignmentStatus;
	}
	
	public void setGoOptiDriverAssignmentStatus(GoOptiDriverAssignmentStatus driverAssignmentStatus) {
		this.driverAssignmentStatus = driverAssignmentStatus;
	}
	
	public PushNotificationStatus getPushNotificationStatus() {
		return this.pushNotificationStatus;
	}
	
	public void setPushNotificationStatus(PushNotificationStatus pushNotificationStatus) {
		this.pushNotificationStatus = pushNotificationStatus;
	}
	
	public String getNumber() {
		return this.number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public Integer getTravelOrderId() {
		return this.travelOrderId;
	}
	
	public void setTravelOrderId(Integer travelOrderId) {
		this.travelOrderId = travelOrderId;
	}
	
}

package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class NotificationRegistration extends BaseEntity {
	
	@ManyToOne
	public TrackingUser user;
	
	@ManyToOne
	public DeviceRecord device;
	
	public String service; // APNS or GCM
	public String notificationToken;
	
	public Date timestamp;
	
	public TrackingUser getUser() {
		return user;
	}
	public void setUser(TrackingUser user) {
		this.user = user;
	}
	public DeviceRecord getDevice() {
		return device;
	}
	public void setDevice(DeviceRecord device) {
		this.device = device;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getNotificationToken() {
		return notificationToken;
	}
	public void setNotificationToken(String notificationToken) {
		this.notificationToken = notificationToken;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public NotificationRegistration() {}
	public NotificationRegistration(TrackingUser user, DeviceRecord device, String service, String notificationToken, Date timestamp) {
		this.user = user;
		this.device = device;
		this.service = service;
		this.notificationToken = notificationToken;
		this.timestamp = timestamp;
	}
}

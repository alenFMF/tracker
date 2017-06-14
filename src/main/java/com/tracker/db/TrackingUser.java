package com.tracker.db;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
public class TrackingUser extends BaseEntity {
	public String userId;
	public String passwordHash; 
	public String name;
	public String email;
	public Date timestamp; 

	@Column(unique = true) 
	public String postingSecret;

	@Column(nullable=false)
	public Boolean admin = false;
	
	public String provider;
	
	public Boolean monitored;
	
	@OneToMany(mappedBy="user")
	private List<UserGroupAssignment> userGroupAssignments;
	
	@OneToOne
	public OrganizationGroup personalGroup;
	
	@OneToMany(mappedBy="user")
	private List<NotificationRegistration> notificationDevices;
	
	@ManyToOne
	private NotificationRegistration primaryNotificationDevice;
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPostingSecret() {
		return postingSecret;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public void setPostingSecret(String postingSecret) {
		this.postingSecret = postingSecret;
	}
	
	public OrganizationGroup getPersonalGroup() {
		return personalGroup;
	}

	public void setPersonalGroup(OrganizationGroup personalGroup) {
		this.personalGroup = personalGroup;
	}
	
	public List<UserGroupAssignment> getUserGroupAssignments() {
		return userGroupAssignments;
	}

	public void setUserGroupAssignments(List<UserGroupAssignment> userGroupAssignments) {
		this.userGroupAssignments = userGroupAssignments;
	}

	public TrackingUser() {}
	
	public TrackingUser(String userId, String password, PasswordEncoder passwordEncoder) {
		this.userId = userId;
		this.passwordHash = hashPassword(password, passwordEncoder);
		this.email = userId;
		this.timestamp = new Date();
	}
	
	public static String hashPassword(String password, PasswordEncoder passwordEncoder) {
		return passwordEncoder.encode(password);
	}
	
	public boolean checkPassword(String password, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(password, this.passwordHash);
	}
	
	public void setPassword(String password, PasswordEncoder passwordEncoder) {
		if(password == null || passwordEncoder == null) {
			this.passwordHash = null;   // no password hash. Login through provider.
			return;
		}
		this.passwordHash = hashPassword(password, passwordEncoder);
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public List<NotificationRegistration> getNotificationDevices() {
		return notificationDevices;
	}

	public void setNotificationDevices(List<NotificationRegistration> notificationDevices) {
		this.notificationDevices = notificationDevices;
	}

	public NotificationRegistration getPrimaryNotificationDevice() {
		return primaryNotificationDevice;
	}

	public void setPrimaryNotificationDevice(NotificationRegistration primaryNotificationDevice) {
		this.primaryNotificationDevice = primaryNotificationDevice;
	}

	public Boolean getMonitored() {
		return monitored;
	}

	public void setMonitored(Boolean monitored) {
		this.monitored = monitored;
	} 
	
}

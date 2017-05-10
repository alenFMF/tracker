package com.tracker.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
public class TrackingUser extends BaseEntity {
	public String userId;
	public String passwordHash; 
	
	@Column(unique = true) 
	public String postingSecret;
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(nullable=false)
	public Boolean admin = false;
	
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

	public TrackingUser() {}
	
	public TrackingUser(String userId, String password, PasswordEncoder passwordEncoder) {
		this.userId = userId;
		this.passwordHash = hashPassword(password, passwordEncoder);
	}
	
	public static String hashPassword(String password, PasswordEncoder passwordEncoder) {
		return passwordEncoder.encode(password);
	}
	
	public boolean checkPassword(String password, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(password, this.passwordHash);
	}
	
	public void setPassword(String password, PasswordEncoder passwordEncoder) {
		this.passwordHash = hashPassword(password, passwordEncoder);
	} 
}

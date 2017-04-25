package com.tracker.db;

import javax.persistence.Entity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
public class TrackingUser extends BaseEntity {
	public String userId;
	public String passwordHash; 
	
	public TrackingUser() {}
	
	public TrackingUser(String userId, String password, PasswordEncoder passwordEncoder) {
		this.userId = userId;
		this.passwordHash = hashPassword(password, passwordEncoder);
	}
	
	public static String hashPassword(String password, PasswordEncoder passwordEncoder) {
		//Todo properly
		return passwordEncoder.encode(password);
	}
	
	public boolean checkPassword(String password, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(password, this.passwordHash);
	}
	
	public void setPassword(String password, PasswordEncoder passwordEncoder) {
		this.passwordHash = hashPassword(password, passwordEncoder);
	} 
}

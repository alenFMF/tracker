package com.tracker.apientities.user;

public class APIPropertyStatus {
	public String key;
	public String status;
	public String errorMessage;
	
	public APIPropertyStatus(String key, String status, String errorMessage) {
		this.key = key;
		this.status = status;
		this.errorMessage = errorMessage;
	}
}

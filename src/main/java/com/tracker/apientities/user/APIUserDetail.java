package com.tracker.apientities.user;

public class APIUserDetail {
	public String userId;
	public Boolean admin;
	public String provider;
	
	public APIUserDetail(String userId, Boolean admin, String provider) {
		this.userId = userId;
		this.admin = admin;
		this.provider = provider;
	}
}

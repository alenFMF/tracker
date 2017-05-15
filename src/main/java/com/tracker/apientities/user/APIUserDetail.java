package com.tracker.apientities.user;

public class APIUserDetail {
	public String userId;
	public Boolean admin;
	
	public APIUserDetail(String userId, Boolean admin) {
		this.userId = userId;
		this.admin = admin;
	}
}

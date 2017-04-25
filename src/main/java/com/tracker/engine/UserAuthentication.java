package com.tracker.engine;

import java.util.Date;

public class UserAuthentication {
	public String userId;
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date validUntil;
	public String token;
	
	public UserAuthentication() {
		this.validUntil = new Date();
	}
}

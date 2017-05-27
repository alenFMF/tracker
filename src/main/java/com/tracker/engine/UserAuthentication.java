package com.tracker.engine;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class UserAuthentication {
	private static int tokenDurationInMinutes;
	private String userId;
	private String provider;
	private Date validUntil;
	private String token;
	
	public String getUserId() {
		return userId;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public String getToken() {
		return token;
	}

	public boolean isValid() {
		return this.validUntil.after(new Date());
	}
	
	public UserAuthentication(String userId, String token, String provider) {
		this.userId = userId;
		this.token = token;
		this.provider = provider;
		this.validUntil = DateUtils.addMinutes(new Date(), UserAuthentication.tokenDurationInMinutes);
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
}

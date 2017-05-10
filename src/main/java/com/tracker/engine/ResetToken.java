package com.tracker.engine;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class ResetToken {
	static int tokenDurationInHours = 2;
	public String userId;
	public Date validUntil;
	public String token;
	
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
	
	public ResetToken(String userId, String token) {
		this.userId = userId;
		this.token = token;
		this.validUntil = DateUtils.addHours(new Date(), ResetToken.tokenDurationInHours);
	}
}

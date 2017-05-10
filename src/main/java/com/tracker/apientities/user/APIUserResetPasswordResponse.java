package com.tracker.apientities.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tracker.apientities.APIBaseResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIUserResetPasswordResponse extends APIBaseResponse {
	public String resetToken;
	public String userId;
	
	public APIUserResetPasswordResponse(String userId, String token) {
		this.userId = null;
		this.resetToken = token;
	}	
}

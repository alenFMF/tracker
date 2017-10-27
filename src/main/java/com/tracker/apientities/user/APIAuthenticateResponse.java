package com.tracker.apientities.user;

import com.tracker.apientities.APIBaseResponse;

public class APIAuthenticateResponse extends APIBaseResponse {
	public String token;
	public Boolean primaryDeviceOverride;
	public Boolean monitored;
	public String postingSecret;
	public String userId;
	public Boolean isPrimaryDevice;
	
	public APIAuthenticateResponse(String token) {
		this.token = token;
		if(token == null) {
			this.status = "AUTH_ERROR";
		}
	}
	
	public APIAuthenticateResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}		
}

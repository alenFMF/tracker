package com.tracker.apientities.user;

import com.tracker.apientities.APIBaseResponse;

public class APIAuthenticateResponse extends APIBaseResponse {
	public String token;
	
	public APIAuthenticateResponse(String token) {
		this.token = token;
		if(token == null) {
			this.status = "AUTH_ERROR";
		}
	}
}

package com.tracker.apientities;

public class APIAuthenticateResponse extends APIBaseResponse {
	public String token;
	
	public APIAuthenticateResponse(String token) {
		this.token = token;
		if(token == null) {
			this.status = "AUTH_ERROR";
		}
	}
}

package com.tracker.apientities.user;

import com.tracker.apientities.APIBaseResponse;

public class APIUserSecretResponse extends APIBaseResponse{
	public String secret;
	
	public APIUserSecretResponse() {
		super();
	}

	
	public APIUserSecretResponse(String secret) {
		super();
		this.secret = secret;
	}
	
	public APIUserSecretResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
	
}

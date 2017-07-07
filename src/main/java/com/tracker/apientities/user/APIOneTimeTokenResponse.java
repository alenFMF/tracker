package com.tracker.apientities.user;

import com.tracker.apientities.APIBaseResponse;

public class APIOneTimeTokenResponse extends APIBaseResponse {
	public String oneTimeToken;
	
	public APIOneTimeTokenResponse() {
		super();
	}
	public APIOneTimeTokenResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
	public APIOneTimeTokenResponse(String token) {
		super();
		this.oneTimeToken = token;
	}		
}

package com.tracker.apientities.user;

import com.tracker.apientities.APIBaseResponse;

public class APIUserUpdateResponse extends APIBaseResponse{
	public String token;
	
	public APIUserUpdateResponse() {
		super();
	}
	
	public APIUserUpdateResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
}

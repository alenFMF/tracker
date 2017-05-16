package com.tracker.apientities.user;

import com.tracker.apientities.APIBaseResponse;

public class APIUserRegisterResponse extends APIBaseResponse{
	public String token;
	
	public APIUserRegisterResponse() {
		super();
	}
	
	public APIUserRegisterResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
}

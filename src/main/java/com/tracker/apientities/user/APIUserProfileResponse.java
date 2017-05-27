package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIUserProfileResponse extends APIBaseResponse {
	public String userId;
	public String postingSecret;
	public Boolean isAdmin;
	public String personalGroup;
	public String provider;
	public List<String> adminGroups;
	public List<String> userGroups;
	
	public APIUserProfileResponse() {}
	public APIUserProfileResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
}

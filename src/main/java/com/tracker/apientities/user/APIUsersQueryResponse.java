package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIUsersQueryResponse extends APIBaseResponse {
	public List<APIUserDetail> users;
	
	public APIUsersQueryResponse(List<APIUserDetail> users) {
		super();
		this.users = users;
	}
	
	public APIUsersQueryResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
}

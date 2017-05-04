package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIUsersQueryResponse extends APIBaseResponse {
	public List<String> users;
	
	public APIUsersQueryResponse(List<String> users) {
		this.users = users;
	}

}

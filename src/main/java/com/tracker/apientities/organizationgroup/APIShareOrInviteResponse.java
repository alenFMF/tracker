package com.tracker.apientities.organizationgroup;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIShareOrInviteResponse extends APIBaseResponse {
	public List<String> statuses;
	
	public APIShareOrInviteResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
	public APIShareOrInviteResponse(List<String> statuses) {
		super();
		this.statuses = statuses;
	}	
}

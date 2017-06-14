package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIPropertySetResponse extends APIBaseResponse{
	public List<APIPropertyStatus> statuses;
	public APIPropertySetResponse() {
		super();
	}
	public APIPropertySetResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
	public APIPropertySetResponse(List<APIPropertyStatus> statuses) {
		super();
		this.statuses = statuses;
	}	
}

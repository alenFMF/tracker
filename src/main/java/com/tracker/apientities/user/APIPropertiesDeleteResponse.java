package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIPropertiesDeleteResponse extends APIBaseResponse{
	public List<APIPropertyStatus> statuses;
	public APIPropertiesDeleteResponse() {
		super();
	}
	public APIPropertiesDeleteResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
	public APIPropertiesDeleteResponse(List<APIPropertyStatus> statuses) {
		super();
		this.statuses = statuses;
	}	
}

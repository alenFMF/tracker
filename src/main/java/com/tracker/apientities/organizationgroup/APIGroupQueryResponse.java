package com.tracker.apientities.organizationgroup;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIGroupQueryResponse extends APIBaseResponse {
	public List<APIGroupDetail> groups;
	
	public APIGroupQueryResponse() {
		super();
	}
	
	public APIGroupQueryResponse(List<APIGroupDetail> groups) {
		super();
		this.groups = groups;
	}
	
	public APIGroupQueryResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
}

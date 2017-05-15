package com.tracker.apientities.organizationgroup;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIUserGroupAssignmentResponse extends APIBaseResponse {
	public List<APIUserGroupAssignmentDetail> assignments;
	
	public APIUserGroupAssignmentResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
	public APIUserGroupAssignmentResponse(List<APIUserGroupAssignmentDetail> assignments) {
		super();
		this.assignments = assignments;
	}		
}

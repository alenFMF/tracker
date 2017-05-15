package com.tracker.apientities.organizationgroup;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIUserGroupAssignmentUpdateResponse extends APIBaseResponse  {
	public List<String> confirmStatuses;
	public List<String> rejectStatuses;
	
	public APIUserGroupAssignmentUpdateResponse() {
		super();
	}
	
	public APIUserGroupAssignmentUpdateResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}		
}

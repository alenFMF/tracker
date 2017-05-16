package com.tracker.apientities.organizationgroup;

import java.util.Date;
import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIUserGroupTimeIntervalResponse extends APIBaseResponse{
	public Date[][] timeInterval;

	public APIUserGroupTimeIntervalResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
	
	public APIUserGroupTimeIntervalResponse(Date[][] timeInterval) {
		super();
		this.timeInterval = timeInterval;
	}		
	
}

package com.tracker.apientities.vehicle;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIVehicleProfileResponse extends APIBaseResponse{

	public String description;
	public String groupId;
	public List<String> groups;

	
	public APIVehicleProfileResponse(String description, String groupId, List<String> groups) {
		this.description = description;
		this.groupId = groupId;
		this.groups = groups;
	}


	public APIVehicleProfileResponse(String string, String string2) {
		super(string, string2);
	}
	
//	public APIVehicleProfileResponse(String status, String errorMessage) {
//		super(status, errorMessage);
//	}

	
}

package com.tracker.apientities.vehicle;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.db.VehicleGroupAssignment;

public class APIVehicleLinkListResponse extends APIBaseResponse {
	public List<VehicleGroupAssignment> VehicleGroupAssignments;
	
	
	
	public APIVehicleLinkListResponse(List<VehicleGroupAssignment> vehicleGroupAssignments) {
		this.VehicleGroupAssignments = vehicleGroupAssignments;
	}

	public APIVehicleLinkListResponse(String string, String string2) {
		super(string, string2);
	}




}

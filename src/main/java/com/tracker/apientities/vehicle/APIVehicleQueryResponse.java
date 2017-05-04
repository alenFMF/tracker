package com.tracker.apientities.vehicle;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIVehicleQueryResponse extends APIBaseResponse {


	public List<APIVehicleListEntry> vehicles; 
	
	public APIVehicleQueryResponse(List<APIVehicleListEntry> vehicles) {
		this.vehicles = vehicles;
	}
}

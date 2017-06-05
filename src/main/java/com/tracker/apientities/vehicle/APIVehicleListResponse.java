package com.tracker.apientities.vehicle;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIVehicleListResponse extends APIBaseResponse{
	public List<String> vehicles;
	public APIVehicleListResponse(List<String> vehicles){
		this.vehicles = vehicles;
	}
	public APIVehicleListResponse(String status, String errorMessage) {
		this.status = status;
		this.error_message = errorMessage;
	}
	
}

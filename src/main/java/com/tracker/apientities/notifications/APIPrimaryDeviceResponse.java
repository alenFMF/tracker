package com.tracker.apientities.notifications;

import com.tracker.apientities.APIBaseResponse;

public class APIPrimaryDeviceResponse extends APIBaseResponse {
	public String platform;
	public String model;
	public String manufacturer;
	public String uuid;

	public APIPrimaryDeviceResponse() {
		super();
	}
	
	public APIPrimaryDeviceResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
}

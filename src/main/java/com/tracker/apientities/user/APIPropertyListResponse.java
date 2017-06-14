package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIPropertyListResponse extends APIBaseResponse{
	public List<APIProperty> properties;
	public String provider;
	
	public APIPropertyListResponse() {
		super();
	}
	public APIPropertyListResponse(List<APIProperty> properties, String provider) {
		super();
		this.properties = properties;
		this.provider = provider;
	}
	public APIPropertyListResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}		
}

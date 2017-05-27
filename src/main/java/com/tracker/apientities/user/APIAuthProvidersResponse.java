package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;

public class APIAuthProvidersResponse extends APIBaseResponse{
	public List<String> providers;
	
	public APIAuthProvidersResponse() {
		super();
	}
	public APIAuthProvidersResponse(List<String> providers) {
		super();
		this.providers = providers;
	}
}

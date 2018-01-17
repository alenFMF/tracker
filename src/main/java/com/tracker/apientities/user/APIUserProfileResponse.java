package com.tracker.apientities.user;

import java.util.List;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.notifications.APIRegistredDevice;

public class APIUserProfileResponse extends APIBaseResponse {
	public String userId;
	public String postingSecret;
	public Boolean isAdmin;
	public String personalGroup;
	public String provider;
//	public List<String> adminGroups;
//	public List<String> userGroups;
	public List<APIRegistredDevice> devices;
	public APIRegistredDevice primaryDevice;
	public Boolean monitored;
	public String name;
	public String primaryDeviceName;
	public String fewDevicesName;
	
	public APIUserProfileResponse() {
		super();
	}
	public APIUserProfileResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}
}

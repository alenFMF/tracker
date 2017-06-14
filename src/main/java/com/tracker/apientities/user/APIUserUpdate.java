package com.tracker.apientities.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIUserUpdate {
	public String token;
	public String userId;
	public String provider;
	public String oldPassword;
	public String newPassword;
	public Boolean resetSecret;
	public Boolean makeAdmin;
	public Boolean monitored;
}

package com.tracker.apientities.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIUserUpdate {
	public String token;
	public String userId;
	public String oldPassword;
	public String newPassword;
	public Boolean resetSecret;
	public Boolean makeAdmin;
	public String setProvider;  // if not null, new password should be password for provider.
}

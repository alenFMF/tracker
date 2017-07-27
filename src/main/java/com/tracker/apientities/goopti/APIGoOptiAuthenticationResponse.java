package com.tracker.apientities.goopti;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIGoOptiAuthenticationResponse extends APIGoOptiAuthentication {
	public String status;	
	public String token;
	public String name;
	public String surname;
	public String email;
	public String phone;
	public String locale;
	public String currency;	
	public boolean isLocked = false;
	public List<APIGoOptiFranchiseRole> franchiseRoles;
}

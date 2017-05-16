package com.tracker.apientities.goopti;

public class APIGoOptiAuthenticationResponse extends APIGoOptiAuthentication {
	public String token;
	public String name;
	public String surname;
	public String email;
	public String phone;
	public String locale;
	public String currency;	
	public boolean isLocked = false;
}

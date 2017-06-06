package com.tracker.apientities.goopti;

public class APIGoOptiAuthentication {
	public String username;
	public String password;
	public String provider;
	
	public APIGoOptiAuthentication() {}
	public APIGoOptiAuthentication(String username, String password) {
		this.username = username;
		this.password = password;
		this.provider = "GOOPTI";
	}
 }

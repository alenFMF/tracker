package com.tracker.engine;

import java.util.List;

public class AuthenticationObject {
	private String status;
	private String errorMessage;
	private String token;
	private String name;
	private String username;
	private String email;
	private List<ProviderGroupRoles> groupRoles;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<ProviderGroupRoles> getGroupRoles() {
		return groupRoles;
	}
	public void setGroupRoles(List<ProviderGroupRoles> groupRoles) {
		this.groupRoles = groupRoles;
	}
}

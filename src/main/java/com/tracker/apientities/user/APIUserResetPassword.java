package com.tracker.apientities.user;

import io.swagger.annotations.ApiModelProperty;

public class APIUserResetPassword {
	public String userId;
	public String resetToken;
	public String newPassword;
	public String token;
	
	@ApiModelProperty(value = "User ID must be provided to trigger reset password generation.")	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@ApiModelProperty(value = "Valid token, a new password and authentication token belonging to an admin must be provided to change the password.")	
	public String getResetToken() {
		return resetToken;
	}
	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	@ApiModelProperty(value = "Authentication token. Must belong to a user with administrator privileges.")	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	
	
}

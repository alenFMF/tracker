package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class AppConfiguration extends BaseEntity{
	public Integer identifier; 
	public String resetPasswordSecret;
	public Integer getIdentifier() {
		return identifier;
	}
	public void setIdentifier(Integer identifier) {
		this.identifier = identifier;
	}
	public String getResetPasswordSecret() {
		return resetPasswordSecret;
	}
	public void setResetPasswordSecret(String resetPasswordSecret) {
		this.resetPasswordSecret = resetPasswordSecret;
	}
}

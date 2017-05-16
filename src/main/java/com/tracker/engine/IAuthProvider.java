package com.tracker.engine;

public interface IAuthProvider {
	public AuthenticationObject authenticate(String username, String password);
}

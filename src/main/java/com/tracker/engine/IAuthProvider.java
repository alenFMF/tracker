package com.tracker.engine;

import com.tracker.db.TrackingUser;
import com.tracker.utils.SessionKeeper;

public interface IAuthProvider {
	public AuthenticationObject authenticate(String username, String password);
	public void updateRoles(SessionKeeper sk, TrackingUser user, AuthenticationObject auth);
	public String getKey();
}

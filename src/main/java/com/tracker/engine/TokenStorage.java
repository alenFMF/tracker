package com.tracker.engine;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.criterion.Restrictions;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tracker.db.TrackingUser;
import com.tracker.utils.SessionKeeper;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TokenStorage {
	private Map<String, UserAuthentication> tokenToAuth = new ConcurrentHashMap<>();	
	private Map<String, UserAuthentication> userToAuth = new ConcurrentHashMap<>();	
	private SecureRandom generator;
	private int tokenLength = 24;
	
	public TokenStorage() throws NoSuchAlgorithmException {
		generator = SecureRandom.getInstance("SHA1PRNG");
	}
	
	private void invalidate(UserAuthentication auth) {
		userToAuth.remove(auth.userId);
		tokenToAuth.remove(auth.token);					
	}
	
	private boolean checkOrInvalidate(UserAuthentication auth) {
		if(auth.validUntil.after(new Date())) {
			invalidate(auth);
			return false;
		}		
		return true;
	}
	public String authenticatedUserForToken(String token) {
		if(!tokenToAuth.containsKey(token)) {
			return null;
		}
		UserAuthentication auth = tokenToAuth.get(token);
		if(!checkOrInvalidate(auth)) {
			return null;
		}
		return auth.userId;
	}
	
	public boolean isAuthenticated(String userId) {
		return userToAuth.containsKey(userId)
					&& checkOrInvalidate(userToAuth.get(userId));
	}
	
	private String reauthenticateUser(String userId) {	
		UserAuthentication auth = new UserAuthentication();
		String token = generateToken();
		auth.setUserId(userId); 
		auth.setToken(token);
		userToAuth.put(userId, auth);
		tokenToAuth.put(token, auth);
		return token;
	}
	
	public String authenticate(SessionKeeper sk, String userId, String password, PasswordEncoder passwordEncoder) {
		TrackingUser user = (TrackingUser)sk.createCriteria(TrackingUser.class).add(Restrictions.eq("userId", userId)).uniqueResult();
		if(user == null) return null;
		if(!user.checkPassword(password, passwordEncoder)) return null;
		return reauthenticateUser(userId);
	}
	
	public String generateToken() {		
		String token = new BigInteger(this.tokenLength*4, this.generator).toString(16);
		return String.format("%1$" + this.tokenLength + "s", token).replace(' ', '0');
	}
	
}

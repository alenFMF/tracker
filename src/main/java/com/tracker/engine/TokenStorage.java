package com.tracker.engine;

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
	private Map<String, ResetToken> passwordResetTokens = new ConcurrentHashMap<>();
	private SecureRandom generator;
	private int tokenLength = 24;
	
	public TokenStorage() throws NoSuchAlgorithmException {
		generator = SecureRandom.getInstance("SHA1PRNG");
	}
	
	private void invalidate(UserAuthentication auth) {
		userToAuth.remove(auth.getUserId());
		tokenToAuth.remove(auth.getToken());					
	}
	
	private void invalidate(ResetToken rt) {
		passwordResetTokens.remove(rt.getToken());		
	}	
	
	private boolean checkOrInvalidate(UserAuthentication auth) {
		if(auth.isValid()) {
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
		return auth.getUserId();
	}
	
	public boolean isAuthenticated(String userId) {
		return userToAuth.containsKey(userId)
					&& checkOrInvalidate(userToAuth.get(userId));
	}
	
	private String reauthenticateUser(String userId) {	
		String token = generateToken();
		UserAuthentication auth = new UserAuthentication(userId, token);
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
	
	public String passwordResetUser(String userId) {
		// user must exist!
		String token = generateToken();
		ResetToken rt = new ResetToken(userId, token);
		passwordResetTokens.put(token, rt);
		return token;
	}
	

	public String userIdForResetToken(String token) {	
		ResetToken rt = passwordResetTokens.get(token);
		if(rt == null) return null;
		if(!rt.isValid()) {
			invalidate(rt);
			return null;
		}
		return rt.getUserId();
	}
	
	public void clearPasswordResetToken(String token) {
		passwordResetTokens.remove(token);
	}
}

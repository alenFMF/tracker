package com.tracker.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tracker.db.TrackingUser;
import com.tracker.utils.SessionKeeper;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TokenStorage {
	private Map<String, UserAuthentication> tokenToAuth = new ConcurrentHashMap<>();	
	private Map<String, ResetToken> passwordResetTokens = new ConcurrentHashMap<>();
	private SecureRandom generator;
	private int tokenLength = 24;
	
	public TokenStorage() throws NoSuchAlgorithmException {
		generator = SecureRandom.getInstance("SHA1PRNG");
	}
	
	private void invalidate(UserAuthentication auth) {
		tokenToAuth.remove(auth.getToken());					
	}
	
	private void invalidate(ResetToken rt) {
		passwordResetTokens.remove(rt.getToken());		
	}	
	
	private boolean checkOrInvalidate(UserAuthentication auth) {
		if(!auth.isValid()) {
			invalidate(auth);
			return false;
		}		
		return true;
	}
	
	
	public UserAuthentication authenticatedUserForToken(String token) {
		if(!tokenToAuth.containsKey(token)) {
			return null;
		}
		UserAuthentication auth = tokenToAuth.get(token);
		if(!checkOrInvalidate(auth)) {
			return null;
		}
		return auth;
	}
		
	private String reauthenticateUser(String userId, String provider) {	
		String token = null;
		while(true) {   // ensure unique token
			token = generateToken();
			if(!tokenToAuth.containsKey(token)) break;
		}
		
		UserAuthentication auth = new UserAuthentication(userId, token, provider);
		tokenToAuth.put(token, auth);
		return token;
	}
	
	public String authenticate(SessionKeeper sk, TrackingUser user, String password, PasswordEncoder passwordEncoder, AuthProviderFactory authFactory, String providerId) {
		if(providerId == null) {
			if(!user.checkPassword(password, passwordEncoder)) return null;
			return reauthenticateUser(user.getUserId(), null);
		}
		
		IAuthProvider provider = authFactory.getProvider(providerId);
		if(provider == null) {
			return null;
		}
		AuthenticationObject authObj = provider.authenticate(user.getUserId(), password);
		if(!authObj.getStatus().equals("OK")) {
			return null;
		}
		String token = authenticateUserWithTokenFromProvider(user.getUserId(), authObj.getToken(), provider.getKey());
		provider.updateRoles(sk, user, authObj);
		return token;
	}
	
	public String authenticateUserWithTokenFromProvider(String userId, String token, String provider) {
		if(tokenToAuth.containsKey(token)) return null; // duplicate token, reject
		UserAuthentication auth = new UserAuthentication(userId, token, provider);
//		userToAuth.put(userId, auth);
		tokenToAuth.put(token, auth);
		return token;
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

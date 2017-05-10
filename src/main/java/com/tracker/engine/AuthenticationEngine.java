package com.tracker.engine;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.devices.APIDeviceQuery;
import com.tracker.apientities.devices.APIDeviceResponse;
import com.tracker.apientities.user.APIAuthenticateRequest;
import com.tracker.apientities.user.APIAuthenticateResponse;
import com.tracker.apientities.user.APIUserProfile;
import com.tracker.apientities.user.APIUserProfileResponse;
import com.tracker.apientities.user.APIUserRegisterRequest;
import com.tracker.apientities.user.APIUserResetPassword;
import com.tracker.apientities.user.APIUserUpdate;
import com.tracker.apientities.user.APIUsersQuery;
import com.tracker.apientities.user.APIUsersQueryResponse;
import com.tracker.db.DeviceRecord;
import com.tracker.db.TrackingUser;
import com.tracker.utils.SessionKeeper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AuthenticationEngine {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private TokenStorage tokens = null;
	
	private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	public AuthenticationEngine() throws NoSuchAlgorithmException  {
		this.tokens = new TokenStorage();
	}
	
	public APIBaseResponse registerUser(APIUserRegisterRequest req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			TrackingUser user = (TrackingUser) sk.createCriteria(TrackingUser.class).add(Restrictions.eq("userId", req.userId)).uniqueResult();
			if(user == null) {
				user = new TrackingUser(req.userId, req.password, passwordEncoder);
				sk.save(user);
				sk.commit();
				return new APIBaseResponse();
			}
			return new APIBaseResponse("USER_EXISTS", "");
		}		
	}
	
	public APIBaseResponse resetPassword(APIUserResetPassword req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser user = getUser(sk, req.userId);
			if(user == null) {		
				return new APIBaseResponse("NO_SUCH_USER", "");
			}
			user.setPassword(req.newPassword, passwordEncoder);
			sk.saveOrUpdate(user);	
			sk.commit();
//			if(req.resetToken != null && tokens.checkPasswordResetToken(req.userId, req.resetToken)) {
//				if(!this.isPasswordOk(req.newPassword)) {
//					return new APIBaseResponse("PASSWORD_NOT_COMPLEX_ENOUGH", "");
//				}
//				user.setPassword(req.newPassword, passwordEncoder);
//				tokens.clearPasswordResetToken(req.resetToken);
//				return new APIBaseResponse();
//			}
//			String resetToken = tokens.passwordResetUser(user.getUserId());
//			sendTokenLinkEmail(user.getUserId(), resetToken);
			
		}		
		return new APIBaseResponse();
	}
	
	private void sendTokenLinkEmail(String userId, String resetToken) {
		//TODO
	}
	
	private APIBaseResponse processPasswordChange(TrackingUser user, TrackingUser tokenUser, String oldPassword, String newPassword) {
		if(newPassword == null) {
			return null;
		}
		if(!this.isPasswordOk(newPassword)) {
				return new APIBaseResponse("PASSWORD_NOT_COMPLEX_ENOUGH", "");
		}
		
		if(tokenUser != null && tokenUser.getAdmin() == true) {
			user.setPassword(newPassword, passwordEncoder);
			return null;
		} 
				
		if(tokenUser != null && user != null && tokenUser.getUserId() == user.getUserId()) {
			if(user.checkPassword(oldPassword, passwordEncoder)) {
				user.setPassword(newPassword, passwordEncoder);
				return null;
			} 
			return new APIBaseResponse("OLD_PASSWORD_WRONG", "");			
		} 
		// do nothing
		return null;
	}

	private APIBaseResponse processSecretChange(TrackingUser user, TrackingUser tokenUser, String secret) {
		if(secret == null) return null;
		
		if(!this.isSecretOk(secret)) {
			return new APIBaseResponse("SECRET_NOT_OK", "");
		}
			
		if(tokenUser != null && tokenUser.getAdmin() == true) {
			user.setPostingSecret(secret);
			return null;
		} 
		if(user != null && tokenUser != null) { 
			if(user.getUserId() == tokenUser.getUserId()) {
				user.setPostingSecret(secret);
				return null;		
			}
			return new APIBaseResponse("SECRET_CHANGE_DENIED", "Token must match userId or token not of an admin.");
		}
		return null;
	}	
	
	private APIBaseResponse processMakeAdmin(TrackingUser user, TrackingUser tokenUser, boolean makeAdmin) {
		if(tokenUser != null && tokenUser.getAdmin()) {
			user.setAdmin(true);
			return null;
		} 	
		return new APIBaseResponse("SET_ADMIN_DENIED", "Admin status can be changed by admin only.");		
	}
	
	public TrackingUser getUser(SessionKeeper sk, String userId) {
		return (TrackingUser) sk.createCriteria(TrackingUser.class).add(Restrictions.eq("userId", userId)).uniqueResult();
	}
	
	public TrackingUser getTokenUser(SessionKeeper sk, String token) {
		if(token == null) return null;
		String tokenUserId = this.tokens.authenticatedUserForToken(token);
		return getUser(sk, tokenUserId);		
	}
	
	public APIBaseResponse update(APIUserUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser user = getUser(sk, req.userId);
			if(user == null) {		
				return new APIBaseResponse("NO_SUCH_USER", "");
			}		

			TrackingUser tokenUser = getTokenUser(sk, req.token);
			
			//password change
			APIBaseResponse resp = processPasswordChange(user, tokenUser, req.oldPassword, req.newPassword);
			if(resp != null) return resp;
			
			// secret change
			resp = processSecretChange(user, tokenUser, req.secret);
			if(resp != null) return resp;
			
			// admin change
			if(req.makeAdmin != null) {
				resp = processMakeAdmin(user, tokenUser, req.makeAdmin);
				if(resp != null) return resp;
			}

			sk.saveOrUpdate(user);	
			sk.commit();
		}		
		return new APIBaseResponse();
	}	
	
	private boolean isPasswordOk(String password) {
		return password.length() > 5;
	} 
	
	private boolean isSecretOk(String secret) {
		return 	secret.length() > 8;
	}
	
	public APIAuthenticateResponse authenticate(APIAuthenticateRequest req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {				
			return new APIAuthenticateResponse(tokens.authenticate(sk, req.userId, req.password, passwordEncoder)); 
		}			
	}
	
	
	@SuppressWarnings("unchecked")
	public APIBaseResponse userProfile(APIUserProfile req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser user = getUser(sk, req.userId);
			if(user == null) return new APIBaseResponse("WRONG_USERID", ""); 
			
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) return new APIBaseResponse("INVALID_AUTH_TOKEN", "");
			
			APIUserProfileResponse resp = new APIUserProfileResponse();
			resp.userId = user.getUserId();
			resp.isAdmin = user.getAdmin();
			resp.postingSecret = user.getPostingSecret();
			resp.adminGroups = null;
			resp.userGroups = null;
			resp.personalGroup = null;
			return resp;
		}
	}	
	@SuppressWarnings("unchecked")
	public APIUsersQueryResponse listUsers(APIUsersQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			Criteria c = sk.createCriteria(TrackingUser.class);						
			List<TrackingUser> recs = c.list();	
			List<String> users = recs.stream()
				.map(x -> x.userId)
				.collect(Collectors.toList());
			APIUsersQueryResponse res = new APIUsersQueryResponse(users);
			return res;
		}
	}	
}

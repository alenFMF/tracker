package com.tracker.engine;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.user.APIAuthenticateRequest;
import com.tracker.apientities.user.APIAuthenticateResponse;
import com.tracker.apientities.user.APIUserDetail;
import com.tracker.apientities.user.APIUserProfile;
import com.tracker.apientities.user.APIUserProfileResponse;
import com.tracker.apientities.user.APIUserRegisterRequest;
import com.tracker.apientities.user.APIUserResetPassword;
import com.tracker.apientities.user.APIUserResetPasswordResponse;
import com.tracker.apientities.user.APIUserSecret;
import com.tracker.apientities.user.APIUserSecretResponse;
import com.tracker.apientities.user.APIUserUpdate;
import com.tracker.apientities.user.APIUsersQuery;
import com.tracker.apientities.user.APIUsersQueryResponse;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingUser;
import com.tracker.db.UserGroupAssignment;
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

				Date now = new Date();
				OrganizationGroup group = new OrganizationGroup();
				group.setGroupId(req.userId);
				group.setDescription(req.userId);
				group.setCreator(user); 
				group.setPersonalGroupUser(user);
				user.setPersonalGroup(group);
				group.setTimestamp(now);
				
				UserGroupAssignment asgn = new UserGroupAssignment();
				asgn.setAsPersonalGroup(user, group, now);
				
				sk.save(user);
				sk.save(group);
				sk.save(asgn);
				sk.commit();
				return new APIBaseResponse();
			}
			return new APIBaseResponse("USER_EXISTS", "");
		}		
	}
	
	public APIUserResetPasswordResponse resetPassword(APIUserResetPassword req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser user = null;
			
			if(req.resetToken == null) {
				user = getUser(sk, req.userId);
				if(user == null) {		
					return new APIUserResetPasswordResponse("NO_SUCH_USER", "");
				}				
				String resetToken = tokens.passwordResetUser(req.userId);
				APIUserResetPasswordResponse res = new APIUserResetPasswordResponse();
				res.userId = null;
				res.resetToken = resetToken;
				return res;
			}
			
			if(req.resetToken != null) {
				String userId = tokens.userIdForResetToken(req.resetToken);
				if(req.token == null) {
					return new APIUserResetPasswordResponse("AUTH_TOKEN_MISSING", "");					
				}
				TrackingUser tokenUser = getTokenUser(sk, req.token);
				if(tokenUser == null) {
					return new APIUserResetPasswordResponse("WRONG_TOKEN", "");										
				}
				if(!tokenUser.getAdmin()) {
					return new APIUserResetPasswordResponse("TOKEN_NOT_OF_ADMIN", "");
				}
				if(userId == null) {
					return new APIUserResetPasswordResponse("INVALID_OR_EXPIRED_RESET_TOKEN", "");
				}
				if(!this.isPasswordOk(req.newPassword)) {
					return new APIUserResetPasswordResponse("PASSWORD_NOT_COMPLEX_ENOUGH", "");
				}
				user = getUser(sk, userId);
				user.setPassword(req.newPassword, passwordEncoder);
				tokens.clearPasswordResetToken(req.resetToken);				
				sk.saveOrUpdate(user);	
				sk.commit();
				APIUserResetPasswordResponse res = new APIUserResetPasswordResponse();
				res.userId = userId;
				res.resetToken = null;
				return res;
			}			
		}		
		return new APIUserResetPasswordResponse();
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
				
		if(tokenUser != null && user != null && tokenUser.getUserId().equals(user.getUserId())) {
			if(user.checkPassword(oldPassword, passwordEncoder)) {
				user.setPassword(newPassword, passwordEncoder);
				return null;
			} 
			return new APIBaseResponse("OLD_PASSWORD_WRONG", "");			
		} 
		// do nothing
		return null;
	}

	private APIBaseResponse processSecretChange(SessionKeeper sk, TrackingUser user, TrackingUser tokenUser, Boolean secretChange) {
		if(secretChange == null || !secretChange) {
			return null;
		}
		String secret = tokens.generateToken();	
		if(tokenUser != null && tokenUser.getAdmin() == true) {
			user.setPostingSecret(secret);
			return null;
		} 
		if(user != null && tokenUser != null) { 
			if(user.getUserId().equals(tokenUser.getUserId())) {
				user.setPostingSecret(secret);
				return null;		
			}
			return new APIBaseResponse("SECRET_CHANGE_DENIED", "Token must match userId or token not of an admin.");
		}
		return null;
	}	
	
	private APIBaseResponse processMakeAdmin(TrackingUser user, TrackingUser tokenUser, boolean makeAdmin) {
		if(tokenUser != null && tokenUser.getAdmin()) {
			user.setAdmin(makeAdmin);
			return null;
		} 	
		return new APIBaseResponse("SET_ADMIN_DENIED", "Admin status can be changed by admin only.");		
	}
	
	public TrackingUser getUser(SessionKeeper sk, String userId) {
		if(userId == null) return null;
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
			
			// secret change - TODO in future secrets should not be changable but generated due to security reasons.
			// Attack: one user finds occupied secret and starts to post data
			resp = processSecretChange(sk, user, tokenUser, req.resetSecret);
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
	
//	private boolean isSecretOk(SessionKeeper sk, String secret) {
//		if(secret.length() < 8 && secret.length() > 0) return false;
//		int n = ((Number) sk.createCriteria(TrackingUser.class).add(Restrictions.eq("postingSecret", secret)).setProjection(Projections.rowCount()).uniqueResult()).intValue(); 
//		return n == 0;		
//	}
	
	public APIAuthenticateResponse authenticate(APIAuthenticateRequest req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {				
			return new APIAuthenticateResponse(tokens.authenticate(sk, req.userId, req.password, passwordEncoder)); 
		}			
	}
	
	@SuppressWarnings("unchecked")
	public APIUserProfileResponse userProfile(APIUserProfile req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) return new APIUserProfileResponse("AUTH_ERROR", "");

			TrackingUser user = null;
			if(req.userId != null) {
				user = getUser(sk, req.userId);
			} else {
				user = tokenUser;
			}
			if(user == null) return new APIUserProfileResponse("WRONG_USERID", ""); 
			
			
			
			APIUserProfileResponse resp = new APIUserProfileResponse();
			resp.userId = user.getUserId();
			resp.isAdmin = user.getAdmin();
			resp.postingSecret = tokenUser.getUserId().equals(user.getUserId()) ? user.getPostingSecret() : null;
//			resp.adminGroups = null;
//			resp.userGroups = null;
			resp.personalGroup = tokenUser.getPersonalGroup().getGroupId();
			return resp;
		}
	}	
	
	@SuppressWarnings("unchecked")
	public APIUsersQueryResponse listUsers(APIUsersQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIUsersQueryResponse("AUTH_ERROR", "Invalid token.");
			}
			if(!tokenUser.getAdmin()) {
				return new APIUsersQueryResponse("AUTH_ERROR", "Token user does not have admin privileges.");				
			}
			Criteria c = sk.createCriteria(TrackingUser.class);						
			List<TrackingUser> recs = c.list();	
			List<APIUserDetail> users = recs.stream()
				.map(x -> new APIUserDetail(x.getUserId(), x.getAdmin()))
				.collect(Collectors.toList());
			APIUsersQueryResponse res = new APIUsersQueryResponse(users);
			return res;
		}
	}	
	
	public APIUserSecretResponse getPostingSecret(APIUserSecret req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIUserSecretResponse("AUTH_ERROR", "Invalid token.");
			}
			if(tokenUser.getPostingSecret() == null) {
				processSecretChange(sk, tokenUser, tokenUser, true);
				sk.saveOrUpdate(tokenUser);
				sk.commit();
			}
			return new APIUserSecretResponse(tokenUser.getPostingSecret());
		}		
	}
}

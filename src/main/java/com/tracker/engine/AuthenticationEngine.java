
package com.tracker.engine;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.notifications.APIDevice;
import com.tracker.apientities.notifications.APIRegistredDevice;
import com.tracker.apientities.user.APIAuthProvidersResponse;
import com.tracker.apientities.user.APIAuthenticate;
import com.tracker.apientities.user.APIAuthenticateResponse;
import com.tracker.apientities.user.APIOneTimeToken;
import com.tracker.apientities.user.APIOneTimeTokenResponse;
import com.tracker.apientities.user.APIPropertiesDelete;
import com.tracker.apientities.user.APIPropertiesDeleteResponse;
import com.tracker.apientities.user.APIProperty;
import com.tracker.apientities.user.APIPropertyList;
import com.tracker.apientities.user.APIPropertyListResponse;
import com.tracker.apientities.user.APIPropertySet;
import com.tracker.apientities.user.APIPropertySetResponse;
import com.tracker.apientities.user.APIPropertyStatus;
import com.tracker.apientities.user.APIUserDetail;
import com.tracker.apientities.user.APIUserProfile;
import com.tracker.apientities.user.APIUserProfileResponse;
import com.tracker.apientities.user.APIUserRegister;
import com.tracker.apientities.user.APIUserRegisterResponse;
import com.tracker.apientities.user.APIUserResetPassword;
import com.tracker.apientities.user.APIUserResetPasswordResponse;
import com.tracker.apientities.user.APIUserSecret;
import com.tracker.apientities.user.APIUserSecretResponse;
import com.tracker.apientities.user.APIUserUpdate;
import com.tracker.apientities.user.APIUserUpdateResponse;
import com.tracker.apientities.user.APIUsersQuery;
import com.tracker.apientities.user.APIUsersQueryResponse;
import com.tracker.db.AppConfiguration;
import com.tracker.db.DeviceRecord;
import com.tracker.db.NotificationRegistration;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingProperty;
import com.tracker.db.TrackingUser;
import com.tracker.db.UserGroupAssignment;
import com.tracker.utils.SessionKeeper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AuthenticationEngine {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private AuthProviderFactory authFactory;
	
	private TokenStorage tokens = null;
	
	private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	public AuthenticationEngine() throws NoSuchAlgorithmException  {
		this.tokens = new TokenStorage();
	}
	
	public List<GroupRoles> processProviderAuthentication(SessionKeeper sk, AuthenticationObject authObj) {
		return null;
	}
	
	
	private UserGeneration generateUser(SessionKeeper sk, String userId, String password, String providerId) {
		// user must not exist!
		TrackingUser user = null;
		UserGeneration ugen = new UserGeneration();
		AuthenticationObject authObj = null;
		IAuthProvider provider = null;
		if(providerId == null) {
			user = new TrackingUser(userId, password, passwordEncoder);
		} else {
			provider = authFactory.getProvider(providerId);
			if(provider == null) {
				ugen.status = "WRONG_PROVIDER";
				return ugen;
			}
			authObj = provider.authenticate(userId, password);
			if(!authObj.getStatus().equals("OK")) {
				ugen.status = "PROVIDER_AUTH_ERROR" + " " + authObj.getStatus() + " " + authObj.getErrorMessage();
				return ugen;
			}
			ugen.provider = providerId;
			ugen.auth = authObj;
			user = new TrackingUser();
			user.setAdmin(false);
			user.setEmail(authObj.getEmail());
			user.setName(authObj.getName());
			user.setPassword(null, null);  // no password
			user.setProvider(providerId);
			user.setUserId(userId);					
		}
		
		ugen.user = user;
		String secret = tokens.generateToken();	
		user.setPostingSecret(secret);

		Date now = new Date();
		user.setTimestamp(now);
		OrganizationGroup group = generatePersonalGroup(user, providerId, now);
		ugen.personalGroup = group;
		UserGroupAssignment asgn1 = new UserGroupAssignment();
		asgn1.setAsPersonalGroup(user, group, now, "ADMIN");
		UserGroupAssignment asgn2 = new UserGroupAssignment();
		asgn2.setAsPersonalGroup(user, group, now, "USER");		
		ugen.status = "OK";
		sk.save(ugen.user);
		sk.save(ugen.personalGroup);
		sk.save(asgn1);
		sk.save(asgn2);
		if(provider != null) {
			provider.updateRoles(sk, user, authObj);
		}
		return ugen;
	}
	
	private String personalGroupName(String userId, String provider) {
		return provider + "#" + userId;
	}
	
	public OrganizationGroup generatePersonalGroup(TrackingUser user, String provider, Date now) {		
		OrganizationGroup group = new OrganizationGroup();
		String groupId = personalGroupName(user.getUserId(), provider);
		group.setGroupId(groupId);
		group.setDescription(groupId);
		group.setCreator(user); 
		group.setPersonalGroupUser(user);
		group.setProvider(provider);
		user.setPersonalGroup(group);		
		group.setTimestamp(now);
		return group;
	}
	
	public APIUserRegisterResponse registerUser(APIUserRegister req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			if(!req.userId.contains("@")) {
				//TODO check email validity
				return new APIUserRegisterResponse("USERNAME_NOT_EMAIL", "");
			}
			TrackingUser user = getUser(sk, req.userId, null); 
			if(user == null) {
				UserGeneration ugen = generateUser(sk, req.userId, req.password, null);
				if(ugen.status == "OK") {	
					sk.commit();
					APIUserRegisterResponse res =  new APIUserRegisterResponse();
					res.token = tokens.authenticate(sk, ugen.user, req.password, passwordEncoder, null, null);
					return res;
				}
				return new APIUserRegisterResponse("ERROR", "");
			}
			return new APIUserRegisterResponse("USER_EXISTS", "");
		}		
	}
	
	private AppConfiguration getAppConfiguration(SessionKeeper sk) {
		return (AppConfiguration)sk.createCriteria(AppConfiguration.class).add(Restrictions.eq("identifier", 1)).uniqueResult();
	}
	
	public APIUserResetPasswordResponse resetPassword(APIUserResetPassword req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser user = null;

			
			AppConfiguration appConf = this.getAppConfiguration(sk);
			if(req.secret == null || appConf == null || appConf.getResetPasswordSecret() == null || !req.secret.equals(appConf.getResetPasswordSecret())) {
				return new APIUserResetPasswordResponse("WRONG_SECRET", "Configure and provide correct password reset secret.");
			}
			if(req.resetToken == null) {
				user = getUser(sk, req.userId, null);  // cannot reset password for provider's user
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
				if(userId == null) {
					return new APIUserResetPasswordResponse("INVALID_OR_EXPIRED_RESET_TOKEN", "");
				}
				if(!this.isPasswordOk(req.newPassword)) {
					return new APIUserResetPasswordResponse("PASSWORD_NOT_COMPLEX_ENOUGH", "");
				}
				user = getUser(sk, userId, null);
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

	
	private APIUserUpdateResponse processPasswordChange(TrackingUser user, TrackingUser tokenUser, String oldPassword, String newPassword) {
		if(newPassword == null) {
			return null;
		}
		if(!this.isPasswordOk(newPassword)) {
				return new APIUserUpdateResponse("PASSWORD_NOT_COMPLEX_ENOUGH", "");
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
			return new APIUserUpdateResponse("OLD_PASSWORD_WRONG", "");			
		} 
		// do nothing
		return null;
	}

	private APIUserUpdateResponse processSecretChange(SessionKeeper sk, TrackingUser user, TrackingUser tokenUser, Boolean secretChange) {
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
			return new APIUserUpdateResponse("SECRET_CHANGE_DENIED", "Token must match userId or token not of an admin.");
		}
		return null;
	}	
	
	private APIUserUpdateResponse processMakeAdmin(TrackingUser user, TrackingUser tokenUser, boolean makeAdmin) {
		if(tokenUser != null && tokenUser.getAdmin()) {
			user.setAdmin(makeAdmin);
			return null;
		} 	
		return new APIUserUpdateResponse("SET_ADMIN_DENIED", "Admin status can be changed by admin only.");		
	}

	private APIUserUpdateResponse processMonitored(TrackingUser user, TrackingUser tokenUser, boolean monitored) {
		if(tokenUser != null && tokenUser.getAdmin()) {
			user.setMonitored(monitored);
			return null;
		} 	
		return new APIUserUpdateResponse("SET_MONITORED_DENIED", "Monitored status can be changed by admin only.");		
	}
	
	public TrackingUser getUser(SessionKeeper sk, String userId, String provider) {
		if(userId == null) return null;
		Criteria c = sk.createCriteria(TrackingUser.class).add(Restrictions.eq("userId", userId));
		if(provider == null) {
			c.add(Restrictions.isNull("provider"));
		} else {
			c.add(Restrictions.eq("provider", provider));
		}
		return (TrackingUser) c.uniqueResult();
	}
	
	public TrackingUser getTokenUser(SessionKeeper sk, String token) {
		if(token == null) return null;
		UserAuthentication auth = this.tokens.authenticatedUserForToken(token);
		if(auth == null) return null;
		return getUser(sk, auth.getUserId(), auth.getProvider());		
	}
	
	public APIUserUpdateResponse update(APIUserUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser user = getUser(sk, req.userId, req.provider); // not possible update for a provider
			
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(user == null) {
				if(req.userId == null) {
					user = tokenUser;
				} else {
					return new APIUserUpdateResponse("NO_SUCH_USER", "");
				}
			}		

			
			APIUserUpdateResponse resp = null;
			
			if(user.getProvider() == null) {
				//password change
				resp = processPasswordChange(user, tokenUser, req.oldPassword, req.newPassword);
				if(resp != null) return resp;
			}
			resp = processSecretChange(sk, user, tokenUser, req.resetSecret);
			if(resp != null) return resp;
			
			
			if(user.getProvider() == null) {
				// admin change
				if(req.makeAdmin != null) {
					resp = processMakeAdmin(user, tokenUser, req.makeAdmin);
					if(resp != null) return resp;
				}
			}
			
			if(req.monitored != null) {
				resp = processMonitored(user, tokenUser, req.monitored);
				if(resp != null) return resp;
			}
			APIUserUpdateResponse res = new APIUserUpdateResponse();
			sk.saveOrUpdate(user);	
			sk.commit();
			return res;
		}		
	}	
	
	private boolean isPasswordOk(String password) {
		return password.length() > 5;
	} 
	
	public APIAuthenticateResponse authenticate(APIAuthenticate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser user = null;
			String token = null;
			if(req.oneTimeToken != null) {
				UserAuthentication uauth = tokens.verifyOneTimeTokenAndClear(req.oneTimeToken);
				if(uauth == null) {
					return new APIAuthenticateResponse("AUTH_ERROR", "Wrong one time auth token.");
				}
				user = getUser(sk, uauth.getUserId(), uauth.getProvider());
				if(user == null) {
					return new APIAuthenticateResponse("AUTH_ERROR", "Cannot find a user for one time auth token.");
				}	
				token = uauth.getToken();
			} else {
				user = getUser(sk, req.userId, req.provider);
			}
			String status = "OK";
			if(user != null) { // existing user
				if(token == null) {
					token = tokens.authenticate(sk, user, req.password, passwordEncoder, authFactory, req.provider);
				}
			} else {
					if(req.provider == null) {
						return new APIAuthenticateResponse("AUTH_ERROR", "User does not exist.");
					}
					// user is null and provider != null
					if(authFactory.getProvider(req.provider) == null) {
						return new APIAuthenticateResponse("WRONG_PROVIDER", "");
					}
					// generate new provider user
					UserGeneration ugen = generateUser(sk, req.userId, req.password, req.provider);
					user = ugen.user;
					if(!ugen.status.equals("OK")) { 
						return new APIAuthenticateResponse(ugen.status, "");
					}
					token = tokens.authenticateUserWithTokenFromProvider(ugen.user.getUserId(), ugen.auth.getToken(), req.provider);
			}
			if(token == null) {
				return new APIAuthenticateResponse(null);
			}
			if((req.device == null && req.notificationToken != null) || (req.device != null && req.notificationToken == null)) {
				return new APIAuthenticateResponse("MISSING_DEVICE_OR_TOKEN", "Missing exactly one of device or notification token.");
			}
			
			if(req.device != null && (req.forcePrimary != null && req.forcePrimary || user.getPrimaryNotificationDevice() == null)) {					
				status = NotificationEngine.registerPrimaryDevice(sk, user, req.device, req.notificationToken);
			} 
			if(status.equals("OK")) {
				sk.commit();				
				APIAuthenticateResponse res = new APIAuthenticateResponse(token);
				res.primaryDeviceOverride = false;
				res.monitored = user.getMonitored();
				res.postingSecret = user.getPostingSecret();
				res.userId = user.getUserId();
				if(req.device != null && req.device.uuid.equals(user.getPrimaryNotificationDevice().device.getUuid())) res.isPrimaryDevice = true;
				return res;
			}
			if(status.equals("OK_OVERRIDE")) {
				sk.commit();
				APIAuthenticateResponse res = new APIAuthenticateResponse(token);
				res.primaryDeviceOverride = true;
				res.monitored = user.getMonitored();
				res.postingSecret = user.getPostingSecret();
				res.userId = user.getUserId();
				return res;
			} 
			return new APIAuthenticateResponse(status, "");
		}			
	}
	
	@SuppressWarnings("unchecked")
	public APIUserProfileResponse userProfile(APIUserProfile req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) return new APIUserProfileResponse("AUTH_ERROR", "");

			TrackingUser user = null;
			if(req.userId != null) {
				user = getUser(sk, req.userId, req.provider);
			} else {
				user = tokenUser;
			}
			if(user == null) return new APIUserProfileResponse("WRONG_USERID", ""); 	
			
			APIUserProfileResponse resp = new APIUserProfileResponse();
			resp.userId = user.getUserId();
			resp.isAdmin = user.getAdmin();
			resp.postingSecret = tokenUser.getUserId().equals(user.getUserId()) ? user.getPostingSecret() : null;
			resp.provider = user.getProvider();
			resp.monitored = user.getMonitored();
//			resp.adminGroups = null;
//			resp.userGroups = null;
			resp.personalGroup = user.getPersonalGroup().getGroupId();
			NotificationRegistration reg = user.getPrimaryNotificationDevice();
			if(reg != null) {
				APIRegistredDevice rd = new APIRegistredDevice();
				rd.device = new APIDevice(reg.getDevice());
				rd.registrationDate = reg.getTimestamp();
				resp.primaryDevice = rd;
			}
			List<NotificationRegistration> regs = user.getNotificationDevices();
			List<APIRegistredDevice> regDevices = new LinkedList<APIRegistredDevice>();
			if(regs != null && regs.size() > 0) {
				for(NotificationRegistration notf: regs) {
					APIRegistredDevice rd = new APIRegistredDevice();
					rd.device = new APIDevice(notf.getDevice());
					rd.registrationDate = notf.getTimestamp();	
					regDevices.add(rd);
				}
				resp.devices = regDevices;
			}
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
			if(!tokenUser.getAdmin() && req.queryString == null) {
				return new APIUsersQueryResponse("AUTH_ERROR", "Token user does not have admin privileges.");				
			}
			Criteria c = sk.createCriteria(TrackingUser.class);
			if(req.queryString != null) {
				if(req.queryString.length() < 3) {
					return new APIUsersQueryResponse("ERROR", "Query string must have at least 3 characters.");
				}
				c.add(Restrictions.disjunction()
						.add(Restrictions.ilike("userId", "%" + req.queryString + "%"))
						.add(Restrictions.ilike("name", "%" + req.queryString + "%"))
				);
				String provider = tokenUser.getProvider();
				if(provider != null) {
					c.add(Restrictions.eqOrIsNull("provider", provider));
				} else {
					c.add(Restrictions.isNull("provider"));
				}
				c.setMaxResults(10);
			} 
			List<TrackingUser> recs = c.list();	
			List<APIUserDetail> users = recs.stream()
				.map(x -> new APIUserDetail(x.getUserId(), x.getAdmin(), x.getProvider()))
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

	public APIAuthProvidersResponse listAuthProviders() {
		return new APIAuthProvidersResponse(authFactory.listProviders());
	}

	private boolean checkConversion(String type, String value) {
		if(type == null || value == null) return false;
		switch(type) {
			case "Integer":
				try {
					Integer.parseInt(value);
					return true;
				} catch(Exception e) {
					return false;
				}
			case "Double":
				try {
					Double.parseDouble(value);
					return true;
				} catch(Exception e) {
					return false;
				}
			case "Boolean":
				try {
					Boolean.parseBoolean(value);
					return true;
				} catch(Exception e) {
					return false;
				}
			case "String":
				return true;
			default:
				return false; // something strange provided as a type
		}		
	}
	
	public APIPropertySetResponse propertySet(APIPropertySet req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIPropertySetResponse("AUTH_ERROR", "Invalid token.");
			}
			if(!tokenUser.getAdmin()) {
				return new APIPropertySetResponse("AUTH_ERROR", "Properties can be set by admins only.");
			}
			List<APIPropertyStatus> statuses = new LinkedList<APIPropertyStatus>();
			for(APIProperty prop: req.properties) {
				if(!checkConversion(prop.type, prop.value)) {
					statuses.add(new APIPropertyStatus(prop.key, "CONVERSION_FAILED", ""));
					continue;
				}
				Criteria c = sk.createCriteria(TrackingProperty.class)
											.add(Restrictions.eq("dataKey", prop.key));
				if(req.provider != null) {
					c.add(Restrictions.eq("provider", req.provider));
				} else {
					c.add(Restrictions.isNull("provider"));
				}
				TrackingProperty aProp = (TrackingProperty)c.uniqueResult();
				if(aProp == null) {
					aProp = new TrackingProperty();
					aProp.setKey(prop.key);
					aProp.setType(prop.type);
					aProp.setValue(prop.value);
					aProp.setProvider(req.provider);
					sk.save(aProp);
				} else {
					aProp.setType(prop.type);
					aProp.setValue(prop.value);
					sk.saveOrUpdate(aProp);
				}
				statuses.add(new APIPropertyStatus(prop.key, "OK", ""));
			}
			sk.commit();
			return new APIPropertySetResponse(statuses);
		}		
	}

	public APIPropertyListResponse propertyList(APIPropertyList req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIPropertyListResponse("AUTH_ERROR", "Invalid token.");
			}
			Criteria c = sk.createCriteria(TrackingProperty.class);
			String provider = null;
			if(tokenUser.getAdmin()) {
				provider = req.provider;
			} else {
				provider = tokenUser.getProvider();
				if((provider == null && req.provider != null) ||
				   (provider != null && req.provider != null && !provider.equals(req.provider))) {
					return new APIPropertyListResponse("WRONG_PROVIDER", "Non-admin can check properties for his provider only.");
				}
			}
			if(provider != null) {
				c.add(Restrictions.eq("provider", provider));
			} else {
				c.add(Restrictions.isNull("provider"));
			}
			@SuppressWarnings("unchecked")
			List<TrackingProperty> properties = c.list();
			List<APIProperty> outProps = new LinkedList<APIProperty>();
			for(TrackingProperty prop: properties) {
				outProps.add(new APIProperty(prop));
			}
			return new APIPropertyListResponse(outProps, provider);			
		}

		
	}
	
	public APIPropertiesDeleteResponse propertiesDelete(APIPropertiesDelete req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIPropertiesDeleteResponse("AUTH_ERROR", "Invalid token.");
			}
			if(!tokenUser.getAdmin()) {
				return new APIPropertiesDeleteResponse("AUTH_ERROR", "Properties can be set by admins only.");
			}
			List<APIPropertyStatus> statuses = new LinkedList<APIPropertyStatus>();
			for(String propKey: req.propertyKeys) {
				Criteria c = sk.createCriteria(TrackingProperty.class)
											.add(Restrictions.eq("dataKey", propKey));
				if(req.provider != null) {
					c.add(Restrictions.eq("provider", req.provider));
				} else {
					c.add(Restrictions.isNull("provider"));
				}
				TrackingProperty aProp = (TrackingProperty)c.uniqueResult();
				if(aProp == null) {
					statuses.add(new APIPropertyStatus(propKey, "MISSING", ""));
				} else {
					sk.delete(aProp);
					statuses.add(new APIPropertyStatus(propKey, "OK", ""));
				}
			}
			sk.commit();
			return new APIPropertiesDeleteResponse(statuses);
		}		
	}
	
	public APIOneTimeTokenResponse getOneTimeAuthToken(APIOneTimeToken req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			String oneTimeToken = tokens.getOneTimeToken(req.token);
			if(oneTimeToken == null) {
				return new APIOneTimeTokenResponse("AUTH_ERROR", "Wrong token.");
			}
			return new APIOneTimeTokenResponse(oneTimeToken);
		}		
	}	
}

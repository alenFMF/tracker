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
import com.tracker.apientities.devices.APIDevicesQuery;
import com.tracker.apientities.devices.APIDevicesResponse;
import com.tracker.apientities.user.APIAuthenticateRequest;
import com.tracker.apientities.user.APIAuthenticateResponse;
import com.tracker.apientities.user.APIUserRegisterRequest;
import com.tracker.apientities.user.APIUserResetPasswordRequest;
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
	
	public APIBaseResponse resetPassword(APIUserResetPasswordRequest req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			TrackingUser user = (TrackingUser) sk.createCriteria(TrackingUser.class).add(Restrictions.eq("userId", req.userId)).uniqueResult();
			if(user == null) {		
				return new APIBaseResponse("NO_SUCH_USER", "");
			}			
			user.setPassword(req.newPassword, passwordEncoder);
			sk.saveOrUpdate(user);	
			sk.commit();
		}		
		return new APIBaseResponse();
	}
	
	public APIAuthenticateResponse authenticate(APIAuthenticateRequest req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {				
			return new APIAuthenticateResponse(tokens.authenticate(sk, req.userId, req.password, passwordEncoder)); 
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

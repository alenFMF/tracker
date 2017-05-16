package com.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.user.APIAuthenticateRequest;
import com.tracker.apientities.user.APIAuthenticateResponse;
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
import com.tracker.engine.AuthenticationEngine;
import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("authentication")
public class AuthenticationController {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ApiOperation(value = "Register a new user.", notes = "To register a new user a username in the form of an email"
			+ " and a password of sufficient complexity have to be provided. A personal user group is created together with "
			+ "the user with the same groupId as userId. ")
	@ResponseBody
	public ResponseEntity<APIBaseResponse> registerUser(@RequestBody APIUserRegisterRequest req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(authEngine.registerUser(req), HttpStatus.OK);
	}		
	
	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	@ApiOperation(value = "Reset a password.", notes = "To reset a password authentication token of a system"
			+ " admin and userId have to be provided for the first request. Reset token is returned. "
			+ "For the second request authentication token of an system admin, resetToken and a new password"
			+ " of sufficient complexity have to be provided.")
	@ResponseBody
	public ResponseEntity<APIUserResetPasswordResponse> resetPassword(@RequestBody APIUserResetPassword req) {
		inputLogger(req);
		return new ResponseEntity<APIUserResetPasswordResponse>(authEngine.resetPassword(req), HttpStatus.OK);
	}		

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ApiOperation(value = "Update a user profile.", notes = "")
	@ResponseBody
	public ResponseEntity<APIBaseResponse> update(@RequestBody APIUserUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(authEngine.update(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "profile", method = RequestMethod.POST)
	@ApiOperation(value = "Show user profile data.", notes = "Listing user or admin groups is not yet supported.")
	@ResponseBody
	public ResponseEntity<APIUserProfileResponse> profile(@RequestBody APIUserProfile req) {
		inputLogger(req);
		return new ResponseEntity<APIUserProfileResponse>(authEngine.userProfile(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "authenticate", method = RequestMethod.POST)
	@ApiOperation(value = "Authenticate user.", notes = "Provide username and password to authenticate "
			+ "and obtain authentication token that is needed for most of other services.")
	@ResponseBody
	public ResponseEntity<APIAuthenticateResponse> authenticate(@RequestBody APIAuthenticateRequest req) {
		inputLogger(req);
		return new ResponseEntity<APIAuthenticateResponse>(authEngine.authenticate(req), HttpStatus.OK);
	}		
	
	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ApiOperation(value = "List users.", notes = "System admins can list users together with their system roles (USER, ADMIN).")
	@ResponseBody
	public ResponseEntity<APIUsersQueryResponse> listUsers(@RequestBody APIUsersQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIUsersQueryResponse>(authEngine.listUsers(req), HttpStatus.OK);
	}	

//	@RequestMapping(value = "secret", method = RequestMethod.POST)
//	@ApiOperation(value = "Returns user secret for posting tracks.", notes = "")
//	@ResponseBody
//	public ResponseEntity<APIUserSecretResponse> getPostingSecret(@RequestBody APIUserSecret req) {
//		inputLogger(req);
//		return new ResponseEntity<APIUserSecretResponse>(authEngine.getPostingSecret(req), HttpStatus.OK);
//	}	
	
    @ExceptionHandler
    public ResponseEntity<APIBaseResponse> handleException(Exception exc) {
        APIBaseResponse.logError(exc);
        return APIBaseResponse.createResponse(exc);
    }	
    
    public void inputLogger(Object req) {
		try {
			logger.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(req));
		} catch (Exception e) {
			logger.error("Error logging json", e);
		}    	
    }
	
}

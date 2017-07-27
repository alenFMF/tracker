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
import com.tracker.apientities.user.APIAuthProvidersResponse;
import com.tracker.apientities.user.APIAuthenticate;
import com.tracker.apientities.user.APIAuthenticateResponse;
import com.tracker.apientities.user.APIOneTimeToken;
import com.tracker.apientities.user.APIOneTimeTokenResponse;
import com.tracker.apientities.user.APIPropertiesDelete;
import com.tracker.apientities.user.APIPropertiesDeleteResponse;
import com.tracker.apientities.user.APIPropertyList;
import com.tracker.apientities.user.APIPropertyListResponse;
import com.tracker.apientities.user.APIPropertySet;
import com.tracker.apientities.user.APIPropertySetResponse;
import com.tracker.apientities.user.APIUserProfile;
import com.tracker.apientities.user.APIUserProfileResponse;
import com.tracker.apientities.user.APIUserRegister;
import com.tracker.apientities.user.APIUserResetPassword;
import com.tracker.apientities.user.APIUserResetPasswordResponse;
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
	public ResponseEntity<APIBaseResponse> registerUser(@RequestBody APIUserRegister req) {
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
	@ApiOperation(value = "Update a user profile.", notes = "If provider is successfully changed then new authentication token is returned.")
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
	public ResponseEntity<APIAuthenticateResponse> authenticate(@RequestBody APIAuthenticate req) {
		inputLogger(req);
		return new ResponseEntity<APIAuthenticateResponse>(authEngine.authenticate(req), HttpStatus.OK);
	}		
	
	@RequestMapping(value = "oneTimeToken", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIOneTimeTokenResponse> oneTimeAuthenticateToken(@RequestBody APIOneTimeToken req) {
		inputLogger(req);
		return new ResponseEntity<APIOneTimeTokenResponse>(authEngine.getOneTimeAuthToken(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "property/set", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIPropertySetResponse> propertySet(@RequestBody APIPropertySet req) {
		inputLogger(req);
		return new ResponseEntity<APIPropertySetResponse>(authEngine.propertySet(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "property/delete", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIPropertiesDeleteResponse> propertiesDelete(@RequestBody APIPropertiesDelete req) {
		inputLogger(req);
		return new ResponseEntity<APIPropertiesDeleteResponse>(authEngine.propertiesDelete(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "property/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIPropertyListResponse> propertyList(@RequestBody APIPropertyList req) {
		inputLogger(req);
		return new ResponseEntity<APIPropertyListResponse>(authEngine.propertyList(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ApiOperation(value = "List users.", notes = "System admins can list users together with their system roles (USER, ADMIN).")
	@ResponseBody
	public ResponseEntity<APIUsersQueryResponse> listUsers(@RequestBody APIUsersQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIUsersQueryResponse>(authEngine.listUsers(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "providers/list", method = RequestMethod.GET)
	@ApiOperation(value = "List authentication providers.", notes = "")
	@ResponseBody
	public ResponseEntity<APIAuthProvidersResponse> listAuthProviders() {
		return new ResponseEntity<APIAuthProvidersResponse>(authEngine.listAuthProviders(), HttpStatus.OK);
	}	
	
    @ExceptionHandler
    public ResponseEntity<APIBaseResponse> handleException(Exception exc) {
        APIBaseResponse.logError(exc);
        return APIBaseResponse.createResponse(exc);
    }	
    
    public void inputLogger(Object req) {
		try {			
			logger.info(AuthenticationController.cleanLogs(new ObjectMapper()
							.writerWithDefaultPrettyPrinter()
							.writeValueAsString(req))
					);
		} catch (Exception e) {
			logger.error("Error logging json", e);
		}    	
    }
    
    public static String cleanLogs(String s) {
		return s.replaceAll("\\\"password\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"password\" : \"*********\"")
		.replaceAll("\\\"notificationToken\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"notificationToken\" : \"*********\"");
    }	
}

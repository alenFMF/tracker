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
import com.tracker.apientities.user.APIUserRegisterRequest;
import com.tracker.apientities.user.APIUserResetPasswordRequest;
import com.tracker.apientities.user.APIUsersQuery;
import com.tracker.apientities.user.APIUsersQueryResponse;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.TestEngine;

@Controller
@RequestMapping("authentication")
public class AuthenticationController {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> registerUser(@RequestBody APIUserRegisterRequest req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(authEngine.registerUser(req), HttpStatus.OK);
	}		
	
	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> resetPassword(@RequestBody APIUserResetPasswordRequest req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(authEngine.resetPassword(req), HttpStatus.OK);
	}		

	@RequestMapping(value = "authenticate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> resetPassword(@RequestBody APIAuthenticateRequest req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(authEngine.authenticate(req), HttpStatus.OK);
	}		
	
	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIUsersQueryResponse> listUsers(@RequestBody APIUsersQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIUsersQueryResponse>(authEngine.listUsers(req), HttpStatus.OK);
	}	
	
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

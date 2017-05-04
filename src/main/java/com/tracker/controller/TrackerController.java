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
import com.tracker.apientities.tracks.APITrackQuery;
import com.tracker.apientities.tracks.APITrackQueryResponse;
import com.tracker.apientities.tracks.APITrackerPost;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.TestEngine;


@Controller
@RequestMapping("tracker")
public class TrackerController {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	@Autowired
	TestEngine testEngine;
	
	@Autowired 
	AuthenticationEngine authEngine;
	
//	@RequestMapping(value = "test_service_1", method = RequestMethod.GET)
//	@ResponseBody
//	public String testService1() {
//		return "Hello world!";
//	}
	
//	@RequestMapping(value = "test_service_2", method = RequestMethod.POST)
//	@ResponseBody
//	public APITest2 testService2(@RequestBody APITest1 req) {
//		return testEngine.handleService2(req);
//	}

//	@RequestMapping(value = "print", method = RequestMethod.POST)
//	@ResponseBody
//	public String testService2(@RequestBody String req) {
//		System.out.println(req);
//		return req;
//	}

	@RequestMapping(value = "gpsPost", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> testService2(@RequestBody APITrackerPost req) {
		inputLogger(req);
		try {
			return new ResponseEntity<APIBaseResponse>(testEngine.handleTrackerPost(req), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<APIBaseResponse>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "gpsQuery", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APITrackQueryResponse> queryGPS(@RequestBody APITrackQuery req) {
		inputLogger(req);
		return new ResponseEntity<APITrackQueryResponse>(testEngine.handleTrackerQuery(req), HttpStatus.OK);
	}
	
//	@RequestMapping(value = "devices", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIDevicesResponse> devicesQuery(@RequestBody APIDevicesQuery req) {
//		inputLogger(req);
//		return new ResponseEntity<APIDevicesResponse>(testEngine.handleDevicesQuery(req), HttpStatus.OK);
//	}	

//	@RequestMapping(value = "registerUser", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIBaseResponse> registerUser(@RequestBody APIUserRegisterRequest req) {
//		inputLogger(req);
//		return new ResponseEntity<APIBaseResponse>(authEngine.registerUser(req), HttpStatus.OK);
//	}		
//	
//	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIBaseResponse> resetPassword(@RequestBody APIUserResetPasswordRequest req) {
//		inputLogger(req);
//		return new ResponseEntity<APIBaseResponse>(authEngine.resetPassword(req), HttpStatus.OK);
//	}		
//
//	@RequestMapping(value = "authenticate", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIBaseResponse> resetPassword(@RequestBody APIAuthenticateRequest req) {
//		inputLogger(req);
//		return new ResponseEntity<APIBaseResponse>(authEngine.authenticate(req), HttpStatus.OK);
//	}		
//	
//	@RequestMapping(value = "users", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIUsersQueryResponse> listUsers(@RequestBody APIUsersQuery req) {
//		inputLogger(req);
//		return new ResponseEntity<APIUsersQueryResponse>(authEngine.listUsers(req), HttpStatus.OK);
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

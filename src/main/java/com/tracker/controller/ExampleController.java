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
import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.apientities.APITrackerPost;
import com.tracker.engine.TestEngine;


@Controller
@RequestMapping("test")
public class ExampleController {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	@Autowired
	TestEngine testEngine;
	
	
	@RequestMapping(value = "test_service_1", method = RequestMethod.GET)
	@ResponseBody
	public String testService1() {
		return "Hello world!";
	}
	
	@RequestMapping(value = "test_service_2", method = RequestMethod.POST)
	@ResponseBody
	public APITest2 testService2(@RequestBody APITest1 req) {
		return testEngine.handleService2(req);
	}

	@RequestMapping(value = "print", method = RequestMethod.POST)
	@ResponseBody
	public String testService2(@RequestBody String req) {
		System.out.println(req);
		return req;
	}

	@RequestMapping(value = "gps", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> testService2(@RequestBody APITrackerPost req) {
		try {
			logger.info(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(req));
		} catch (Exception e) {
			logger.error("Error logging json", e);
		}
		try {
			return new ResponseEntity<APIBaseResponse>(testEngine.handleTrackerPost(req), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<APIBaseResponse>(HttpStatus.BAD_REQUEST);
		}
	}
	
    @ExceptionHandler
    public ResponseEntity<APIBaseResponse> handleException(Exception exc) {
        APIBaseResponse.logError(exc);
        return APIBaseResponse.createResponse(exc);
    }	
	
}

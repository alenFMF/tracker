package com.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.apientities.APIStatusResponse;
import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.apientities.APITrackerPost;
import com.tracker.engine.TestEngine;


@Controller
@RequestMapping("test")
public class ExampleController {

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
	public ResponseEntity<APIStatusResponse> testService2(@RequestBody APITrackerPost req) {
		try {
			System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(req));
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

		try {
			return new ResponseEntity<APIStatusResponse>(testEngine.handleTrackerPost(req), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<APIStatusResponse>(HttpStatus.BAD_REQUEST);
		}
	}
	
}

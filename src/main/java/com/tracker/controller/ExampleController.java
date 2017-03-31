package com.tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
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
	
}

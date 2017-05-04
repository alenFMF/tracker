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
import com.tracker.apientities.devices.APIDeviceRegister;
import com.tracker.apientities.devices.APIDeviceUpdate;
import com.tracker.apientities.devices.APIDeviceQuery;
import com.tracker.apientities.devices.APIDeviceResponse;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.DeviceEngine;

@Controller
@RequestMapping("device")
public class DeviceController {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@Autowired
	DeviceEngine deviceEngine;
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> registerDevice(@RequestBody APIDeviceRegister req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(deviceEngine.register(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> updateVehicle(@RequestBody APIDeviceUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(deviceEngine.update(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIDeviceResponse> listDevices(@RequestBody APIDeviceQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIDeviceResponse>(deviceEngine.list(req), HttpStatus.OK);
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

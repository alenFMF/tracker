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
import com.tracker.apientities.user.APIUserRegister;
import com.tracker.apientities.vehicle.APIVehicleLinkList;
import com.tracker.apientities.vehicle.APIVehicleLinkListResponse;
import com.tracker.apientities.vehicle.APIVehicleLinkRegister;
import com.tracker.apientities.vehicle.APIVehicleLinkRegisterResponse;
import com.tracker.apientities.vehicle.APIVehicleProfile;
import com.tracker.apientities.vehicle.APIVehicleProfileResponse;
import com.tracker.apientities.vehicle.APIVehicleQuery;
import com.tracker.apientities.vehicle.APIVehicleQueryResponse;
import com.tracker.apientities.vehicle.APIVehicleRegister;
import com.tracker.apientities.vehicle.APIVehicleUpdate;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.VehicleEngine;

@Controller
@RequestMapping("vehicle")
public class VehicleController {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@Autowired
	VehicleEngine vehicleEngine;
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> registerVehicle(@RequestBody APIVehicleRegister req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(vehicleEngine.register(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> updateVehicle(@RequestBody APIVehicleUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(vehicleEngine.update(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIVehicleQueryResponse> listVehicles(@RequestBody APIVehicleQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIVehicleQueryResponse>(vehicleEngine.listVehicles(req), HttpStatus.OK);
	}		
	@RequestMapping(value = "profile", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIVehicleProfileResponse> vehicleProfile(@RequestBody APIVehicleProfile req) {
		inputLogger(req);
		return new ResponseEntity<APIVehicleProfileResponse>(vehicleEngine.vehicleProfile(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "link/register", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIVehicleLinkRegisterResponse> linkRegister(@RequestBody APIVehicleLinkRegister req) {
		inputLogger(req);
		return new ResponseEntity<APIVehicleLinkRegisterResponse>(vehicleEngine.vehicleGroupRegister(req), HttpStatus.OK);
	}
	
	@RequestMapping(value = "link/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIVehicleLinkListResponse> linkList(@RequestBody APIVehicleLinkList req) {
		inputLogger(req);
		return new ResponseEntity<APIVehicleLinkListResponse>(vehicleEngine.listAssignments(req), HttpStatus.OK);
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

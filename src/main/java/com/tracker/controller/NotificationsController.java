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
import com.tracker.apientities.notifications.APIDeviceQuery;
import com.tracker.apientities.notifications.APIDeviceRegister;
import com.tracker.apientities.notifications.APIDeviceResponse;
import com.tracker.apientities.notifications.APIDeviceUpdate;
import com.tracker.apientities.notifications.APIMarkReadMessages;
import com.tracker.apientities.notifications.APINotifications;
import com.tracker.apientities.notifications.APINotificationsResponse;
import com.tracker.apientities.notifications.APIPushNotificationReceived;
import com.tracker.apientities.notifications.APISendNotification;
import com.tracker.apientities.notifications.APISendNotificationResponse;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.NotificationEngine;

@Controller
@RequestMapping("notifications")
public class NotificationsController {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@Autowired
	NotificationEngine notificationEngine;
	
//	@RequestMapping(value = "register", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIBaseResponse> registerDevice(@RequestBody APIDeviceRegister req) {
//		inputLogger(req);
//		return new ResponseEntity<APIBaseResponse>(notificationEngine.register(req), HttpStatus.OK);
//	}	

//	@RequestMapping(value = "register", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIBaseResponse> registerDevice(@RequestBody APIDeviceRegister req) {
//		inputLogger(req);
//		return new ResponseEntity<APIBaseResponse>(notificationEngine.register(req), HttpStatus.OK);
//	}	
	
	
	@RequestMapping(value = "notify", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APISendNotificationResponse> notifyUsers(@RequestBody APISendNotification req) {
		inputLogger(req);
		return new ResponseEntity<APISendNotificationResponse>(notificationEngine.notify(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APINotificationsResponse> listMessages(@RequestBody APINotifications req) {
		inputLogger(req);
		return new ResponseEntity<APINotificationsResponse>(notificationEngine.list(req), HttpStatus.OK);
	}	
//	
//	@RequestMapping(value = "update", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIBaseResponse> updateVehicle(@RequestBody APIDeviceUpdate req) {
//		inputLogger(req);
//		return new ResponseEntity<APIBaseResponse>(deviceEngine.update(req), HttpStatus.OK);
//	}	
//
//	@RequestMapping(value = "list", method = RequestMethod.POST)
//	@ResponseBody
//	public ResponseEntity<APIDeviceResponse> listDevices(@RequestBody APIDeviceQuery req) {
//		inputLogger(req);
//		return new ResponseEntity<APIDeviceResponse>(deviceEngine.list(req), HttpStatus.OK);
//	}		
//	
	@RequestMapping(value = "pushNotificationReceived", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> pushNotificationReceived(@RequestBody APIPushNotificationReceived req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(notificationEngine.pushNotificationReceived(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "markReadMessages", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> markReadMessages(@RequestBody APIMarkReadMessages req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(notificationEngine.markReadMessages(req), HttpStatus.OK);
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
	
	
}

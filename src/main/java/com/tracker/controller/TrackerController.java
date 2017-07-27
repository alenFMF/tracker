package com.tracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.tracks.APICoords2;
import com.tracker.apientities.tracks.APITrackQuery;
import com.tracker.apientities.tracks.APITrackQueryResponse;
import com.tracker.apientities.tracks.APITrackerPost;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.TrackerEngine;

import io.swagger.annotations.ApiOperation;


@Controller
@RequestMapping("tracker")
public class TrackerController {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	@Autowired
	TrackerEngine testEngine;
	
	@Autowired 
	AuthenticationEngine authEngine;

	@RequestMapping(value = "print", method = RequestMethod.POST)
	@ResponseBody
	public String testService2(@RequestBody String req) {
		System.out.println(req);
		return req;
	}	
	
	@RequestMapping(value = "trackPost/{userSecret:.+}", method = RequestMethod.POST)
	@ApiOperation(value = "Post sample(s).", notes = "Route for posting GPS samples compatible with react-native-background-geolocation.")
	@ResponseBody
	public ResponseEntity<APIBaseResponse> userPost(@PathVariable("userSecret") String userSecret, @RequestBody APITrackerPost req) {
		inputLogger(req);
		try {
			APIBaseResponse res = testEngine.handleTrackingPost(userSecret, req);
			if(res != null) {
				return new ResponseEntity<APIBaseResponse>(res, HttpStatus.OK);
			}
			return new ResponseEntity<APIBaseResponse>(new APIBaseResponse("SECRET_INVALID", ""), HttpStatus.BAD_REQUEST);					
		} catch (Exception e) {
			return new ResponseEntity<APIBaseResponse>(HttpStatus.BAD_REQUEST);
		}
	}	

	@RequestMapping(value = "shortPost/{userSecret:.+}", method = RequestMethod.POST)
	@ApiOperation(value = "Post sample(s).", notes = "Route for posting GPS samples compatible with react-native-background-geolocation.")
	@ResponseBody
	public ResponseEntity<APIBaseResponse> shortPost(@PathVariable("userSecret") String userSecret, @RequestBody List<APICoords2> req) {
		inputLogger(req);
		try {
			APIBaseResponse res = testEngine.handleShortPost(userSecret, req);
			if(res != null) {
				return new ResponseEntity<APIBaseResponse>(res, HttpStatus.OK);
			}
			return new ResponseEntity<APIBaseResponse>(new APIBaseResponse("SECRET_INVALID", ""), HttpStatus.BAD_REQUEST);					
		} catch (Exception e) {
			return new ResponseEntity<APIBaseResponse>(HttpStatus.BAD_REQUEST);
		}
	}	
	
	@RequestMapping(value = "gpsQuery", method = RequestMethod.POST)
	@ApiOperation(value = "Get GPS track subject to filters.", notes = "For a user with a token it lists user tracks in time between startDate "
			+ "and endDate. A list of required userIds can be provided and requiredAccuracy in meters. If groupId	!= null then "
			+ "token user must be admin in the group listing user tracks for that group. If the list userIds is empty and groupId == null "
			+ "tracks for token user (in personal group)")
	@ResponseBody
	public ResponseEntity<APITrackQueryResponse> queryGPS(@RequestBody APITrackQuery req) {
		inputLogger(req);
		return new ResponseEntity<APITrackQueryResponse>(testEngine.handleTrackerQuery(req), HttpStatus.OK);
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

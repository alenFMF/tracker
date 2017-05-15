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
import com.tracker.apientities.organizationgroup.APIGroupQuery;
import com.tracker.apientities.organizationgroup.APIGroupQueryResponse;
import com.tracker.apientities.organizationgroup.APIGroupRegister;
import com.tracker.apientities.organizationgroup.APIGroupUpdate;
import com.tracker.apientities.organizationgroup.APIShareOrInvite;
import com.tracker.apientities.organizationgroup.APIShareOrInviteResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentQuery;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentUpdate;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentUpdateResponse;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.GroupEngine;

@Controller
@RequestMapping("group")
public class GroupController {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@Autowired
	GroupEngine groupEngine;
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> registerGroup(@RequestBody APIGroupRegister req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(groupEngine.register(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIBaseResponse> updateGroup(@RequestBody APIGroupUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(groupEngine.update(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIGroupQueryResponse> listGroups(@RequestBody APIGroupQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIGroupQueryResponse>(groupEngine.list(req), HttpStatus.OK);
	}		

	@RequestMapping(value = "link/register", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIShareOrInviteResponse> registerLink(@RequestBody APIShareOrInvite req) {
		inputLogger(req);
		return new ResponseEntity<APIShareOrInviteResponse>(groupEngine.registerLinks(req), HttpStatus.OK);
	}		

	@RequestMapping(value = "link/update", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIUserGroupAssignmentUpdateResponse> updateLink(@RequestBody APIUserGroupAssignmentUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIUserGroupAssignmentUpdateResponse>(groupEngine.updateLink(req), HttpStatus.OK);
	}
	
	@RequestMapping(value = "link/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<APIUserGroupAssignmentResponse> listLinks(@RequestBody APIUserGroupAssignmentQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIUserGroupAssignmentResponse>(groupEngine.listLinks(req), HttpStatus.OK);
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

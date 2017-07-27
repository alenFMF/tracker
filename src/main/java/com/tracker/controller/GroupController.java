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
import com.tracker.apientities.organizationgroup.APIMakeMigrationUpdates;
import com.tracker.apientities.organizationgroup.APIShareOrInvite;
import com.tracker.apientities.organizationgroup.APIShareOrInviteResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentQuery;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentUpdate;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentUpdateResponse;
import com.tracker.engine.AuthenticationEngine;
import com.tracker.engine.GroupEngine;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("group")
public class GroupController {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);
	
	@Autowired 
	AuthenticationEngine authEngine;
	
	@Autowired
	GroupEngine groupEngine;
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ApiOperation(value = "Register a new group.", notes = "Authenticated user can provide a token, groupId "
			+ "and description to create a group. GroupId must be unique and must not be in the form of an emali. "
			+ "Email format is reserved for userIds and personal groups that have the same name as userIds."
			+ "A user that registers a group becomes a creator and a first admin.")
	@ResponseBody
	public ResponseEntity<APIBaseResponse> registerGroup(@RequestBody APIGroupRegister req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(groupEngine.register(req), HttpStatus.OK);
	}	
	
	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ApiOperation(value = "Update a group.", notes = "Update a group definition. Currently only description can be "
			+ "changed by an user providing authentication token with system admin rights or group admin rights."
			)	
	@ResponseBody
	public ResponseEntity<APIBaseResponse> updateGroup(@RequestBody APIGroupUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(groupEngine.update(req), HttpStatus.OK);
	}	

	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ApiOperation(value = "List groups.", notes = "Lists groups the token user or user provided as forUserId is a member of.")	
	@ResponseBody
	public ResponseEntity<APIGroupQueryResponse> listGroups(@RequestBody APIGroupQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIGroupQueryResponse>(groupEngine.list(req), HttpStatus.OK);
	}		

	@RequestMapping(value = "link/register", method = RequestMethod.POST)
	@ApiOperation(value = "Share tracks to a group or invite a user to a group.", notes = "User can share its track in "
			+ "a possibly limited time period to a group. Sharing to a group is equivalent to asking a group for permission to join in a role of a USER."
			+ "A group admin can also invite a user either in role USER (request for sharing a track) or in a role ADMIN (become a group admin)."
			+ "If a group is public, sharing in a USER role is autoconfirmed.")	
	@ResponseBody
	public ResponseEntity<APIShareOrInviteResponse> registerLink(@RequestBody APIShareOrInvite req) {
		inputLogger(req);
		return new ResponseEntity<APIShareOrInviteResponse>(groupEngine.registerLinks(req), HttpStatus.OK);
	}		

	@RequestMapping(value = "link/update", method = RequestMethod.POST)
	@ApiOperation(value = "Confirm/reject shares or invites users to groups.", 
			notes = "Assignment (link) ids to accept or reject pending assignemnt requests have to be provided in relevant lists.")	
	@ResponseBody
	public ResponseEntity<APIUserGroupAssignmentUpdateResponse> updateLink(@RequestBody APIUserGroupAssignmentUpdate req) {
		inputLogger(req);
		return new ResponseEntity<APIUserGroupAssignmentUpdateResponse>(groupEngine.updateLink(req), HttpStatus.OK);
	}
	
	@RequestMapping(value = "link/list", method = RequestMethod.POST)
	@ApiOperation(value = "List assignments (links) for user or a group.", 
	notes = "Pending, accepted or rejected assignments can be listed for a user or a group. Group listing can be done "
			+ "only with system admin or group admin privileges. With system admin privileges assignments can be "
			+ "listed for specific user.")	
	@ResponseBody
	public ResponseEntity<APIUserGroupAssignmentResponse> listLinks(@RequestBody APIUserGroupAssignmentQuery req) {
		inputLogger(req);
		return new ResponseEntity<APIUserGroupAssignmentResponse>(groupEngine.listLinks(req), HttpStatus.OK);
	}
	
	@RequestMapping(value = "migration/update", method = RequestMethod.POST)
	@ApiOperation(value = "Trigers idempotent scripts needed for data model migration purposes.")
	@ResponseBody
	public ResponseEntity<APIBaseResponse> migrationUpdate(@RequestBody APIMakeMigrationUpdates req) {
		inputLogger(req);
		return new ResponseEntity<APIBaseResponse>(groupEngine.migrationUpdate(req), HttpStatus.OK);
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

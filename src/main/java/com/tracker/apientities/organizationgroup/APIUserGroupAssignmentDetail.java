package com.tracker.apientities.organizationgroup;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.tracker.db.UserGroupAssignment;

public class APIUserGroupAssignmentDetail {
	public Integer id;
	public String groupId;
	public String userId;
	public String groupUserId;
	public Boolean accepted;
	public Date fromDate;
	public Date untilDate; // if null and from < now assignment still valid
	public String groupRole; // currently USER, ADMIN
	public String inviteType; // USER, GROUP that invites
	public String grant; // ALLOW, DENY
	public Date timestamp; // accept time
	public Date userAction; 
	public Date groupAction;
	public String periodic;  // null default, DAILY, WEEKLY, MONTLY, YEARLY
	public Integer repeatTimes; // -1 means indefinitely
	
	public APIUserGroupAssignmentDetail() {}
	
	public APIUserGroupAssignmentDetail(UserGroupAssignment assignment) {
//		BeanUtils.copyProperties(assignment, this);
		this.id = assignment.getId();		
		this.groupId = assignment.getGroup().getGroupId();
		this.userId = assignment.getUser().getUserId();
		if(assignment.getGroupUser() != null) {
			this.groupUserId = assignment.getGroupUser().getUserId();
		}
		this.accepted = assignment.getAccepted();
		this.fromDate = assignment.getFromDate();
		this.untilDate = assignment.getUntilDate();
		this.groupRole = assignment.getGroupRole();
		this.inviteType = assignment.getInviteType();
		this.grant = assignment.getGrant();
		this.timestamp = assignment.getTimestamp();
		this.userAction = assignment.getUserAction();
		this.groupAction = assignment.getGroupAction();
		this.periodic = assignment.getPeriodic();
		this.repeatTimes = assignment.getRepeatTimes();
	}
	
}

package com.tracker.apientities.organizationgroup;

import java.util.Date;

import com.tracker.db.UserGroupAssignment;

public class APIUserGroupAssignment {
	public String groupId;
	public String userId;
	public String inviteType; //USER, GROUP
	public Date fromDate;
	public Date untilDate; // if null and from < now assignment still valid
	public String groupRole; // currently USER, ADMIN
	public String grant; // ALLOW, DENY
	public String periodic;  // null default, DAILY, WEEKLY, MONTLY, YEARLY
	public Integer repeatTimes; // -1 means indefinitely
}

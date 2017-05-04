package com.tracker.apientities.organizationgroup;

import java.util.Date;

public class APIUserGroupAssignment {
	public String userId;
	public Date form;   // if null, from now
	public Date until; // if null, indefinitely. If entry exists
	public String type; //one of ALLOW, DENY
}

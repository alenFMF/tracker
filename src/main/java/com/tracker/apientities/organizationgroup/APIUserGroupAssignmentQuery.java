package com.tracker.apientities.organizationgroup;

public class APIUserGroupAssignmentQuery {
	public String token;
	public String forUserId;  
	public String forUserIdProvider;
	public String forGroupId;  
	public Boolean pendingOnly;
	public Boolean accept; // if null, accepted and rejected assignments are returned. If true, accepted only, and if false rejected only.
}


//if this is not null, then we are checking assignments to group, but only if token user is admin or tokenUser or forUserId is ADMIN in the group.
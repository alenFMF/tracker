package com.tracker.apientities.organizationgroup;


public class APIPendingAssignments {
// If only token is present then tokenUser is userId.
// Else only one of userId or groupId can be present
// If tokenUser is system admin, then all assigments are available
// Otherwise users have access to teh assignment they are involved in.
// Group admins have access to assignments involving group with groupId. 
// If user has both roles in group it has to choose the role. Default USER	
	public String token;   
	public String groupRole;  // USER, ADMIN   
	public String userId;
	public String groupId;
}

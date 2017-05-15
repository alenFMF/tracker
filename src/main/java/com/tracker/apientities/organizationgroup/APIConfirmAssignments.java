package com.tracker.apientities.organizationgroup;

import java.util.List;

public class APIConfirmAssignments {
	public String token;
	public String assignmentRole; //USER (as sharing user), GROUP (as group admin)
	public List<Integer> userGroupAssignments;
}

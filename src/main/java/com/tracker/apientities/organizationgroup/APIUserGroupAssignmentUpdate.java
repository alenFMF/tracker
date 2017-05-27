package com.tracker.apientities.organizationgroup;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIUserGroupAssignmentUpdate {
	public String token;
	public String forUserId;
	public String forUserIdProvider;
	public List<Integer> confirmLinks;
	public List<Integer> rejectLinks;
}

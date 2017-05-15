package com.tracker.apientities.organizationgroup;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import com.tracker.db.OrganizationGroup;

public class APIGroupDetail {
	public String groupId;
	public String description;
	public String creatorId;
	public Boolean privateGroup; // true (default) if inclusion has to be confirmed by group admin
	public String personalGroupUserId; 
	public Date timestamp; // time of creation;	
	public List<APIUserGroupRolesDetail> users;
	
	public List<APIUserGroupRolesDetail> getUsers() {
		return users;
	}
	public void setUsers(List<APIUserGroupRolesDetail> users) {
		this.users = users;
	}
	
	
//	public List<APIVehicleInGroup> vehicles
	
//	public APIGroupDetail() {}
//	public APIGroupDetail(OrganizationGroup group) {
//		BeanUtils.copyProperties(group, this);
//		this.userAssignments = group.getUserAssignments()
//								.stream()
//								.map(x -> new APIUserGroupAssignmentDetail(x))
//								.collect(Collectors.toList());		
//	}
}

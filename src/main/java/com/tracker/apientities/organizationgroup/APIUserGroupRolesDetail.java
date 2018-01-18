package com.tracker.apientities.organizationgroup;

import com.tracker.engine.GroupRoles;

public class APIUserGroupRolesDetail {
	public String userId;
	public Boolean isAdmin;
	public Boolean isUser;
	public String name;
	
	public APIUserGroupRolesDetail(GroupRoles role) {
		this.userId = role.getUser().getUserId();
		this.isAdmin = role.isAdminRole();
		this.isUser = role.isUserRole();
		this.name = role.getUser().name;
	}
}

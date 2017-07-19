package com.tracker.apientities.organizationgroup;

import com.tracker.engine.GroupRoles;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;

public class APIUserGroupRolesDetail {
	public String userId;
	public Boolean isAdmin;
	public Boolean isUser;
	public List<Pair<Date, Date>> adminIntervals;
	public List<Pair<Date, Date>> userIntervals;
	
	public APIUserGroupRolesDetail(GroupRoles role) {
		this.userId = role.getUser().getUserId();
		this.isAdmin = role.isAdminRole();
		this.isUser = role.isUserRole();
		this.adminIntervals = role.getAdminIntervals();
		this.userIntervals = role.getUserIntervals();
	}
}

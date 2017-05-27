package com.tracker.engine;

import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingUser;

public class GroupRoles {
	private TrackingUser user;
	private OrganizationGroup group;
	private boolean adminRole;
	private boolean userRole;
	
	public GroupRoles(TrackingUser user, OrganizationGroup group, boolean adminRole, boolean userRole) {
		this.user = user;
		this.group = group;
		this.adminRole = adminRole;
		this.userRole = userRole;
	}
	
	public OrganizationGroup getGroup() {
		return group;
	}

	public void setGroup(OrganizationGroup group) {
		this.group = group;
	}

	public TrackingUser getUser() {
		return user;
	}

	public void setUser(TrackingUser user) {
		this.user = user;
	}

	public boolean isAdminRole() {
		return adminRole;
	}

	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	public boolean isUserRole() {
		return userRole;
	}

	public void setUserRole(boolean userRole) {
		this.userRole = userRole;
	}
	

	
}

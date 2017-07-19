package com.tracker.engine;

import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingUser;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;

public class GroupRoles {
	private TrackingUser user;
	private OrganizationGroup group;
	private boolean adminRole;
	private List<Pair<Date, Date>> adminIntervals;
	private List<Pair<Date, Date>> userIntervals;

	public GroupRoles(TrackingUser user, OrganizationGroup group, boolean adminRole, boolean userRole) {
		this.user = user;
		this.group = group;
		this.adminRole = adminRole;
		this.userRole = userRole;
	}

	public GroupRoles(TrackingUser user, OrganizationGroup group, boolean adminRole, boolean userRole, List<Pair<Date, Date>> adminIntervals, List<Pair<Date, Date>> userIntervals) {
		this.user = user;
		this.group = group;
		this.adminRole = adminRole;
		this.userRole = userRole;
		this.adminIntervals = adminIntervals;
		this.userIntervals = userIntervals;
	}

	public List<Pair<Date, Date>> getUserIntervals() {
		return userIntervals;
	}

	public void setUserIntervals(List<Pair<Date, Date>> userIntervals) {
		this.userIntervals = userIntervals;
	}

	private boolean userRole;

	public List<Pair<Date, Date>> getAdminIntervals() {
		return adminIntervals;
	}

	public void setAdminIntervals(List<Pair<Date, Date>> adminIntervals) {
		this.adminIntervals = adminIntervals;
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

package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class UserGroupAssignment {
	@ManyToOne
	public OrganizationGroup group;
	
	@ManyToOne
	public TrackingUser user;
	
	public Date form;
	public Date until; // if null and from < now assignment still valid
}

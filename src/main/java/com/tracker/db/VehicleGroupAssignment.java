package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class VehicleGroupAssignment {
	@ManyToOne
	public OrganizationGroup group;
	
	@ManyToOne
	public Vehicle vehicle;
	
	public Date from;
	public Date until;  // if null and from < now assignment still valid
}

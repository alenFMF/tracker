package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class VehicleGroupAssignment extends BaseEntity {
	@ManyToOne
	public OrganizationGroup group;
	
	@ManyToOne
	public Vehicle vehicle;
	
	public Date fromDate;
	public Date untilDate;  // if null and from < now assignment still valid

	public boolean pending;
	
	public Object getVehicle() {
		return this.vehicle;
	}
	public VehicleGroupAssignment() {
		super();
	}
}

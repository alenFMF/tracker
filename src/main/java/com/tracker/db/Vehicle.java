package com.tracker.db;


import javax.persistence.Entity;

import javax.persistence.ManyToOne;

@Entity
public class Vehicle extends BaseEntity {

	public String vehicleId;
	public String description;
	
	@ManyToOne
	public OrganizationGroup groupId;
	
	public Vehicle(){
		super();
	}
	
	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public OrganizationGroup getGroupId() {
		return groupId;
	}

	public void setGroupId(OrganizationGroup groupId) {
		this.groupId = groupId;
	}

	
	public Vehicle(String vehicleId, String description) {
		this.vehicleId = vehicleId;
		this.description = description;
	}	
}
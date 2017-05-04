package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class Vehicle extends BaseEntity {

	public String vehicleId;
	public String description;
	
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

	public Vehicle() {}
	
	public Vehicle(String vehicleId, String description) {
		this.vehicleId = vehicleId;
		this.description = description;
	}	
}

package com.tracker.db;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class TravelOrder extends BaseEntity {
	public String travelOrderId;
	
	@OneToMany
	public List<DriverAssignment> assignment;
	public Vehicle vehicle;
	
	@OneToMany
	@OrderBy("position")
	public List<LocationVisit> locations;
	
	@OneToMany
	@OrderBy("recordTime")
	public List<TravelEvent> events;
	
}

package com.tracker.db;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class LocationVisit extends BaseEntity {
	public Double longitude;
	public Double latitude;
	public String name;
	public Date arrivalTime;
	public Date departureDate;
	public int position; // position in list
	public Date actualArivalTime;
	public Date actualDepartureTime;
	
	@ManyToOne
	public TravelOrder travelOrder;
	
	@OneToMany
	public List<TravelEvent> events;
}

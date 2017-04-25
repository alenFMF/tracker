package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class DriverAssignment extends BaseEntity {
	@ManyToOne
	public TravelOrder travelOrder;
	
	@ManyToOne
	public TrackingUser user;
	
	public DeviceRecord device; 
	
	public Date startTime;
	public Date endTime;  // null if active
}

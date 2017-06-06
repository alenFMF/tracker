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
	
	public Date startTime;
	public Date endTime;  // null if active
	
	public TravelOrder getTravelOrder() {
		return travelOrder;
	}
	public void setTravelOrder(TravelOrder travelOrder) {
		this.travelOrder = travelOrder;
	}
	public TrackingUser getUser() {
		return user;
	}
	public void setUser(TrackingUser user) {
		this.user = user;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
}

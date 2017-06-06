package com.tracker.db;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TaskGoal extends BaseEntity {
	public Double longitude;
	public Double latitude;
	public Double radius;
	public Date fromTime;
	public Date untilTime;
	
	@ManyToOne
	public TravelOrder travelOrder;	
}

package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class TravelEvent extends BaseEntity {
	public String type;
	public String customDescription;
	public Date recordTime;
	public Date actualTime;
	public LocationVisit location;
}

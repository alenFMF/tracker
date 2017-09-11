package com.tracker.apientities.tracks;

import java.util.Date;

import com.tracker.db.GPSRecord;

public class APITrackSample {
	public Date timestamp;
	public Date recorded;
	public double longitude;
	public double latitude;
	public double speed;
	public double heading;	
	public Integer stopDuration; // minutes
	public Double batteryLevel;
	
	public APITrackSample() {}
	
	public APITrackSample(GPSRecord rec) {
		this.timestamp = rec.getTimestamp();
		this.longitude = rec.getLongitude();
		this.latitude = rec.getLatitude();
		this.speed = rec.getSpeed();
		this.heading = rec.getHeading();
		this.recorded = rec.getRecorded();
		this.batteryLevel = rec.getBattery() != null ? rec.getBattery().getBatteryLevel() : null; 
	}
	
	public APITrackSample(Date timestamp, Date recorded, double longitude, double latitude, double speed, double heading, Integer stopDuration, Double batteryLevel) {
		this.timestamp = timestamp;
		this.longitude = longitude;
		this.latitude = latitude;
		this.speed = speed;
		this.heading = heading;		
		this.recorded = recorded;
		this.batteryLevel = batteryLevel;
	}
}

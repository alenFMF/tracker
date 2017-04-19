package com.tracker.apientities;

import java.util.Date;

import com.tracker.db.GPSRecord;

public class APITrackSample {
	public Date timestamp;
	public double longitude;
	public double latitude;
	public double speed;
	public double heading;		
	
	public APITrackSample() {}
	
	public APITrackSample(GPSRecord rec) {
		this.timestamp = rec.timestamp;
		this.longitude = rec.longitude;
		this.latitude = rec.latitude;
		this.speed = rec.speed;
		this.heading = rec.heading;
	}
}

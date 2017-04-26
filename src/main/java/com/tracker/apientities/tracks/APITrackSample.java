package com.tracker.apientities.tracks;

import java.util.Date;

import com.tracker.db.GPSRecord;

public class APITrackSample {
	public Date timestamp;
	public double longitude;
	public double latitude;
	public double speed;
	public double heading;	
	public Integer stopDuration; // minutes
	
	public APITrackSample() {}
	
	public APITrackSample(GPSRecord rec) {
		this.timestamp = rec.timestamp;
		this.longitude = rec.longitude;
		this.latitude = rec.latitude;
		this.speed = rec.speed;
		this.heading = rec.heading;
	}
	
	public APITrackSample(Date timestamp, double longitude, double latitude, double speed, double heading, Integer stopDuration) {
		this.timestamp = timestamp;
		this.longitude = longitude;
		this.latitude = latitude;
		this.speed = speed;
		this.heading = heading;		
	}
}

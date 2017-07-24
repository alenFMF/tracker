package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "timestamp") })
public class GPSRecord extends BaseEntity {
	// Time
	public Date timestamp;
	public Date recorded;
	
	// Coords
	public double speed;
	public double longitude;
	public double latitude;
	public double heading;	
	public double accuracy;
	
	// Length of column @Column(length = 60)
	// public String recordUUID;	
	
	@ManyToOne(fetch=FetchType.LAZY)
	public TrackingUser user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	public AltitudeRecord altitude;

	@ManyToOne(fetch=FetchType.LAZY)
	public ActivityRecord activity;
	
	@ManyToOne(fetch=FetchType.LAZY)	
	public BatteryRecord battery;
	
	@ManyToOne(fetch=FetchType.LAZY)
	public DeviceRecord device;
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public TrackingUser getUser() {
		return user;
	}

	public void setUser(TrackingUser user) {
		this.user = user;
	}

	public AltitudeRecord getAltitude() {
		return altitude;
	}

	public void setAltitude(AltitudeRecord altitude) {
		this.altitude = altitude;
	}

	public ActivityRecord getActivity() {
		return activity;
	}

	public void setActivity(ActivityRecord activity) {
		this.activity = activity;
	}

	public BatteryRecord getBattery() {
		return battery;
	}

	public void setBattery(BatteryRecord battery) {
		this.battery = battery;
	}

	public DeviceRecord getDevice() {
		return device;
	}

	public void setDevice(DeviceRecord device) {
		this.device = device;
	}

	public Date getRecorded() {
		return recorded;
	}

	public void setRecorded(Date recorded) {
		this.recorded = recorded;
	}
	
	
	
	// Altitude
//	public double altitude_accuracy;   //
//	public double altitude;            //
	
	// Extras
	// ---
	
	// Activity
//	public boolean isMoving;         // 
//	public double odometer;          //
//    public String activityType;      //
//    public int activityConfidence;   //	

    // Battery
//	public double batteryLevel;      //
//	public boolean batteryCharging;  //
	
	
	
	// Device
//	public String uuid;
//	public String manufacturer; //
//	public String model;        //
//	public String version;      //
//	public String platform;     //
	
	// User
	

	
}

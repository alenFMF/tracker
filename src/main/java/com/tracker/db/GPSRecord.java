package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class GPSRecord extends BaseEntity {
	//coords
	public double speed;
	public double longitude;
	public double latitude;
	public double accuracy;
	public double altitude_accuracy;
	public double altitude;
	public double heading;	
	
	//extras
	// ---
	
	public boolean isMoving;
	public double odometer;
	public String recordUUID;	
	
	// activity
    public String activityType;
    public int activityConfidence;

    //battery
	public double batteryLevel;
	public boolean batteryCharging;
	
	
	public Date timestamp;
	
	//Device
	public String uuid;
	public String manufacturer;
	public String model;
	public String version;
	public String platform;
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
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public double getAltitude_accuracy() {
		return altitude_accuracy;
	}
	public void setAltitude_accuracy(double altitude_accuracy) {
		this.altitude_accuracy = altitude_accuracy;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getHeading() {
		return heading;
	}
	public void setHeading(double heading) {
		this.heading = heading;
	}
	public String getType() {
		return activityType;
	}
	public void setActivityType(String type) {
		this.activityType = type;
	}
	public int getConfidence() {
		return activityConfidence;
	}
	public void setActivityConfidence(int confidence) {
		this.activityConfidence = confidence;
	}
	public double getLevel() {
		return batteryLevel;
	}
	public void setBatteryLevel(double level) {
		this.batteryLevel = level;
	}
	public boolean isIs_charging() {
		return batteryCharging;
	}
	public void setBatteryCharging(boolean is_charging) {
		this.batteryCharging = is_charging;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getUuid() {
		return uuid;
	}
	public void setDeviceUUID(String uuid) {
		this.uuid = uuid;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setDeviceManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getModel() {
		return model;
	}
	public void setDeviceModel(String model) {
		this.model = model;
	}
	public String getVersion() {
		return version;
	}
	public void setDeviceVersion(String version) {
		this.version = version;
	}
	public String getPlatform() {
		return platform;
	}
	public void setDevicePlatform(String platform) {
		this.platform = platform;
	}
	public boolean isMoving() {
		return isMoving;
	}
	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}
	public double getOdometer() {
		return odometer;
	}
	public void setOdometer(double odometer) {
		this.odometer = odometer;
	}
	public String getRecordUUID() {
		return recordUUID;
	}
	public void setRecordUUID(String recordUUID) {
		this.recordUUID = recordUUID;
	}	
	
	
	
}

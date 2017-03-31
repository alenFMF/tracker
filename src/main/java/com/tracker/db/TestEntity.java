package com.tracker.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class TestEntity extends BaseEntity {
	
	@Column(length = 20)
	private String deviceId;
	
	@Column
	private Date timestamp;
	
	@Column
	private double latitude;
	
	@Column
	private double longitude;
	

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}

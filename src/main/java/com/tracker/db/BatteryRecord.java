package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class BatteryRecord extends BaseEntity {
	public Double batteryLevel;      
	public Boolean batteryCharging;
	public Double getBatteryLevel() {
		return batteryLevel;
	}
	public void setBatteryLevel(Double batteryLevel) {
		this.batteryLevel = batteryLevel;
	}
	public Boolean getBatteryCharging() {
		return batteryCharging;
	}
	public void setBatteryCharging(Boolean batteryCharging) {
		this.batteryCharging = batteryCharging;
	}
}

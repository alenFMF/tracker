package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class AltitudeRecord extends BaseEntity {
	public Double altitudeAccuracy;
	public Double altitude;
	public Double getAltitudeAccuracy() {
		return altitudeAccuracy;
	}
	public void setAltitudeAccuracy(Double altitudeAccuracy) {
		this.altitudeAccuracy = altitudeAccuracy;
	}
	public Double getAltitude() {
		return altitude;
	}
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}
	

}

package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class ActivityRecord extends BaseEntity {
	public Boolean isMoving;
	public Double odometer;
	public String activityType;
    public Integer activityConfidence;

	public Boolean getIsMoving() {
		return isMoving;
	}

	public void setIsMoving(Boolean isMoving) {
		this.isMoving = isMoving;
	}

	public Double getOdometer() {
		return odometer;
	}

	public void setOdometer(Double odometer) {
		this.odometer = odometer;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public Integer getActivityConfidence() {
		return activityConfidence;
	}

	public void setActivityConfidence(Integer activityConfidence) {
		this.activityConfidence = activityConfidence;
	}
    
}

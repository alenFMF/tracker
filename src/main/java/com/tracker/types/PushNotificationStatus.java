package com.tracker.types;

public enum PushNotificationStatus implements GoOptiTrackerEnum<PushNotificationStatus> {
	SENT,
	DELIEVERED,
	FAILED;
	
	@Override
	public String getName() {
		if (this == SENT) return "SENT";
		else if (this == DELIEVERED) return "DELIEVERED";
		else if (this == FAILED) return "FAILED";
		else return "Unknown";
	}

}
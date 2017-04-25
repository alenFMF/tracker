package com.tracker.apientities;

import java.util.Date;

public class APITrackQuery {
	public String token;  
	public String userId;  // one of userId or deviceId mandatory
	public String deviceId;
	public Date startDate;
	public Date endDate;
	public Double requiredAccuracy;
}

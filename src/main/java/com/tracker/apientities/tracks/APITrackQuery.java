package com.tracker.apientities.tracks;

import java.util.Date;
import java.util.List;

public class APITrackQuery {
	public String token;  
	// only one of lists can be used as a filter. Others null or empty
	public List<String> userIds;
	
    public String organizationGroup;  // query can be done for one organization group only. Mandatory.
	
	public Date startDate;
	public Date endDate;
	public Double requiredAccuracy;
	public Boolean singlePointStops;
}

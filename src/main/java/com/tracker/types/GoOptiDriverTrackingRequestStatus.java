package com.tracker.types;

/**
 *	Represents a tracking driver assignment status.
 *
 *	<p>
 *	Possible statuses are:
 *	<ul>
 *      <li> TRACKING_REQ_SENT: tracking request sent
 *      <li> TRACKING_REQ_ACCEPTED: tracking request accepted
 *  </ul>
 *  <p>
 */
public enum GoOptiDriverTrackingRequestStatus implements GoOptiTrackerEnum<GoOptiDriverTrackingRequestStatus> {
	
	TRACKING_REQ_SENT,
	TRACKING_REQ_ACCEPTED;
	
	@Override
	public String getName() {
		if (this == TRACKING_REQ_SENT) return "Tracking request sent";
		else if (this == TRACKING_REQ_ACCEPTED) return "Tracking request accepted";
		else return "Unknown";
	}

}

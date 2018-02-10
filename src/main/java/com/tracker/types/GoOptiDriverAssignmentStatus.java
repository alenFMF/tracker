package com.tracker.types;

/**
 *	Represents a driver assignment status.
 *
 *	<p>
 *	Possible statuses are:
 *	<ul>
 *	<li> NOT_CONTACTED: initial status
 *  	<li> CONTACTED: driver has been contacted
 * 	<li> APPROVED: driver approved (details not sent)
 * 	<li> APPROVED_LAST_MINUTE: driver approved (last minute order added to travel order)
 * 	<li> APPROVED_DETAILS_SENT: driver approved (details sent)
 *  	<li> REJECTED: driver rejected
 *  	<li> CANCEL: driver cancelled
 *  	<li> EXPIRED
 *  </ul>
 *  <p>
 */
public enum GoOptiDriverAssignmentStatus implements GoOptiTrackerEnum<GoOptiDriverAssignmentStatus> {
	
	NOT_CONTACTED,
	CONTACTED,
	APPROVED,
	APPROVED_LAST_MINUTE,
	APPROVED_DETAILS_SENT,
	REJECTED,
	CANCEL,
	EXPIRED;
	
	@Override
	public String getName() {
		if (this == NOT_CONTACTED) return "Not contacted";
		else if (this == CONTACTED) return "Contacted";
		else if (this == APPROVED) return "Approved (details not sent)";
		else if (this == APPROVED_LAST_MINUTE) return "Approved (last minute)";
		else if (this == APPROVED_DETAILS_SENT) return "Approved (details sent)";
		else if (this == REJECTED) return "Rejected";
		else if (this == CANCEL) return "Cancelled";
		else if (this == EXPIRED) return "Expired";
		else return "Unknown";
	}

}
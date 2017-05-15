package com.tracker.apientities.organizationgroup;

import java.util.Date;

public class APIVehicleGroupAssignment {
	public String vehicleId;
	public Date fromDate;   // if null, from now
	public Date untilDate; // if null, indefinitely. If entry exists
	public String type; //one of ALLOW, DENY. Sequence defines current privileges.
}

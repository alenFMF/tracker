package com.tracker.apientities.notifications;

import java.util.List;
import com.tracker.types.GoOptiDriverAssignmentStatus;

/**
 * Possible types:
 * NOTIFICATION - notification message 
 * START - start/resume tracking
 * END - stop/pause tracking
 * GROUP_NOTIFICATION - communication is in group context. Receiver can be empty
 * MOBILE_LOG - mobile log (stored on S3).
 *
 * Possible messageType:
 * TEXT 
 * HTML
 * FILE
 */ 

public class APISendNotification {
	public String token;
	public List<APIUsers> recipients;   // for type==NOTIFICATION only
	public String to;  // for type EMAIL only
	public String toGroup;  // relevant only if type==GROUP_NOTIFICATION
	public String fromGroup;  // sender must be admin of a group
	public String message;
	public String messageType;
	public String title;   // subject for email, title for notification
	public String type;
	public Integer travelOrderId;
	public Integer taskGoalId;
	public String senderNameToBeDisplayed;
	public String openPage;
	public Integer driverAssignmentId;
	public GoOptiDriverAssignmentStatus driverAssignmentStatus;
	public Boolean smsSuccessfullySentSimulatneously;
	public String franchiseUids;
	public String user;
	public String driverName;
	public String number;
	public Boolean sentToBackendTestEmail;
}

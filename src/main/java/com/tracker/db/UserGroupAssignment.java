package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class UserGroupAssignment extends BaseEntity {
	@ManyToOne
	public OrganizationGroup group;
	
	@ManyToOne
	public TrackingUser user;	
	
	@ManyToOne 
	public TrackingUser groupUser;  // initiator or confirmator/rejector from the group side.
	
	public Boolean accepted; // null - undefined, true accepted, false rejected.
	public Date fromDate;
	public Date untilDate; 
	public String groupRole; // USER, ADMIN
	public String inviteType; // USER, GROUP who invites
	public String grant; // ALLOW, DENY - if untilDate == null or < now, group has to agree.
	public Date timestamp; // if accept == true is set, otherwise not. 
	public Date userAction; 
	public Date groupAction;
	public String periodic;  // null default, DAILY, WEEKLY, MONTLY, YEARLY
	public Integer repeatTimes; // -1 means indefinitely
	
	public String getGroupRole() {
		return groupRole;
	}
	public void setGroupRole(String groupRole) {
		this.groupRole = groupRole;
	}
	public String getInviteType() {
		return inviteType;
	}
	public void setInviteType(String inviteType) {
		this.inviteType = inviteType;
	}
	public String getGrant() {
		return grant;
	}
	public void setGrant(String grant) {
		this.grant = grant;
	}
	public String getPeriodic() {
		return periodic;
	}
	public void setPeriodic(String periodic) {
		this.periodic = periodic;
	}
	public Integer getRepeatTimes() {
		return repeatTimes;
	}
	public void setRepeatTimes(Integer repeatTimes) {
		this.repeatTimes = repeatTimes;
	}	
	public OrganizationGroup getGroup() {
		return group;
	}
	public void setGroup(OrganizationGroup group) {
		this.group = group;
	}
	public TrackingUser getUser() {
		return user;
	}
	public void setUser(TrackingUser user) {
		this.user = user;
	}
	public Date getFromDate() {
		return fromDate;
	}
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	public Date getUntilDate() {
		return untilDate;
	}
	public void setUntilDate(Date untilDate) {
		this.untilDate = untilDate;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public TrackingUser getGroupUser() {
		return groupUser;
	}
	public void setGroupUser(TrackingUser groupUser) {
		this.groupUser = groupUser;
	}
	public Boolean getAccepted() {
		return accepted;
	}
	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}
	public Date getUserAction() {
		return userAction;
	}
	public void setUserAction(Date userAction) {
		this.userAction = userAction;
	}
	public Date getGroupAction() {
		return groupAction;
	}
	public void setGroupAction(Date groupAction) {
		this.groupAction = groupAction;
	}
	
}

//ADMIN - can manage group, can watch tracks of others, does not share his own track.
//USER - shares his track
// ALLOW
// - User offers sharing his data to group. If group is public, user is autoconfirmed if the role is USER. If the role is ADMIN, group admin has to confirm. Not posible for personal groups.
// If group is private, group admin must accept.
// - Group requires access to data. User has to confirm if USER. Automatic if ADMIN.



package com.tracker.db;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class OrganizationGroup extends BaseEntity  {

	@Column(unique = true) 
	public String groupId;
	public String description;
	public String creatorId;
	public Boolean privateGroup; // true (default) if inclusion has to be confirmed by group admin
	public String personalGroupUserId; // null except for personal groups

	public Date timestamp; // time of creation;

	@OneToMany(mappedBy = "group")
	List<UserGroupAssignment> userAssignments;
	
	@OneToMany(mappedBy = "group")
	List<VehicleGroupAssignment> vehicleAssignments;
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public List<VehicleGroupAssignment> getVehicleAssignments() {
		return vehicleAssignments;
	}

	public void setVehicleAssignments(List<VehicleGroupAssignment> vehicleAssignments) {
		this.vehicleAssignments = vehicleAssignments;
	}

	public List<UserGroupAssignment> getUserAssignments() {
		return userAssignments;
	}

	public void setUserAssignments(List<UserGroupAssignment> userAssignments) {
		this.userAssignments = userAssignments;
	}

	public Boolean getPrivateGroup() {
		return privateGroup;
	}

	public void setPrivateGroup(Boolean privateGroup) {
		this.privateGroup = privateGroup;
	}

	public String getPersonalGroupUserId() {
		return personalGroupUserId;
	}

	public void setPersonalGroupUserId(String personalGroupUserId) {
		this.personalGroupUserId = personalGroupUserId;
	}
	

}

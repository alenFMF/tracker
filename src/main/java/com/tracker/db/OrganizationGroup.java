package com.tracker.db;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class OrganizationGroup extends BaseEntity  {

	@Column(unique = true) 
	public String groupId;
	public String description;

	@ManyToOne
	public TrackingUser creator;
	
	public Boolean privateGroup; // true (default) if inclusion has to be confirmed by group admin

	@OneToOne
	public TrackingUser personalGroupUser;
	
	public String authenticationProvider; 
	public String providerParentGroupId;
	
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

	public TrackingUser getCreator() {
		return creator;
	}

	public void setCreator(TrackingUser creator) {
		this.creator = creator;
	}

	public TrackingUser getPersonalGroupUser() {
		return personalGroupUser;
	}

	public void setPersonalGroupUser(TrackingUser personalGroupUser) {
		this.personalGroupUser = personalGroupUser;
	}
	
	public String getAuthenticationProvider() {
		return authenticationProvider;
	}

	public void setAuthenticationProvider(String authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}

	public String getProviderParentGroupId() {
		return providerParentGroupId;
	}

	public void setProviderParentGroupId(String providerParentGroupId) {
		this.providerParentGroupId = providerParentGroupId;
	}

	public OrganizationGroup() {
		super();
		this.privateGroup = false;
	}
}

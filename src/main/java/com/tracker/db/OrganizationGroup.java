package com.tracker.db;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
	
	public String provider; 
	public String providerParentGroupId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	public OrganizationGroup parentProviderGroup;
	
	@OneToMany(mappedBy = "parentProviderGroup")
	public List<OrganizationGroup> childProviderGroups;
	
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
	
	public String getProvider() {
		return provider;
	}

	public void setProvider(String authenticationProvider) {
		this.provider = authenticationProvider;
	}

	public String getProviderParentGroupId() {
		return providerParentGroupId;
	}

	public void setProviderParentGroupId(String providerParentGroupId) {
		this.providerParentGroupId = providerParentGroupId;
	}

	public OrganizationGroup getParentProviderGroup() {
		return parentProviderGroup;
	}

	public void setParentProviderGroup(OrganizationGroup parentProviderGroup) {
		this.parentProviderGroup = parentProviderGroup;
	}

	public List<OrganizationGroup> getChildProviderGroups() {
		return childProviderGroups;
	}

	public void setChildProviderGroups(List<OrganizationGroup> childProviderGroups) {
		this.childProviderGroups = childProviderGroups;
	}

	public OrganizationGroup() {
		super();
		this.privateGroup = false;
	}
	
}

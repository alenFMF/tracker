package com.tracker.db;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class OrganizationGroup {
	@Column(unique = true) 
	public String groupId;
	public String description;
	
	@OneToMany
	List<VehicleGroupAssignment> vehicleAssignments;
	
	@OneToMany
	List<UserGroupAssignment> userAssignments;
}

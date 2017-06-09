package com.tracker.db;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class TravelOrder extends BaseEntity {
	public String travelOrderId;
	
	public String description;
	
	@OneToMany(mappedBy="user")
	public List<DriverAssignment> driverAssignments;
	
	@ManyToOne
	public Vehicle vehicle;
	
	@OneToMany(mappedBy="travelOrder")
	public List<TaskGoal> taskGoals;
	
	@OneToMany(mappedBy="travelOrder")
	@OrderBy("timestamp")
	public List<EventMessage> events;

	public String getTravelOrderId() {
		return travelOrderId;
	}

	public void setTravelOrderId(String travelOrderId) {
		this.travelOrderId = travelOrderId;
	}

	public List<DriverAssignment> getDriverAssignments() {
		return driverAssignments;
	}

	public void setDriverAssignments(List<DriverAssignment> driverAssignments) {
		this.driverAssignments = driverAssignments;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public List<TaskGoal> getTaskGoals() {
		return taskGoals;
	}

	public void setTaskGoals(List<TaskGoal> taskGoals) {
		this.taskGoals = taskGoals;
	}

	public List<EventMessage> getEvents() {
		return events;
	}

	public void setEvents(List<EventMessage> events) {
		this.events = events;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}

package com.tracker.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.user.APIUserUpdateResponse;
import com.tracker.apientities.vehicle.APIVehicleLinkList;
import com.tracker.apientities.vehicle.APIVehicleLinkListResponse;
import com.tracker.apientities.vehicle.APIVehicleLinkRegister;
import com.tracker.apientities.vehicle.APIVehicleLinkRegisterResponse;
import com.tracker.apientities.vehicle.APIVehicleListEntry;
import com.tracker.apientities.vehicle.APIVehicleProfile;
import com.tracker.apientities.vehicle.APIVehicleProfileResponse;
import com.tracker.apientities.vehicle.APIVehicleQuery;
import com.tracker.apientities.vehicle.APIVehicleQueryResponse;
import com.tracker.apientities.vehicle.APIVehicleRegister;
import com.tracker.apientities.vehicle.APIVehicleUpdate;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingUser;
import com.tracker.db.Vehicle;
import com.tracker.db.VehicleGroupAssignment;
import com.tracker.utils.SessionKeeper;

@Component
public class VehicleEngine {
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private AuthenticationEngine authEngine;
	
	public APIBaseResponse register(APIVehicleRegister req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			Vehicle vehicle = (Vehicle)sk.createCriteria(Vehicle.class).add(Restrictions.eq("vehicleId", req.vehicleId)).uniqueResult();
			if(vehicle == null) {
				if (req.groupId == null) {
					return new APIBaseResponse("GROUP_IS_NULL", "");
				}
				OrganizationGroup group = (OrganizationGroup)sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("groupId", req.groupId)).uniqueResult();
				if(group == null){
					return new APIBaseResponse("NO_SUCH_GROUP","");
				}
				else{vehicle = new Vehicle(req.vehicleId, req.description);
				vehicle.setGroupId(group);
				sk.save(vehicle);
				sk.commit();
				return new APIBaseResponse();}
			}
			return new APIBaseResponse("VEHICLE_EXISTS", "");
		}	
	}	
	
	public APIBaseResponse update(APIVehicleUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			Vehicle vehicle = (Vehicle)sk.createCriteria(Vehicle.class).add(Restrictions.eq("vehicleId", req.vehicleId)).uniqueResult();
			if(vehicle == null) {		
				return new APIBaseResponse("NO_SUCH_VEHICLE", "");
			}			
			vehicle.setDescription(req.description);
			sk.saveOrUpdate(vehicle);	
			sk.commit();
		}				
		return new APIBaseResponse();
	}
	public APIVehicleProfileResponse vehicleProfile(APIVehicleProfile req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			Vehicle vehicle = (Vehicle)sk.createCriteria(Vehicle.class).add(Restrictions.eq("vehicleId", req.vehicleId)).uniqueResult();
			if(vehicle == null) {		
				return new APIVehicleProfileResponse("NO_SUCH_VEHICLE", "");
			}
			Criteria c = sk.createCriteria(VehicleGroupAssignment.class);		
			List<VehicleGroupAssignment> recs = c.list();	
			
			List<String> groups = new ArrayList<String>();
			for (VehicleGroupAssignment g : recs){
				if (g.vehicle == vehicle){
					groups.add(g.group.getGroupId());
				}
			}
			APIVehicleProfileResponse profile = new APIVehicleProfileResponse(vehicle.description,vehicle.getGroupId().groupId, groups);
			return profile;
		}				
	}
	
	@SuppressWarnings("unchecked")
	public APIVehicleLinkRegisterResponse vehicleGroupRegister(APIVehicleLinkRegister req){
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {		

			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIVehicleLinkRegisterResponse("AUTH_ERROR", "");
			}	
			Vehicle vehicle = (Vehicle)sk.createCriteria(Vehicle.class).add(Restrictions.eq("vehicleId", req.vehicleId)).uniqueResult();
			if(vehicle == null) {		
				return new APIVehicleLinkRegisterResponse("NO_SUCH_VEHICLE", "");
			}
			OrganizationGroup group = (OrganizationGroup)sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("groupId", req.groupId)).uniqueResult();
			if (group == null){
				return new APIVehicleLinkRegisterResponse("NO_SUCH_GROUP", "");
			}
			if (vehicle.groupId == group){
				return new APIVehicleLinkRegisterResponse("IS_ALREADY_IN_GROUP", "");
			}
			if (vehicle.groupId.creator == tokenUser){
				VehicleGroupAssignment vgassignment = new VehicleGroupAssignment();
				vgassignment.group = group;
				vgassignment.vehicle = vehicle;
				vgassignment.fromDate = req.fromDate;
				vgassignment.untilDate = req.untilDate;
				if (authEngine.isAdmin(sk, req.token, req.groupId)){
					vgassignment.pending = false;
					sk.save(vgassignment);
					sk.commit();
					return new APIVehicleLinkRegisterResponse("CONFIRMED", "");
				}else{
					vgassignment.pending = true;
					sk.save(vgassignment);
					sk.commit();
					return new APIVehicleLinkRegisterResponse("PENDING", "");
				}
			}else{
				return new APIVehicleLinkRegisterResponse("NOT_VALID_ADMIN", "");
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public APIVehicleLinkListResponse listAssignments(APIVehicleLinkList req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)){
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIVehicleLinkListResponse("AUTH_ERROR", "");
			}
			OrganizationGroup group = (OrganizationGroup)sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("groupId", req.groupId)).uniqueResult();
			if (group == null){
				return new APIVehicleLinkListResponse("NO_SUCH_GROUP", "");
			}
			if (tokenUser == group.creator){
				Criteria c = sk.createCriteria(VehicleGroupAssignment.class);		
				List<VehicleGroupAssignment> recs = c.list();	
				List<VehicleGroupAssignment> assign = new ArrayList<VehicleGroupAssignment>();
				for (VehicleGroupAssignment g : recs){
					if (g.group == group){
						assign.add(g);
					}
				}
				return new APIVehicleLinkListResponse(assign);
			}else{
				return new APIVehicleLinkListResponse("NOT_AUTHORISED", "");
			}
			
			
		}
		
	}	
	
	@SuppressWarnings("unchecked")
	public APIVehicleQueryResponse listVehicles(APIVehicleQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			Criteria c = sk.createCriteria(Vehicle.class);		
			List<Vehicle> recs = c.list();	
			List<APIVehicleListEntry> vehicles = recs.stream()
				.map(x -> new APIVehicleListEntry(x.getVehicleId(), x.getDescription()))
				.collect(Collectors.toList());
			APIVehicleQueryResponse res = new APIVehicleQueryResponse(vehicles);
			return res;
		}
	}

	
}

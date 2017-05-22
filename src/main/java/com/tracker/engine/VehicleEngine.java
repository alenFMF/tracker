package com.tracker.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.vehicle.APIVehicleListEntry;
import com.tracker.apientities.vehicle.APIVehicleProfile;
import com.tracker.apientities.vehicle.APIVehicleProfileResponse;
import com.tracker.apientities.vehicle.APIVehicleQuery;
import com.tracker.apientities.vehicle.APIVehicleQueryResponse;
import com.tracker.apientities.vehicle.APIVehicleRegister;
import com.tracker.apientities.vehicle.APIVehicleUpdate;
import com.tracker.db.Vehicle;
import com.tracker.db.VehicleGroupAssignment;
import com.tracker.utils.SessionKeeper;

@Component
public class VehicleEngine {
	@Autowired
	private SessionFactory sessionFactory;
	
	public APIBaseResponse register(APIVehicleRegister req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {		
			Vehicle vehicle = (Vehicle)sk.createCriteria(Vehicle.class).add(Restrictions.eq("vehicleId", req.vehicleId)).uniqueResult();
			if(vehicle == null) {
				vehicle = new Vehicle(req.vehicleId, req.description);
				sk.save(vehicle);
				sk.commit();
				return new APIBaseResponse();
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
			
			//Trenutno ne dela pravilno
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

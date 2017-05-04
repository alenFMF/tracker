package com.tracker.engine;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.vehicle.APIVehicleListEntry;
import com.tracker.apientities.vehicle.APIVehicleQuery;
import com.tracker.apientities.vehicle.APIVehicleQueryResponse;
import com.tracker.apientities.vehicle.APIVehicleRegister;
import com.tracker.apientities.vehicle.APIVehicleUpdate;
import com.tracker.db.Vehicle;
import com.tracker.utils.SessionKeeper;

@Component
public class GroupEngine {
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

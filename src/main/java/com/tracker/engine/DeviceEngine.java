package com.tracker.engine;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.devices.APIDevice;
import com.tracker.apientities.devices.APIDeviceRegister;
import com.tracker.apientities.devices.APIDeviceUpdate;
import com.tracker.apientities.devices.APIDeviceQuery;
import com.tracker.apientities.devices.APIDeviceResponse;
import com.tracker.db.DeviceRecord;
import com.tracker.db.Vehicle;
import com.tracker.utils.SessionKeeper;

@Component
public class DeviceEngine {
	@Autowired
	private SessionFactory sessionFactory;
	
	public APIBaseResponse register(APIDeviceRegister req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {		
			DeviceRecord device = (DeviceRecord)sk.createCriteria(Vehicle.class).add(Restrictions.eq("uuid", req.device.uuid)).uniqueResult();
			if(device == null) {
				device = new DeviceRecord(req.device.uuid, req.device.manufacturer, req.device.model, req.device.version, req.device.platform);
				sk.save(device);
				sk.commit();
				return new APIBaseResponse();
			}
			return new APIBaseResponse("DEVICE_EXISTS", "");
		}		
	}	
	
	public APIBaseResponse update(APIDeviceUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			DeviceRecord device = (DeviceRecord)sk.createCriteria(Vehicle.class).add(Restrictions.eq("uuid", req.device.uuid)).uniqueResult();
			if(device == null) {		
				return new APIBaseResponse("NO_SUCH_DEVICE", "");
			}		
			device.setManufacturer(req.device.manufacturer);
			device.setModel(req.device.model);
			device.setVersion(req.device.version);
			device.setPlatform(req.device.platform);
			sk.saveOrUpdate(device);	
			sk.commit();
		}				
		return new APIBaseResponse();
	}
	
	@SuppressWarnings("unchecked")
	public APIDeviceResponse list(APIDeviceQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {						
			Criteria c = sk.createCriteria(DeviceRecord.class);						
			List<DeviceRecord> recs = c.list();	
			List<APIDevice> devList = recs.stream()
				.map(x -> new APIDevice(x.getUuid(), x.getManufacturer(), x.getModel(), x.getVersion(), x.getPlatform()))
				.collect(Collectors.toList());
			APIDeviceResponse res = new APIDeviceResponse(devList);
			res.devices = devList;
			return res;
		}
	}	
	
}

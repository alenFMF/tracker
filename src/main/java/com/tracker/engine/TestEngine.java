package com.tracker.engine;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIGPSLocation;
import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.apientities.APITrackerPost;
import com.tracker.db.GPSRecord;
import com.tracker.db.TestEntity;
import com.tracker.utils.SessionKeeper;

@Component
public class TestEngine {

	@Autowired
	private SessionFactory sessionFactory;

	public APITest2 handleService2(APITest1 req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TestEntity e = new TestEntity();
			
			e.setDeviceId(req.deviceId);
			e.setTimestamp(req.timestamp);
			e.setLatitude(req.latitude);
			e.setLatitude(req.longitude);
			sk.save(e);
			sk.commit();
			
			return new APITest2("SUCCESS");
		} catch (Throwable t) {
			return new APITest2("ERROR");
		}	
	}

	public APIBaseResponse handleTrackerPost(APITrackerPost req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			for (APIGPSLocation loc : req.location) {
				GPSRecord r = new GPSRecord();
				r.setSpeed(loc.coords.speed);
				r.setLongitude(loc.coords.longitude);
				r.setLatitude(loc.coords.latitude);
				r.setAccuracy(loc.coords.accuracy);
				r.setAltitude(loc.coords.altitude);
				r.setAltitude_accuracy(loc.coords.altitude_accuracy);
				r.setHeading(loc.coords.heading);
				r.setMoving(loc.is_moving);
				r.setOdometer(loc.odometer);
				r.setRecordUUID(loc.uuid);
				r.setActivityType(loc.activity.type);
				r.setActivityConfidence(loc.activity.confidence);
				r.setBatteryLevel(loc.battery.level);
				r.setBatteryCharging(loc.battery.is_charging);
				r.setTimestamp(loc.timestamp);
				r.setDeviceUUID(req.device.uuid);
				r.setDeviceManufacturer(req.device.manufacturer);
				r.setDeviceModel(req.device.model);
				r.setDeviceVersion(req.device.version);
				r.setDevicePlatform(req.device.platform);
				sk.save(r);
			}
			sk.commit();			
		}
		return new APIBaseResponse();
	}
}

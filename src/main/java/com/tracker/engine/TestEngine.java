package com.tracker.engine;

import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIGPSLocation;
import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.apientities.APITrackQuery;
import com.tracker.apientities.APITrackQueryResponse;
import com.tracker.apientities.APITrackSample;
import com.tracker.apientities.APITrackerPost;
import com.tracker.db.ActivityRecord;
import com.tracker.db.AltitudeRecord;
import com.tracker.db.BatteryRecord;
import com.tracker.db.DeviceRecord;
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
				r.setTimestamp(loc.timestamp);
				r.setSpeed(loc.coords.speed);
				r.setLongitude(loc.coords.longitude);
				r.setLatitude(loc.coords.latitude);
				r.setAccuracy(loc.coords.accuracy);
				r.setHeading(loc.coords.heading);
				if(loc.coords.altitude != null) {
					AltitudeRecord ar = new AltitudeRecord();
					ar.setAltitude(loc.coords.altitude);
					ar.setAltitudeAccuracy(loc.coords.altitude_accuracy);
					r.setAltitude(ar);
					sk.save(ar);
				}
				if(loc.activity != null || loc.is_moving != null || loc.odometer != null) {
					ActivityRecord acr = new ActivityRecord();
					if(loc.activity != null) {
						acr.setActivityType(loc.activity.type);
						acr.setActivityConfidence(loc.activity.confidence);
					}
					acr.setIsMoving(loc.is_moving);
					acr.setOdometer(loc.odometer);
					sk.save(acr);
				}
//				r.setRecordUUID(loc.uuid);
				
				if(loc.battery != null) {
					BatteryRecord bat = new BatteryRecord();
					bat.setBatteryCharging(loc.battery.is_charging);
					bat.setBatteryLevel(loc.battery.level);
					sk.save(bat);
				}
				if(req.device != null) {
					DeviceRecord drec = (DeviceRecord)sk.createCriteria(DeviceRecord.class).add(Restrictions.eq("uuid", req.device.uuid)).uniqueResult();
					
					if(drec == null) {
						drec = new DeviceRecord();
					}
					drec.setUuid(req.device.uuid);
					drec.setManufacturer(req.device.manufacturer);
					drec.setModel(req.device.model);
					drec.setPlatform(req.device.platform);
					sk.saveOrUpdate(drec);
				}
				sk.save(r);
			}
			sk.commit();			
		}
		return new APIBaseResponse();
	}

	@SuppressWarnings("unchecked")
	public APITrackQueryResponse handleTrackerQuery(APITrackQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {			
			List<GPSRecord> recs = sk.createCriteria(GPSRecord.class).add(Restrictions.ge("timestamp", req.startDate)).add(Restrictions.le("timestamp", req.endDate)).list();	
			List<APITrackSample> samples = recs.stream()
				.map(x -> new APITrackSample(x))
				.collect(Collectors.toList());
			APITrackQueryResponse res = new APITrackQueryResponse("OK", "");
			res.samples = samples;
			res.deviceUuid = "neki";
			res.userId = "username";
			return res;
		}
	}
}

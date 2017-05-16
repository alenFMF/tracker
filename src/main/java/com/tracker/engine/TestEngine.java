package com.tracker.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.apientities.devices.APIDeviceQuery;
import com.tracker.apientities.devices.APIDeviceResponse;
import com.tracker.apientities.tracks.APIGPSLocation;
import com.tracker.apientities.tracks.APITrackDetail;
import com.tracker.apientities.tracks.APITrackQuery;
import com.tracker.apientities.tracks.APITrackQueryResponse;
import com.tracker.apientities.tracks.APITrackSample;
import com.tracker.apientities.tracks.APITrackerPost;
import com.tracker.db.ActivityRecord;
import com.tracker.db.AltitudeRecord;
import com.tracker.db.BatteryRecord;
import com.tracker.db.DeviceRecord;
import com.tracker.db.GPSRecord;
import com.tracker.db.TestEntity;
import com.tracker.db.TrackingUser;
import com.tracker.utils.SessionKeeper;

@Component
public class TestEngine {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired 
	private AuthenticationEngine authEngine;	

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

	public APIBaseResponse handleTrackingPost(String userSecret, APITrackerPost req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser user = (TrackingUser)sk.createCriteria(TrackingUser.class).add(Restrictions.eq("postingSecret", userSecret)).uniqueResult();
			if(user == null) {
				return null;
			}			
			for (APIGPSLocation loc : req.location) {
				GPSRecord r = new GPSRecord();
				r.setUser(user);
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
					r.setDevice(drec);
					
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
			if(req.token == null) {
				return new APITrackQueryResponse("AUTH_TOKEN_MISSING", "");
			}			
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APITrackQueryResponse("AUTH_ERROR", "");
			}
			Criteria c = sk.createCriteria(GPSRecord.class);
			c.createAlias("device", "Device");
			c.createAlias("user", "User");
			
			if(tokenUser.getAdmin()) {
				if (req.userIds != null && !req.userIds.isEmpty()) {
					c.add(Restrictions.in("User.userId", req.userIds));		
				} else {  // get admins track
					c.add(Restrictions.eq("User.userId", tokenUser.getUserId()));
				}		
			} else {
				c.add(Restrictions.eq("User.userId", tokenUser.getUserId()));
			}
//			if (req.deviceIds != null && !req.deviceIds.isEmpty()) {
//				c.add(Restrictions.in("Device.uuid", req.deviceIds));
//			} 
//			if (req.userIds != null && !req.userIds.isEmpty()) {
//				c.createAlias("user", "User");
//				c.add(Restrictions.in("User.userId", req.userIds));
//				criteriaCount++;
//			}

//			if (req.organizationGroup != null) {
//				c.createAlias("user", "User");
//				c.add(Restrictions.in("User.userId", req.userIds));	
//				criteriaCount++;
//			}
			
			
			if(req.requiredAccuracy != null && req.requiredAccuracy > 0) {
				c.add(Restrictions.le("accuracy", req.requiredAccuracy));
			}
			
			c.add(Restrictions.ge("timestamp", req.startDate))
			 .add(Restrictions.le("timestamp", req.endDate));
			
			c.setProjection( Projections.projectionList()
			        .add( Projections.property("timestamp"), "timestamp" )
			        .add( Projections.property("longitude"), "longitude" )
			        .add( Projections.property("latitude"), "latitude" )
			        .add( Projections.property("speed"), "speed" )
			        .add( Projections.property("heading"), "heading" )
			        .add( Projections.property("Device.uuid"), "deviceId" )
			        .add( Projections.property("User.userId"), "userId" )
			    );
			
			c.addOrder(Order.asc("timestamp"));
			c.setResultTransformer(Transformers.aliasToBean(TableSample.class));
			
			List<TableSample> records = c.list();	
			Map<Pair<String, String>, List<TableSample>> sampleMap = (Map<Pair<String, String>, List<TableSample>>)records.stream()
				.collect(Collectors.groupingBy(x -> Pair.of(x.userId, x.deviceId)));
			
			APITrackQueryResponse res = new APITrackQueryResponse("OK", "");
			
			List<APITrackDetail> trackList = new ArrayList<APITrackDetail>();
			
			for(Map.Entry<Pair<String, String>, List<TableSample>> e: sampleMap.entrySet()) {
				APITrackDetail det = new APITrackDetail();
				det.userId = e.getKey().getLeft();
				det.deviceUuid = e.getKey().getRight();
				det.samples = e.getValue()
								.stream()
								.map(el -> 
								    new APITrackSample(el.timestamp, el.longitude, el.latitude, el.speed, el.heading,null))
								.collect(Collectors.toList());
				
				trackList.add(det);
			}
			res.tracks = trackList;			
			return res;
		}
	}
	
	
	//Table classes

	public static class TableSample {
		public Date timestamp;
		public double longitude;
		public double latitude;
		public double speed;
		public double heading;	
		public int stopDuration; // minutes
		public String deviceId;
		public String userId;
	}
}

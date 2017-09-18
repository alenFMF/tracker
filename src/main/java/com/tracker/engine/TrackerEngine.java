package com.tracker.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.apientities.tracks.APICoords2;
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
import com.tracker.db.NotificationRegistration;
import com.tracker.db.TestEntity;
import com.tracker.db.TrackingUser;
import com.tracker.utils.SessionKeeper;

@Component
public class TrackerEngine {

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
			Date now = new Date();
			GPSRecord lastRecord = user.getLastRecord();
			for (APIGPSLocation loc : req.location) {
				GPSRecord r = new GPSRecord();
				r.setUser(user);
				r.setTimestamp(loc.timestamp);
				r.setSpeed(loc.coords.speed);
				r.setLongitude(loc.coords.longitude);
				r.setLatitude(loc.coords.latitude);
				r.setAccuracy(loc.coords.accuracy);
				r.setHeading(loc.coords.heading);
				r.setRecorded(now);
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
					r.setActivity(acr);
					sk.save(acr);
				}
//				r.setRecordUUID(loc.uuid);
				
				if(loc.battery != null) {
					BatteryRecord bat = new BatteryRecord();
					bat.setBatteryCharging(loc.battery.is_charging);
					bat.setBatteryLevel(loc.battery.level);
					r.setBattery(bat);
					sk.save(bat);
				}
				if(req.device != null) {
					DeviceRecord drec = (DeviceRecord)sk.createCriteria(DeviceRecord.class).add(Restrictions.eq("uuid", req.device.uuid)).uniqueResult();
					
					if(drec == null) {
						drec = new DeviceRecord();
						drec.setUuid(req.device.uuid);
						drec.setManufacturer(req.device.manufacturer);
						drec.setModel(req.device.model);
						drec.setPlatform(req.device.platform);		
						sk.save(drec);
					}
					r.setDevice(drec);					
				}
				if(lastRecord == null) {
					lastRecord = r;
				} else {
					lastRecord = lastRecord.getTimestamp().before(r.getTimestamp()) ? r : lastRecord;
				}				
				sk.save(r);
			}
			user.setLastRecord(lastRecord);
			sk.saveOrUpdate(user);
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
			
			Criteria c = null;
			boolean isPersonalQuery = true;
			if(req.lastPositionsOnly == null || req.lastPositionsOnly == false) {  // full query
				c = sk.createCriteria(GPSRecord.class);
				c.createAlias("device", "Device");
				c.createAlias("user", "User");
				c.createAlias("battery", "Battery");
						
				if (req.userIds != null && !req.userIds.isEmpty() && (tokenUser.getProvider() != null || tokenUser.getAdmin())) {
					c.add(Restrictions.in("User.userId", req.userIds));		
					isPersonalQuery = false;
				} else {  // get admins track
					c.add(Restrictions.eq("User.userId", tokenUser.getUserId()));
					isPersonalQuery = true;
				}		
							
				if(req.requiredAccuracy != null && req.requiredAccuracy > 0) {
					c.add(Restrictions.le("accuracy", req.requiredAccuracy));
				}
				

				c.add(Restrictions.ge("timestamp", req.startDate))
				 .add(Restrictions.le("timestamp", req.endDate));
				
				c.setProjection( Projections.projectionList()
				        .add( Projections.property("timestamp"), "timestamp" )
				        .add( Projections.property("recorded"), "recorded" )
				        .add( Projections.property("longitude"), "longitude" )
				        .add( Projections.property("latitude"), "latitude" )
				        .add( Projections.property("speed"), "speed" )
				        .add( Projections.property("heading"), "heading" )
				        .add( Projections.property("Battery.batteryLevel"), "batteryLevel")
				        .add( Projections.property("Device.uuid"), "deviceId" )
				        .add( Projections.property("User.userId"), "userId" )				        
				    );
				
				c.addOrder(Order.asc("timestamp"));
				c.setResultTransformer(Transformers.aliasToBean(TableSample.class));
			} else {  // lastPositions only
				isPersonalQuery = false; 
				c = sk.createCriteria(TrackingUser.class, "User");
				c.createAlias("lastRecord", "Record");
				c.createAlias("Record.device", "Device");
				c.createAlias("Record.battery", "Battery");		
							
				if(req.requiredAccuracy != null && req.requiredAccuracy > 0) {
					c.add(Restrictions.le("accuracy", req.requiredAccuracy));
				}
				
				c.add(Restrictions.isNotNull("User.lastRecord"));
				
				c.setProjection( Projections.projectionList()
				        .add( Projections.property("Record.timestamp"), "timestamp" )
				        .add( Projections.property("Record.recorded"), "recorded" )
				        .add( Projections.property("Record.longitude"), "longitude" )
				        .add( Projections.property("Record.latitude"), "latitude" )
				        .add( Projections.property("Record.speed"), "speed" )
				        .add( Projections.property("Record.heading"), "heading" )
				        .add( Projections.property("Battery.batteryLevel"), "batteryLevel" )
				        .add( Projections.property("Device.uuid"), "deviceId" )
				        .add( Projections.property("User.userId"), "userId" )
				    );
				
				c.setResultTransformer(Transformers.aliasToBean(TableSample.class));				
			}
			
			List<TableSample> records = c.list();	
			
			Map<Pair<String, String>, List<TableSample>> sampleMap = (Map<Pair<String, String>, List<TableSample>>)records.stream()
				.collect(Collectors.groupingBy(x -> Pair.of(x.userId, x.deviceId)));
			
			APITrackQueryResponse res = new APITrackQueryResponse("OK", "");
			
			List<APITrackDetail> trackList = new ArrayList<APITrackDetail>();
			
			Date now = new Date();
			
			// to bi moralo biti Map<String, List<Interval>>, preslikava user -> dovoljeni intervali
			Map<String, Boolean> allowForUsers = new HashMap<String, Boolean>();
			if(isPersonalQuery) {
				allowForUsers.put(tokenUser.getUserId(), true);  // dodati cel interval
			} else {
				if(tokenUser.getAdmin()) {
					allowForUsers = null; // indicates no conditions
				} else {
					// ta mora vrniti Map<String, List<Interval>>
					Map<String, String> tmpMap = GroupEngine.allowedUsersForAdminToSee(sk, tokenUser, req.userIds, now);
					for(String key: tmpMap.keySet()) {
						allowForUsers.put(key, true);
					}
					// dodati cel interval
					allowForUsers.put(tokenUser.getUserId(), true); // always allow for self
				}
			}
			
			for(Map.Entry<Pair<String, String>, List<TableSample>> e: sampleMap.entrySet()) {
				if(allowForUsers != null) {
					Boolean isOk = allowForUsers.get(e.getKey().getLeft());
					if (isOk == null || isOk == false) continue;
				}
				APITrackDetail det = new APITrackDetail();
				det.userId = e.getKey().getLeft();
				det.deviceUuid = e.getKey().getRight();
				// tukaj bo for zanka ki bo filtrirala po intervalih
				det.samples = e.getValue()
								.stream()
								.map(el -> 
								    new APITrackSample(el.timestamp, el.recorded, el.longitude, el.latitude, el.speed, el.heading, null, el.batteryLevel))
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
		public Date recorded;
		public double longitude;
		public double latitude;
		public double speed;
		public double heading;	
		public int stopDuration; // minutes
		public Double batteryLevel;
		public String deviceId;
		public String userId;
	}


	public APIBaseResponse handleShortPost(String userSecret, List<APICoords2> req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser user = (TrackingUser)sk.createCriteria(TrackingUser.class).add(Restrictions.eq("postingSecret", userSecret)).uniqueResult();
			if(user == null) return null;
			NotificationRegistration regis = user.getPrimaryNotificationDevice();
			if(regis == null) return null;
			DeviceRecord drec = regis.getDevice();
			if(drec == null) return null;
			Date now = new Date();
			for (APICoords2 loc : req) {
				GPSRecord r = new GPSRecord();
				r.setUser(user);
				r.setTimestamp(loc.time);
				r.setSpeed(loc.speed);
				r.setLongitude(loc.lon);
				r.setLatitude(loc.lat);
				r.setHeading(loc.head);
				r.setAccuracy(loc.accur);
				r.setRecorded(now);
				if(loc.alt != null) {
					AltitudeRecord ar = new AltitudeRecord();
					ar.setAltitude(loc.alt);
					r.setAltitude(ar);
					sk.save(ar);
				}
				
				r.setDevice(drec);	
				if(loc.batLev != null) {
					BatteryRecord bat = new BatteryRecord();
					bat.setBatteryCharging(loc.batChg);
					bat.setBatteryLevel(loc.batLev);
					r.setBattery(bat);
					sk.save(bat);					
				}
				sk.save(r);
			}
			sk.commit();			
		}
		return new APIBaseResponse();
	}

//	public APITrackQueryResponse lastPositions(APILastRecordQuery req) {
//		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
//			if(req.token == null) {
//				return new APITrackQueryResponse("AUTH_TOKEN_MISSING", "");
//			}			
//			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
//			if(tokenUser == null) {
//				return new APITrackQueryResponse("AUTH_ERROR", "");
//			}		
//			Date now = new Date();
//			Map<String, String> tmpMap = GroupEngine.allowedUsersForAdminToSee(sk, tokenUser, null, now);
//			List<String> allowedUsers = new LinkedList<String>(tmpMap.keySet());
//			return null;
//		}
//	}
}

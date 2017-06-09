package com.tracker.engine;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.notifications.APIDevice;
import com.tracker.apientities.notifications.APIDeviceQuery;
import com.tracker.apientities.notifications.APIDeviceRegister;
import com.tracker.apientities.notifications.APIDeviceResponse;
import com.tracker.apientities.notifications.APIDeviceUpdate;
import com.tracker.apientities.notifications.APINotificationMessage;
import com.tracker.apientities.notifications.APINotificationStatus;
import com.tracker.apientities.notifications.APINotifications;
import com.tracker.apientities.notifications.APINotificationsResponse;
import com.tracker.apientities.notifications.APISendNotification;
import com.tracker.apientities.notifications.APISendNotificationResponse;
import com.tracker.apientities.notifications.APIUsers;
import com.tracker.db.DeviceRecord;
import com.tracker.db.EventMessage;
import com.tracker.db.MessageBody;
import com.tracker.db.NotificationRegistration;
import com.tracker.db.TrackingUser;
import com.tracker.db.Vehicle;
import com.tracker.utils.SessionKeeper;

@Component
public class NotificationEngine {
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired 
	private AuthenticationEngine authEngine;		
	
	@Autowired
	private NotificationService notificationService;
	
	public APIBaseResponse register(APIDeviceRegister req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIBaseResponse("AUTH_ERROR", "");
			}					
			if(req.notificationToken == null) {
				return new APIBaseResponse("NO_NOTIFICATON_TOKEN","");
			}
			String platform = req.device.platform;
			if(!platform.equals("iOS") && !platform.equals("Android")) {
				return new APIBaseResponse("WRONG_PLATFORM", platform != null ? platform : "null");
			}
			Date now = new Date();
			DeviceRecord device = (DeviceRecord)sk.createCriteria(Vehicle.class).add(Restrictions.eq("uuid", req.device.uuid)).uniqueResult();
			// update device
			NotificationRegistration regtoken = null;
			if(device == null) {
				device = new DeviceRecord(req.device.uuid, req.device.manufacturer, req.device.model, req.device.version, req.device.platform);
				regtoken = new NotificationRegistration(tokenUser, device, platform, req.notificationToken, now);				
				sk.save(device);
				sk.save(regtoken);
			} else {
				regtoken = (NotificationRegistration) sk.createCriteria(NotificationRegistration.class)
														.add(Restrictions.eq("user", tokenUser))
														.add(Restrictions.eq("device", device))
														.uniqueResult();
				if(regtoken != null && !regtoken.notificationToken.equals(req.notificationToken)) {
					regtoken.notificationToken = req.notificationToken;
					sk.saveOrUpdate(regtoken);
				} else {
					regtoken = new NotificationRegistration(tokenUser, device, platform, req.notificationToken, now);
					sk.save(regtoken);
				}
			}
			sk.commit();
			return new APIBaseResponse();
		}		
	}	
		
	public static boolean makeDevicePrimary(SessionKeeper sk, TrackingUser user, String deviceId) {
		@SuppressWarnings("unchecked")
		NotificationRegistration primary = (NotificationRegistration) sk.createCriteria(NotificationRegistration.class)
					.createAlias("device", "Device")
					.add(Restrictions.eq("user", user))
					.add(Restrictions.eq("Device.deviceId", deviceId))
					.uniqueResult();
		if(primary == null) {
			return false;
		}
		user.setPrimaryNotificationDevice(primary);
		sk.saveOrUpdate(user);
		sk.commit();
		return true;
	}
	
	public static String registerPrimaryDevice(SessionKeeper sk, TrackingUser user, APIDevice deviceRecord, String notificationToken) {
			if(user == null) {
				return "USER_NULL";
			}					
			String platform = deviceRecord.platform;
			if(!platform.equals("iOS") && !platform.equals("Android")) {
				return "WRONG_PLATFORM";
			}
			DeviceRecord device = (DeviceRecord)sk.createCriteria(DeviceRecord.class).add(Restrictions.eq("uuid", deviceRecord.uuid)).uniqueResult();
			boolean deviceOverride = false;
			Date now = new Date();
			NotificationRegistration regtoken = null;
			if(device == null) {
				device = new DeviceRecord(deviceRecord.uuid, deviceRecord.manufacturer, deviceRecord.model, deviceRecord.version, deviceRecord.platform);
				regtoken = new NotificationRegistration(user, device, platform, notificationToken, now);
				user.setPrimaryNotificationDevice(regtoken);
				sk.save(device);
				sk.save(regtoken);
				sk.saveOrUpdate(user);
			} else {
				regtoken = (NotificationRegistration) sk.createCriteria(NotificationRegistration.class)
														.add(Restrictions.eq("user", user))
														.add(Restrictions.eq("device", device))
														.uniqueResult();
				if(regtoken != null) {		
						
					String currentToken = user.getPrimaryNotificationDevice() == null ? null : user.getPrimaryNotificationDevice().notificationToken;	
					deviceOverride = currentToken != null && !currentToken.equals(notificationToken);
					
					// fix token for existing device
					if(regtoken.notificationToken == null || !regtoken.notificationToken.equals(notificationToken)) {
						regtoken.notificationToken = notificationToken;
						sk.saveOrUpdate(regtoken);
					}
					// make device primary
					if(user.getPrimaryNotificationDevice() == null || !user.getPrimaryNotificationDevice().equals(regtoken)) {
						user.setPrimaryNotificationDevice(regtoken);
						sk.saveOrUpdate(user);
					}
				} else {
					regtoken = new NotificationRegistration(user, device, platform, notificationToken, now);
					user.setPrimaryNotificationDevice(regtoken);
					sk.save(regtoken);
					sk.saveOrUpdate(user);
				}
			}
	
			if(deviceOverride) return "OK_OVERRIDE";
			return "OK";
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

	public APISendNotificationResponse notify(APISendNotification req) {
// Android		
//        String regId = "eMknyAoS-Hw:APA91bGjNajIfzVJrEuJQaP9155_LMDpQSE5Xk_SrWIhg5qNtURiH1ioW5iWOrg5I-bwOJViUi0IvC_mrAz32I67dQb80f16QG1G3H_PkoO5FrJJ8MG9LoY3ztd3FpQx-HmMjpRuxwFO";
		//iOS
//	      String regId = "80e719168986a6953445dc561dbbd6806657c5929c0f0aab97d7b19b64f0d319";
//        boolean test = pushNotificationToGCM(regId, message);
//	      String message = "Živjo svet!";
//	      boolean test = pushNotificationToAPNS(regId, "GOOPTI", message);
//        return new APISendNotificationResponse("OK", "Pa je šlo." + test);
		
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APISendNotificationResponse("AUTH_ERROR", "");
			}	
			if(req.type == null) {
				return new APISendNotificationResponse("TYPE_MISSING", "");
			}
			if(req.type.equals("NOTIFICATION") && (req.recipients == null || req.recipients.size() == 0)) {
				return new APISendNotificationResponse("NO_RECIPIENT","NOTIFICATION type requires recipient");
			}
			if(req.message != null) {
				if(req.messageType == null || !(req.messageType.equals("TEXT") || req.messageType.equals("HTML")) ) {
					return new APISendNotificationResponse("WRONG_MESSAGE_TYPE","");
				}
			}
			List<Pair<APINotificationStatus, EventMessage>> records = new LinkedList<Pair<APINotificationStatus, EventMessage>>();
			MessageBody mBody = null;
			if(req.message != null) {
				mBody = new MessageBody();
				mBody.setMessageType(req.messageType);
				mBody.setMessage(req.message);				
			}
			Date now = new Date();
			boolean send = false;
			if(req.type.equals("NOTIFICATION")) {
				if(mBody == null) {
					return new APISendNotificationResponse("EMPTY_MESSAGE","Null messages are not allowed for NOTIFICATION type.");
				}
				if(req.recipients.size() == 0) {
					return new APISendNotificationResponse("RECIPIENT_MISSING","At least one recipient must be provided for NOTIFICATION type.");
				}
				for(APIUsers aUser : req.recipients) {
					TrackingUser user = authEngine.getUser(sk, aUser.userId, aUser.provider);
					if(user == null) {
						APINotificationStatus status = new APINotificationStatus(null, "NO_SUCH_USER", 
								"(" + aUser.userId == null ? "-" : aUser.userId + ", " 
										+ aUser.provider == null ? "-" : aUser.provider);
						records.add(Pair.of(status, null));
						continue;
					} 
					// get recieiver token
					NotificationRegistration deviceRegistration = user.getPrimaryNotificationDevice();
					String regToken = deviceRegistration == null ? null : deviceRegistration.getNotificationToken();
					if( deviceRegistration == null || regToken == null) {
						APINotificationStatus stat = new APINotificationStatus(user, "RECIPIENT_HAS_NO_DEVICE", "");
						records.add(Pair.of(stat, null));
						continue;
					}
					DeviceRecord device = deviceRegistration.getDevice();
					String platform = device.getPlatform();
					String title = req.title == null ? "" : req.title;
					if(notificationService.push(regToken, title, req.message, platform)) {
							send = true;
					} else {
							APINotificationStatus stat = new APINotificationStatus(user, "PUSH_FAILED", platform);
							records.add(Pair.of(stat, null));
							continue;													
					}
					
					EventMessage msg = new EventMessage();
					msg.setSender(tokenUser);
					msg.setReceiver(user);
					msg.setSent(true);
					msg.setType("NOTIFICATION");
					msg.setTitle(req.title);
					msg.setTimestamp(now);
					msg.setTimeRecorded(now);
					msg.setBody(mBody);
					APINotificationStatus stat = new APINotificationStatus(user, "OK", "");
					records.add(Pair.of(stat, msg));
				}
				if(send) {
					sk.save(mBody);
					for(Pair<APINotificationStatus, EventMessage> p: records) {
						if(p.getRight() != null) {
							sk.save(p.getRight());
						}
					}
				}
				sk.commit();
				List<APINotificationStatus> statuses = new LinkedList<APINotificationStatus>();
				for(Pair<APINotificationStatus, EventMessage> p: records) {
					APINotificationStatus aStat = p.getLeft();
					if(p.getRight() != null) {
						aStat.messageId = p.getRight().getId();
					}
					statuses.add(aStat);
				}
				APISendNotificationResponse res = new APISendNotificationResponse();
				res.notificationStatus = statuses;
				return res;				
			} 
			
			if(req.type.equals("START") || req.type.equals("END")) {
				EventMessage msg = new EventMessage();
				msg.setSender(tokenUser);
				msg.setSent(true);
				msg.setTimestamp(now);
				msg.setTimeRecorded(now);
				msg.setType(req.type);
				sk.save(msg);
				sk.commit();
				APINotificationStatus stat = new APINotificationStatus(tokenUser, "OK", "");
				stat.messageId = msg.getId();
				APISendNotificationResponse res = new APISendNotificationResponse();
				List<APINotificationStatus> statuses = new LinkedList<APINotificationStatus>();
				statuses.add(stat);
				res.notificationStatus = statuses;
				return res;
			}
			return new APISendNotificationResponse("ERROR","");
		}					
	}

	public APINotificationsResponse list(APINotifications req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APINotificationsResponse("AUTH_ERROR", "");
			}
			if(req.fromDate == null) {
				return new APINotificationsResponse("DATE_ERROR", "fromDate must be non-null.");
			}
			Criteria crit1 = sk.createCriteria(EventMessage.class)
					.add(Restrictions.eq("sender", tokenUser))
					.add(Restrictions.ge("timestamp", req.fromDate));
			if(req.untilDate != null) {
					crit1.add(Restrictions.le("timestamp", req.untilDate));		
			}
			crit1.addOrder(Order.asc("timestamp"));
			List<EventMessage> sentMessages = crit1.list();
			List<APINotificationMessage> sentResp = new LinkedList<APINotificationMessage>();
			for(EventMessage mes: sentMessages) {
				sentResp.add(new APINotificationMessage(mes));
			}
			APINotificationsResponse res = new APINotificationsResponse();
			res.sent = sentResp;
			Criteria crit2 = sk.createCriteria(EventMessage.class)
					.add(Restrictions.eq("receiver", tokenUser))
					.add(Restrictions.ge("timestamp", req.fromDate));
			if(req.untilDate != null) {
					crit2.add(Restrictions.le("timestamp", req.untilDate));		
			}
			crit2.addOrder(Order.asc("timestamp"));
			List<EventMessage> receivedMessages = crit2.list();
			List<APINotificationMessage> receivedResp = new LinkedList<APINotificationMessage>();
			for(EventMessage mes: receivedMessages) {
				receivedResp.add(new APINotificationMessage(mes));
			}		
			res.received = receivedResp;
			return res;			
		}
	}	
}

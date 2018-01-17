package com.tracker.engine;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.notifications.APIDevice;
import com.tracker.apientities.notifications.APIDeviceQuery;
import com.tracker.apientities.notifications.APIDeviceRegister;
import com.tracker.apientities.notifications.APIDeviceResponse;
import com.tracker.apientities.notifications.APIDeviceUpdate;
import com.tracker.apientities.notifications.APIMarkReadMessages;
import com.tracker.apientities.notifications.APINotificationMessage;
import com.tracker.apientities.notifications.APINotificationStatus;
import com.tracker.apientities.notifications.APINotifications;
import com.tracker.apientities.notifications.APINotificationsResponse;
import com.tracker.apientities.notifications.APIPushNotificationReceived;
import com.tracker.apientities.notifications.APISendNotification;
import com.tracker.apientities.notifications.APISendNotificationResponse;
import com.tracker.apientities.notifications.APISendSmsRequest;
import com.tracker.apientities.notifications.APISendSmsResponse;
import com.tracker.apientities.notifications.APIUsers;
import com.tracker.db.DeviceRecord;
import com.tracker.db.EventMessage;
import com.tracker.db.MessageBody;
import com.tracker.db.NotificationRegistration;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.PushNotificationMessage;
import com.tracker.db.TrackingUser;
import com.tracker.db.Vehicle;
import com.tracker.types.PlatformType;
import com.tracker.types.PushNotificationStatus;
import com.tracker.utils.SessionKeeper;

@Component
public class NotificationEngine {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationEngine.class);
	private static final int PUSH_NOTIFICATION_NOT_DELIEVERED_IN_TIME = 300000;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired 
	private AuthenticationEngine authEngine;		
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private S3Service s3Service;
	
	@Autowired 
	private EmailService emailService;
	
	@Value("#{generalProperties.gooptiAuthCheckNotifToken}")
	private String service;
	
	@Value("#{generalProperties.gooptiSendSms}")
	private String serviceSendSms;
	
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
			if(!platform.equals(PlatformType.IOS.getName()) && !platform.equals(PlatformType.ANDROID.getName()) && !platform.equals(PlatformType.IPHONEOS.getName())) {
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
			if(!platform.equals(PlatformType.IOS.getName()) && !platform.equals(PlatformType.ANDROID.getName()) && !platform.equals(PlatformType.IPHONEOS.getName())) {
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
	
	public static OrganizationGroup processMessageGroup(SessionKeeper sk, TrackingUser user, String groupName) {
		if(groupName != null) {
			String provider = user.getProvider();
			if(provider != null) {
				if(groupName.startsWith("#")) {
					groupName = provider + groupName;
				}
			}
			return (OrganizationGroup) sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("groupId", groupName)).uniqueResult();
		}
		return null;		
	}
	
	public APISendNotificationResponse notify(APISendNotification req) {
		
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
		        String url = UriComponentsBuilder.fromHttpUrl(service)
		                .queryParam("push_notification_token", req.token)
		                .build().encode().toUriString();
		        
				 //TODO - check if it is ok
		        RestTemplate restTempl = new RestTemplate();
		        HttpStatus result = restTempl.getForObject(url, HttpStatus.class);
				if(!result.equals(HttpStatus.OK)) return new APISendNotificationResponse("AUTH_ERROR", "");
			}	
			if(req.type == null) {
				return new APISendNotificationResponse("TYPE_MISSING", "");
			}
			if(req.type.equals("NOTIFICATION") && (req.recipients == null || req.recipients.size() == 0)) {
				return new APISendNotificationResponse("NO_RECIPIENT","NOTIFICATION type requires recipient");
			}
			if(req.message != null) {
				if(req.messageType == null || !(req.messageType.equals("TEXT") || req.messageType.equals("HTML") || req.messageType.equals("FILE")) ) {
					return new APISendNotificationResponse("WRONG_MESSAGE_TYPE","");
				}
			}
			List<Triple<APINotificationStatus, EventMessage, PushNotificationMessage>> records = new LinkedList<Triple<APINotificationStatus, EventMessage, PushNotificationMessage>>();
			Date now = new Date();
			boolean send = false;
			if(req.type.equals("NOTIFICATION")) {
				OrganizationGroup fromGroup = processMessageGroup(sk, tokenUser, req.fromGroup);
				if(req.fromGroup != null && fromGroup == null) {
					return new APISendNotificationResponse("WRONG_FROM_GROUP","Group '" + req.fromGroup + "' does not exist.");					
				}
				MessageBody mBody = null;
				if(req.message != null) {
					mBody = new MessageBody();
					mBody.setMessageType(req.messageType);
					mBody.setMessage(req.message);				
				}

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
						records.add(Triple.of(status, null, null));
						continue;
					} 
					// get recieiver token
					NotificationRegistration deviceRegistration = user.getPrimaryNotificationDevice();
					String regToken = deviceRegistration == null ? null : deviceRegistration.getNotificationToken();
					if( deviceRegistration == null || regToken == null) {
						APINotificationStatus stat = new APINotificationStatus(user, "RECIPIENT_HAS_NO_DEVICE", "");
						records.add(Triple.of(stat, null, null));
						continue;
					}
					DeviceRecord device = deviceRegistration.getDevice();
					String platform = device.getPlatform();
					String title = req.title == null ? "" : req.title;
					
					String serviceMessageId = notificationService.push(regToken, title, req.message, platform, req.travelOrderId, req.openPage);
					if(serviceMessageId != null) {
							send = true;
					} else {
							APINotificationStatus stat = new APINotificationStatus(user, "PUSH_FAILED", platform);
							records.add(Triple.of(stat, null, null));
							continue;													
					}
					
					PushNotificationMessage pnMessage = new PushNotificationMessage();
					pnMessage.setContent(req.message);
					pnMessage.setServicePushNotificationId(serviceMessageId);
					pnMessage.setSender(req.user);
					pnMessage.setReceipient(req.driverName);
					pnMessage.setFranchiseUids(req.franchiseUids);
					pnMessage.setSmsType("DRIVER_NOTIFICATION");
					pnMessage.setSmsSuccessfullySentSimulatneously(req.smsSuccessfullySentSimulatneously);
					pnMessage.setDriverAssignmentId(req.driverAssignmentId);
					pnMessage.setTravelOrderId(req.travelOrderId);
					pnMessage.setTitle(req.title);
					pnMessage.setSenderNameToBeDisplayed(req.senderNameToBeDisplayed);
					pnMessage.setDriverAssignmentId(req.driverAssignmentId);
					pnMessage.setGoOptiDriverAssignmentStatus(req.driverAssignmentStatus);
					pnMessage.setNumber(req.number);
					pnMessage.setPushNotificationStatus(PushNotificationStatus.SENT);
					pnMessage.setTimeSent(now);
								
					EventMessage msg = new EventMessage();
					msg.setSender(tokenUser);
					msg.setReceiver(user);
					msg.setSent(true);
					msg.setType("NOTIFICATION");
					msg.setTitle(req.title);
					msg.setTimestamp(now);
					msg.setTimeRecorded(now);
					msg.setBody(mBody);
					msg.setPushNotificationMessage(pnMessage);
					if(fromGroup != null) {
						msg.setSenderGroup(fromGroup);
					}
					APINotificationStatus stat = new APINotificationStatus(user, "OK", "");
					records.add(Triple.of(stat, msg, pnMessage));
				}
				if(send) {
					sk.save(mBody);
					for(Triple<APINotificationStatus, EventMessage, PushNotificationMessage> p: records) {
						if(p.getMiddle() != null) {
							sk.save(p.getMiddle());
							sk.save(p.getRight());
						}
					}
				}
				sk.commit();
				List<APINotificationStatus> statuses = new LinkedList<APINotificationStatus>();
				for(Triple<APINotificationStatus, EventMessage, PushNotificationMessage> p: records) {
					APINotificationStatus aStat = p.getLeft();
					if(p.getMiddle() != null) {
						aStat.messageId = p.getMiddle().getId();
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
			
			if(req.type.equals("GROUP_NOTIFICATION")) {  // notification sent to whole group. Can be sent by admin or user
				EventMessage msg = new EventMessage();
				msg.setSender(tokenUser);
//				msg.setContextGroupId(req.toGroup);
				OrganizationGroup senderGroup = processMessageGroup(sk, tokenUser, req.fromGroup);
				if(req.fromGroup != null && senderGroup == null) {
					return new APISendNotificationResponse("WRONG_FROM_GROUP","Group '" + req.fromGroup + "' does not exist.");					
				}
				if(req.toGroup == null) {
					return new APISendNotificationResponse("NO_GROUP","GROUP_NOTIFICATION requires toGroup parameter or group does not exist.");
				}								
				OrganizationGroup receiverGroup = processMessageGroup(sk, tokenUser, req.toGroup);
				if(req.fromGroup != null && senderGroup == null) {
					return new APISendNotificationResponse("WRONG_TO_GROUP","Group '" + req.toGroup + "' does not exist.");					
				}
				
				MessageBody mBody = null;
				if(req.message != null) {
					mBody = new MessageBody();
					mBody.setMessageType(req.messageType);
					mBody.setMessage(req.message);
					msg.setBody(mBody);
					sk.save(mBody);
				}
				
				msg.setSent(true);
				msg.setTimestamp(now);
				msg.setTimeRecorded(now);
				msg.setType(req.type);		
				msg.setSenderGroup(senderGroup);
				msg.setReceiverGroup(receiverGroup);
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

			if(req.type.equals("MOBILE_LOG")) {
				EventMessage msg = new EventMessage();
				msg.setSender(tokenUser);
				msg.setSent(true);
				msg.setTimestamp(now);
				msg.setTimeRecorded(now);
				msg.setType(req.type);
				
				MessageBody mBody = null;
				if(req.message != null) {
					mBody = new MessageBody();
					mBody.setMessageType(req.messageType);
					String link = s3Service.putRawText(req.message);
					mBody.setLink(link);				
				}
				
				sk.save(mBody);
				msg.setBody(mBody);				
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

			if(req.type.equals("EMAIL_TEXT")) {
				EventMessage msg = new EventMessage();
				msg.setSender(tokenUser);
				msg.setSent(true);
				msg.setTimestamp(now);
				msg.setTimeRecorded(now);
				msg.setType(req.type);
				msg.setTitle(req.title);
				OrganizationGroup senderGroup = processMessageGroup(sk, tokenUser, req.fromGroup);  
				msg.setSenderGroup(senderGroup);
				MessageBody mBody = null;
				if(req.message != null) {
					mBody = new MessageBody();
					mBody.setMessageType(req.messageType);
					mBody.setMessage(req.message);
					emailService.send(req.to, req.title, req.message, req.messageType);
				}
				
				TrackingUser user = authEngine.getUser(sk, req.to, tokenUser.getProvider());
				if(user != null) {
					msg.setReceiver(user);
				} else {
					msg.setEmailTo(req.to);
				}
				sk.save(mBody);
				msg.setBody(mBody);				
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
			OrganizationGroup forGroup = null;			
			if(req.forGroup != null) {
				String provider = tokenUser.getProvider();
				String groupName = req.forGroup;
				if(provider != null) {
					if(groupName.startsWith("#")) {
						groupName = provider + groupName;
					}
				}
				forGroup = (OrganizationGroup) sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("groupId", groupName)).uniqueResult();
				if(forGroup == null) {
					return new APINotificationsResponse("WRONG_GROUP","Group '" + req.forGroup + "' does not exist.");					
				}		
				//check if user admin in group
			}
			
			Criteria crit1 = sk.createCriteria(EventMessage.class);
			if(forGroup == null) {
					crit1.add(Restrictions.eq("sender", tokenUser));
			} else {
					crit1.add(Restrictions.eq("senderGroup", forGroup));
			}
			crit1.add(Restrictions.ge("timestamp", req.fromDate));
			if(req.untilDate != null) {
					crit1.add(Restrictions.le("timestamp", req.untilDate));		
			}
			String webRootLink = this.s3Service.getS3WebLink();
			crit1.addOrder(Order.asc("timestamp"));
			List<EventMessage> sentMessages = crit1.list();
			List<APINotificationMessage> sentResp = new LinkedList<APINotificationMessage>();
			for(EventMessage mes: sentMessages) {
				sentResp.add(new APINotificationMessage(mes, webRootLink));
			}
			APINotificationsResponse res = new APINotificationsResponse();
			res.sent = sentResp;
			
			
			Criteria crit2 = sk.createCriteria(EventMessage.class);
			if(forGroup == null) {
					crit2.add(Restrictions.eq("receiver", tokenUser));
			} else {
				crit2.createAlias("sender", "Sender");
				Date now = new Date();
				Map<String, String> tmpMap = GroupEngine.allowedUsersForAdminToSee(sk, tokenUser, null, now);
				List<OrganizationGroup> allowedGroups = GroupEngine.subgroupTree(sk, forGroup);
				crit2.add(Restrictions.disjunction()
						.add(Restrictions.conjunction()
								.add(Restrictions.in("Sender.userId", tmpMap.keySet()))
								.add(Restrictions.isNull("receiverGroup"))
						 )
						.add(Restrictions.in("receiverGroup", allowedGroups))
				);										
			}
			crit2.add(Restrictions.ge("timestamp", req.fromDate));
			if(req.untilDate != null) {
					crit2.add(Restrictions.le("timestamp", req.untilDate));		
			}
			crit2.addOrder(Order.asc("timestamp"));
			List<EventMessage> receivedMessages = crit2.list();
			List<APINotificationMessage> receivedResp = new LinkedList<APINotificationMessage>();
			for(EventMessage mes: receivedMessages) {
				receivedResp.add(new APINotificationMessage(mes, webRootLink));
			}		
			res.received = receivedResp;
			return res;			
		}
	}	
	
	public APIBaseResponse pushNotificationReceived (APIPushNotificationReceived req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) { 
			if(req.messageId != null) {
				PushNotificationMessage msg = (PushNotificationMessage) sk.createCriteria(PushNotificationMessage.class).add(Restrictions.eq("servicePushNotificationId", req.messageId)).uniqueResult();
				msg.setTimeDelievered(req.pushNotificationReceived);
				msg.setPushNotificationStatus(PushNotificationStatus.DELIEVERED);
				sk.saveOrUpdate(msg);
				sk.commit();
				return new APIBaseResponse("OK", "");
			} 
			return new APIBaseResponse("ERROR", "messageId is null");
		} catch (Exception e) {
			logger.error("ERROR", e);
			return new APIBaseResponse("ERROR", e.getMessage());
		}
	}

	
	public APIBaseResponse markReadMessages (APIMarkReadMessages req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIBaseResponse("AUTH_ERROR", "");
			}
			if(req.readMessagesIDs != null || !req.readMessagesIDs.isEmpty()) {
				for(Integer messageId : req.readMessagesIDs) {
					if (messageId == null) continue;
					PushNotificationMessage pnm = (PushNotificationMessage) sk.createCriteria(PushNotificationMessage.class).add(Restrictions.eq("id", messageId)).uniqueResult();
					pnm.setMarkMessageAsRead(true);
					sk.saveOrUpdate(pnm);
				}				
			} 
			sk.commit();
			return new APIBaseResponse("OK", "");
		} catch (Exception e) {
			logger.error("ERROR", e);
			return new APIBaseResponse("ERROR", e.getMessage());
		}
	}
	

	@Scheduled(fixedDelay=30000)
	@SuppressWarnings("unchecked")
	public void sendSMSIfPushNotificationDeliveryFailes() {
		logger.info("Scheduled Job - sendSMSIfPushNotificationDeliveryFailes");
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) { 
			Date now = new Date(System.currentTimeMillis() - PUSH_NOTIFICATION_NOT_DELIEVERED_IN_TIME);
			List<PushNotificationMessage> listOfMessages = sk.createCriteria(PushNotificationMessage.class, "pnm")
					.add(Restrictions.eq("pnm.smsSuccessfullySentSimulatneously", false))
					.add(Restrictions.eq("pnm.pushNotificationStatus", PushNotificationStatus.SENT))
					.add(Restrictions.ge("pnm.timeSent", now))
 					.list();
	        String url = UriComponentsBuilder.fromHttpUrl(serviceSendSms)
	                .build().encode().toUriString();
			RestTemplate restTempl = new RestTemplate();
			for(int i=0; i<listOfMessages.size(); i++) {
				PushNotificationMessage pn = listOfMessages.get(i);
				APISendSmsRequest smsReq = new APISendSmsRequest(pn.getNumber(), pn.getContent(), pn.getFranchiseUids(), pn.getReceipient(), pn.getSmsType());
				APISendSmsResponse smsResp = restTempl.postForObject(url, smsReq, APISendSmsResponse.class);
				if(smsResp.isSent()) {
					pn.setSmsSuccessfullySentSimulatneously(true);
					sk.saveOrUpdate(pn);
				}
			}
			sk.commit();	
		} catch (Exception e) {
			logger.error("ERROR", e);
		}
	} 

//	@Scheduled(fixedDelay=2000)
//	public void doSomething() {
//		String pass = "neki spredi \"password\":\"neki neki\" neki zadi";
//	    System.out.println(pass.replaceAll("\\\"password\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\"password\" : \"*********\""));
//	}
	
}

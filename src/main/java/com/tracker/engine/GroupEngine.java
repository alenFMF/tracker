package com.tracker.engine;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APIBaseResponse;
import com.tracker.apientities.organizationgroup.APIGroupDetail;
import com.tracker.apientities.organizationgroup.APIGroupQuery;
import com.tracker.apientities.organizationgroup.APIGroupQueryResponse;
import com.tracker.apientities.organizationgroup.APIGroupRegister;
import com.tracker.apientities.organizationgroup.APIGroupUpdate;
import com.tracker.apientities.organizationgroup.APIMakeMigrationUpdates;
import com.tracker.apientities.organizationgroup.APIShareOrInvite;
import com.tracker.apientities.organizationgroup.APIShareOrInviteResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignment;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentDetail;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentQuery;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentUpdate;
import com.tracker.apientities.organizationgroup.APIUserGroupAssignmentUpdateResponse;
import com.tracker.apientities.organizationgroup.APIUserGroupRolesDetail;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingUser;
import com.tracker.db.UserGroupAssignment;
import com.tracker.utils.SessionKeeper;

@Component
public class GroupEngine {
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired 
	private AuthenticationEngine authEngine;		
	
	
	private OrganizationGroup getGroup(SessionKeeper sk, String groupId) {
		return (OrganizationGroup)sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("groupId", groupId)).uniqueResult();
	}
	
	public APIBaseResponse register(APIGroupRegister req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIBaseResponse("AUTH_ERROR", "");
			}		
			if(req.groupId.contains("@")) {
				return new APIBaseResponse("WRONG_GROUP_NAME", "Non-personal group names must not contain @ as a character.");
			}
			OrganizationGroup group = getGroup(sk, req.groupId);
			if(group == null) {
				Date now = new Date();				
				group = new OrganizationGroup();
				group.setGroupId(req.groupId);
				group.setDescription(req.description);
				group.setCreator(tokenUser); 
				group.setTimestamp(now);
				
				UserGroupAssignment asgn = new UserGroupAssignment();
				asgn.setAsPersonalGroup(tokenUser, group, now);		
				sk.saveOrUpdate(tokenUser);
				sk.save(group);
				sk.save(asgn);				
				sk.commit();
				return new APIBaseResponse();
			}
			return new APIBaseResponse("GROUP_EXISTS", "A group with the same groupId already exists.");
		}		
	}	
	
	private void addIntervalValidityRestrictions(Criteria c, String fromDateName, String untilDateName, Date nowTime) {

//		(fromDate == null && (untilDate == null || untilDate >= nowTime)) ||
//	    fromDate != null && (fromDate <= nowTime && (untilDate == null || untilDate >= nowTime)
		
		c.add(Restrictions.disjunction()
				.add(Restrictions.conjunction()
						.add(Restrictions.isNull(fromDateName))
						.add(Restrictions.disjunction()
								.add(Restrictions.isNull(untilDateName))
								.add(Restrictions.ge(untilDateName, nowTime))
							)
					)
				.add(Restrictions.conjunction()
						.add(Restrictions.isNotNull(fromDateName))
						.add(Restrictions.conjunction()
								.add(Restrictions.le(fromDateName, nowTime))
								.add(Restrictions.disjunction()
										.add(Restrictions.isNull(untilDateName))
										.add(Restrictions.ge(untilDateName, nowTime))
									)
							)
					)
			 );	
	}
		
//	public void addPendingRestriction(Criteria c, String timestampName, String acceptFirstName, String acceptSecondName, Date time) {
//			c.add(Restrictions.isNull(timestampName));
//			c.add(Restrictions.eq("accepted", false));
////			c.add(Restrictions.disjunction()
////						.add(Restrictions.conjunction()
////								.add(Restrictions.isNull(acceptFirstName))
////								.add(Restrictions.isNotNull(acceptSecondName))
////								.add(Restrictions.le(acceptSecondName, time))
////							)
////						.add(Restrictions.conjunction()
////								.add(Restrictions.isNotNull(acceptFirstName))
////								.add(Restrictions.isNull(acceptSecondName))		
////								.add(Restrictions.le(acceptFirstName, time))
////							)
////					);
//	}

	@SuppressWarnings("unchecked")
	public List<UserGroupAssignment> usersGroupAssignments(SessionKeeper sk, String userId, String groupId, Date time, Boolean pendingOnly, Boolean accept) {
		// if now = true only assignments that are valid at the moment are listed
		if(userId == null && groupId == null) return null;  
		Criteria c = sk.createCriteria(UserGroupAssignment.class);
		c.createAlias("user", "User");
		c.createAlias("group", "Group");
		if(userId != null) {			
			c.add(Restrictions.eq("User.userId", userId));
		}
		if(groupId != null) {			
			c.add(Restrictions.eq("Group.groupId", groupId));
		}	
		if(time != null) {
			addIntervalValidityRestrictions(c, "fromDate", "untilDate", time);
		}
		if(pendingOnly) {
			c.add(Restrictions.isNull("accepted"));
		}  else {
			if(accept != null) {
				c.add(Restrictions.eq("accepted", accept));
			}
			
			c.add(Restrictions.le("timestamp", time));	// accepted only	
			c.addOrder(Order.desc("timestamp"));
		}	
		
		return c.list();
	}
	
	// returns effective role for combination of user and group based on assignments.
	// group may not be present, meaning that a list of roles is given for all groups based on assignments.
	// all assignments not related to user are ignored.
	private List<GroupRoles> userRolesInGroupsAtTime(List<UserGroupAssignment> assignments, TrackingUser user, String groupId) {
//		if(user == null || group == null) return null;
//		List<UserGroupAssignment> assignments = usersRolesInGroups(sk, user, group, time, false);
		
		// filter by groupId
		List<UserGroupAssignment> assgn2 = assignments;
		if(groupId != null) {
			assgn2 = assignments.stream()
							.filter(x -> x.getGroup().getGroupId().equals(groupId))
							.collect(Collectors.toList());
		}
		Map<String, List<UserGroupAssignment>> byGroup = assgn2.stream()
				.filter(x -> x.user.getUserId().equals(user.getUserId()))
				.collect(Collectors.groupingBy(x -> x.group.getGroupId()));
		
		List<GroupRoles> outRoles = new LinkedList<>();
		GroupRoles primaryGroup = null;
		
		for(Map.Entry<String, List<UserGroupAssignment>> e: byGroup.entrySet()) {
			OrganizationGroup group = e.getValue().get(0).getGroup();
			@SuppressWarnings("unchecked")
			List<UserGroupAssignment> groupAssignments = (List<UserGroupAssignment>) e.getValue().stream()
											.sorted(Comparator.comparing(x -> x.getTimestamp()))
											.collect(Collectors.toList());;

			Boolean adminCnt = false;
			Boolean userCnt = false;
			for(UserGroupAssignment asgn: groupAssignments) { 
				String role = asgn.getGroupRole();
				String grant = asgn.getGrant();
				if(role.equals("ADMIN")) {
					if(grant.equals("ALLOW")) {
						adminCnt = true;
					} else if(grant.equals("DENY")){
						adminCnt = false;
					}
				} else if(role.equals("USER")) {
					if(grant.equals("ALLOW")) {
						userCnt = true;
					} else if(grant.equals("DENY")){
						userCnt = false;
					}				
				}
			}
			
			String personalGroupOnwer = group.getPersonalGroupUser().getUserId();
			if( personalGroupOnwer != null && user.getUserId().equals(group.getPersonalGroupUser().getUserId()) ) {
				adminCnt = true;
			}
			if(user.getAdmin()) { // system admin
				adminCnt = true;
			}
			GroupRoles grol = new GroupRoles(user, group, adminCnt, userCnt);
			if(group.getPersonalGroupUser().getUserId().equals(user.getUserId())) {
				primaryGroup = grol;
			} else {
				outRoles.add(grol);
			}
		}
		List<GroupRoles> result = outRoles.stream()   // sort by description
			.sorted(Comparator.comparing(x -> x.getGroup().getDescription()))
			.collect(Collectors.toList());
		if(primaryGroup != null) {
			result.add(0, primaryGroup);
		}
		return result;
	}	
	
	// determines the roles of users in a group.
	private List<GroupRoles> userRolesInGroup(List<UserGroupAssignment> assignments, String groupId) {
		Map<String, List<UserGroupAssignment>> byUser = assignments.stream()
				.filter(x -> x.group.getGroupId().equals(groupId))
				.collect(Collectors.groupingBy(x -> x.user.getUserId()));
		List<GroupRoles> result = new LinkedList<GroupRoles>();
		for(Map.Entry<String, List<UserGroupAssignment>> e: byUser.entrySet()) {
			TrackingUser user = e.getValue().get(0).getUser();
			List<UserGroupAssignment> asgnList = e.getValue().stream()
							.sorted(Comparator.comparing(x -> x.getTimestamp()))
							.collect(Collectors.toList());
			List<GroupRoles> roles = userRolesInGroupsAtTime(asgnList, user, groupId);
			if(roles.size() == 1) {
				result.add(roles.get(0));
			}
		}
		return result;
	}
	 
	public APIBaseResponse update(APIGroupUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIBaseResponse("AUTH_ERROR", "");
			}

			List<UserGroupAssignment> assignments = usersGroupAssignments(sk, tokenUser.getUserId(), req.groupId, new Date(), false, true);
			List<GroupRoles> roles = userRolesInGroupsAtTime(assignments, tokenUser, req.groupId);
			if(roles == null || roles.size() == 0) {
				return new APIBaseResponse("NO_SUCH_GROUP", "");
			}
			GroupRoles role = roles.get(0);
			if(!(role.isAdminRole() || tokenUser.getAdmin())) {
				return new APIBaseResponse("AUTH_ERROR", "Only group admin or system admin can update a group.");
			}
			if(req.description != null) {				
				OrganizationGroup group = role.getGroup();
				group.setDescription(req.description);		
				sk.saveOrUpdate(group);	
				sk.commit();				
			}
			return new APIBaseResponse();
		}	
	}
		
	@SuppressWarnings("unchecked")
	public APIGroupQueryResponse list(APIGroupQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIGroupQueryResponse("AUTH_ERROR", "");
			}	
			TrackingUser user = tokenUser;
			if(req.forUser != null && tokenUser.getUserId() != req.forUser) {
				if(tokenUser.getAdmin()) {
					TrackingUser tmpUser = authEngine.getUser(sk, req.forUser);
					if(tmpUser == null) {
						return new APIGroupQueryResponse("NO_SUCH_USER", "");
					}
					user = tmpUser;
				} else {
					return new APIGroupQueryResponse("AUTH_ERROR", "Only admin can list groups for other users.");
				}
			} 
			List<UserGroupAssignment> assignments = usersGroupAssignments(sk, user.getUserId(), null, new Date(), false, true);
			List<GroupRoles> roles = userRolesInGroupsAtTime(assignments, user, null);
						
			List<APIGroupDetail> groups = new LinkedList<APIGroupDetail>();
			for (GroupRoles role : roles) {
				OrganizationGroup group = role.getGroup();
			    APIGroupDetail det = new APIGroupDetail();
			    det.groupId = group.getGroupId();
			    det.description = group.getDescription();
			    det.privateGroup = group.getPrivateGroup();
				
				List<GroupRoles> rolesToList = new LinkedList<GroupRoles>();
			    if(role.isAdminRole() || user.getUserId().equals(group.getPersonalGroupUser().getUserId())) {
				    det.creatorId = group.getCreator().getUserId();			    	
				    det.personalGroupUserId = group.getPersonalGroupUser().getUserId();
				    det.timestamp = group.getTimestamp();
				    String userId = user.getUserId();
					rolesToList = userRolesInGroup(assignments, group.getGroupId()).stream()
							.filter(x -> x.getUser().getUserId() != userId)
							.sorted(Comparator.comparing(x -> x.getUser().getUserId()))
							.collect(Collectors.toList());				    
			    } else {
			    	rolesToList.add(role);
			    }
			    det.setUsers(rolesToList.stream()
			    				.map(x -> new APIUserGroupRolesDetail(x))
			    				.collect(Collectors.toList())
			    		    );
			    groups.add(det);
			}
			APIGroupQueryResponse res = new APIGroupQueryResponse(groups);
			return res;
		}
	}	
	
	public APIShareOrInviteResponse registerLinks(APIShareOrInvite req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIShareOrInviteResponse("AUTH_ERROR", "");
			}
			if(req.requests == null || req.requests.isEmpty()) {
				return new APIShareOrInviteResponse("NO_REQUESTS", "");
			}
			List<String> statuses = new LinkedList<String>();
			for(APIUserGroupAssignment asgn : req.requests) {
				TrackingUser user = authEngine.getUser(sk, asgn.userId);
				if(user == null) {
					statuses.add("NO_USER");
					continue;
				}
				OrganizationGroup group = getGroup(sk, asgn.groupId);
				if(group == null) {
					statuses.add("NO_GROUP");
					continue;
				}
				if(!(asgn.inviteType.equals("USER") || asgn.inviteType.equals("GROUP"))) {
					statuses.add("WRONG_INVITETYPE");  // if user invite, then userId must match tokenUser
					continue;										
				}
				if(!(asgn.grant.equals("ALLOW") || asgn.grant.equals("DENY"))) {
					statuses.add("WRONG_GRANT");  // if user invite, then userId must match tokenUser
					continue;										
				}
				if(asgn.inviteType.equals("USER") && asgn.grant.equals("ADMIN")) {
					statuses.add("USER_CANNOT_PROPOSE_ADMIN_ROLE");
					continue;
				}
				if(user.getUserId() != tokenUser.getUserId() && asgn.inviteType != "GROUP") {
					statuses.add("WRONG_USERID");  // if user invite, then userId must match tokenUser
					continue;					
				}
				if(asgn.inviteType.equals("GROUP")) {
					List<UserGroupAssignment> assignments = usersGroupAssignments(sk, tokenUser.getUserId(), asgn.groupId, new Date(), false, true);
					List<GroupRoles> roles = userRolesInGroupsAtTime(assignments, tokenUser, asgn.groupId);
					if(roles == null || roles.isEmpty()) {
						statuses.add("TOKENUSER_NOT_GROUP_ADMIN");
						continue;
					}
					if(!roles.get(0).isAdminRole()) {
						statuses.add("GROUP_INVITE_BY_NOT_ADMIN");
						continue;
					}
				}
				if(asgn.fromDate != null && asgn.untilDate != null && asgn.fromDate.after(asgn.untilDate)) {
					statuses.add("UNTIL_DATE_TOO_EARLY");
					continue;
				}
				if(asgn.periodic != null && !(asgn.periodic.equals("DAILY") 
												|| asgn.periodic.equals("WEEKLY") 
												|| asgn.periodic.equals("MONTLY") 
												|| asgn.periodic.equals("YEARLY")
											 )) {
					statuses.add("WRONG_PERIODIC_FORMAT");
					continue;
				}
				if(asgn.periodic != null && asgn.repeatTimes == null) {
					statuses.add("MISSING_REPEAT_TIMES");
					continue;
				}
				UserGroupAssignment ug = new UserGroupAssignment();
				ug.setGroup(group);
				if(asgn.inviteType.equals("USER")) {
					ug.setUser(tokenUser);		
					ug.setUserAction(new Date());
				} else {
					ug.setGroupUser(user);
					ug.setGroupAction(new Date());
				}
				ug.setFromDate(asgn.fromDate);
				ug.setUntilDate(asgn.untilDate);
				ug.setGroupRole(asgn.groupRole);
				ug.setInviteType(asgn.inviteType);
				ug.setGrant(asgn.grant);
				ug.setPeriodic(asgn.periodic);
				ug.setRepeatTimes(asgn.repeatTimes);
				sk.save(ug);
				statuses.add("OK");
			}
			sk.commit();								
			return new APIShareOrInviteResponse(statuses);
		}
	}
	
	public APIUserGroupAssignmentUpdateResponse updateLink(APIUserGroupAssignmentUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIUserGroupAssignmentUpdateResponse("AUTH_ERROR", "");
			}
			TrackingUser user = tokenUser;
			if(req.forUserId != null && tokenUser.getUserId() != req.forUserId) {
				if(tokenUser.getAdmin()) {
					TrackingUser tmpUser = authEngine.getUser(sk, req.forUserId);
					if(tmpUser == null) {
						return new APIUserGroupAssignmentUpdateResponse("NO_SUCH_USER", "");
					}
					user = tmpUser;
				} else {
					return new APIUserGroupAssignmentUpdateResponse("AUTH_ERROR", "Only system admin can update assignments on other user behalf.");
				}
			} 	
			
			// confirm
			List<String> confirmStatuses = new LinkedList<String>();
			if(req.confirmLinks != null && !req.confirmLinks.isEmpty()) {
				Criteria c = sk.createCriteria(UserGroupAssignment.class);
				c.add(Restrictions.in("id", req.confirmLinks));
				c.createAlias("user", "User");
				c.createAlias("group", "Group");
				
				@SuppressWarnings("unchecked")
				List<UserGroupAssignment> toConfirm = c.list();
				
				for(UserGroupAssignment uga : toConfirm) {
					if(uga.inviteType.equals("GROUP")) {
						if(uga.getUser().getUserId().equals(user.getUserId())) {
							uga.setAccepted(true);
							Date now = new Date();
							uga.setTimestamp(now);
							uga.setUserAction(now);
							sk.saveOrUpdate(uga);
							confirmStatuses.add("OK");						
							continue;
						} else {
							confirmStatuses.add("DENIED");
							continue;
						}
					} else { // invite type eq "USER"
						OrganizationGroup group = uga.getGroup();
						List<UserGroupAssignment> assignments = usersGroupAssignments(sk, user.getUserId(), group.getGroupId(), new Date(), false, true);
						List<GroupRoles> roles = userRolesInGroupsAtTime(assignments, user, group.getGroupId());
						if(roles == null || roles.isEmpty() || !roles.get(0).isAdminRole()) {
							confirmStatuses.add("USER_NOT_GROUP_ADMIN");
							continue;
						}
						uga.setAccepted(true);
						Date now = new Date();
						uga.setTimestamp(now);
						uga.setGroupAction(now);
						sk.saveOrUpdate(uga);
						confirmStatuses.add("OK");						
						continue;
					}
				}
			}
			// reject
			// confirm
			List<String> rejectStatuses = new LinkedList<String>();
			if(req.rejectLinks != null && !req.rejectLinks.isEmpty()) {
				Criteria c = sk.createCriteria(UserGroupAssignment.class);
				c.add(Restrictions.in("id", req.rejectLinks));
				c.createAlias("user", "User");
				c.createAlias("group", "Group");
	
				@SuppressWarnings("unchecked")
				List<UserGroupAssignment> toReject = c.list();
				
				for(UserGroupAssignment uga : toReject) {
					if(uga.inviteType.equals("GROUP")) {
						if(uga.getUser().getUserId().equals(user.getUserId())) {
							uga.setAccepted(false);
							Date now = new Date();
	//						uga.setTimestamp(now);
							uga.setUserAction(now);
							sk.saveOrUpdate(uga);
							rejectStatuses.add("OK");						
							continue;
						} else {
							rejectStatuses.add("DENIED");
							continue;
						}
					} else { // invite type eq "USER"
						OrganizationGroup group = uga.getGroup();
						List<UserGroupAssignment> assignments = usersGroupAssignments(sk, user.getUserId(), group.getGroupId(), new Date(), false, true);
						List<GroupRoles> roles = userRolesInGroupsAtTime(assignments, user, group.getGroupId());
						if(roles == null || roles.isEmpty() || !roles.get(0).isAdminRole()) {
							rejectStatuses.add("USER_NOT_GROUP_ADMIN");
							continue;
						}
						uga.setAccepted(false);
						Date now = new Date();
	//					uga.setTimestamp(now);
						uga.setGroupAction(now);
						sk.saveOrUpdate(uga);
						rejectStatuses.add("OK");						
						continue;
					}
				} // for
			}
			sk.commit();
			APIUserGroupAssignmentUpdateResponse result = new APIUserGroupAssignmentUpdateResponse();
			result.confirmStatuses = confirmStatuses;
			result.rejectStatuses = rejectStatuses;
			return result;
		}
	}
	
	public APIUserGroupAssignmentResponse listLinks(APIUserGroupAssignmentQuery req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIUserGroupAssignmentResponse("AUTH_ERROR", "");
			}	
			TrackingUser user = tokenUser;
			if(req.forUserId != null && tokenUser.getUserId() != req.forUserId) {
				if(tokenUser.getAdmin()) {
					TrackingUser tmpUser = authEngine.getUser(sk, req.forUserId);
					if(tmpUser == null) {
						return new APIUserGroupAssignmentResponse("NO_SUCH_USER", "");
					}
					user = tmpUser;
				} else {
					return new APIUserGroupAssignmentResponse("AUTH_ERROR", "Only system admin can list assignments for other users.");
				}
			} 
			if(req.forGroupId != null) {
				List<UserGroupAssignment> roleAsg = usersGroupAssignments(sk, user.getUserId(), req.forGroupId, new Date(), false, true);
				if(roleAsg == null || roleAsg.isEmpty()) {
					return new APIUserGroupAssignmentResponse("NO_SUCH_GROUP", "");
				}
				List<GroupRoles> rolesUser = userRolesInGroupsAtTime(roleAsg, user, req.forGroupId);
				if(!(rolesUser.get(0).isAdminRole() || (req.forUserId == null && tokenUser.getAdmin()))) {
					return new APIUserGroupAssignmentResponse("USER_NOT_GROUP_ADMIN", "");					
				}
			}
			List<UserGroupAssignment> assignments = null;
			if(req.forGroupId == null) {
				assignments = usersGroupAssignments(sk, user.getUserId(), null, new Date(), req.pendingOnly != null && req.pendingOnly, req.accept);
			} else {
				assignments = usersGroupAssignments(sk, null, req.forGroupId, new Date(), req.pendingOnly != null && req.pendingOnly, req.accept);
			}		
			return new APIUserGroupAssignmentResponse(assignments.stream()
								.map(x -> new APIUserGroupAssignmentDetail(x))
								.collect(Collectors.toList())						
					);
		}		
	}	

	public APIBaseResponse migrationUpdate(APIMakeMigrationUpdates req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null || !tokenUser.getAdmin()) {
				return new APIBaseResponse("AUTH_ERROR", "Only system admin can make migration updates.");
			}
			
			Criteria c = sk.createCriteria(TrackingUser.class)
					.add(Restrictions.isNull("personalGroup"));
			@SuppressWarnings("unchecked")
			List<TrackingUser> userWithoutPersonalGroups = c.list();
			for(TrackingUser user: userWithoutPersonalGroups) {				
				Date now = new Date();
				OrganizationGroup group = new OrganizationGroup();
				group.setGroupId(user.getUserId());
				group.setDescription(user.getUserId());
				group.setCreator(user); 
				group.setPersonalGroupUser(user);
				user.setPersonalGroup(group);
				group.setTimestamp(now);
				
				UserGroupAssignment asgn = new UserGroupAssignment();
				asgn.setAsPersonalGroup(user, group, now);
				
				sk.saveOrUpdate(user);
				sk.save(group);
				sk.save(asgn);
								
			}
			sk.commit();
			return new APIBaseResponse();
		}
	}
	
	
}

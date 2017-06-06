package com.tracker.engine;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
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
import com.tracker.db.AppConfiguration;
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
				
				// assign creator just as an ADMION
				UserGroupAssignment asgn1 = new UserGroupAssignment();   
				asgn1.setAsPersonalGroup(tokenUser, group, now, "ADMIN");		

				sk.saveOrUpdate(tokenUser);
				sk.save(group);
				sk.save(asgn1);				
				sk.commit();
				return new APIBaseResponse();
			}
			return new APIBaseResponse("GROUP_EXISTS", "A group with the same groupId already exists.");
		}		
	}	
	
	private static void addIntervalValidityRestrictions(Criteria c, String fromDateName, String untilDateName, Date nowTime) {

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
	
	/**
	 * Obtains all relevant user group assignments subject to criteria. 
	 * @param sk
	 * @param userId - if not null, filter according to userId  
	 * @param groupId - if not null, filter according to groupId
	 * @param time - assignment must be relevant for time (interval must contain time)
	 * @param pendingOnly - if true, pending only assignment are returned
	 * @param accept - if true, only accepted assignments are returned
	 * @param provider - if not null, filter according to provider. If null, use only non-provider assignments.
	 * @return a list of UserGroupAssignment subject to criteria defined by parameters.
	 */
	@SuppressWarnings("unchecked")
	public static List<UserGroupAssignment> usersGroupAssignments(SessionKeeper sk, String userId, String groupId, Date time, Boolean pendingOnly, Boolean accept, String provider) {
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
		if(provider != null) {
			c.add(Restrictions.eq("User.provider", provider));
			c.add(Restrictions.eq("Group.provider", provider));
		} else {
			c.add(Restrictions.isNull("User.provider"));
			c.add(Restrictions.isNull("Group.provider"));
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
//		List<UserGroupAssignment> obtainedAssignments = c.list();
//		if(provider != null && groupId == null && pendingOnly != true && accept == true)) {
//			// calculate and add inferred assignments for parent group.
//			List<UserGroupAssignment> inferredAssignments = new LinkedList<UserGroupAssignment>();
//			for(UserGroupAssignment uga: obtainedAssignments) {
//				inferredAssignments.add(uga);
//				if(uga.getGroupRole().equals("USER")) {
//					
//				}
//				
//			}
//			return inferredAssignments;
//		} else {
//			return obtainedAssignments;
//		}
	}
	
	private static boolean assignmentRelevantForTime(UserGroupAssignment asgn, Date time) {  
	    return (asgn.getFromDate() == null && (asgn.getUntilDate() == null || asgn.getUntilDate().compareTo(time) >= 0)) ||
	    	   (asgn.getFromDate() != null && (asgn.getFromDate().compareTo(time) <= 0 && 
	    	   									(asgn.getUntilDate() == null || asgn.getUntilDate().compareTo(time) >= 0)));
	}
	
	/**
	 * Returns effective roles for a user in groups based on assignments.
	 * @param assignments - assignments from which roles are deduced (must be all existing containing 'time') 
	 * @param user - must be not null (no checking currently, null pointer exception). All assignments not related to user are ignored.
	 * @param groupId - apply filter for specific group. Not mandatory.
	 * @param time - filter out assignments for containing specific time (if not null).
	 * @return list of roles
	 */
	public static List<GroupRoles> rolesForUserInGroupsAtTime(List<UserGroupAssignment> assignments, TrackingUser user, String groupId, Date time) {
		if(user == null) return null;
		
		List<UserGroupAssignment> assgn2 = assignments;
		if(time != null) {  // filter by time
			assgn2 = assgn2.stream()
							.filter(x -> GroupEngine.assignmentRelevantForTime(x, time))
							.collect(Collectors.toList());
		}
		if(groupId != null) { // filter by group
			assgn2 = assgn2.stream()
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
			
			String personalGroupOwner = group.getPersonalGroupUser() != null ? group.getPersonalGroupUser().getUserId() : null;
			if( personalGroupOwner != null && user.getUserId().equals(group.getPersonalGroupUser().getUserId()) ) {
				adminCnt = true;
			}
			if(user.getAdmin()) { // system admin
				adminCnt = true;
			}
			GroupRoles grol = new GroupRoles(user, group, adminCnt, userCnt);
			if(personalGroupOwner != null && personalGroupOwner.equals(user.getUserId())) {
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
	
	/**
	 * Given a group with groupId it determines all roles in the group for all users at time
	 * @param assignments - assignments from which roles are calculated. Must be all relevant containing time.
	 * @param groupId - defines a group for which we are checking roles.
	 * @param time - if time is not null it is used for filtering assignments.
	 * @return list of GroupRoles.
	 */
	public static List<GroupRoles> rolesForAllUsersInGroup(List<UserGroupAssignment> assignments, String groupId, Date time) {
		if(groupId == null) return null;
		Map<String, List<UserGroupAssignment>> byUser = assignments.stream()
				.filter(x -> x.group.getGroupId().equals(groupId))
				.collect(Collectors.groupingBy(x -> x.user.getUserId()));
		List<GroupRoles> result = new LinkedList<GroupRoles>();
		for(Map.Entry<String, List<UserGroupAssignment>> e: byUser.entrySet()) {
			TrackingUser user = e.getValue().get(0).getUser();
			List<UserGroupAssignment> asgnList = e.getValue().stream()
							.sorted(Comparator.comparing(x -> x.getTimestamp()))
							.collect(Collectors.toList());
			List<GroupRoles> roles = GroupEngine.rolesForUserInGroupsAtTime(asgnList, user, groupId, time);
			if(roles.size() == 1) {
				result.add(roles.get(0));
			}
		}
		return result;
	}
	 
	/**
	 * Returns a list of GroupRoles including the ones that are obtained by propagation of USER role to parent groups.
	 * @param roles - initial roles
	 * @return
	 */
	public static List<GroupRoles> inferParentRoles(List<GroupRoles> roles) {
		Map<Pair<String, String>, GroupRoles> userAndGroupToRole = new HashMap<Pair<String, String>, GroupRoles>();
		for(GroupRoles groles: roles) { // initialize map
			userAndGroupToRole.put(Pair.of(groles.getUser().getUserId(), groles.getGroup().getGroupId()), groles);
		}
		
		for(GroupRoles groles: roles) {
			if(!groles.isUserRole()) continue;
			OrganizationGroup parentGroup = groles.getGroup().getParentProviderGroup();
			String userId = groles.getUser().getUserId();
			while(parentGroup != null) {
				String groupId = parentGroup.getGroupId();
				if(!userAndGroupToRole.containsKey(Pair.of(userId, groupId))) {
					userAndGroupToRole.put(Pair.of(userId, groupId), new GroupRoles(groles.getUser(), parentGroup, false, true));
				} else {
					userAndGroupToRole.get(Pair.of(userId, groupId)).setUserRole(true);
				}
				parentGroup = parentGroup.getParentProviderGroup();				
			}
		}
		
		return new LinkedList<GroupRoles>(userAndGroupToRole.values());
	}
	
	public APIBaseResponse update(APIGroupUpdate req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
			if(tokenUser == null) {
				return new APIBaseResponse("AUTH_ERROR", "");
			}

			List<UserGroupAssignment> assignments = GroupEngine.usersGroupAssignments(sk, tokenUser.getUserId(), req.groupId, new Date(), false, true, tokenUser.getProvider());
			List<GroupRoles> roles = GroupEngine.rolesForUserInGroupsAtTime(assignments, tokenUser, req.groupId, null);
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
					TrackingUser tmpUser = authEngine.getUser(sk, req.forUser, req.forUserProvider);
					if(tmpUser == null) {
						return new APIGroupQueryResponse("NO_SUCH_USER", "");
					}
					user = tmpUser;
				} else {
					return new APIGroupQueryResponse("AUTH_ERROR", "Only admin can list groups for other users.");
				}
			} 
			Date now = new Date();
			List<UserGroupAssignment> assignments = GroupEngine.usersGroupAssignments(sk, user.getUserId(), null, now, false, true, user.getProvider());
			List<GroupRoles> roles = GroupEngine.rolesForUserInGroupsAtTime(assignments, user, null, now);
		    if(user.getProvider() != null || user.getAdmin()) {  // add infered USER roles on parent groups.
		    	roles = GroupEngine.inferParentRoles(roles); 
		    }						
			List<APIGroupDetail> groups = new LinkedList<APIGroupDetail>();
			for (GroupRoles role : roles) {
				OrganizationGroup group = role.getGroup();
			    APIGroupDetail det = new APIGroupDetail();
			    det.groupId = group.getGroupId();
			    det.description = group.getDescription();
			    det.privateGroup = group.getPrivateGroup();
				
				List<GroupRoles> rolesToList = new LinkedList<GroupRoles>();
			    if(role.isAdminRole() || (group.getPersonalGroupUser() != null && user.getUserId().equals(group.getPersonalGroupUser().getUserId()))) {
				    det.creatorId = group.getCreator().getUserId();			    	
				    det.personalGroupUserId = group.getPersonalGroupUser() != null ? group.getPersonalGroupUser().getUserId() : null;
				    det.timestamp = group.getTimestamp();
				    String userId = user.getUserId();
				    List<UserGroupAssignment> asgnmts2 = GroupEngine.usersGroupAssignments(sk, null, group.getGroupId(), new Date(), false, true, user.getProvider());
					rolesToList = rolesForAllUsersInGroup(asgnmts2, group.getGroupId(), now).stream()
//							.filter(x -> x.getUser().getUserId() != userId)
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
				TrackingUser user = authEngine.getUser(sk, asgn.userId, null); // share/invite possible only for users not authenticated through a provider.
				if(user == null) {
					statuses.add("NO_USER");
					continue;
				}
				OrganizationGroup group = getGroup(sk, asgn.groupId);
				if(group == null) {
					statuses.add("NO_GROUP");
					continue;
				}
				if(group.getProvider() != null) {
					statuses.add("PROVIDER_GROUP_LINK_DENIED");
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
					List<UserGroupAssignment> assignments = GroupEngine.usersGroupAssignments(sk, tokenUser.getUserId(), asgn.groupId, new Date(), false, true, tokenUser.getProvider());
					List<GroupRoles> roles = GroupEngine.rolesForUserInGroupsAtTime(assignments, tokenUser, asgn.groupId, null);
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
					TrackingUser tmpUser = authEngine.getUser(sk, req.forUserId, req.forUserIdProvider);
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
						List<UserGroupAssignment> assignments = GroupEngine.usersGroupAssignments(sk, user.getUserId(), group.getGroupId(), new Date(), false, true, user.getProvider());
						List<GroupRoles> roles = GroupEngine.rolesForUserInGroupsAtTime(assignments, user, group.getGroupId(), null);
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
						List<UserGroupAssignment> assignments = usersGroupAssignments(sk, user.getUserId(), group.getGroupId(), new Date(), false, true, null);
						List<GroupRoles> roles = rolesForUserInGroupsAtTime(assignments, user, group.getGroupId(), null);
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
					TrackingUser tmpUser = authEngine.getUser(sk, req.forUserId, req.forUserIdProvider);
					if(tmpUser == null) {
						return new APIUserGroupAssignmentResponse("NO_SUCH_USER", "");
					}
					user = tmpUser;
				} else {
					return new APIUserGroupAssignmentResponse("AUTH_ERROR", "Only system admin can list assignments for other users.");
				}
			} 
			if(req.forGroupId != null) {
				List<UserGroupAssignment> roleAsg = GroupEngine.usersGroupAssignments(sk, user.getUserId(), req.forGroupId, new Date(), false, true, user.getProvider());
				if(roleAsg == null || roleAsg.isEmpty()) {
					return new APIUserGroupAssignmentResponse("NO_SUCH_GROUP", "");
				}
				List<GroupRoles> rolesUser = GroupEngine.rolesForUserInGroupsAtTime(roleAsg, user, req.forGroupId, null);
				if(!(rolesUser.get(0).isAdminRole() || (req.forUserId == null && tokenUser.getAdmin()))) {
					return new APIUserGroupAssignmentResponse("USER_NOT_GROUP_ADMIN", "");					
				}
			}
			List<UserGroupAssignment> assignments = null;
			if(req.forGroupId == null) {
				assignments = GroupEngine.usersGroupAssignments(sk, user.getUserId(), null, new Date(), req.pendingOnly != null && req.pendingOnly, req.accept, user.getProvider());
			} else {
				assignments = GroupEngine.usersGroupAssignments(sk, null, req.forGroupId, new Date(), req.pendingOnly != null && req.pendingOnly, req.accept, user.getProvider());
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
			
			boolean status = migration1(sk);
			status = migration2(sk);
			status = migration3(sk);
			if(status) {
				return new APIBaseResponse();
			} else {
				return new APIBaseResponse("ERROR", "Migration failed. Check errors.");
			}
		}
	}

	public boolean providerGroupCleanup(SessionKeeper sk) {
		// delete assignments from provider users to non-provider groups and vice versa
		@SuppressWarnings("unchecked")
		List<UserGroupAssignment> toDelete = sk.createCriteria(UserGroupAssignment.class)
			.createAlias("group", "Group")
			.add(Restrictions.isNotNull("Group.provider"))
			.list();
		
		sk.beginTransaction();
		for(UserGroupAssignment uga: toDelete) {
			sk.delete(uga.getGroup());
//			sk.delete(uga);
		}
		sk.endTransaction();
		sk.commit();
		return true;
	}
	
	
	public boolean migration3(SessionKeeper sk) {
		// delete assignments from provider users to non-provider groups and vice versa
		@SuppressWarnings("unchecked")
		List<UserGroupAssignment> wrongOnes = sk.createCriteria(UserGroupAssignment.class)
			.createAlias("user", "User")
			.createAlias("group", "Group")
			.add(Restrictions.disjunction()
					.add(Restrictions.conjunction()
							.add(Restrictions.isNotNull("User.provider"))
							.add(Restrictions.isNull("Group.provider"))
						)
					.add(Restrictions.conjunction()
							.add(Restrictions.isNull("User.provider"))
							.add(Restrictions.isNotNull("Group.provider"))							
						)
				).list();
		sk.beginTransaction();
		for(UserGroupAssignment uga: wrongOnes) {
			sk.delete(uga);
		}
		sk.endTransaction();
		sk.commit();
		return true;
	}
	
	public boolean migration2(SessionKeeper sk) {
		// create initial configuration
		Criteria c = sk.createCriteria(AppConfiguration.class);
		@SuppressWarnings("unchecked")
		List<AppConfiguration> lst = c.list();
		if(lst.size() > 0) return true;
		AppConfiguration conf = new AppConfiguration();
		conf.setIdentifier(1);
		conf.setResetPasswordSecret("veRy.Big>SeCrET");
		sk.save(conf);
		sk.commit();
		return true;
	}
	
	public boolean migration1(SessionKeeper sk) {
		// create missing personal groups
		Criteria c = sk.createCriteria(TrackingUser.class)
				.add(Restrictions.isNull("personalGroup"));
		Date now = new Date();
		@SuppressWarnings("unchecked")
		List<TrackingUser> userWithoutPersonalGroups = c.list();
		for(TrackingUser user: userWithoutPersonalGroups) {				
			OrganizationGroup group = new OrganizationGroup();
			group.setGroupId(user.getUserId());
			group.setDescription(user.getUserId());
			group.setCreator(user); 
			group.setPersonalGroupUser(user);
			user.setPersonalGroup(group);
			group.setTimestamp(now);
			
			UserGroupAssignment asgn1 = new UserGroupAssignment();
			asgn1.setAsPersonalGroup(user, group, now, "ADMIN");
			UserGroupAssignment asgn2 = new UserGroupAssignment();
			asgn2.setAsPersonalGroup(user, group, now, "USER");
			
			sk.saveOrUpdate(user);
			sk.save(group);
			sk.save(asgn1);
			sk.save(asgn2);
							
		}
//		sk.commit();
		// create missing user assignments to personal group
		List<TrackingUser> allUsers = sk.createCriteria(TrackingUser.class).list();
		for(TrackingUser usr: allUsers) {
			OrganizationGroup group = usr.getPersonalGroup();
			List<UserGroupAssignment> assignments = usersGroupAssignments(sk, usr.getUserId(), group.getGroupId(), new Date(), false, true, usr.getProvider());
			List<GroupRoles> roles = rolesForUserInGroupsAtTime(assignments, usr, group.getGroupId(), null);
			if(roles.size() == 0) {
				UserGroupAssignment asgn1 = new UserGroupAssignment();
				asgn1.setAsPersonalGroup(usr, group, now, "ADMIN");		
				UserGroupAssignment asgn2 = new UserGroupAssignment();
				asgn2.setAsPersonalGroup(usr, group, now, "USER");
				sk.save(asgn1);
				sk.save(asgn2);
			} else {
				if(!roles.get(0).isAdminRole()) {
					UserGroupAssignment asgn1 = new UserGroupAssignment();
					asgn1.setAsPersonalGroup(usr, group, now, "ADMIN");
					sk.save(asgn1);
				}
				if(!roles.get(0).isUserRole()) {
					UserGroupAssignment asgn2 = new UserGroupAssignment();
					asgn2.setAsPersonalGroup(usr, group, now, "USER");
					sk.save(asgn2);					
				}
			}
		}
		sk.commit();
		return true;
	}
}

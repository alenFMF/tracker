package com.tracker.engine;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Calendar;
import java.util.ArrayList;

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

import static java.util.Collections.binarySearch;

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
		List<String> userIds = null;
		if(userId != null) {
			userIds = new LinkedList<String>();
			userIds.add(userId);
		}
		return usersListGroupAssignments(sk, userIds, groupId, time, pendingOnly, accept, provider);
	}
	
	
	public static List<UserGroupAssignment> usersListGroupAssignments(SessionKeeper sk, List<String> userIds, String groupId, Date time, Boolean pendingOnly, Boolean accept, String provider) {
		// if now = true only assignments that are valid at the moment are listed
		if(userIds == null && groupId == null) return null;  
		Criteria c = sk.createCriteria(UserGroupAssignment.class);
		c.createAlias("user", "User");
		c.createAlias("group", "Group");
		if(userIds != null) {			
			c.add(Restrictions.in("User.userId", userIds));
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
			c.addOrder(Order.asc("timestamp"));
		}	
		
		return c.list();
	}	
	
	private static boolean assignmentRelevantForTime(UserGroupAssignment asgn, Date time) {  
	    return (asgn.getFromDate() == null && (asgn.getUntilDate() == null || asgn.getUntilDate().compareTo(time) >= 0)) ||
	    	   (asgn.getFromDate() != null && (asgn.getFromDate().compareTo(time) <= 0 && 
	    	   									(asgn.getUntilDate() == null || asgn.getUntilDate().compareTo(time) >= 0)));
	}

	/**
	 * Returns intersection of admin intervals (when admin is allowed to be admin) and user intervals (when user allows to be tracked).
	 * If adminId is null it returns user intervals.
	 * @param sk - session keeper.
	 * @param userId - must be not null.
	 * @param groupId - must be not null (group for which we are calculating intersection of intervals).
	 * @param adminId - not mandatory (if we want to find out when an admin can see a certain user in a group).
	 * @param fromDate - fromDate.
	 * @param untilDate - untilDate.
	 * @return list of intervals.
	 */
	private static List<Pair<Date, Date>> calculateIntervals(SessionKeeper sk, String userId, String groupId, String adminId, Date fromDate, Date untilDate) {
		if (userId == null || groupId == null) return null;
		Criteria userC = sk.createCriteria(UserGroupAssignment.class);
		userC.createAlias("user", "User");
		userC.createAlias("group", "Group");
		userC.add(Restrictions.eq("groupRole", "USER"));
		userC.add(Restrictions.eq("Group.groupId", groupId));
		userC.add(Restrictions.le("fromDate", untilDate));
		userC.add(Restrictions.eq("User.userId", userId));
		userC.add(Restrictions.eq("accepted", true));
		userC.addOrder(Order.desc("timestamp"));
		List<UserGroupAssignment> userAssignments = userC.list();
		List<Pair<Date, Date>> userIntervals = getAllowedIntervals(userAssignments, fromDate, untilDate);
		if (adminId != null) {
			Criteria adminC = sk.createCriteria(UserGroupAssignment.class);
			adminC.createAlias("user", "User");
			adminC.createAlias("group", "Group");
			adminC.add(Restrictions.eq("groupRole", "ADMIN"));
			adminC.add(Restrictions.eq("Group.groupId", groupId));
			adminC.add(Restrictions.le("fromDate", untilDate));
			adminC.add(Restrictions.eq("User.userId", adminId));
			adminC.add(Restrictions.eq("accepted", true));
			adminC.addOrder(Order.desc("timestamp"));
			List<UserGroupAssignment> adminAssignments = adminC.list();
			List<Pair<Date, Date>> adminIntervals = getAllowedIntervals(adminAssignments, fromDate, untilDate);

			List<Pair<Date, Date>> intervalsIntersection = new ArrayList<>();
			Integer userIndex = 0;
			Integer adminIndex = 0;
			while (userIndex < userIntervals.size() && adminIndex < adminIntervals.size()) {
			    Pair<Date, Date> userI = userIntervals.get(userIndex);
                Pair<Date, Date> adminI = adminIntervals.get(adminIndex);
                if (userI.getLeft().before(adminI.getRight()) && adminI.getLeft().before(userI.getRight())) {
                	Date a = userI.getLeft().after(adminI.getLeft()) ? userI.getLeft() : adminI.getLeft();
                	Date b = userI.getRight().before(adminI.getRight()) ? userI.getRight() : adminI.getRight();
                	intervalsIntersection.add(Pair.of(a , b));
				}
				userIndex = userI.getRight().after(adminI.getRight()) ? userIndex : userIndex + 1;
				adminIndex = adminI.getRight().after(userI.getRight()) ? adminIndex : adminIndex + 1;
				if (userI.getRight().equals(adminI.getRight())) {
					userIndex++;
					adminIndex++;
				}
            }
			return intervalsIntersection;
		}
		return userIntervals;
	}

	/**
	 * Calculates allowed intervals from assignments.
	 * @param assignments - list of assignments (ALLOWS and DENIES) from which the intervals are calculated.
	 * @param fromDate - fromDate.
	 * @param untilDate - untilDate.
	 * @return list of intervals.
	 */
	private static List<Pair<Date, Date>> getAllowedIntervals(List<UserGroupAssignment> assignments, Date fromDate, Date untilDate) {
		List<Pair<Date, Date>> intervals = new ArrayList<>();
		for (UserGroupAssignment assignment : assignments) {
			List<Pair<Date, Date>> allIntervals = new ArrayList<>();
			if (assignment.getPeriodic() == null) {
				Date a = assignment.getFromDate() == null ? fromDate : assignment.getFromDate();
				Date b = assignment.getUntilDate() == null ? untilDate : assignment.getUntilDate();
				if (a.before(untilDate) && b.after(fromDate)) {
					a = fromDate.after(a) ? fromDate : a;
					b = untilDate.before(b) ? untilDate : b;
					allIntervals.add(Pair.of(a, b));
				} else {
					continue;
				}
			} else {
				for (int i = 0; i < assignment.getRepeatTimes(); i++) {
					Calendar calendarFrom = Calendar.getInstance();
					Calendar calendarUntil = Calendar.getInstance();
					calendarFrom.setTime(assignment.getFromDate());
					calendarUntil.setTime(assignment.getUntilDate());
					if (assignment.getPeriodic().equals("DAILY")) {
						calendarFrom.add(Calendar.DATE, i);
						calendarUntil.add(Calendar.DATE, i);
					} else if (assignment.getPeriodic().equals("WEEKLY")) {
						calendarFrom.add(Calendar.DATE, 7*i);
						calendarUntil.add(Calendar.DATE, 7*i);
					} else if (assignment.getPeriodic().equals("MONTLY")) {
						calendarFrom.add(Calendar.MONTH, i);
						calendarUntil.add(Calendar.MONTH, i);
					} else if (assignment.getPeriodic().equals("YEARLY")) {
						calendarFrom.add(Calendar.YEAR, i);
						calendarUntil.add(Calendar.YEAR, i);
					}
					if (calendarFrom.getTime().before(untilDate) && calendarUntil.getTime().after(fromDate)) {
						Date a = fromDate.after(calendarFrom.getTime()) ? fromDate : calendarFrom.getTime();
						Date b = untilDate.before(calendarUntil.getTime()) ? untilDate : calendarUntil.getTime();
						allIntervals.add(Pair.of(a, b));
					} else if (calendarFrom.getTime().after(untilDate)) {
						break;
					}
				}
			}

			for (Pair<Date, Date> newInterval : allIntervals) {
				Comparator<Pair<Date, Date>> fromDateComparator = (d1, d2) -> {
                    if (d1.getLeft().equals(d2.getLeft())) return 0;
                    return d1.getLeft().before(d2.getLeft()) ? -1 : 1;
                };
				Comparator<Pair<Date, Date>> untilDateComparator = (d1, d2) -> {
					if (d1.getRight().equals(d2.getRight())) return 0;
					return d1.getRight().before(d2.getRight()) ? -1 : 1;
				};
				Integer fromIndex = binarySearch(intervals, newInterval, fromDateComparator);
				Integer untilIndex = binarySearch(intervals, newInterval, untilDateComparator);
				fromIndex = fromIndex < 0 ? Math.abs(fromIndex) - 1 : fromIndex;
				untilIndex = untilIndex < 0 ? Math.abs(untilIndex) - 1 : untilIndex;
				if (newInterval.getRight().after(intervals.get(untilIndex).getLeft())) {
					if (fromIndex > 0 && newInterval.getLeft().before(intervals.get(fromIndex - 1).getRight())) {
						if (assignment.getGrant().equals("ALLOW")) {
							intervals.add(fromIndex - 1, Pair.of(intervals.get(fromIndex - 1).getLeft(), intervals.get(untilIndex).getRight()));
							fromIndex--;
						} else if (assignment.getGrant().equals("DENY")) {
							intervals.set(fromIndex - 1, Pair.of(intervals.get(fromIndex - 1).getLeft(), newInterval.getLeft()));
							intervals.set(untilIndex, Pair.of(newInterval.getRight(), intervals.get(untilIndex).getRight()));
						}
					} else {
						if (assignment.getGrant().equals("ALLOW")) {
							intervals.add(fromIndex, Pair.of(newInterval.getLeft(), intervals.get(untilIndex).getRight()));
						} else if (assignment.getGrant().equals("DENY")) {
							intervals.set(untilIndex, Pair.of(newInterval.getRight(), intervals.get(untilIndex).getRight()));
						}
					}
				} else {
					if (fromIndex > 0 && newInterval.getLeft().before(intervals.get(fromIndex - 1).getRight())) {
						if (assignment.getGrant().equals("ALLOW")) {
							intervals.add(fromIndex - 1, Pair.of(intervals.get(fromIndex - 1).getLeft(), newInterval.getRight()));
							fromIndex--;
						} else if (assignment.getGrant().equals("DENY")) {
							intervals.set(fromIndex - 1, Pair.of(intervals.get(fromIndex - 1).getLeft(), newInterval.getLeft()));
						}
					} else {
						if (assignment.getGrant().equals("ALLOW")) {
							intervals.add(fromIndex, Pair.of(newInterval.getLeft(), newInterval.getRight()));
							untilIndex--;
						}
					}
				}
				if (assignment.getGrant().equals("DENY")) {
					fromIndex--;
					untilIndex -= 2;
				}
				for (Integer i = untilIndex - fromIndex; i >= 0; i--) {
					intervals.remove(fromIndex + i + 1);
				}
			}
		}
		return intervals;
	}

	public static List<GroupRoles> rolesForUserInGroupsAtTime(List<UserGroupAssignment> assignments, TrackingUser user, String groupId, Date time) {
		return rolesForUserInGroupsAtTime(assignments, user, groupId, time, null, null);
	}

		/**
         * Returns effective roles for a user in groups based on assignments.
         * @param assignments - assignments from which roles are deduced (must be all existing containing 'time')
         * @param user - must be not null (no checking currently, null pointer exception). All assignments not related to user are ignored.
         * @param groupId - apply filter for specific group. Not mandatory.
         * @param time - filter out assignments for containing specific time (if not null).
         * @return list of roles
         */
	public static List<GroupRoles> rolesForUserInGroupsAtTime(List<UserGroupAssignment> assignments, TrackingUser user, String groupId, Date time, Date fromDate, Date untilDate) {
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
											.collect(Collectors.toList());

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

			List<UserGroupAssignment> userAssignments = groupAssignments.stream()
					.filter(x -> x.getGroupRole().equals("USER"))
					.collect(Collectors.toList());

			List<UserGroupAssignment> adminAssignments = groupAssignments.stream()
					.filter(x -> x.getGroupRole().equals("ADMIN"))
					.collect(Collectors.toList());

			List<Pair<Date, Date>> adminIntervals = null;
			List<Pair<Date, Date>> userIntervals = null;
			if (fromDate != null) {
				adminIntervals = getAllowedIntervals(adminAssignments, fromDate, untilDate != null ? untilDate : time);
				userIntervals = getAllowedIntervals(userAssignments, fromDate, untilDate != null ? untilDate : time);
			}

			GroupRoles grol = new GroupRoles(user, group, adminCnt, userCnt, adminIntervals, userIntervals);
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
	 
//	/**
//	 * Returns a list of GroupRoles including the ones that are obtained by propagation of USER role to parent groups.
//	 * @param roles - initial roles
//	 * @return
//	 */
//	public static List<GroupRoles> inferParentRoles(SessionKeeper sk, List<GroupRoles> roles) {
//		Map<Pair<String, String>, GroupRoles> userAndGroupToRole = new HashMap<Pair<String, String>, GroupRoles>();
//		for(GroupRoles groles: roles) { // initialize map
//			userAndGroupToRole.put(Pair.of(groles.getUser().getUserId(), groles.getGroup().getGroupId()), groles);
//		}
//		
//		for(GroupRoles groles: roles) {
//			if(!groles.isUserRole()) continue;
//			OrganizationGroup parentGroup = groles.getGroup().getParentProviderGroup();
//			String userId = groles.getUser().getUserId();
//			while(parentGroup != null) {
//				String groupId = parentGroup.getGroupId();
//				if(!userAndGroupToRole.containsKey(Pair.of(userId, groupId))) {
//					userAndGroupToRole.put(Pair.of(userId, groupId), new GroupRoles(groles.getUser(), parentGroup, false, true));
//				} else {
//					userAndGroupToRole.get(Pair.of(userId, groupId)).setUserRole(true);
//				}
//				parentGroup = parentGroup.getParentProviderGroup();				
//			}
//		}
//		
//		return new LinkedList<GroupRoles>(userAndGroupToRole.values());
//	}
	
	
	/**
	 * Returns a list of GroupRoles obtained by propagating ADMIN down and USER up
	 * @param roles - initial roles
	 * @return
	 */
	public static List<GroupRoles> inferRoles(SessionKeeper sk, List<GroupRoles> roles) {
		if(roles.size() == 0) return new LinkedList<GroupRoles>();
		String provider = roles.get(0).getUser().getProvider();
		if(provider == null) {
			return new LinkedList<GroupRoles>();
		}
		@SuppressWarnings("unused")
		List<OrganizationGroup> providerGroups = sk.createCriteria(OrganizationGroup.class).add(Restrictions.eq("provider", provider)).list();
		
		// mapping groupId -> group
		Map<String, OrganizationGroup> toGroup = new HashMap<String, OrganizationGroup>();
		for(OrganizationGroup grp: providerGroups) {
			toGroup.put(grp.getGroupId(), grp);
		}
		
		// mapping groupId -> list of children group id
		// init
		Map<String, List<String>> toChildren = new HashMap<String, List<String>>();
		for(OrganizationGroup grp: providerGroups) {
			if(!toChildren.containsKey(grp.getGroupId())) {
				toChildren.put(grp.getGroupId(), new LinkedList<String>());
			}
		}
		// mapping
		for(OrganizationGroup grp: providerGroups) {
			OrganizationGroup parent = grp.getParentProviderGroup();
			if(parent != null) {
				toChildren.get(parent.getGroupId()).add(grp.getGroupId());
			}
		}

		Map<Pair<String, String>, GroupRoles> userAndGroupToRole = new HashMap<Pair<String, String>, GroupRoles>();
				
		for(GroupRoles groles: roles) { // initialize map
			userAndGroupToRole.put(Pair.of(groles.getUser().getUserId(), groles.getGroup().getGroupId()), groles);
		}
				
		// depth first search - infer ADMIN ROLES
		for(GroupRoles groles: roles) {
			if(!groles.isAdminRole()) continue;
			LinkedList<String> stack = new LinkedList<String>();
			String groupId = groles.getGroup().getGroupId();
			String userId = groles.getUser().getUserId();
			stack.push(groupId);
			// depth first search
			while(stack.size() > 0) {
				String tmpGroupId = stack.pop();				
				OrganizationGroup currentGroup = toGroup.get(tmpGroupId);
				if(!userAndGroupToRole.containsKey(Pair.of(userId, tmpGroupId))) {
					userAndGroupToRole.put(Pair.of(userId, tmpGroupId), new GroupRoles(groles.getUser(), currentGroup, true, false));
				} else {
					userAndGroupToRole.get(Pair.of(userId, tmpGroupId)).setAdminRole(true);
				}
				if(toChildren.containsKey(tmpGroupId)) {
					for(String child: toChildren.get(tmpGroupId)) {
						stack.push(child);
					}					
				}				
			}
		}	
		
		// infer user roles
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
		
		return new LinkedList<GroupRoles>(userAndGroupToRole.values().stream()
				.sorted(Comparator.comparing(x -> x.getGroup().getGroupId()))
				.collect(Collectors.toList()));
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
		
	
	public static Map<String, List<GroupRoles>> groupRolesForUser(SessionKeeper sk, TrackingUser user, List<String> userFilter, Date time, Date fromDate, Date untilDate) {
		List<UserGroupAssignment> assignments = GroupEngine.usersGroupAssignments(sk, user.getUserId(), null, time, false, true, user.getProvider());
		List<GroupRoles> roles = null;
		List<GroupRoles> originalRoles = GroupEngine.rolesForUserInGroupsAtTime(assignments, user, null, time, fromDate, untilDate);
	    if(user.getProvider() != null || user.getAdmin()) {  // add infered USER roles on parent groups.
	    	roles = GroupEngine.inferRoles(sk, originalRoles); 
	    } else {
	    	roles = originalRoles;						
	    }
 	    Map<String, List<GroupRoles>> groupToRoles = new HashMap<String, List<GroupRoles>>();
	    String userId = user.getUserId();

		for (GroupRoles role : roles) {
			OrganizationGroup group = role.getGroup();
			List<GroupRoles> rolesToList = new LinkedList<GroupRoles>();
		    if(role.isAdminRole() || (group.getPersonalGroupUser() != null && user.getUserId().equals(group.getPersonalGroupUser().getUserId()))) {
			    List<UserGroupAssignment> asgnmts2 = GroupEngine.usersListGroupAssignments(sk, userFilter, group.getGroupId(), new Date(), false, true, user.getProvider());							    		
			    List<GroupRoles> tmpRoleList = rolesForAllUsersInGroup(asgnmts2, group.getGroupId(), time);
			    List<GroupRoles> checkList = tmpRoleList.stream().filter(x -> x.getUser().getUserId() == userId).collect(Collectors.toList());
			    if(checkList.size() == 0) {  // fix propagated admin role
			    	tmpRoleList.add(role);
			    } else {   
			    	tmpRoleList.get(0).setAdminRole(true);
			    }				    
			    rolesToList = tmpRoleList.stream()
						.sorted(Comparator.comparing(x -> x.getUser().getUserId()))
						.collect(Collectors.toList());				    
		    } else {			    	
		    	rolesToList.add(role);
		    }
		    groupToRoles.put(group.getGroupId(), rolesToList);		
		}
		return groupToRoles;
	}

	public static Map<String, List<GroupRoles>> groupRolesForUser(SessionKeeper sk, TrackingUser user, List<String> userFilter, Date time) {
		return groupRolesForUser(sk, user, userFilter, time, null, null);
	}

	public static Map<String, String> allowedUsersForAdminToSee(SessionKeeper sk, TrackingUser user, List<String> userFilter, Date time) {
		Map<String, List<GroupRoles>> allToSee = groupRolesForUser(sk, user, userFilter, time);
		Set<String> allowedUsers = new HashSet<String>();
		Set<String> initialCandidates = new HashSet<String>(userFilter);
		for(String groupId: allToSee.keySet()) {
			Set<String> candidates = new HashSet<String>();
			boolean isAdmin = false;
			for(GroupRoles role: allToSee.get(groupId)) {
				String current = role.getUser().getUserId();
				if(current.equals(user.getUserId())) {
					isAdmin = role.isAdminRole();
				} else if(initialCandidates.contains(current)) {
					candidates.add(current);
				}
			}
			if(isAdmin) {
				allowedUsers.addAll(candidates);
			}
		}
		Map<String, String> result = new HashMap<String, String>();
		for(String cand: allowedUsers) {
			result.put(cand, "");  // currently nothing
		}
		return result;
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
			Map<String, List<GroupRoles>> adminGroups = groupRolesForUser(sk, user, null, now, req.fromDate, req.untilDate);
			
			List<APIGroupDetail> groups = new LinkedList<APIGroupDetail>();
			List<String> sortedGroups = adminGroups.keySet().stream().sorted().collect(Collectors.toList());
			for (String groupId : sortedGroups) {
				List<GroupRoles> rolesToList = adminGroups.get(groupId);
				if(rolesToList == null || rolesToList.size() == 0) continue;
				OrganizationGroup group = rolesToList.get(0).getGroup();
			    APIGroupDetail det = new APIGroupDetail();
			    det.groupId = group.getGroupId();
			    det.description = group.getDescription();
			    det.privateGroup = group.getPrivateGroup();
			    det.creatorId = group.getCreator().getUserId();			    	
			    det.personalGroupUserId = group.getPersonalGroupUser() != null ? group.getPersonalGroupUser().getUserId() : null;
			    det.timestamp = group.getTimestamp();
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

	
//	@SuppressWarnings("unchecked")
//	public APIGroupQueryResponse list(APIGroupQuery req) {
//		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {	
//			TrackingUser tokenUser = authEngine.getTokenUser(sk, req.token);
//			if(tokenUser == null) {
//				return new APIGroupQueryResponse("AUTH_ERROR", "");
//			}	
//			TrackingUser user = tokenUser;
//			if(req.forUser != null && tokenUser.getUserId() != req.forUser) {
//				if(tokenUser.getAdmin()) {
//					TrackingUser tmpUser = authEngine.getUser(sk, req.forUser, req.forUserProvider);
//					if(tmpUser == null) {
//						return new APIGroupQueryResponse("NO_SUCH_USER", "");
//					}
//					user = tmpUser;
//				} else {
//					return new APIGroupQueryResponse("AUTH_ERROR", "Only admin can list groups for other users.");
//				}
//			} 
//			Date now = new Date();
//			List<UserGroupAssignment> assignments = GroupEngine.usersGroupAssignments(sk, user.getUserId(), null, now, false, true, user.getProvider());
//			List<GroupRoles> roles = null;
//			List<GroupRoles> originalRoles = GroupEngine.rolesForUserInGroupsAtTime(assignments, user, null, now);
//		    if(user.getProvider() != null || user.getAdmin()) {  // add infered USER roles on parent groups.
//		    	roles = GroupEngine.inferRoles(sk, originalRoles); 
//		    }						
//			List<APIGroupDetail> groups = new LinkedList<APIGroupDetail>();
//			for (GroupRoles role : roles) {
//				OrganizationGroup group = role.getGroup();
//			    APIGroupDetail det = new APIGroupDetail();
//			    det.groupId = group.getGroupId();
//			    det.description = group.getDescription();
//			    det.privateGroup = group.getPrivateGroup();
//
//				List<GroupRoles> rolesToList = new LinkedList<GroupRoles>();
//			    if(role.isAdminRole() || (group.getPersonalGroupUser() != null && user.getUserId().equals(group.getPersonalGroupUser().getUserId()))) {
//				    det.creatorId = group.getCreator().getUserId();			    	
//				    det.personalGroupUserId = group.getPersonalGroupUser() != null ? group.getPersonalGroupUser().getUserId() : null;
//				    det.timestamp = group.getTimestamp();
//				    String userId = user.getUserId();
//				    List<UserGroupAssignment> asgnmts2 = GroupEngine.usersGroupAssignments(sk, null, group.getGroupId(), new Date(), false, true, user.getProvider());							    		
//				    List<GroupRoles> tmpRoleList = rolesForAllUsersInGroup(asgnmts2, group.getGroupId(), now);
//				    List<GroupRoles> checkList = tmpRoleList.stream().filter(x -> x.getUser().getUserId() == userId).collect(Collectors.toList());
//				    if(checkList.size() == 0) {  // fix propagated admin role
//				    	tmpRoleList.add(role);
//				    } else {   
//				    	tmpRoleList.get(0).setAdminRole(true);
//				    }				    
//				    rolesToList = tmpRoleList.stream()
//							.sorted(Comparator.comparing(x -> x.getUser().getUserId()))
//							.collect(Collectors.toList());				    
//			    } else {			    	
//			    	rolesToList.add(role);
//			    }
//			    det.setUsers(rolesToList.stream()
//			    				.map(x -> new APIUserGroupRolesDetail(x))
//			    				.collect(Collectors.toList())
//			    		    );
//			    groups.add(det);
//			}
//			APIGroupQueryResponse res = new APIGroupQueryResponse(groups);
//			return res;
//		}
//	}	
	
	
	
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
			
//			boolean status = migration1(sk);
			boolean status = migration2(sk);
//			status = migration3(sk);
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

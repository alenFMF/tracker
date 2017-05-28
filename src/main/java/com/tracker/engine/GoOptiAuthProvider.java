package com.tracker.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.criterion.Restrictions;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.tracker.apientities.goopti.APIGoOptiAuthentication;
import com.tracker.apientities.goopti.APIGoOptiAuthenticationResponse;
import com.tracker.apientities.goopti.APIGoOptiFranchiseRole;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.TrackingUser;
import com.tracker.db.UserGroupAssignment;
import com.tracker.utils.SessionKeeper;

public class GoOptiAuthProvider implements IAuthProvider {
	
//	private String service = "https://test.goopti.com/goopti-services/authentication/signin";
	private String service = "http://172.31.26.216/goopti-services/authentication/signin";
	
	@Override
	public AuthenticationObject authenticate(String username, String password) {
		// TODO Auto-generated method stub
		RestTemplate restTempl = new RestTemplate();
		APIGoOptiAuthentication request = new APIGoOptiAuthentication(username, password);
		try {
			APIGoOptiAuthenticationResponse res = restTempl.postForObject(service, request, APIGoOptiAuthenticationResponse.class);
			if(res.isLocked) {
				AuthenticationObject auth = new AuthenticationObject();
				auth.setStatus("AUTH_ERROR");
				auth.setErrorMessage("User is locked.");
				return auth;				
			}
			AuthenticationObject auth = new AuthenticationObject();
			auth.setToken(res.token);
			auth.setName(res.name + " " + res.surname);
			auth.setUsername(username);
			auth.setEmail(res.email);
			auth.setStatus("OK");
			auth.setErrorMessage("");
			List<ProviderGroupRoles> roles = new LinkedList<ProviderGroupRoles>();
			for(APIGoOptiFranchiseRole role: res.franchiseRoles) {
				String roleMap = getRoleMapping(role.role);
				if(roleMap != null) {
					ProviderGroupRoles pgr = new ProviderGroupRoles();	
					pgr.roleId = roleMap;
					pgr.originalRoleDescription = role.role;
					pgr.groupId = getKey() + "#" + role.franchiseId; 
					pgr.groupDescripton = role.franchiseName;
					pgr.parentGroupId = getKey() + "#" + role.parentFranchiseId;
					roles.add(pgr);
				}
			}
			auth.setGroupRoles(roles);
			return auth;
		} catch (RestClientException e) {
			AuthenticationObject auth = new AuthenticationObject();
			auth.setStatus("CLIENT_ERROR");
			auth.setErrorMessage(e.getMessage());
			return auth;
		} catch (Exception e) {
			AuthenticationObject auth = new AuthenticationObject();
			auth.setStatus("ERROR");
			auth.setErrorMessage(e.getMessage());	
			return auth;
		}	
	}
	
	private String getRoleMapping(String role) {
		switch (role) {
	        case "hqadmin":
	        case "admin":
	        case "plan organizer":
	        case "plan coordinator":
	        case "plan dispatcher":
	        	return "ADMIN";
	        case "driver":
	        	return "USER";
	        default:
	            return null;
		}
	}
	
	@Override
	public String getKey() {
		return "GOOPTI";
	}

	@Override
	public void updateRoles(SessionKeeper sk, TrackingUser user, AuthenticationObject auth) {
		// generate missing groups	
		List<ProviderGroupRoles> roles = auth.getGroupRoles();
		@SuppressWarnings("unchecked")
		List<OrganizationGroup> currentProviderGroups = sk.createCriteria(OrganizationGroup.class)
														.add(Restrictions.eq("provider", getKey()))
														.list();

		Map<String, OrganizationGroup> idToGroup = new HashMap<String, OrganizationGroup>();
		for(OrganizationGroup group : currentProviderGroups) {
			idToGroup.put(group.getGroupId(), group);
		}

//		Set<String> currentGroupIdsSet = currentProviderGroups.stream()
//											.map(x -> x.getGroupId())
//											.collect(Collectors.toCollection(HashSet::new));
		
		// new groups with descriptions
		Map<String, Pair<String, String>> newGroupIds = new HashMap<String, Pair<String, String>>();
		Map<String, Pair<String, String>> newGroupDescriptons = new HashMap<String, Pair<String, String>>();
		for(ProviderGroupRoles rl : roles) {
			if(!idToGroup.containsKey(rl.groupId)) {
				if(!newGroupIds.containsKey(rl.groupId) || newGroupIds.get(rl.groupId) == null) {
					newGroupIds.put(rl.groupId, Pair.of(rl.groupDescripton, rl.parentGroupId));
				} 
			} else { // update descriptions
				if(rl.groupDescripton != null && !idToGroup.get(rl.groupId).equals(rl.groupDescripton)) {
					newGroupDescriptons.put(rl.groupId, Pair.of(rl.groupDescripton, rl.parentGroupId));
				}
			}
			 
			if(rl.parentGroupId != null) {
				String pgName = rl.parentGroupId;
				if(!idToGroup.containsKey(pgName) && !newGroupIds.containsKey(pgName)) {
					newGroupIds.put(pgName, null);
				}
			}
		}
		
		// update existing groups
		Date now = new Date();
		
		for(Map.Entry<String, Pair<String, String>> ngrp: newGroupDescriptons.entrySet()) {
			OrganizationGroup group = idToGroup.get(ngrp.getKey());			
			group.setDescription(ngrp.getValue().getLeft());
			group.setProviderParentGroupId(ngrp.getValue().getRight());
			sk.saveOrUpdate(group);
		}
		
		// generate new groups
		for(Map.Entry<String, Pair<String, String>> ngrp: newGroupIds.entrySet()) {
			OrganizationGroup group = new OrganizationGroup();
			group.setGroupId(ngrp.getKey());
			if(ngrp.getValue() != null) {
				group.setDescription(ngrp.getValue().getLeft());
				group.setProviderParentGroupId(ngrp.getValue().getRight());				
			}
			group.setCreator(user); 
			group.setTimestamp(now);			
			group.setProvider(getKey());
			
			sk.save(group);
			idToGroup.put(group.getGroupId(), group);
		}
		// fix parent groups
		
		for(Map.Entry<String, OrganizationGroup> itg: idToGroup.entrySet()) {
			OrganizationGroup group = itg.getValue();
			if(group.getProviderParentGroupId() != null) {
				if(group.getParentProviderGroup() == null || 
						!group.parentProviderGroup.getGroupId().equals(group.getProviderParentGroupId())) {
					group.setParentProviderGroup(idToGroup.get(group.getProviderParentGroupId()));
					sk.saveOrUpdate(group);
				}
			}
		}
		
		// existing user roles
		List<UserGroupAssignment> userAssignments = GroupEngine.usersGroupAssignments(sk, user.getUserId(), null, now, false, true, getKey());
		List<GroupRoles> userRoles = GroupEngine.rolesForUserInGroupsAtTime(userAssignments, user, null, null);	
		
		Map<Pair<String, String>, Boolean> currentRoles = new HashMap<Pair<String, String>, Boolean>();
		for(GroupRoles gr: userRoles) {
			if(gr.isAdminRole()) {
				currentRoles.put(Pair.of(gr.getGroup().getGroupId(), "ADMIN"), null);
			}
			if(gr.isUserRole()) {
				currentRoles.put(Pair.of(gr.getGroup().getGroupId(), "USER"), null);
			}
		}
		
		Map<Pair<String, String>, Boolean> newRoles = new HashMap<Pair<String, String>, Boolean>();
		for(ProviderGroupRoles pgr: roles) {
			if(pgr.getRoleId() != null) {
				newRoles.put(Pair.of(pgr.getGroupId(), pgr.getRoleId()), null);
			}
		}
		
		// check which current roles are still valid
		
		Set<Pair<String, String>> additionalRoles = new HashSet<Pair<String, String>>();
		for(Pair<String, String> oneRole: newRoles.keySet()) {
			if(currentRoles.containsKey(oneRole)) {
				currentRoles.put(oneRole, true); // mark as existing
			} else {
				additionalRoles.add(oneRole); // mark as new, additional
			}
		}
		
		Set<Pair<String, String>> depricatedRoles = new HashSet<Pair<String, String>>();
		for(Pair<String, String> oneRole: currentRoles.keySet()) {
			if(currentRoles.get(oneRole) == null && !oneRole.getLeft().contains("@")) {  // personal groups are exceptions
				depricatedRoles.add(oneRole);
			}
		}
		
		// grant DENY from now
		for(Pair<String, String> oneRole: depricatedRoles) {
			sk.save(new UserGroupAssignment(user, idToGroup.get(oneRole.getLeft()), now, oneRole.getRight(), "DENY"));
		}
		
		// grant ALLOW from now
		for(Pair<String, String> oneRole: additionalRoles) {
			sk.save(new UserGroupAssignment(user, idToGroup.get(oneRole.getLeft()), now, oneRole.getRight(), "ALLOW"));
		}
		
		sk.commit();
	}

	
}

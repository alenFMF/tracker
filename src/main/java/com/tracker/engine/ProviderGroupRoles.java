package com.tracker.engine;

public class ProviderGroupRoles {
	public String groupId;
	public String groupDescripton;
	public String parentGroupId;	
	public String roleId;
	public String originalRoleDescription;
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getGroupDescripton() {
		return groupDescripton;
	}
	public void setGroupDescripton(String groupDescripton) {
		this.groupDescripton = groupDescripton;
	}
	public String getParentGroupId() {
		return parentGroupId;
	}
	public void setParentGroupId(String parentGroupId) {
		this.parentGroupId = parentGroupId;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getOriginalRoleDescription() {
		return originalRoleDescription;
	}
	public void setOriginalRoleDescription(String originalRoleDescription) {
		this.originalRoleDescription = originalRoleDescription;
	}
}

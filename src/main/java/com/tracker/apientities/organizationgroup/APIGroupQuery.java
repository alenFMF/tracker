package com.tracker.apientities.organizationgroup;

// returns a list of groups which token user is member of.
public class APIGroupQuery {
	public String token;
	public String forUser; // only sys admin can list groups for other users.
	 					   // if null, it is meant for the token user
	public String forUserProvider;
	public String queryString;
}

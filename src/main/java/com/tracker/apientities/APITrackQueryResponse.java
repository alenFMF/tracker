package com.tracker.apientities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APITrackQueryResponse extends APIBaseResponse {
	public String deviceUuid;
	public String userId;
	public List<APITrackSample> samples;
	
	public APITrackQueryResponse() {}
	
	public APITrackQueryResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
}

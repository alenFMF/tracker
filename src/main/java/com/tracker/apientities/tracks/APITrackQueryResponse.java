package com.tracker.apientities.tracks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tracker.apientities.APIBaseResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APITrackQueryResponse extends APIBaseResponse {
	
	public List<APITrackDetail> tracks;
	
	public List<APITrackDetail> getTracks() {
		return tracks;
	}

	public void setTracks(List<APITrackDetail> tracks) {
		this.tracks = tracks;
	}

	public APITrackQueryResponse() {}
	
	public APITrackQueryResponse(String status, String errorMessage) {
		super(status, errorMessage);
	}	
}

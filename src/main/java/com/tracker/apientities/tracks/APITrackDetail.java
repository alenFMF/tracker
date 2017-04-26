package com.tracker.apientities.tracks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APITrackDetail {
	public String deviceUuid;
	public List<APITrackSample> samples;
}

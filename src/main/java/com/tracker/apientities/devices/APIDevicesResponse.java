package com.tracker.apientities.devices;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tracker.apientities.APIBaseResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIDevicesResponse extends APIBaseResponse {
	public List<String> devices;
}

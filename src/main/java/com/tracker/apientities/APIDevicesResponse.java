package com.tracker.apientities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIDevicesResponse extends APIBaseResponse {
	public List<String> devices;
}

package com.tracker.apientities;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIGPSLocation {
	public APICoords coords;	
	//public APIExtras extras;
	public boolean is_moving;
	public double odometer;
	public String uuid;
	public APIActivity activity;
	public APIBattery battery;
	public Date timestamp;
}

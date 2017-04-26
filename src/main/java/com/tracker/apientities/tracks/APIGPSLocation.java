package com.tracker.apientities.tracks;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIGPSLocation {
	public APICoords coords;	
	//public APIExtras extras;
	public Boolean is_moving;
	public Double odometer;
	public String uuid;
	public APIActivity activity;
	public APIBattery battery;
	public Date timestamp;
}

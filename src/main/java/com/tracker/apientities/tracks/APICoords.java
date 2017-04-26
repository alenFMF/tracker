package com.tracker.apientities.tracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APICoords {
	public double speed;
	public double longitude;
	public double latitude;
	public double accuracy;
	public Double altitude_accuracy;
	public Double altitude;
	public double heading;
}

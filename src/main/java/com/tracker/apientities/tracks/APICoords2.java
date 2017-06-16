package com.tracker.apientities.tracks;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APICoords2 {
	public double speed;
	public double lon;
	public double lat;
	public Double alt;
	public double head;
	public Date time;
}

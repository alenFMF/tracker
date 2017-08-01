package com.tracker.apientities.tracks;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APICoords2 {
	public Double speed;
	public Double lon;
	public Double lat;
	public Double alt;
	public Double head;
	public Date time;
	public Double accur;
	public Double batLev;
	public Boolean batChg;
}

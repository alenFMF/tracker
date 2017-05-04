package com.tracker.apientities.devices;

public class APIDevice {
	public String uuid;
	public String manufacturer;
	public String model;
	public String version;
	public String platform;
	
	public APIDevice() {}
	public APIDevice(String uuid, String manufacturer, String model, String version, String platform) {
		this.uuid = uuid;
		this.manufacturer = manufacturer;
		this.model = model;
		this.version = version;
		this.platform = platform;
	}
}

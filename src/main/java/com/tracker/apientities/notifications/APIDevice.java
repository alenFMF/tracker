package com.tracker.apientities.notifications;

import com.tracker.db.DeviceRecord;

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
	
	public APIDevice(DeviceRecord device) {
		this.uuid = device.getUuid();
		this.manufacturer = device.getManufacturer();
		this.model = device.getModel();
		this.version = device.getVersion();
		this.platform = device.getPlatform();
	}
}

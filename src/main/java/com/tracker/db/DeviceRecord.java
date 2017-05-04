package com.tracker.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
public class DeviceRecord extends BaseEntity {
	
	@Column(unique = true) 
	private String uuid;
	
	private String manufacturer;
	private String model;
	private String version;
	private String platform;
	
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public DeviceRecord() {	}
	
	public DeviceRecord(String uuid, String manufacturer, String model, String version, String platform) {
		this.uuid = uuid;
		this.manufacturer = manufacturer;
		this.model = model;
		this.version = version;
		this.platform = platform;
	}
}

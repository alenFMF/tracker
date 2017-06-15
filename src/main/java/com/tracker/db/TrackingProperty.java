package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class TrackingProperty extends BaseEntity {
	public String provider;
	public String key;
	public String dataType;
	public String dataValue;
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getType() {
		return dataType;
	}
	public void setType(String type) {
		this.dataType = type;
	}
	public String getValue() {
		return dataValue;
	}
	public void setValue(String value) {
		this.dataValue = value;
	}
	
	public TrackingProperty() {
		super();
	}
	
}

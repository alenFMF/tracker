package com.tracker.db;

import javax.persistence.Entity;

@Entity
public class TrackingProperty extends BaseEntity {
	public String provider;
	public String key;
	public String type;
	public String value;
	
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
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public TrackingProperty() {
		super();
	}
	
}

package com.tracker.apientities.user;

import com.tracker.db.TrackingProperty;

public class APIProperty {
	public String key;
	public String type;
	public String value;
	
	public APIProperty() {}
	public APIProperty(TrackingProperty prop) {
		this.key = prop.dataKey;
		this.type = prop.dataType;
		this.value = prop.dataValue;
	}
}

package com.tracker.types;

public enum PlatformType implements GoOptiTrackerEnum<PlatformType> {
	
	IOS,
	ANDROID,
	IPHONEOS;
	
	@Override
	public String getName() {
		if (this == IOS) return "iOS";
		else if (this == ANDROID) return "Android";
		else if (this == IPHONEOS) return "iPhone OS";
		else return "Unknown";
	}

}
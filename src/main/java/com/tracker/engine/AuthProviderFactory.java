package com.tracker.engine;

import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public class AuthProviderFactory {
	private HashMap<String, IAuthProvider> keyToProvider;
	
	public AuthProviderFactory() {
		keyToProvider = new HashMap<String, IAuthProvider>();
		keyToProvider.put("GOOPTI", new GoOptiAuthProvider());
	}
	
	public IAuthProvider getProvider(String key) {
		return keyToProvider.get(key);
	} 
}

package com.tracker.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AuthProviderFactory {
	private HashMap<String, IAuthProvider> keyToProvider;
	
	public AuthProviderFactory() {
		keyToProvider = new HashMap<String, IAuthProvider>();
		register(new GoOptiAuthProvider());
		// register other providers.
	}
	
	public IAuthProvider getProvider(String key) {
		return keyToProvider.get(key);
	} 
	
	public void register(IAuthProvider provider) {
		keyToProvider.put(provider.getKey(), provider);		
	}
	
	public List<String> listProviders() {
		List<String> lst = new ArrayList<String>(keyToProvider.keySet());
		Collections.sort(lst);
		return lst;
	}
}

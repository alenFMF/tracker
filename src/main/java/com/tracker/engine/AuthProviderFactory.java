package com.tracker.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthProviderFactory {
	private HashMap<String, IAuthProvider> keyToProvider;
	
	@Autowired
	private GoOptiAuthProvider gooptiProvider;
	
	
	@PostConstruct
	public void postConstuct() {
		keyToProvider = new HashMap<String, IAuthProvider>();
		register(gooptiProvider);
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

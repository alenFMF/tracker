package com.tracker.engine;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.tracker.apientities.goopti.APIGoOptiAuthentication;
import com.tracker.apientities.goopti.APIGoOptiAuthenticationResponse;

public class GoOptiAuthProvider implements IAuthProvider {
	
	private String service = "https://www.goopti.com/goopti-services/authentication/signin";
	@Override
	public AuthenticationObject authenticate(String username, String password) {
		// TODO Auto-generated method stub
		RestTemplate restTempl = new RestTemplate();
		APIGoOptiAuthentication request = new APIGoOptiAuthentication(username, password);
		try {
			APIGoOptiAuthenticationResponse res = restTempl.postForObject(service, request, APIGoOptiAuthenticationResponse.class);
			AuthenticationObject auth = new AuthenticationObject();
			auth.setToken(res.token);
			auth.setName(res.name + " " + res.surname);
			auth.setUsername(username);
			auth.setEmail(res.email);
			auth.setStatus("OK");
			auth.setErrorMessage("");
			return auth;
		} catch (RestClientException e) {
			AuthenticationObject auth = new AuthenticationObject();
			auth.setStatus("CLIENT_ERROR");
			auth.setErrorMessage(e.getMessage());
			return auth;
		} catch (Exception e) {
			AuthenticationObject auth = new AuthenticationObject();
			auth.setStatus("ERROR");
			auth.setErrorMessage(e.getMessage());	
			return auth;
		}	
	}

}

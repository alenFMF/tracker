package com.tracker.engine;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tracker.apientities.APITest1;
import com.tracker.apientities.APITest2;
import com.tracker.db.TestEntity;
import com.tracker.utils.SessionKeeper;

@Component
public class TestEngine {

	@Autowired
	private SessionFactory sessionFactory;

	public APITest2 handleService2(APITest1 req) {
		try (SessionKeeper sk = SessionKeeper.open(sessionFactory)) {
			TestEntity e = new TestEntity();
			
			e.setDeviceId(req.deviceId);
			e.setTimestamp(req.timestamp);
			e.setLatitude(req.latitude);
			e.setLatitude(req.longitude);
			sk.save(e);
			sk.commit();
			
			return new APITest2("SUCCESS");
		} catch (Throwable t) {
			return new APITest2("ERROR");
		}	
	}
	
	
}

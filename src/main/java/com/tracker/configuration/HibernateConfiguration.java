package com.tracker.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.tracker.db.ActivityRecord;
import com.tracker.db.AltitudeRecord;
import com.tracker.db.AppConfiguration;
import com.tracker.db.BatteryRecord;
import com.tracker.db.DeviceRecord;
import com.tracker.db.DriverAssignment;
import com.tracker.db.GPSRecord;
import com.tracker.db.MessageBody;
import com.tracker.db.EventMessage;
import com.tracker.db.NotificationRegistration;
import com.tracker.db.OrganizationGroup;
import com.tracker.db.TaskGoal;
import com.tracker.db.TestEntity;
import com.tracker.db.TrackingUser;
import com.tracker.db.TravelOrder;
import com.tracker.db.UserGroupAssignment;
import com.tracker.db.Vehicle;
import com.tracker.db.VehicleGroupAssignment;


@Configuration
public class HibernateConfiguration {

	@Value("#{hibernateProperties}")
	private Properties hibernateProperties;

	@Bean
	public LocalSessionFactoryBean sessionFactoryBean() {
		LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
		bean.setAnnotatedClasses(new Class<?>[] { 
			TestEntity.class, 
			GPSRecord.class, 
			ActivityRecord.class, 
			AltitudeRecord.class,
			BatteryRecord.class, 
			DeviceRecord.class, 
			TrackingUser.class,
			Vehicle.class,
			OrganizationGroup.class,
			UserGroupAssignment.class,
			VehicleGroupAssignment.class,
			AppConfiguration.class,
			NotificationRegistration.class,
			EventMessage.class,
			MessageBody.class,
			TaskGoal.class,
			TravelOrder.class,
			DriverAssignment.class
			});
		bean.setHibernateProperties(hibernateProperties);
		//bean.setEntityInterceptor(new AuditInterceptor());
		return bean;
	}
	
	@Bean
	public HibernateTransactionManager transactionManagerBean() {
		// logger.info("This is TransactionManager Get.");
		return new HibernateTransactionManager(sessionFactoryBean().getObject());
	}
	
}
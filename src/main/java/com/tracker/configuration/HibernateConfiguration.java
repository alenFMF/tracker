package com.tracker.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.tracker.db.ActivityRecord;
import com.tracker.db.AltitudeRecord;
import com.tracker.db.BatteryRecord;
import com.tracker.db.DeviceRecord;
import com.tracker.db.GPSRecord;
import com.tracker.db.TestEntity;
import com.tracker.db.User;


@Configuration
public class HibernateConfiguration {

	@Value("#{hibernateProperties}")
	private Properties hibernateProperties;

	@Bean
	public LocalSessionFactoryBean sessionFactoryBean() {
		LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
		bean.setAnnotatedClasses(new Class<?>[] { TestEntity.class, GPSRecord.class, ActivityRecord.class, AltitudeRecord.class,
			BatteryRecord.class, DeviceRecord.class, User.class});
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
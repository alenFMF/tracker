package com.tracker.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tracker.engine.S3Service;

//@Configuration
public class S3Configuration {
//	@Value("#{generalProperties.S3ConfigPath}")
//	private String S3ConfigPath;
//	
//	@Bean
//	public S3Service s3Service() {
//		 InputStream input = null;
//		 try {
//				input = new FileInputStream(S3ConfigPath);
//				Properties prop2 = new Properties();
//				// load a properties file
//				prop2.load(input);
//				S3Service service = new S3Service(prop2);
//				return service;										
//		 } catch (Exception ex) {
//			 System.err.println("ERROR: Problem with S3 configuration.");
//			 ex.printStackTrace();
//		 } finally {
//				if (input != null) {
//					try {
//						input.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//		 }				 
//		 return null;		
//	}

}

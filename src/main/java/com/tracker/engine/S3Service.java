package com.tracker.engine;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

//@Component
public class S3Service {
	
	@Value("#{generalProperties.S3ConfigPath}")
	private String S3ConfigPath;
	
	private String S3UserName;
	private String S3AccessKeyID;
	private String S3SecretAccessKey;
	private String S3Bucket;
	private String S3Folder;
	private String S3WebLinkRoot;
	private String S3Region;
	private AmazonS3 s3Client;
	private SecureRandom generator;

	@PostConstruct
	public void postConstruct() {
//	public S3Service(Properties properties) throws NoSuchAlgorithmException {
		 InputStream input = null;
		 try {
				input = new FileInputStream(S3ConfigPath);
				Properties properties = new Properties();
				// load a properties file
				properties.load(input);				
				this.S3UserName = properties.getProperty("S3UserName");
				this.S3AccessKeyID = properties.getProperty("S3AccessKeyID");
				this.S3SecretAccessKey = properties.getProperty("S3SecretAccessKey");
				this.S3Bucket = properties.getProperty("S3Bucket");
				this.S3Folder = properties.getProperty("S3Folder");	
				this.S3WebLinkRoot = properties.getProperty("S3WebLinkRoot");	
				this.S3Region = properties.getProperty("S3Region");	
				
				this.generator = SecureRandom.getInstance("SHA1PRNG");
				BasicAWSCredentials creds = new BasicAWSCredentials(S3AccessKeyID, S3SecretAccessKey); 
				s3Client = AmazonS3ClientBuilder.standard().withRegion(S3Region).withCredentials(new AWSStaticCredentialsProvider(creds)).build();		
		 } catch (Exception ex) {
			 System.err.println("ERROR: Problem with S3 configuration.");
			 ex.printStackTrace();
		 } finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		 }				 
	} 	
	
	public void putTextFile(String text, String keyName) {
		try {		       
	        byte[] fileContentBytes = text.getBytes(StandardCharsets.UTF_8);
	        InputStream fileInputStream = new ByteArrayInputStream(fileContentBytes);
	        ObjectMetadata metadata = new ObjectMetadata();
	        metadata.setContentType("text/plain; charset=utf-8");
	        metadata.setContentLength(fileContentBytes.length);
	        PutObjectRequest putObjectRequest = new PutObjectRequest(
	                S3Bucket, S3Folder + '/' + keyName, fileInputStream, metadata);
	        s3Client.putObject(putObjectRequest);
		} catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
		}		
	} 
	
	public String generateName() {		
		int tokenLength = 24;
		String token = new BigInteger(tokenLength*4, this.generator).toString(16);
		return String.format("%1$" + tokenLength + "s", token).replace(' ', '0');
	}
	
	
	public String putRawText(String text) {
		String name = generateName();
		name = name + ".txt";
		this.putTextFile(text, name);
		return name;
	}

	public String getS3WebLink() {
		return S3WebLinkRoot + S3Folder + "/";
	}

	
	
}

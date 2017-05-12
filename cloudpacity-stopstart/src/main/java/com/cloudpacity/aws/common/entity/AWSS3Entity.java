package com.cloudpacity.aws.common.entity;
/**
 * 
 * Copyright 2015 Cloudpacity
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Scott Wheeler
 *
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.Validate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.cloudpacity.aws.common.error.CPRuntimeException;
import com.cloudpacity.aws.common.util.CPLogger;

public class AWSS3Entity {
	
	protected CPLogger logger = null;	
	protected AmazonS3 s3Client = null; 

	public AWSS3Entity (AWSCredentials awsCredentials, String regionName, CPLogger logger) {
		
		Validate.notNull(awsCredentials, "The AWS credentials supplied were null!");
		Validate.notNull(logger, "The logger supplied was null!");
		Validate.notEmpty(regionName, "The AWS region name provided is empty!");
		
		this.logger =  logger;
        this.s3Client = AmazonS3ClientBuilder.standard()
				.withRegion(regionName)
				.build();
	}
	
	/**
	 * 
	 * @param bucketName
	 * @param objectKey
	 * @param content
	 * @param fileName
	 */
	public void saveObject(String bucketName, String objectKey, String content, String fileName) {
		
		Validate.notEmpty(bucketName, "Error, the bucketName is empty!");
		Validate.notEmpty(objectKey, "Error, the objectKey is empty!");
		Validate.notEmpty(content, "Error, the content is empty!");
		Validate.notEmpty(fileName, "Error, the fileName is empty!");

			InputStream inputStream =  new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			ObjectMetadata metaData=new ObjectMetadata();
			metaData.setContentDisposition("attachment; filename=\"" + fileName);
			metaData.setContentType("text/plain");			
			metaData.setContentLength(content.length());
			
			this.s3Client.putObject(bucketName, objectKey, inputStream, metaData);
   } 	
	/**
	 * 
	 * @param bucketName
	 * @param objectKey
	 * @return
	 */
	public String getObjectAsString(String bucketName, String objectKey) {
		
		try {
			String result = "";
			S3Object s3Object = this.s3Client.getObject(bucketName, objectKey);
			
			 result = getStringFromStream( s3Object.getObjectContent());
		
			return result;
		}
	   	catch (IOException e){
	   		throw new CPRuntimeException("Error getting a string from an S3 object! " + e.getStackTrace());
	   } 	
	}
	
	/**
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	protected String getStringFromStream(InputStream inputStream) throws IOException {
	
			String outputString = "";
			
			if (inputStream == null) { return outputString; }
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			while (true) {
				String line = reader.readLine();
					if (line == null) {
						break;
					}
					else {
						outputString = outputString + line;
					}
			}
			
			return outputString;
	}
}

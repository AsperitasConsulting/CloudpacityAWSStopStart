package com.cloudpacity.aws.common.entity;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.cloudpacity.aws.common.CPCommonEnv;
import com.cloudpacity.aws.common.util.CPLogger;
import java.util.List;
import org.apache.commons.lang3.Validate;

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
public class AWSSQSEntity extends AWSObjectEntity {

	protected AmazonSQS sqsClient = null; 
	
	public AWSSQSEntity (AWSCredentials awsCredentials, String regionName, CPLogger logger,CPCommonEnv env) {
		
		super( logger,env);
		Validate.notNull(awsCredentials, "The AWS credentials supplied were null!");
		Validate.notEmpty(regionName, "The AWS region name provided is empty!");
		
		this.logger =  logger;
        this.sqsClient = AmazonSQSClientBuilder.standard()
				.withRegion(regionName)
				.build();
	}
	
	/**
	 * 
	 * @param queueName
	 * @param message
	 * @return
	 */
	public SendMessageResult postMessage(String queueName, String message) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        String myQueueUrl = this.sqsClient.createQueue(createQueueRequest).getQueueUrl();
        
       return this.sqsClient.sendMessage(new SendMessageRequest(myQueueUrl, message));
	}
	
	/**
	 * 
	 * @param queueName
	 * @param message
	 * @return
	 */
	public List<Message> getMessage(String queueName, String message) {
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        String myQueueUrl =  this.sqsClient.createQueue(createQueueRequest).getQueueUrl();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        return this.sqsClient.receiveMessage(receiveMessageRequest).getMessages();
	}
}

package com.cloudpacity.aws.common.entity;

import org.apache.commons.lang3.Validate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.cloudpacity.aws.common.CPCommonEnv;
import com.cloudpacity.aws.common.util.CPLogger;
import com.cloudpacity.aws.stopstart.CPStopStartEnv;

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
public class AWSSNSEntity  extends AWSObjectEntity {

	protected AmazonSNS snsClient = null; 
	
    public AWSSNSEntity(AWSCredentials awsCredentials, CPLogger logger,CPCommonEnv env)
    {
        super(logger,env);
        Validate.notNull(awsCredentials, "The AWS credentials supplied were null!", new Object[0]);
        Validate.notEmpty(CPStopStartEnv.getRegionName(), "The AWS region name provided is empty!", new Object[0]);
        this.logger = logger;
        snsClient = AmazonSNSClientBuilder.standard()
				.withRegion(CPStopStartEnv.getRegionName())
				.build();
    }
    
//	public PublishResult postMessage(String topicArn, String message) {
//		
//		PublishRequest publishRequest = new PublishRequest(topicArn, message);
//		return snsClient.publish(publishRequest);
//	}
	
	public static PublishResult postMessage(String message) {
		
		AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
													.withRegion(CPStopStartEnv.getRegionName())
													.build();
		PublishRequest publishRequest = new PublishRequest(CPStopStartEnv.getSNSARN(), message);
		return snsClient.publish(publishRequest);		
	}
}

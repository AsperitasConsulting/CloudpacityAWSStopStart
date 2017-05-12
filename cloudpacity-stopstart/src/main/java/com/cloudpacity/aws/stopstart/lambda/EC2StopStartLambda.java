package com.cloudpacity.aws.stopstart.lambda;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.cloudpacity.aws.common.dao.EC2StopStartDAO;
import com.cloudpacity.aws.common.util.CPLogger;
import com.cloudpacity.aws.stopstart.CPStopStartEnv;
import com.cloudpacity.aws.stopstart.pojo.StartStopRequest;
import com.cloudpacity.aws.stopstart.service.EC2StopStart;
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
public class EC2StopStartLambda implements RequestHandler<StartStopRequest, String> {

    protected AWSCredentials awsCredentials;
    protected AmazonS3 s3Client;
    protected CPLogger logger;
    public static final String NO_STATE_CHANGE = "no state change";
    
    public EC2StopStartLambda()
    {
        awsCredentials = (new EnvironmentVariableCredentialsProvider()).getCredentials();
    }

    public String handleRequest(StartStopRequest request, Context context)
    {
	   	 this.logger = new CPLogger( context.getLogger());
	   	 
	   	try {
	   		logger.log("Begin EC2 StartStop");
	   		
	   		StartStopRequest stopStartReq = populateStopStartRequest(request,context);
	   		logger.log(stopStartReq.toString());
		    EC2StopStart ec2StartStop = new EC2StopStart(this.logger,this.awsCredentials);
		    
	   		return ec2StartStop.invoke( stopStartReq);
	   	}
	   	catch (Throwable e){
	   		logger.log("Exception in EC2Service.startStop!");
	   		logger.log(logger.getDebugMessages());
	   		logger.log(e.getMessage());
	   		logger.log(ExceptionUtils.getStackTrace(e));
	   		return e.getMessage();
	   }
    }

    public String handleDynamoRequest(DynamodbEvent dynamoEvent, Context context)
    {
	   	 this.logger = new CPLogger( context.getLogger());  	 
	     StreamRecord streamRecord = null;
	     StartStopRequest request = new StartStopRequest();
	    		 
	   	try {
	   		logger.log("Begin handleDynamoRequest");

	    	for (DynamodbStreamRecord record: dynamoEvent.getRecords()) {
	    		if(CPStopStartEnv.DYNAMODB_INSERT_EVENT.equalsIgnoreCase( record.getEventName())){
		    		streamRecord = record.getDynamodb();
		    		if (streamRecord != null) {
		    			request = marshallStartStopReq(streamRecord);
		    			break;
		    		}
	    		}
	    	}
	    	
	   		StartStopRequest startStopReq = populateStopStartRequest(request,context);
		    EC2StopStart ec2StartStop = new EC2StopStart(this.logger,this.awsCredentials);
		     
	   		return ec2StartStop.invoke(startStopReq);
	   	}
	   	catch (Throwable e){
	   		logger.log("Exception in EC2StartStopLambda.handleDynamoRequest!");
	   		logger.log(logger.getDebugMessages());
	   		logger.log(e.getMessage());
	   		logger.log(ExceptionUtils.getStackTrace(e));
	   		return e.getMessage();
	   } 
    }

    private StartStopRequest marshallStartStopReq(StreamRecord streamRecord)
    {
        validateDynamoEvent(streamRecord);
    	Map<String, AttributeValue> columnMap = streamRecord.getNewImage();
        StartStopRequest startStopReq = new StartStopRequest();
        startStopReq.setOriginatingLambdaRequestId(getDynamoColStringValue(columnMap, EC2StopStartDAO.ORIG_REQ_ID_COL_NAME));
        startStopReq.setCurrentLambdaRequestId(getDynamoColStringValue(columnMap, EC2StopStartDAO.CURR_REQ_ID_COL_NAME));
        startStopReq.setRequestStartTimeString(getDynamoColStringValue(columnMap, EC2StopStartDAO.REQ_START_TIME_COL_NAME));
        startStopReq.setFilter1TagName(getDynamoColStringValue(columnMap, EC2StopStartDAO.FILTER1_TAG_NAME_COL_NAME));
        startStopReq.setFilter1TagValue(getDynamoColStringValue(columnMap, EC2StopStartDAO.FILTER1_TAG_VALUE_COL_NAME));
        startStopReq.setIterationString(getDynamoColStringValue(columnMap, EC2StopStartDAO.ITERATION_COL_NAME));
        startStopReq.setActionPauseSec(getDynamoColNumericValue(columnMap, EC2StopStartDAO.ACTION_PAUSE_SEC_NAME));
        startStopReq.setMaxRecursiveCalls(getDynamoColNumericValue(columnMap, EC2StopStartDAO.MAX_RECURSIVE_CALLS_NAME));		
        startStopReq.setMaxRunMinutes(getDynamoColNumericValue(columnMap, EC2StopStartDAO.MAX_RUN_MIN_NAME));
        startStopReq.setInstanceDependencyTag(getDynamoColStringValue(columnMap, EC2StopStartDAO.INSTANCE_DEPEDEND_TAG_NAME));
        startStopReq.setAvailableDayTag(getDynamoColStringValue(columnMap, EC2StopStartDAO.AVAILABLE_DAY_TAG_NAME));
        startStopReq.setAvailableBeginTimeTag(getDynamoColStringValue(columnMap, EC2StopStartDAO.AVAILABLE_BEGIN_TIME_TAG_NAME));
        startStopReq.setAvailableEndTimeTag(getDynamoColStringValue(columnMap,  EC2StopStartDAO.AVAILABLE_END_TIME_TAG_NAME));
        return startStopReq;
    }

    private void validateDynamoEvent(StreamRecord record)
    {
        Validate.notNull(record, "The Dynamodb record was null!", new Object[0]);
        Validate.notNull(record.getKeys(), "The key map in the dynamodb record was null!", new Object[0]);
        Validate.notNull(record.getNewImage(), "The record new image was null!", new Object[0]);
    }

    private String getDynamoColStringValue(Map<String, AttributeValue> columnMap, String colName) {
    	
        AttributeValue value = (AttributeValue)columnMap.get(colName);
        if(value != null)
            return value.getS();
        else
            return null;
    }

    private Integer getDynamoColNumericValue(Map<String, AttributeValue> columnMap, String colName) {
    	
		AttributeValue value = columnMap.get(colName);
		if (value != null) {
			String stringValue = value.getS();
			
			try {
				return new Integer(stringValue);
			}
			catch(NumberFormatException nfe) {
				return null;
			}
		}
		return null;
    }

    private StartStopRequest populateStopStartRequest(StartStopRequest request, Context context)
    {
        StartStopRequest stopStartRequest = new StartStopRequest();
        ZoneId zoneCDT = ZoneId.of(CPStopStartEnv.getDefaultTimeZone());
        ZonedDateTime iterationStartDateTime = ZonedDateTime.now(zoneCDT);
        
        // Current Lambda Req Id
        stopStartRequest.setCurrentLambdaRequestId(context.getAwsRequestId());
        
        // Request Start Time
        if(StringUtils.isEmpty(request.getRequestStartTimeString()))
            stopStartRequest.setRequestStartTime(iterationStartDateTime);
        else
            stopStartRequest.setRequestStartTime(request.getRequestStartTime());
        
        // Originating Lambda Req Id
        if(StringUtils.isEmpty(request.getOriginatingLambdaRequestId()))
            stopStartRequest.setOriginatingLambdaRequestId(context.getAwsRequestId());
        else
            stopStartRequest.setOriginatingLambdaRequestId(request.getOriginatingLambdaRequestId());
        
        // Iteration  
        if(request.getIteration() == null || request.getIteration().intValue() < 1)
            stopStartRequest.setIteration(Integer.valueOf(1));
        else
            stopStartRequest.setIteration(request.getIteration());
        
        // Pause Between Actions (secs)
        if(request.getActionPauseSec() == null || request.getActionPauseSec().intValue() < 10)
            stopStartRequest.setActionPauseSec(Integer.valueOf(CPStopStartEnv.getActionPauseSec()));
        else
            stopStartRequest.setActionPauseSec(request.getActionPauseSec());
        
        // Max number of recursive Calls
        if(request.getMaxRecursiveCalls() == null || request.getMaxRecursiveCalls().intValue() < 2)
            stopStartRequest.setMaxRecursiveCalls(Integer.valueOf(CPStopStartEnv.getMaxRecursiveCalls()));
        else
            stopStartRequest.setMaxRecursiveCalls(request.getMaxRecursiveCalls());
        
        // Max run time
        if(request.getMaxRunMinutes() == null || request.getMaxRunMinutes().intValue() < 5)
            stopStartRequest.setMaxRunMinutes(Integer.valueOf(CPStopStartEnv.getMaxRunMinutes()));
        else
            stopStartRequest.setMaxRunMinutes(request.getMaxRunMinutes());
        
        // Available Day tag
        if(StringUtils.isEmpty(request.getAvailableDayTag()))
            stopStartRequest.setAvailableDayTag(CPStopStartEnv.getAvailableDayTag());
        else
            stopStartRequest.setAvailableDayTag(request.getAvailableDayTag());
        
        // Available begin time tag
        if(StringUtils.isEmpty(request.getAvailableBeginTimeTag()))
            stopStartRequest.setAvailableBeginTimeTag(CPStopStartEnv.getAvailableBeginTimeTag());
        else
            stopStartRequest.setAvailableBeginTimeTag(request.getAvailableBeginTimeTag());
        
        // Available End Time tag 
        if(StringUtils.isEmpty(request.getAvailableEndTimeTag()))
            stopStartRequest.setAvailableEndTimeTag(CPStopStartEnv.getAvailableEndTimeTag());
        else
            stopStartRequest.setAvailableEndTimeTag(request.getAvailableEndTimeTag());
        
        // Instance Dependency tag       
        if(StringUtils.isEmpty(request.getInstanceDependencyTag()))
            stopStartRequest.setInstanceDependencyTag(CPStopStartEnv.getInstanceDependencyTag());
        else
            stopStartRequest.setInstanceDependencyTag(request.getInstanceDependencyTag());
        
        // Filter 1  tag Name
        if(StringUtils.isEmpty(request.getFilter1TagName()))
            stopStartRequest.setFilter1EnvVarName(CPStopStartEnv.getFilter1TagName());
        else
            stopStartRequest.setFilter1TagValue(request.getFilter1TagValue());
        
        // Filter 1  tag Value
        if(StringUtils.isEmpty(request.getFilter1TagValue()))
            stopStartRequest.setFilter1EnvVarValue(CPStopStartEnv.getFilter1TagValue());
        else
            stopStartRequest.setFilter1TagValue(request.getFilter1EnvVarValue());
        
        // Filter 2  tag Name       
        if(StringUtils.isEmpty(request.getFilter2TagName()))
            stopStartRequest.setFilter1TagName(CPStopStartEnv.getFilter2TagName());
        else
            stopStartRequest.setFilter1TagName(request.getFilter1TagName());
        
        // Filter 2  tag Value        
        if(StringUtils.isEmpty(request.getFilter1TagValue()))
            stopStartRequest.setFilter1TagValue(CPStopStartEnv.getFilter2TagValue());
        else
            stopStartRequest.setFilter1TagValue(request.getFilter1TagValue());
        
        return stopStartRequest;
    }


}

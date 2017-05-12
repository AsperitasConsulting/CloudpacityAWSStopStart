package com.cloudpacity.aws.common.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.cloudpacity.aws.stopstart.CPStopStartEnv;
import com.cloudpacity.aws.stopstart.pojo.EC2StopStartDO;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
public class EC2StopStartDAO
{

    private AmazonDynamoDB dynamoDBClient;
    
    private static final String TABLE_NAME = "CloudpacityEC2StopStart";
    public static final String PURGE_TIMESTAMP_COL_NAME = "purgeTimestamp";
    public static final String ID_COL_NAME = "Id";
    public static final String ORIG_REQ_ID_COL_NAME = "OrigReqId";
    public static final String CURR_REQ_ID_COL_NAME = "CurrReqId";
    public static final String ITERATION_COL_NAME = "Iteration";
    public static final String REQ_START_TIME_COL_NAME = "RequestStartTime";
    public static final String FILTER1_TAG_NAME_COL_NAME = "Filter1TagName";
    public static final String FILTER1_TAG_VALUE_COL_NAME = "Filter1TagValue";
    public static final String FILTER2_TAG_NAME_COL_NAME = "Filter2TagName";
    public static final String FILTER2_TAG_VALUE_COL_NAME = "Filter2TagValue";
    public static final String FILTER3_TAG_NAME_COL_NAME = "Filter3TagName";
    public static final String FILTER3_TAG_VALUE_COL_NAME = "Filter3TagValue";
    public static final String FILTER4_TAG_NAME_COL_NAME = "Filter4TagName";
    public static final String FILTER4_TAG_VALUE_COL_NAME = "Filter4TagValue";
    public static final String FILTER5_TAG_NAME_COL_NAME = "Filter5TagName";
    public static final String FILTER5_TAG_VALUE_COL_NAME = "Filter5TagValue";
    public static final String FILTER6_TAG_NAME_COL_NAME = "Filter6TagName";
    public static final String FILTER6_TAG_VALUE_COL_NAME = "Filter6TagValue";
    public static final String ACTION_PAUSE_SEC_NAME = "actionPauseSec";
    public static final String MAX_RECURSIVE_CALLS_NAME = "maxRecursiveCalls";
    public static final String MAX_RUN_MIN_NAME = "maxRunMinutes";
    public static final String INSTANCE_DEPEDEND_TAG_NAME = "instanceDependencyTag";
    public static final String AVAILABLE_DAY_TAG_NAME = "availableDayTag";
    public static final String AVAILABLE_BEGIN_TIME_TAG_NAME = "availableBeginTimeTag";
    public static final String AVAILABLE_END_TIME_TAG_NAME = "availableEndTimeTag";
	
    public EC2StopStartDAO()
    {
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("key", new AttributeValue("value"));
		
		this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(CPStopStartEnv.getRegion())
                .build();
    }

    public GetItemResult getItemByKey(String keyValue)
    {
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("key", new AttributeValue("value"));

		return dynamoDBClient.getItem(TABLE_NAME, key);
    }

    public PutItemResult putItem(EC2StopStartDO ec2StopStartDO)
    {
        Validate.notEmpty(ec2StopStartDO.getId(), "The EC2StopStartDO Id is empty!", new Object[0]);
        Validate.notEmpty(ec2StopStartDO.getCurrentLambdaRequestId(), "The EC2StopStartDO current lambda req id is empty!", new Object[0]);
        Validate.notEmpty(ec2StopStartDO.getOriginatingLambdaRequestId(), "The EC2StopStartDO originating Lambda Req id is empty!", new Object[0]);
        Validate.notEmpty(ec2StopStartDO.getIterationString(), "The EC2StopStartDO iteration is empty!", new Object[0]);
        
        PutItemRequest putItemRequest = new PutItemRequest();
        Map<String, AttributeValue> values = new HashMap<String, AttributeValue>();
        putValues(values, ID_COL_NAME, ec2StopStartDO.getId());
        putValues(values, CURR_REQ_ID_COL_NAME, ec2StopStartDO.getCurrentLambdaRequestId());
        putValues(values, ORIG_REQ_ID_COL_NAME, ec2StopStartDO.getOriginatingLambdaRequestId());
        putValues(values, REQ_START_TIME_COL_NAME, ec2StopStartDO.getRequestStartTimeString());
        putValues(values, ITERATION_COL_NAME, ec2StopStartDO.getIterationString());
        putValues(values, ACTION_PAUSE_SEC_NAME, ec2StopStartDO.getActionPauseSecString());
        putValues(values, MAX_RECURSIVE_CALLS_NAME, ec2StopStartDO.getMaxRecursiveCallsString());
        putValues(values, MAX_RUN_MIN_NAME, ec2StopStartDO.getMaxRunMinString());
        putValues(values, INSTANCE_DEPEDEND_TAG_NAME, ec2StopStartDO.getInstanceDependencyTag());
        putValues(values, AVAILABLE_DAY_TAG_NAME, ec2StopStartDO.getAvailableDayTag());
        putValues(values, AVAILABLE_BEGIN_TIME_TAG_NAME, ec2StopStartDO.getAvailableBeginTimeTag());
        putValues(values, AVAILABLE_END_TIME_TAG_NAME, ec2StopStartDO.getAvailableEndTimeTag());
        if(!StringUtils.isEmpty(ec2StopStartDO.getFilter1TagName()))
            putValues(values, FILTER1_TAG_NAME_COL_NAME, ec2StopStartDO.getFilter1TagName());
        if(!StringUtils.isEmpty(ec2StopStartDO.getFilter1TagValue()))
            putValues(values, FILTER1_TAG_VALUE_COL_NAME, ec2StopStartDO.getFilter1TagValue());
        putItemRequest.setItem(values);
        putItemRequest.setTableName(TABLE_NAME);
        putValues(values, PURGE_TIMESTAMP_COL_NAME, getPurgeTimestampString());
        
        return dynamoDBClient.putItem(putItemRequest);
    }

    private void putValues(Map<String, AttributeValue> values, String name, String value)
    {
        Validate.notEmpty(name, "The column name given is empty!", new Object[0]);
        if(!StringUtils.isEmpty(value))
            values.put(name, new AttributeValue(value));
    }

    private String getPurgeTimestampString() {
    	//                                                                                     mill  sec  min  hour
    	long purgeTimestamp = System.currentTimeMillis() + (CPStopStartEnv.getDatabaseRetentionDays() * 1000 * 60 * 60 * 24);
    	return purgeTimestamp + "";
    }
}

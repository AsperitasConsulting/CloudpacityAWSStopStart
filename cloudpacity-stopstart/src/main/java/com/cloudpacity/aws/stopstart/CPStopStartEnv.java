package com.cloudpacity.aws.stopstart;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.regions.Regions;
import com.cloudpacity.aws.common.CPCommonEnv;
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
public class CPStopStartEnv extends CPCommonEnv
{

    public static final String DEFAULT_TIME_ZONE = "America/Chicago";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ENV_VAR_SNS_ARN = "SnsArn";
    public static final String DYNAMODB_INSERT_EVENT = "INSERT";
    public static final String ENV_VAR_ACTION_PAUSE_SECS = "PauseSecs";
    public static final String ENV_VAR_MAX_RECURSIVE_CALLS = "MaxRecursiveCalls";
    public static final String ENV_VAR_MAX_RUN_MINUTES = "MaxRunMinutes";
    public static final String ENV_VAR_TIME_ZONE = "TimeZone";
    public static final String ENV_VAR_FILTER1_TAG_NAME = "Filter1TagName";
    public static final String ENV_VAR_FILTER1_TAG_VALUE = "Filter1TagValue";
    public static final String ENV_VAR_FILTER2_TAG_NAME = "Filter2TagName";
    public static final String ENV_VAR_FILTER2_TAG_VALUE = "Filter2TagValue";
    public static final String ENV_VAR_FILTER3_TAG_NAME = "Filter3TagName";
    public static final String ENV_VAR_FILTER3_TAG_VALUE = "Filter3TagValue";
    public static final String ENV_VAR_FILTER4_TAG_NAME = "Filter4TagName";
    public static final String ENV_VAR_FILTER4_TAG_VALUE = "Filter4TagValue";
    public static final String ENV_VAR_OVERRIDE_AVAILABLE_DAY_TAG = "AvailableDayTag";
    public static final String ENV_VAR_OVERRIDE_NAME_TAG = "NameTag";
    public static final String ENV_VAR_OVERRIDE_AVAILABLE_BEGIN_TIME_TAG = "AvailableBeginTimeTag";
    public static final String ENV_VAR_OVERRIDE_AVAILABLE_END_TIME_TAG = "AvailableEndTimeTag";
    public static final String ENV_VAR_OVERRIDE_INSTANCE_DEPENDENCY_TAG = "InstanceDepdenciesTag";
    public static final String ENV_VAR_OVERRIDE_TIMESTAMP_TAG = "TimestampTag";
    public static final String ENV_VAR_OVERRIDE_INSTANCE_ID_TAG = "InstanceIdTag";
    public static final String ENV_VAR_OVERRIDE_DEVICE_TAG = "DeviceTag";
    public static final String ENV_VAR_OVERRIDE_IMAGE_ID_TAG = "ImageIdTag";
    public static final String ENV_VAR_OVERRIDE_ENABLE_BACKUP_TAG = "EnableBackupTag";
    public static final String ENV_VAR_OVERRIDE_DB_RETENTION_DAYS = "DBRetentionDays";
    public static final String DEFAULT_NAME_TAG = "Name";
    public static final String DEFAULT_AVAILABLE_DAY_TAG = "AvailableDay";
    public static final String DEFAULT_AVAILABLE_BEGIN_TIME_TAG = "AvailableBeginTime";
    public static final String DEFAULT_AVAILABLE_END_TIME_TAG = "AvailableEndTime";
    public static final String DEFAULT_INSTANCE_DEPENDENCY_TAG = "InstanceDependencies";
    public static final String DEFAULT_CREATION_TIMESTAMP_TAG = "CreationTimestamp";
    public static final String DEFAULT_INSTANCE_TAGS_TO_INCLUDE_TAG = "InstanceTagsToInclude";
    public static final String DEFAULT_INSTANCE_ID_TAG = "InstanceId";
    public static final String DEFAULT_DEVICE_TAG = "Device";
    public static final String DEFAULT_IMAGE_ID_TAG = "ImageId";
    public static final String DEFAULT_ENABLE_BACKUP_TAG = "EnableBackup";
    public static final String AVAILABLE_DAY_TAG_ALL_DAYS_CONST = "ALL";
    public static final String AVAILABLE_DAY_TAG_WEEKDAYS_CONST = "WEEKDAYS";
    public static final String AVAILABLE_DAY_TAG_WEEKEND_CONST = "WEEKEND";
    public static final int DEFAULT_DEBUG_QUEUE_SIZE = 20;

    
    public static String getSNSARN()
    {
        String snsARN =  System.getenv(ENV_VAR_SNS_ARN);
        if(StringUtils.isEmpty(snsARN))
            return "";
        else
            return snsARN;
    }

    public static int getActionPauseSec()
    {
		String actionPauseSecString = System.getenv(ENV_VAR_ACTION_PAUSE_SECS);
		if (StringUtils.isEmpty(actionPauseSecString)) {
			return DEFAULT_ACTION_PAUSE_SECS;
		}
		try {
			return new Integer(actionPauseSecString);
		}
		catch (NumberFormatException nfe) {
			return DEFAULT_ACTION_PAUSE_SECS;
		}
    }

    public static int getMaxRecursiveCalls()
    {
		String maxRecursiveCallsString = System.getenv(ENV_VAR_MAX_RECURSIVE_CALLS);
		if (StringUtils.isEmpty(maxRecursiveCallsString)) {
			return DEFAULT_MAX_RECURSIVE_CALLS;
		}
		try {
			return new Integer(maxRecursiveCallsString);
		}
		catch (NumberFormatException nfe) {
			return DEFAULT_MAX_RECURSIVE_CALLS;
		}
    }

    public static int getMaxRunMinutes()
    {
		String maxRunMinutesString = System.getenv(ENV_VAR_MAX_RUN_MINUTES);
		if (StringUtils.isEmpty(maxRunMinutesString)) {
			return DEFAULT_MAX_RUN_MINUTES;
		}
		try {
			return new Integer(maxRunMinutesString);
		}
		catch (NumberFormatException nfe) {
			return DEFAULT_MAX_RUN_MINUTES;
		}
    }

    public static String getDefaultTimeZone()
    {
        String defaultTimeZone = System.getenv(ENV_VAR_TIME_ZONE);
        if(StringUtils.isEmpty(defaultTimeZone))
            return DEFAULT_TIME_ZONE;
        else
            return defaultTimeZone;
    }

    public static String getEnvVar(String envTagName)
    {
        return System.getenv(envTagName);
    }


    public static String getAvailableDayTag()
    {
        String availableDayTag = System.getenv(ENV_VAR_OVERRIDE_AVAILABLE_DAY_TAG);
        if(StringUtils.isEmpty(availableDayTag))
            return DEFAULT_AVAILABLE_DAY_TAG;
        else
            return availableDayTag;
    }

    public static String getAvailableBeginTimeTag()
    {
        String availableBeginTimeTag = System.getenv(ENV_VAR_OVERRIDE_AVAILABLE_BEGIN_TIME_TAG);
        if(StringUtils.isEmpty(availableBeginTimeTag))
            return DEFAULT_AVAILABLE_BEGIN_TIME_TAG;
        else
            return availableBeginTimeTag;
    }

    public static String getAvailableEndTimeTag()
    {
        String availableEndTimeTag = System.getenv(ENV_VAR_OVERRIDE_AVAILABLE_END_TIME_TAG);
        if(StringUtils.isEmpty(availableEndTimeTag))
            return DEFAULT_AVAILABLE_END_TIME_TAG;
        else
            return availableEndTimeTag;
    }

    public static String getInstanceDependencyTag()
    {
        String instanceDependencyTag = System.getenv(DEFAULT_INSTANCE_DEPENDENCY_TAG);
        if(StringUtils.isEmpty(instanceDependencyTag))
            return DEFAULT_INSTANCE_DEPENDENCY_TAG;
        else
            return instanceDependencyTag;
    }

    public static String getCreationTimestampTag()
    {
        String timestampTag = System.getenv(ENV_VAR_OVERRIDE_TIMESTAMP_TAG);
        if(StringUtils.isEmpty(timestampTag))
            return DEFAULT_CREATION_TIMESTAMP_TAG;
        else
            return timestampTag;
    }

    public static String getInstanceTagsToInclude()
    {
        String tagsToInclude = System.getenv(DEFAULT_INSTANCE_TAGS_TO_INCLUDE_TAG);
        if(StringUtils.isEmpty(tagsToInclude))
            return "";
        else
            return tagsToInclude;
    }

    public static String getInstanceIdTag()
    {
        String instanceIdTag = System.getenv(ENV_VAR_OVERRIDE_INSTANCE_ID_TAG);
        if(StringUtils.isEmpty(instanceIdTag))
            return DEFAULT_INSTANCE_ID_TAG;
        else
            return instanceIdTag;
    }

    public static String getDeviceTag()
    {
        String deviceTag = System.getenv(ENV_VAR_OVERRIDE_DEVICE_TAG);
        if(StringUtils.isEmpty(deviceTag))
            return DEFAULT_DEVICE_TAG;
        else
            return deviceTag;
    }

    public static String getEnableBackupTag()
    {
        String enableBackupTag = System.getenv(ENV_VAR_OVERRIDE_ENABLE_BACKUP_TAG);
        if(StringUtils.isEmpty(enableBackupTag))
            return DEFAULT_ENABLE_BACKUP_TAG;
        else
            return enableBackupTag;
    }

    public static String getImageIdTag()
    {
        String imageIdTag = System.getenv(ENV_VAR_OVERRIDE_IMAGE_ID_TAG);
        if(StringUtils.isEmpty(imageIdTag))
            return DEFAULT_IMAGE_ID_TAG;
        else
            return imageIdTag;
    }

    public static String getFilter1TagName()
    {
        return System.getenv(ENV_VAR_FILTER1_TAG_NAME);
    }

    public static String getFilter1TagValue()
    {
        return System.getenv(ENV_VAR_FILTER1_TAG_VALUE);
    }

    public static String getFilter2TagName()
    {
        return System.getenv(ENV_VAR_FILTER2_TAG_NAME);
    }

    public static String getFilter2TagValue()
    {
        return System.getenv(ENV_VAR_FILTER2_TAG_VALUE);
    }

    public static String getAllDaysScheduleConstant()
    {
        return AVAILABLE_DAY_TAG_ALL_DAYS_CONST;
    }

    public static String getWeekdaysScheduleConstant()
    {
        return AVAILABLE_DAY_TAG_WEEKDAYS_CONST;
    }

    public static String getWeekendScheduleConstant()
    {
        return AVAILABLE_DAY_TAG_WEEKEND_CONST;
    }

    public static String getFormattedHMS(ZonedDateTime dateTime)
    {
        String formattedDateTime = "";
        if(dateTime != null)
        {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH.mm.ss");
            formattedDateTime = dateTime.format(dateFormatter);
        }
        return formattedDateTime;
    }

    public static int getDatabaseRetentionDays() {
        
		String dbRetentionDaysString = System.getenv(ENV_VAR_OVERRIDE_DB_RETENTION_DAYS);
		if (StringUtils.isEmpty(dbRetentionDaysString)) {
			return DEFAULT_DB_RETENTION_DAYS;
		}
		try {
			return new Integer(dbRetentionDaysString);
		}
		catch (NumberFormatException nfe) {
			return DEFAULT_DB_RETENTION_DAYS;
		}
    }
}

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StartStopRequest.java

package com.cloudpacity.aws.stopstart.pojo;

import java.time.*;
import java.time.format.DateTimeFormatter;

import com.cloudpacity.aws.stopstart.CPStopStartEnv;

public class StartStopRequest
{

    public StartStopRequest()
    {
    }

    public String getOriginatingLambdaRequestId()
    {
        return originatingLambdaRequestId;
    }

    public void setOriginatingLambdaRequestId(String originatingLambdaRequestId)
    {
        this.originatingLambdaRequestId = originatingLambdaRequestId;
    }

    public void setRequestStartTime(ZonedDateTime requestStartTime)
    {
        this.requestStartTime = requestStartTime;
    }

    public String getRequestStartTimeString()
    {
        if(getRequestStartTime() != null)
        {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss.SSS-z");
            return requestStartTime.format(dateFormatter);
        } else
        {
            return "";
        }
    }

    public void setRequestStartTimeString(String dateString)
    {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss.SSS-z");
        LocalDateTime datetime = LocalDateTime.parse(dateString, dateFormatter);
        ZoneId zone = ZoneId.of(CPStopStartEnv.getDefaultTimeZone());
        requestStartTime = ZonedDateTime.of(datetime, zone);
    }

    public Integer getIteration()
    {
        return iteration;
    }

    public void setIteration(Integer iteration)
    {
        this.iteration = iteration;
    }

    public void setIterationString(String iterationString)
    {
        if(iterationString != null)
            iteration = new Integer(iterationString);
    }

    public String getCurrentLambdaRequestId()
    {
        return currentLambdaRequestId;
    }

    public void setCurrentLambdaRequestId(String currentLambdaRequestId)
    {
        this.currentLambdaRequestId = currentLambdaRequestId;
    }

    public Integer getActionPauseSec()
    {
        return actionPauseSec;
    }

    public void setActionPauseSec(Integer actionPauseSec)
    {
        this.actionPauseSec = actionPauseSec;
    }

    public Integer getMaxRecursiveCalls()
    {
        return maxRecursiveCalls;
    }

    public void setMaxRecursiveCalls(Integer maxRecursiveCalls)
    {
        this.maxRecursiveCalls = maxRecursiveCalls;
    }

    public Integer getMaxRunMinutes()
    {
        return maxRunMinutes;
    }

    public void setMaxRunMinutes(Integer maxRunMinutes)
    {
        this.maxRunMinutes = maxRunMinutes;
    }

    public String getAvailableDayTag()
    {
        return availableDayTag;
    }

    public void setAvailableDayTag(String availableDayTag)
    {
        this.availableDayTag = availableDayTag;
    }

    public String getAvailableBeginTimeTag()
    {
        return availableBeginTimeTag;
    }

    public void setAvailableBeginTimeTag(String availableBeginTimeTag)
    {
        this.availableBeginTimeTag = availableBeginTimeTag;
    }

    public String getAvailableEndTimeTag()
    {
        return availableEndTimeTag;
    }

    public void setAvailableEndTimeTag(String availableEndTimeTag)
    {
        this.availableEndTimeTag = availableEndTimeTag;
    }

    public String getInstanceDependencyTag()
    {
        return instanceDependencyTag;
    }

    public void setInstanceDependencyTag(String instanceDependencyTag)
    {
        this.instanceDependencyTag = instanceDependencyTag;
    }

    public String getFilter1TagName()
    {
        return filter1TagName;
    }

    public void setFilter1TagName(String filter1TagName)
    {
        this.filter1TagName = filter1TagName;
    }

    public String getFilter1TagValue()
    {
        return filter1TagValue;
    }

    public void setFilter1TagValue(String filter1TagValue)
    {
        this.filter1TagValue = filter1TagValue;
    }

    public String getFilter1EnvVarName()
    {
        return filter1EnvVarName;
    }

    public void setFilter1EnvVarName(String filter1EnvVarName)
    {
        this.filter1EnvVarName = filter1EnvVarName;
    }

    public String getFilter1EnvVarValue()
    {
        return filter1EnvVarValue;
    }

    public void setFilter1EnvVarValue(String filter1EnvVarValue)
    {
        this.filter1EnvVarValue = filter1EnvVarValue;
    }

    public String getFilter2TagName()
    {
        return filter2TagName;
    }

    public void setFilter2TagName(String filter2TagName)
    {
        this.filter2TagName = filter2TagName;
    }

    public String getFilter2TagValue()
    {
        return filter2TagValue;
    }

    public void setFilter2TagValue(String filter2TagValue)
    {
        this.filter2TagValue = filter2TagValue;
    }

    public String getFilter3TagName()
    {
        return filter3TagName;
    }

    public void setFilter3TagName(String filter3TagName)
    {
        this.filter3TagName = filter3TagName;
    }

    public String getFilter3TagValue()
    {
        return filter3TagValue;
    }

    public void setFilter3TagValue(String filter3TagValue)
    {
        this.filter3TagValue = filter3TagValue;
    }

    public String getFilter4TagName()
    {
        return filter4TagName;
    }

    public void setFilter4TagName(String filter4TagName)
    {
        this.filter4TagName = filter4TagName;
    }

    public String getFilter4TagValue()
    {
        return filter4TagValue;
    }

    public void setFilter4TagValue(String filter4TagValue)
    {
        this.filter4TagValue = filter4TagValue;
    }

    public String getFilter5TagName()
    {
        return filter5TagName;
    }

    public void setFilter5TagName(String filter5TagName)
    {
        this.filter5TagName = filter5TagName;
    }

    public String getFilter5TagValue()
    {
        return filter5TagValue;
    }

    public void setFilter5TagValue(String filter5TagValue)
    {
        this.filter5TagValue = filter5TagValue;
    }

    public String getFilter6TagName()
    {
        return filter6TagName;
    }

    public void setFilter6TagName(String filter6TagName)
    {
        this.filter6TagName = filter6TagName;
    }

    public String getFilter6TagValue()
    {
        return filter6TagValue;
    }

    public void setFilter6TagValue(String filter6TagValue)
    {
        this.filter6TagValue = filter6TagValue;
    }

    public ZonedDateTime getRequestStartTime()
    {
        return requestStartTime;
    }

    public String toString()
    {
        String output = (new StringBuilder("requestStartTime: ")).append(getRequestStartTimeString()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; originatingLambdaRequestId: ").append(getOriginatingLambdaRequestId()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; currentLambdaRequestId: ").append(getCurrentLambdaRequestId()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; iteration: ").append(getIteration()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; actionPauseSec: ").append(getActionPauseSec()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; maxRecursiveCalls: ").append(getMaxRecursiveCalls()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; maxRunMinutes: ").append(getMaxRunMinutes()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; availableDayTag: ").append(getAvailableDayTag()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; availableBeginTimeTag: ").append(getAvailableBeginTimeTag()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; availableEndTimeTag: ").append(getAvailableEndTimeTag()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; instanceDependencyTag: ").append(getInstanceDependencyTag()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter1TagName: ").append(getFilter1TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter1TagValue: ").append(getFilter1TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter2TagName: ").append(getFilter2TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter2TagValue: ").append(getFilter2TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter3TagName: ").append(getFilter3TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter3TagValue: ").append(getFilter3TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter4TagName: ").append(getFilter4TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter4TagValue: ").append(getFilter4TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter5TagName: ").append(getFilter5TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter5TagValue: ").append(getFilter5TagValue()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter6TagName: ").append(getFilter6TagName()).toString();
        output = (new StringBuilder(String.valueOf(output))).append("; filter6TagValue: ").append(getFilter6TagValue()).toString();
        return output;
    }

    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH.mm.ss.SSS-z";
    private ZonedDateTime requestStartTime;
    private String originatingLambdaRequestId;
    private String currentLambdaRequestId;
    private Integer iteration;
    private Integer actionPauseSec;
    private Integer maxRecursiveCalls;
    private Integer maxRunMinutes;
    private String availableDayTag;
    private String availableBeginTimeTag;
    private String availableEndTimeTag;
    private String instanceDependencyTag;
    private String filter1TagName;
    private String filter1EnvVarName;
    private String filter1EnvVarValue;
    private String filter1TagValue;
    private String filter2TagName;
    private String filter2TagValue;
    private String filter3TagName;
    private String filter3TagValue;
    private String filter4TagName;
    private String filter4TagValue;
    private String filter5TagName;
    private String filter5TagValue;
    private String filter6TagName;
    private String filter6TagValue;
}

package com.cloudpacity.aws.stopstart.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.s3.AmazonS3;
import com.cloudpacity.aws.common.dao.EC2StopStartDAO;
import com.cloudpacity.aws.common.entity.AWSELBEntity;
import com.cloudpacity.aws.common.entity.AWSInstanceEntity;
import com.cloudpacity.aws.common.entity.AWSSNSEntity;
import com.cloudpacity.aws.common.error.CPRuntimeException;
import com.cloudpacity.aws.common.util.CPLogger;
import com.cloudpacity.aws.stopstart.CPStopStartEnv;
import com.cloudpacity.aws.stopstart.pojo.EC2StopStartDO;
import com.cloudpacity.aws.stopstart.pojo.StartStopRequest;
public class EC2StopStart
{
    protected AWSCredentials awsCredentials;
    protected AmazonS3 s3Client;
    protected CPLogger logger;
    protected CPStopStartEnv env;
    public static final String STOP_CONFIG_FILE_TAG = "stop";
    public static final String START_CONFIG_FILE_TAG = "start";
    public static final String NO_STATE_CHANGE = "no state change";
    
    public EC2StopStart(CPLogger cpLogger, AWSCredentials awsCredentials)
    {
   		this.awsCredentials =  awsCredentials;
		this.logger = cpLogger;
		this.env = new CPStopStartEnv();
    }

    public String invoke(StartStopRequest stopStartReq)
    {
	   	try {
	   		
	   		validateStartStopRequest(stopStartReq);
	   		
	   		String tmpReturn = "";
	   		List<Instance> instancesToStartStop = new ArrayList<Instance>();
	   		
	   		logger.log("Request start time: " + stopStartReq.getRequestStartTimeString() + " original lambda request id: " 
	   				+ stopStartReq.getOriginatingLambdaRequestId() + " this iteration: " + stopStartReq.getIteration());
	   		
	        AWSInstanceEntity awsInstance = new AWSInstanceEntity(awsCredentials, CPStopStartEnv.getRegionName(), logger, this.env);
	       
	        List<Instance> candidateInstances = getInstancesToStop(stopStartReq.getFilter1TagName(), stopStartReq.getFilter1TagValue(), awsInstance);
	        instancesToStartStop = valididateCandidateInstancesTags(candidateInstances, stopStartReq);

	   		for (Instance instanceToTest: instancesToStartStop){
	   			processInstanceToStartStop(awsInstance, instanceToTest,instancesToStartStop, CPStopStartEnv.getRegionName(),stopStartReq);
	   		}
	
	        reattachELBInstances(instancesToStartStop, CPStopStartEnv.getRegionName());
	        Thread.sleep(stopStartReq.getActionPauseSec().intValue() * 1000);
	        candidateInstances = getInstancesToStop(stopStartReq.getFilter1TagName(), stopStartReq.getFilter1TagValue(), awsInstance);
	        instancesToStartStop = valididateCandidateInstancesTags(candidateInstances, stopStartReq);
	        checkTimeout(stopStartReq.getRequestStartTime(), stopStartReq.getIteration().intValue(), stopStartReq);

	        if(!allInstancesInCorrectState(instancesToStartStop, stopStartReq)) {
	            writeToDyanmoTrigger(stopStartReq);
	        }
	        else {
	            logger.log("ALL INSTANCES STARTED/STOPPED  iteration: " + stopStartReq.getIteration());
	            logger.log("SNS Message id: " + AWSSNSEntity.postMessage(logger.getLogMessages()).getMessageId());
	        }
	        
	        return tmpReturn;
	   	}
	   	catch (Throwable e){
	   		logger.log("Exception in EC2Service.startStop!");
	   		logger.log(logger.getDebugMessages());
	   		logger.log(e.getMessage());
	   		logger.log(ExceptionUtils.getStackTrace(e));
	   		return e.getMessage();
	   } 
    }

    private void writeToDyanmoTrigger(StartStopRequest stopStartReq)
        throws ExecutionException, InterruptedException
    {
        int nextIteration = stopStartReq.getIteration().intValue() + 1;
        EC2StopStartDO ec2StopStartDO = new EC2StopStartDO();
        ec2StopStartDO.setId((new StringBuilder(String.valueOf(stopStartReq.getOriginatingLambdaRequestId()))).append("-").append(nextIteration).toString());
        ec2StopStartDO.setOriginatingLambdaRequestId(stopStartReq.getOriginatingLambdaRequestId());
        ec2StopStartDO.setCurrentLambdaRequestId(stopStartReq.getCurrentLambdaRequestId());
        ec2StopStartDO.setRequestStartTimeString(stopStartReq.getRequestStartTimeString());
        ec2StopStartDO.setIteration(nextIteration);
        ec2StopStartDO.setFilter1TagName(stopStartReq.getFilter1TagName());
        ec2StopStartDO.setFilter1TagValue(stopStartReq.getFilter1TagValue());
        ec2StopStartDO.setActionPauseSec(stopStartReq.getActionPauseSec());
        ec2StopStartDO.setMaxRecursiveCalls(stopStartReq.getMaxRecursiveCalls());
        ec2StopStartDO.setMaxRunMinutes(stopStartReq.getMaxRunMinutes());
        ec2StopStartDO.setInstanceDependencyTag(stopStartReq.getInstanceDependencyTag());
        ec2StopStartDO.setAvailableDayTag(stopStartReq.getAvailableDayTag());
        ec2StopStartDO.setAvailableBeginTimeTag(stopStartReq.getAvailableBeginTimeTag());
        ec2StopStartDO.setAvailableEndTimeTag(stopStartReq.getAvailableEndTimeTag());
        EC2StopStartDAO stopStartDAO = new EC2StopStartDAO();

        stopStartDAO.putItem(ec2StopStartDO);
        logger.log("StopStart request put to DynamoDB lambda id: " + stopStartReq.getOriginatingLambdaRequestId() + " next iteration: " + nextIteration);
    }

    private boolean allInstancesInCorrectState(List<Instance> instanceList, StartStopRequest stopStartReq)
    {
        boolean allInstancesInCorrectState = true;
        for(Instance instance : instanceList)
        {
            String currentInstanceState = instance.getState().getName();
            String desiredInstanceState = getDesiredInstanceState(instance, stopStartReq);
            if(desiredInstanceState.equalsIgnoreCase(currentInstanceState))
            {
                List<Tag> tags = instance.getTags();
                String instanceName = AWSInstanceEntity.getTagValueFromList(this.env.getNameTag(), tags, "");
                logger.log((new StringBuilder("Instance: '")).append(instanceName).append("' id: '").append(instance.getInstanceId()).append("' is in the CORRECT state: ").append(currentInstanceState).toString());
            } else
            {
                return false;
            }
        }

        return allInstancesInCorrectState;
    }

    protected void reattachELBInstances(List<Instance> instances, String regionName)
    {
	   	 AWSELBEntity awsELB = new AWSELBEntity(this.awsCredentials, regionName,this.logger, this.env);
	   	 
	   	for (Instance instance: instances) {
	   	
	   		if (instance != null && instance.getInstanceId() != null) {
	   			List<String> elbNameList = awsELB.getELBsForInstanceId(instance.getInstanceId());
	   			
	   			if (elbNameList != null ) {
	   				for (String elbName: elbNameList) {
	   					awsELB.deregisterInstanceFromELB(instance.getInstanceId(), elbName);
	   					awsELB.registerInstanceWithELB(instance.getInstanceId(), elbName);
	   				}
	   			}
	   		}
	   	}
    }

    private void checkTimeout(ZonedDateTime startDateTime, int iteration, StartStopRequest stopStartReq)
    {
        ZoneId zoneCDT = ZoneId.of(CPStopStartEnv.getDefaultTimeZone());
        OffsetDateTime timeoutDateTime = startDateTime.plusMinutes(stopStartReq.getMaxRunMinutes().intValue()).toOffsetDateTime();
        ZonedDateTime nowZonedTime = ZonedDateTime.now(zoneCDT);
        OffsetDateTime nowDateTime = nowZonedTime.toOffsetDateTime();
        logger.log((new StringBuilder("Timeout datetime (start plus ")).append(stopStartReq.getMaxRunMinutes()).append(" min): ").append(timeoutDateTime).append("  current: ").append(nowDateTime).toString());
        if(iteration > stopStartReq.getMaxRecursiveCalls().intValue())
            throw new CPRuntimeException((new StringBuilder("The maximum number of recursive calls '")).append(stopStartReq.getMaxRecursiveCalls()).append("' was exceeded").toString());
        if(startDateTime.plusMinutes(stopStartReq.getMaxRunMinutes().intValue()).toOffsetDateTime().isBefore(nowDateTime))
            throw new CPRuntimeException((new StringBuilder("The maximum run time of ")).append(stopStartReq.getMaxRunMinutes()).append(" minutes has been exceeded!").toString());
        else
            return;
    }

    private void processInstanceToStartStop(AWSInstanceEntity awsInstance, Instance instanceToStartStop, List<Instance> instanceList, String region, StartStopRequest stopStartReq)
    {
        if(instanceToStartStop == null)
            throw new CPRuntimeException("Instance to stop was null!");
        String desiredInstanceState = getDesiredInstanceState(instanceToStartStop, stopStartReq);
        String currentInstanceState = instanceToStartStop.getState().getName();
        String instanceId = instanceToStartStop.getInstanceId();
        List<String> dependentInstanceIdArray = AWSInstanceEntity.getInstanceDependencyIdArray(instanceToStartStop.getTags(), stopStartReq.getInstanceDependencyTag());
        String instanceName = AWSInstanceEntity.getTagValueFromList(this.env.getNameTag(), instanceToStartStop.getTags(), "");
        String instanceDependencies = AWSInstanceEntity.getTagValueFromList(stopStartReq.getInstanceDependencyTag(), instanceToStartStop.getTags(), "");
        logger.log((new StringBuilder("Instance '")).append(instanceName).append("' id: '").append(instanceId).append("' desired state: '").append(desiredInstanceState).append("' current state: '").append(currentInstanceState).append("' dependencies: '").append(instanceDependencies).append("' ").toString());
        if(desiredInstanceState.equalsIgnoreCase("no state change"))
            logger.log((new StringBuilder("instance: ")).append(instanceId).append("  requires no state change.").toString());
        else
        if(!desiredInstanceState.equalsIgnoreCase(currentInstanceState))
        {
            if(desiredInstanceState.equalsIgnoreCase("running"))
                startInstance(awsInstance, instanceList, region, instanceId, instanceName, instanceDependencies, dependentInstanceIdArray);
            else
            if(desiredInstanceState.equalsIgnoreCase("stopped"))
                stopInstance(awsInstance, instanceList, region, instanceId, instanceName, dependentInstanceIdArray);
            else
                throw new CPRuntimeException((new StringBuilder("Error!!  Invalid desired instance state '")).append(desiredInstanceState).append(" ! for instance '").append(instanceName).append("' id: '").append(instanceId).append("' ").toString());
        } else
        {
            logger.log((new StringBuilder("Instance: '")).append(instanceName).append("' id: '").append(instanceId).append("' is in the desired state: '").append(currentInstanceState).append("' ").toString());
        }
    }

    private void stopInstance(AWSInstanceEntity awsInstance, List<Instance> instanceList, String region, String instanceId, String instanceName, List<String> dependentInstanceIdArray)
    {
        String dependentInstances = ArrayUtils.toString(dependentInstanceIdArray);
        if(dependentInstanceIdArray == null || dependentInstanceIdArray.size() == 0)
        {
            logger.log("instance: " + instanceName + " id: " + instanceId + " is running and will be stopped.  No dependencies found.");
            awsInstance.stopInstance(instanceId);
        } else
        if(allDependentInstancesInState(instanceName, instanceId, dependentInstanceIdArray, "stopped", instanceList, region))
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is running and will be stopped.  Instances ithat depend on it are stopped.");
            awsInstance.stopInstance(instanceId);
        } else
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is running and will need to wait until dependent instances  '" + dependentInstances + "' are stopped.");
        }
    }

    private void startInstance(AWSInstanceEntity awsInstance, List<Instance> instanceList, String region, String instanceId, String instanceName, String instanceDependencies, List<String> dependentInstanceIdArray)
    {
        if(StringUtils.isEmpty(instanceDependencies))
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is stopped and will be started.  No dependencies found.");
            awsInstance.startInstance(instanceId);
        } else
        if(allDependentInstancesInState(instanceName, instanceId, dependentInstanceIdArray, "running", instanceList, region))
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is stopped and will be started.  Instances it depends on are running.");
            awsInstance.startInstance(instanceId);
        } else
        {
            logger.log("Instance: '" + instanceName + "' id: '" + instanceId + "' is stopped and will need to wait until dependent instances " + instanceDependencies + " are started.");
        }
    }

    private boolean allDependentInstancesInState(String instanceName, String instanceId, List<String> dependentInstanceIdArray, String desiredInstanceState, List<Instance> instanceList, String region)
    {
        if(dependentInstanceIdArray == null || dependentInstanceIdArray.size() == 0 || StringUtils.isEmpty(desiredInstanceState))
            return true;
        for( String dependentInstanceId : dependentInstanceIdArray)
        {

            String trimmedInstanceId = dependentInstanceId.trim();
            Instance dependentInstance = getInstanceForId(instanceList, trimmedInstanceId);
            if(dependentInstance == null)
            {
                logger.log((new StringBuilder("Instance: ")).append(instanceName).append(" has dependent instance: '").append(trimmedInstanceId).append("' which was not found in the list of instances to start/stop, checking its state").toString());
                AWSInstanceEntity awsInstance = new AWSInstanceEntity(awsCredentials, region, logger, this.env);
                try
                {
                    dependentInstance = awsInstance.getInstanceForId(trimmedInstanceId);
                }
                catch(Exception e)
                {
                    throw new CPRuntimeException((new StringBuilder("Error!!  Instance: ")).append(instanceName).append(" has dependent instance: '").append(trimmedInstanceId).append("' was not found when looking up it's state").toString());
                }
                if(dependentInstance != null && desiredInstanceState.equalsIgnoreCase("running"))
                {
                    if(!desiredInstanceState.equalsIgnoreCase(dependentInstance.getState().getName()))
                        throw new CPRuntimeException((new StringBuilder("Error!!  Instance: ")).append(instanceName).append(" has dependent instance: '").append(trimmedInstanceId).append("' which was not found in the list of instances to start AND it was not in the state: ").append(desiredInstanceState).toString());
                } else
                {
                    logger.log((new StringBuilder("Error!!  Instance: ")).append(instanceName).append(" has dependent instance: '").append(trimmedInstanceId).append("' was not found when looking up it's state").toString());
                }
            } else
            if(!desiredInstanceState.equalsIgnoreCase(dependentInstance.getState().getName()))
                return false;
        }

        return true;
    }

    private Instance getInstanceForId(List<Instance> instanceList, String instanceId)
    {
        if(StringUtils.isEmpty(instanceId))
            return null;
        for(Instance instance : instanceList)
        {
            if(instanceId.equalsIgnoreCase(instance.getInstanceId()))
                return instance;
        }

        return null;
    }

    private ArrayList<Instance>  getInstancesToStop(String tagName, String tagValue, AWSInstanceEntity awsInstance)
    {
        ArrayList<Instance> instancesToStartStop;
        if(StringUtils.isEmpty(tagName))
            instancesToStartStop = awsInstance.getAllInstances();
        else
            instancesToStartStop = awsInstance.getInstancesForTag(tagName, tagValue);
        return instancesToStartStop;
    }

//	private Map<String, List<String>> populateDepedencyMap(List<Instance> instancesToStartStop, StartStopRequest stopStartReq){
//		
//   		Map<String, List<String>> dependencyMap = new HashMap<String, List<String>>();
//   				
//        for(Instance instance : instancesToStartStop)
//        {
//            if(instance != null)
//            {
//                List<String> dependentInstanceIdArray = AWSInstanceEntity.getInstanceDependencyIdArray(instance.getTags(), stopStartReq.getInstanceDependencyTag());
//   				
//   				for (String instanceId : dependentInstanceIdArray) {
//					if (dependencyMap.containsKey(instanceId)){
//						dependencyMap.get(instanceId).add(instance.getInstanceId());
//					}
//					else {
//						List<String> newList = new ArrayList<String>();
//						newList.add(instance.getInstanceId());
//						dependencyMap.put(instanceId,newList);
//					}
//   				}  
//            }
//        }
//
//        return dependencyMap;
//    }

    protected String getDesiredInstanceState(Instance instance, StartStopRequest stopStartReq)
    {
        ZoneId zoneCDT = ZoneId.of(CPStopStartEnv.getDefaultTimeZone());
        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneCDT);
        List<Tag> tags = instance.getTags();
        String availableDay = AWSInstanceEntity.getTagValueFromList(stopStartReq.getAvailableDayTag(), tags, "");
        String availableBeginTime = AWSInstanceEntity.getTagValueFromList(stopStartReq.getAvailableBeginTimeTag(), tags, "");
        String availableEndTime = AWSInstanceEntity.getTagValueFromList(stopStartReq.getAvailableEndTimeTag(), tags, "");
        String instanceName = AWSInstanceEntity.getTagValueFromList(this.env.getNameTag(), tags, "");
        if(!isRunningDay(availableDay, currentDateTime, stopStartReq, instanceName))
        {
            logger.log((new StringBuilder("Instance: '")).append(instanceName).append("'  id: '").append(instance.getInstanceId()).append("' Not a running day, '").append(availableDay).append("' given in availabe day parm '").append(CPStopStartEnv.getAvailableDayTag()).append("'").toString());
            return "stopped";
        }
        if(!StringUtils.isEmpty(availableBeginTime) && !StringUtils.isEmpty(availableEndTime))
            return getDesiredInstanceStateBeginEnd(instanceName, instance.getInstanceId(), availableBeginTime, availableEndTime, currentDateTime);
        if(!StringUtils.isEmpty(availableBeginTime))
            return getDesiredInstanceStateBeginTimeOnly(instanceName, instance.getInstanceId(), availableBeginTime, currentDateTime);
        if(!StringUtils.isEmpty(availableEndTime))
            return getDesiredInstanceStateEndTimeOnly(instanceName, instance.getInstanceId(), availableEndTime, currentDateTime);
        else
            throw new CPRuntimeException((new StringBuilder("Error! Instance: '")).append(instanceName).append("'  id: '").append(instance.getInstanceId()).append("' Both begin and end time are empty!").toString());
    }

    protected String getDesiredInstanceStateBeginTimeOnly(String instanceName, String instanceId, String availableBeginTime, ZonedDateTime currentDateTime)
    {
        int beginHour = 0;
        int beginMinute = 0;
        validateTime(availableBeginTime);
        String beginTimeArray[] = StringUtils.split(availableBeginTime, ":");
        beginHour = Integer.parseInt(beginTimeArray[0]);
        if(beginTimeArray.length > 1)
            beginMinute = Integer.parseInt(beginTimeArray[1]);
        if(currentDateTime.getHour() > beginHour)
        {
            logger.log((new StringBuilder("Instance: '")).append(instanceName).append("' id: '").append(instanceId).append("' should be running: current time:: ").append(currentDateTime.getHour()).append(":").append(currentDateTime.getMinute()).append(" begin time: ").append(beginHour).append(":").append(beginMinute).toString());
            return "running";
        }
        if(currentDateTime.getHour() == beginHour && currentDateTime.getMinute() >= beginMinute)
        {
            logger.log((new StringBuilder("Instance: '")).append(instanceName).append("' id: '").append(instanceId).append("' should be running: current time:: ").append(currentDateTime.getHour()).append(":").append(currentDateTime.getMinute()).append(" begin time: ").append(beginHour).append(":").append(beginMinute).toString());
            return "running";
        } else
        {
            logger.log((new StringBuilder("Instance: '")).append(instanceName).append("' id: '").append(instanceId).append("' should not change state: current time:: ").append(currentDateTime.getHour()).append(":").append(currentDateTime.getMinute()).append(" begin time: ").append(beginHour).append(":").append(beginMinute).toString());
            return "no state change";
        }
    }

    protected boolean isRunningDay(String availableDay, ZonedDateTime currentDateTime, StartStopRequest stopStartReq, String instanceName)
    {
        Validate.notEmpty(availableDay, (new StringBuilder("The available day given is empty for instance '")).append(instanceName).append("' !").toString(), new Object[0]);
        Validate.notNull(currentDateTime, (new StringBuilder("The currentDateTime is null for instance '")).append(instanceName).append("' !").toString(), new Object[0]);
        Validate.notNull(stopStartReq, (new StringBuilder("The stopStartReq is null for instance '")).append(instanceName).append("' !").toString(), new Object[0]);
        boolean isRunningDay = true;
        DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();
        if(availableDay.equalsIgnoreCase(CPStopStartEnv.getAllDaysScheduleConstant()))
            return true;
        if(availableDay.equalsIgnoreCase(CPStopStartEnv.getWeekendScheduleConstant()))
        {
            if(dayOfWeek.compareTo(DayOfWeek.SUNDAY) == 0 || dayOfWeek.compareTo(DayOfWeek.SATURDAY) == 0)
                isRunningDay = true;
            else
                isRunningDay = false;
        } else
        if(availableDay.equalsIgnoreCase(CPStopStartEnv.getWeekdaysScheduleConstant()))
        {
            if(dayOfWeek.compareTo(DayOfWeek.MONDAY) == 0 || dayOfWeek.compareTo(DayOfWeek.TUESDAY) == 0 || dayOfWeek.compareTo(DayOfWeek.WEDNESDAY) == 0 || dayOfWeek.compareTo(DayOfWeek.THURSDAY) == 0 || dayOfWeek.compareTo(DayOfWeek.FRIDAY) == 0)
                isRunningDay = true;
            else
                isRunningDay = false;
        } else
        {
            ZoneId zoneCDT = ZoneId.of(CPStopStartEnv.getDefaultTimeZone());
            ZonedDateTime currentZonedDateTime = ZonedDateTime.now(zoneCDT);
            ZonedDateTime availableDateTime = getDateValue(availableDay);
            if(availableDateTime == null)
            {
                logger.log((new StringBuilder("Error: the date format for ")).append(CPStopStartEnv.getAvailableDayTag()).append(" is not recognized. Value: '").append(availableDay).append("'").toString());
                return false;
            }
            return availableDateTime.getDayOfYear() == currentZonedDateTime.getDayOfYear() && availableDateTime.getYear() == currentZonedDateTime.getYear();
        }
        return isRunningDay;
    }

    protected ZonedDateTime getDateValue(String dateString)
    {
        ZonedDateTime zonedDateTime = null;
        zonedDateTime = getDateValueForFormat(dateString, "yyyy-MM-dd");
        if(zonedDateTime != null)
            return zonedDateTime;
        zonedDateTime = getDateValueForFormat(dateString, "MM/dd/yyyy");
        if(zonedDateTime != null)
            return zonedDateTime;
        zonedDateTime = getDateValueForFormat(dateString, "MM-dd-yyyy");
        if(zonedDateTime != null)
            return zonedDateTime;
        else
            return zonedDateTime;
    }

    protected ZonedDateTime getDateValueForFormat(String dateString, String dateFromat)
    {
		try {
			  DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFromat);
			  LocalDate date = LocalDate.parse(dateString, formatter);
			  ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneId.of(CPStopStartEnv.getDefaultTimeZone()));
			  return zonedDateTime;
		}
		catch (Exception e) {
			return null;
		}
    }

    protected String getDesiredInstanceStateEndTimeOnly(String instanceName, String instanceId, String availableEndTime, ZonedDateTime currentDateTime)
    {
        int endHour = 0;
        int endMinute = 0;
        validateTime(availableEndTime);
        String endTimeArray[] = StringUtils.split(availableEndTime, ":");
        endHour = Integer.parseInt(endTimeArray[0]);
        if(endTimeArray.length > 1)
            endMinute = Integer.parseInt(endTimeArray[1]);
        if(currentDateTime.getHour() > endHour || currentDateTime.getHour() == endHour && currentDateTime.getMinute() <= endMinute)
        {
            logger.log((new StringBuilder("Instance: '")).append(instanceName).append("' id: '").append(instanceId).append("'  should be stopped: current time: ").append(CPStopStartEnv.getFormattedHMS(currentDateTime)).append("  end time: ").append(endHour).append(":").append(endMinute).toString());
            return "stopped";
        } else
        {
            return "no state change";
        }
    }

    protected String getDesiredInstanceStateBeginEnd(String instanceName, String instanceId, String availableBeginTime, String availableEndTime, ZonedDateTime currentDateTime)
    {
        int beginHour = 0;
        int beginMinute = 0;
        int endHour = 0;
        int endMinute = 0;
        validateTime(availableBeginTime);
        String beginTimeArray[] = StringUtils.split(availableBeginTime, ":");
        beginHour = Integer.parseInt(beginTimeArray[0]);
        if(beginTimeArray.length > 1)
            beginMinute = Integer.parseInt(beginTimeArray[1]);
        validateTime(availableEndTime);
        String endTimeArray[] = StringUtils.split(availableEndTime, ":");
        endHour = Integer.parseInt(endTimeArray[0]);
        if(endTimeArray.length > 1)
            endMinute = Integer.parseInt(endTimeArray[1]);
        if(currentDateTime.getHour() > beginHour && currentDateTime.getHour() < endHour)
        {
            logger.log((new StringBuilder("Instance '")).append(instanceName).append("'  id: '").append(instanceId).append("'  should be running: current time: ").append(CPStopStartEnv.getFormattedHMS(currentDateTime)).append(" begin time: ").append(beginHour).append(":").append(beginMinute).append("  end time: ").append(endHour).append(":").append(endMinute).toString());
            return "running";
        }
        if(currentDateTime.getHour() == beginHour && currentDateTime.getMinute() >= beginMinute || currentDateTime.getHour() == endHour && currentDateTime.getMinute() <= endMinute)
        {
            logger.log((new StringBuilder("Instance '")).append(instanceName).append("'  id: '").append(instanceId).append("' should be running: current time:: ").append(CPStopStartEnv.getFormattedHMS(currentDateTime)).append(" begin time: ").append(beginHour).append(":").append(beginMinute).append("  end time: ").append(endHour).append(":").append(endMinute).toString());
            return "running";
        } else
        {
            logger.log((new StringBuilder("Instance '")).append(instanceName).append("'  id: '").append(instanceId).append("' should be stopped: current time:: ").append(CPStopStartEnv.getFormattedHMS(currentDateTime)).append(" begin time: ").append(beginHour).append(":").append(beginMinute).append("  end time: ").append(endHour).append(":").append(endMinute).toString());
            return "stopped";
        }
    }

    private void validateStartStopRequest(StartStopRequest stopStartReq)
    {
        Validate.notNull(stopStartReq, "The backup request is null!", new Object[0]);
        Validate.notNull(stopStartReq.getMaxRunMinutes(), "The Max Run Minutes is null!.", new Object[0]);
        Validate.notNull(stopStartReq.getActionPauseSec(), "The pause between actions (PauseSecs) is null!.", new Object[0]);
        Validate.notNull(stopStartReq.getMaxRecursiveCalls(), "The MaxRecursiveCalls is null!.", new Object[0]);
        Validate.notEmpty(stopStartReq.getAvailableBeginTimeTag(), "The AvailableBeginTimeTag is empty!.", new Object[0]);
        Validate.notEmpty(stopStartReq.getAvailableEndTimeTag(), "The AvailableEndTimeTag is empty!.", new Object[0]);
        Validate.notEmpty(stopStartReq.getAvailableDayTag(), "The AvailableDayTag is empty!.", new Object[0]);
    }

    protected boolean validateTime(String timeToValidate)
    {
        if(StringUtils.isEmpty(timeToValidate))
            return false;
        String timeArray[] = StringUtils.split(timeToValidate, ":");
        String as[];
        int j = (as = timeArray).length;
        for(int i = 0; i < j; i++)
        {
            String time = as[i];
            try
            {
                Integer.parseInt(time);
            }
            catch(NumberFormatException nfe)
            {
                logger.log((new StringBuilder("The scheduled start stop time is invalid '")).append(timeToValidate).append("' ").toString());
                return false;
            }
        }

        return true;
    }

    private List<Instance> valididateCandidateInstancesTags(List<Instance> candidateInstances, StartStopRequest stopStartReq)
    {
        List<Instance> instancesToStartStop = new ArrayList<Instance>();
        if(candidateInstances == null || candidateInstances.size() == 0)
            return instancesToStartStop;
        String prevAvailableDay = "";
        String prevAvailableBeginTime = "";
        String prevAvailableEndTime = "";
        
        for(Instance instance: candidateInstances)
        {
            try
            {
                if(instance != null)
                {
                    List<Tag> tags = instance.getTags();
                    String instanceName = AWSInstanceEntity.getTagValueFromList(this.env.getNameTag(), tags, "");
                    String availableDay = "";
                    String availableBeginTime = "";
                    String availableEndTime = "";
                    String instanceDependencies = "";
                    
                    for(Tag tag: tags) {

                        if(stopStartReq.getAvailableDayTag().equalsIgnoreCase(tag.getKey()))
                        {
                            if(StringUtils.isEmpty(tag.getKey()))
                                throw new CPRuntimeException((new StringBuilder("SKIPPING Instance: '")).append(instanceName).append("' instance id: ").append(instance.getInstanceId()).append(" has no tag: ").append(stopStartReq.getAvailableDayTag()).append(" !").toString());
                            if(!StringUtils.isEmpty(prevAvailableDay))
                            {
                                if(!tag.getValue().equalsIgnoreCase(availableDay))
                                    logger.log((new StringBuilder("Possible Error: Instance: '")).append(instanceName).append("' Instance id: '").append(instance.getInstanceId()).append("' tag: '").append(stopStartReq.getAvailableDayTag()).append("' value: '").append(tag.getValue()).append("'  is not equal to previous value:  '").append(availableDay).append("' which will cause problems if there are dependencies").toString());
                                prevAvailableDay = tag.getValue();
                            } else
                            {
                                prevAvailableDay = tag.getValue();
                            }
                            availableDay = tag.getValue();
                        }
                        if(stopStartReq.getAvailableBeginTimeTag().equalsIgnoreCase(tag.getKey()))
                        {
                            if(StringUtils.isEmpty(tag.getKey()))
                                throw new CPRuntimeException((new StringBuilder("SKIPPING Instance: '")).append(instanceName).append("' instance id: ").append(instance.getInstanceId()).append(" has no tag: ").append(stopStartReq.getAvailableBeginTimeTag()).append(" and it will NOT be stopped/started!!").toString());
                            if(!StringUtils.isEmpty(prevAvailableBeginTime))
                            {
                                if(!tag.getValue().equalsIgnoreCase(availableBeginTime))
                                    logger.log((new StringBuilder("Possible Error Instance: '")).append(instanceName).append("' Instance id: '").append(instance.getInstanceId()).append("' tag: '").append(stopStartReq.getAvailableBeginTimeTag()).append("' value: '").append(tag.getValue()).append("'  is not equal to previous value:  '").append(availableBeginTime).append("' which will cause problems if there are dependencies").toString());
                                prevAvailableBeginTime = tag.getValue();
                            } else
                            {
                                prevAvailableBeginTime = tag.getValue();
                            }
                            availableBeginTime = tag.getValue();
                        }
                        if(stopStartReq.getAvailableEndTimeTag().equalsIgnoreCase(tag.getKey()))
                        {
                            if(StringUtils.isEmpty(tag.getKey()))
                                throw new CPRuntimeException((new StringBuilder("SKIPPING Instance: '")).append(instanceName).append("' instance id: ").append(instance.getInstanceId()).append(" has no tag: ").append(stopStartReq.getAvailableEndTimeTag()).append(" and it will NOT be stopped/started!!!").toString());
                            if(!StringUtils.isEmpty(prevAvailableEndTime))
                            {
                                if(!tag.getValue().equalsIgnoreCase(availableEndTime))
                                    logger.log((new StringBuilder("Possible Error Instance: '")).append(instanceName).append("' Instance id: '").append(instance.getInstanceId()).append("' tag: '").append(stopStartReq.getAvailableEndTimeTag()).append("' value: '").append(tag.getValue()).append("'  is not equal to previous value:  '").append(availableEndTime).append("' which may cause timing issues if there are dependencies. ").toString());
                                prevAvailableEndTime = tag.getValue();
                            } else
                            {
                                prevAvailableEndTime = tag.getValue();
                            }
                            availableEndTime = tag.getValue();
                        }
                        if(stopStartReq.getInstanceDependencyTag().equalsIgnoreCase(tag.getKey()))
                            instanceDependencies = tag.getValue();
                    }

                    String dependentInstanceIdArray[] = StringUtils.split(instanceDependencies, ",");
                    String idsNotInInstanceArray = idsNotInInstanceArray(dependentInstanceIdArray, candidateInstances);
                    if(!StringUtils.isEmpty(idsNotInInstanceArray))
                        throw new CPRuntimeException((new StringBuilder("SKIPPING Instance: '")).append(instanceName).append("' Instance id: ").append(instance.getInstanceId()).append(" The instance's dependent instance Ids are not in list of instances to stop: ").append(idsNotInInstanceArray).append("' and it will NOT be stopped/started! ").toString());
                    if(StringUtils.isEmpty(availableBeginTime) && StringUtils.isEmpty(availableEndTime))
                        throw new CPRuntimeException((new StringBuilder("SKIPPING Instance: '")).append(instanceName).append("' Instance id: ").append(instance.getInstanceId()).append(" is missing both begin and end time tags and it will NOT be stopped/started!!  Please enter one or both").toString());
                    if(StringUtils.isEmpty(availableDay))
                        throw new CPRuntimeException((new StringBuilder("SKIPPING Instance: '")).append(instanceName).append("' Instance id: ").append(instance.getInstanceId()).append(" is missing available day tag: '").append(CPStopStartEnv.getAvailableDayTag()).append("' annd it will NOT be stopped/started! ").toString());
                    logger.log((new StringBuilder("ADDING Instance: '")).append(instanceName).append(" Instance id: ").append(instance.getInstanceId()).append(" Begin Time: '").append(availableBeginTime).append("' End Time: '").append(availableEndTime).append("' Available Day: '").append(availableDay).append("' Dependencies: '").append(instanceDependencies).append("' ").toString());
                    instancesToStartStop.add(instance);
                }
            }
            catch(CPRuntimeException validationException)
            {
                logger.log(validationException.getErrorMessage());
            }
        }

        return instancesToStartStop;
    }

    private String idsNotInInstanceArray(String instanceIdArray[], List<Instance> instanceArray)
    {
        ArrayList<String> instanceArrayIds = new ArrayList<String>();
        String idsNotInArray = "";
        for(Instance instance:instanceArray)
        {
            if(instance != null)
                instanceArrayIds.add(instance.getInstanceId());
        }

        String as[];
        int j = (as = instanceIdArray).length;
        for(int i = 0; i < j; i++)
        {
            String instanceId = as[i];
            for(Instance instance: instanceArray)            {
            	
                if(instance != null && StringUtils.isNotEmpty(instanceId))
                {
                    String trimmedId = instanceId.trim();
                    if(!instanceArrayIds.contains(trimmedId))
                        idsNotInArray = (new StringBuilder(String.valueOf(idsNotInArray))).append(" ").append(trimmedId).toString();
                }
            }

        }

        return idsNotInArray;
    }


}

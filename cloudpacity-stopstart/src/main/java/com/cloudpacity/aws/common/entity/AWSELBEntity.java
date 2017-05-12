package com.cloudpacity.aws.common.entity;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancing.model.*;
import com.cloudpacity.aws.common.CPCommonEnv;
import com.cloudpacity.aws.common.util.CPLogger;
import java.util.*;
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
public class AWSELBEntity extends AWSObjectEntity
{

	protected AmazonElasticLoadBalancing elbClient = null;

	/**
	 * 
	 * @param awsCredentials
	 * @param regionName
	 * @param logger
	 */
	public AWSELBEntity (AWSCredentials awsCredentials, String regionName, CPLogger logger,CPCommonEnv env) {
		super( logger,env);
		
		Validate.notNull(awsCredentials, "The AWS credentials supplied were null!");
		Validate.notEmpty(regionName, "The AWS region name provided is empty!");
	
        this.elbClient = AmazonElasticLoadBalancingClientBuilder.standard()
				.withRegion(regionName)
				.build();
	}
	
	/**
	 * 
	 * @param instanceId
	 * @return
	 */
	public List<String>  getELBsForInstanceId(String instanceId) {
		
		Validate.notEmpty(instanceId, "The instance id provided is empty!");
		
		List<String> elbIds = new ArrayList<String>();
		
		DescribeLoadBalancersResult elbResult = this.elbClient.describeLoadBalancers();
		
		for (LoadBalancerDescription elb: elbResult.getLoadBalancerDescriptions()) {
			for (Instance instance: elb.getInstances()) {
				if (instanceId.equalsIgnoreCase(instance.getInstanceId())) {
					elbIds.add(elb.getLoadBalancerName());
				}
			}
		}
	
		return elbIds;
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param loadBalancerName
	 */
	public void registerInstanceWithELB(String instanceId, String loadBalancerName) {
		
		com.amazonaws.services.elasticloadbalancing.model.Instance elbInstance = new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId);
		Collection<com.amazonaws.services.elasticloadbalancing.model.Instance> elbInstances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>(Arrays.asList(elbInstance));
		
		RegisterInstancesWithLoadBalancerRequest registerWithELBRequest = new RegisterInstancesWithLoadBalancerRequest();
		registerWithELBRequest.setInstances(elbInstances);
		registerWithELBRequest.setLoadBalancerName(loadBalancerName);
		
		this.elbClient.registerInstancesWithLoadBalancer(registerWithELBRequest);
	}
	
	/**
	 * 
	 * @param instanceId
	 * @param loadBalancerName
	 */
	public void deregisterInstanceFromELB(String instanceId, String loadBalancerName) {
		
		com.amazonaws.services.elasticloadbalancing.model.Instance elbInstance = new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId);
		Collection<com.amazonaws.services.elasticloadbalancing.model.Instance> elbInstances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>(Arrays.asList(elbInstance));
		
		DeregisterInstancesFromLoadBalancerRequest deregisterWithELBRequest = new DeregisterInstancesFromLoadBalancerRequest();
		deregisterWithELBRequest.setInstances(elbInstances);
		deregisterWithELBRequest.setLoadBalancerName(loadBalancerName);		
		
		this.elbClient.deregisterInstancesFromLoadBalancer(deregisterWithELBRequest);
	}
}

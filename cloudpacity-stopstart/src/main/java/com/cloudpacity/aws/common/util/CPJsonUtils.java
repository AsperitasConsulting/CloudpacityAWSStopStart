package com.cloudpacity.aws.common.util;

import java.io.IOException;

import com.cloudpacity.aws.common.error.CPRuntimeException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class CPJsonUtils
{

	public static Object marshallJson(String jsonString, Class marshallToClass) {
		
	    try {
		    ObjectMapper objectMapper = new ObjectMapper();
		    JsonFactory factory = new JsonFactory();
	
		    JsonParser  jsonParser  = factory.createParser(jsonString);
		    //convert json string to object
		    return objectMapper.readValue(jsonParser, marshallToClass);
	    } 
	    catch (JsonParseException jpe) {
	    	throw new CPRuntimeException ("Json parsing exception for json string: " + jsonString +  "   STACK TRACE: " + jpe.getStackTrace());
	    }
	    catch (JsonMappingException jme) {
	    	throw new CPRuntimeException ("Json mapping exception mapping to class: " + marshallToClass +  "   STACK TRACE: " + jme.getStackTrace());
	    }
	    catch (IOException ioe){
	    	throw new CPRuntimeException ("IOException mapping json file to class: " + marshallToClass +  "   STACK TRACE: " + ioe.getStackTrace());
	    }
	}
}

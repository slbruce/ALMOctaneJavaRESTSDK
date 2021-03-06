/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.adm.nga.sdk.model;

import java.util.Set;

/**
 *
 * This class hold the ErrorModel objects and server as an error data holder
 * entities.
 *
 *
 */
public class ErrorModel extends  EntityModel{
	
	private static final String ERROR_DESCRIPTION_KEY 	= "Description";
	
	/**
	 * Creates a new ErrorModel object with given field models
	 * 
	 * @param value
	 *            - a collection of field models
	 */
	public ErrorModel(Set<FieldModel> value) {
		super(value);
		
	}
	
	/**
	 * Creates a new ErrorModel object with given error message
	 * @param value -  error message
	 */
	public ErrorModel(String value) {
		
		super(ERROR_DESCRIPTION_KEY,value);
			
	}

	public String getDescription() {
		FieldModel descriptionModel = getValue(ErrorModel.ERROR_DESCRIPTION_KEY);
		return descriptionModel != null ? String.valueOf(getValue(ErrorModel.ERROR_DESCRIPTION_KEY).getValue()) : null;
	}
}

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
package com.hpe.adm.nga.sdk.network;

import com.hpe.adm.nga.sdk.entities.OctaneCollection;
import com.hpe.adm.nga.sdk.exception.OctaneException;
import com.hpe.adm.nga.sdk.exception.OctanePartialException;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ErrorModel;
import com.hpe.adm.nga.sdk.model.ModelParser;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * An abstract representation of a request
 */
public final class OctaneRequest {

	private final Logger logger = LoggerFactory.getLogger(OctaneRequest.class.getName());

	private final OctaneUrl octaneUrl;
	protected final OctaneHttpClient octaneHttpClient;

	// constant
	private static final String LOGGER_RESPONSE_FORMAT = "Response: %d - %s - %s";
	private static final String LOGGER_RESPONSE_JSON_FORMAT = "Response_Json: %s";
	private static final long HTTPS_CONFLICT_STATUS_CODE = 409;

	public OctaneRequest(final OctaneHttpClient octaneHttpClient, final String urlDomain) {
		octaneUrl = new OctaneUrl(urlDomain);
		this.octaneHttpClient = octaneHttpClient;
	}

	public OctaneRequest(final OctaneHttpClient octaneHttpClient, final String urlDomain, final String entityId) {
		this (octaneHttpClient, urlDomain);
		octaneUrl.addPaths(entityId);
	}

	public final OctaneUrl getOctaneUrl(){
		return octaneUrl;
	}

	public final String getFinalRequestUrl() {
		return octaneUrl.toString();
	}

	/**
	 * get entities result based on Http Request
	 *
	 * @param octaneHttpRequest - http request
	 * @return entities ased on Http Request
	 * @throws Exception if response parsing fails
	 */
	public final OctaneCollection<EntityModel> getEntitiesResponse(OctaneHttpRequest octaneHttpRequest) throws Exception {

		OctaneCollection<EntityModel> newEntityModels = null;

		OctaneHttpResponse response = octaneHttpClient.execute(octaneHttpRequest);

		String json = response.getContent();
		logger.debug(String.format(LOGGER_RESPONSE_JSON_FORMAT, json));

		if (response.isSuccessStatusCode() && json != null && !json.isEmpty()) {
			newEntityModels = ModelParser.getInstance().getEntities(json);

		}

		return newEntityModels;
	}

	/**
	 * get entity result based on Http Request
	 *
	 * @param octaneHttpRequest the request object
	 * @return EntityModel
	 */
	public EntityModel getEntityResponse(OctaneHttpRequest octaneHttpRequest) {

		EntityModel newEntityModel = null;

		OctaneHttpResponse response = octaneHttpClient.execute(octaneHttpRequest);

		String json = response.getContent();
		logger.debug(String.format(LOGGER_RESPONSE_JSON_FORMAT, json));
		if (response.isSuccessStatusCode() && (json != null && !json.isEmpty())) {

			JSONTokener tokener = new JSONTokener(json);
			JSONObject jsonObj = new JSONObject(tokener);
			newEntityModel = ModelParser.getInstance().getEntityModel(jsonObj);
		}

		return newEntityModel;

	}

	/**
	 * Handle exceptions
	 *
	 * @param e              - exception
	 * @param partialSupport - Is Partial ?
	 */
	public void handleException(Exception e, boolean partialSupport) {

		if (e instanceof HttpResponseException) {

			HttpResponseException httpResponseException = (HttpResponseException) e;
			logger.debug(String.format(LOGGER_RESPONSE_FORMAT, httpResponseException.getStatusCode(), httpResponseException.getStatusMessage(), httpResponseException.getHeaders().toString()));
			if (partialSupport && httpResponseException.getStatusCode() == HTTPS_CONFLICT_STATUS_CODE) {
				Collection<EntityModel> entities = ModelParser.getInstance().getEntities(httpResponseException.getContent());
				Collection<ErrorModel> errorModels = ModelParser.getInstance().getErrorModels(httpResponseException.getContent());
				throw new OctanePartialException(errorModels, entities);
			} else {
				ErrorModel errorModel = ModelParser.getInstance().getErrorModelFromjson(httpResponseException.getContent());
				throw new OctaneException(errorModel);
			}
		} else {
			boolean traverse = true;
			Throwable throwable = e;
			while (traverse) {
				Throwable nextThrowable = throwable.getCause();
				if (nextThrowable == null || nextThrowable == throwable) {
					traverse = false;
				} else {
					throwable = nextThrowable;
				}
			}
			ErrorModel errorModel = new ErrorModel(throwable.getMessage());
			throw new OctaneException(errorModel);
		}
	}
}

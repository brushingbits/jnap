/*
 * Response.java created on 2010-03-02
 *
 * Created by Brushing Bits Labs
 * http://www.brushingbits.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brushingbits.jnap.web;

import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import org.brushingbits.jnap.common.bean.visitor.BeanPropertyFilter;
import org.brushingbits.jnap.validation.ValidationBean;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 * 
 * @see javax.ws.rs.core.HttpHeaders
 * @see javax.ws.rs.core.Response.Status
 */
public class Response {

	private static final long EXPIRES_ALREADY_EXPIRED = 0L;
	public static final long EXPIRES_IN_1_MINUTE = 1000L * 60L;
	public static final long EXPIRES_IN_10_MINUTES = 1000L * 60L * 10L;
	public static final long EXPIRES_IN_30_MINUTES = 1000L * 60L * 30L;
	public static final long EXPIRES_IN_1_HOUR = 1000L * 60L * 60L;
	public static final long EXPIRES_IN_6_HOURS = 1000L * 60L * 60L * 6L;
	public static final long EXPIRES_IN_12_HOURS = 1000L * 60L * 60L * 12L;
	public static final long EXPIRES_IN_24_HOURS = 1000L * 60L * 60L * 24L;

	public static final String PRAGMA_HEADER = "Pragma";
	public static final String CACHE_CONTROL_VALUE_NO_CACHE_1_0 = "no-cache";
	public static final String CACHE_CONTROL_VALUE_NO_CACHE_1_1 = "no-store, no-cache, must-revalidate";
	public static final String CACHE_CONTROL_VALUE_NO_CACHE_IE = "post-check=0, pre-check=0";

	protected String resultName;
	protected String wrap;
	protected Object entity;
	protected Status status;
	protected boolean cacheEnabled;
	protected boolean useETag;
	protected Date lastModified;
	protected Long expires;
	protected BeanPropertyFilter propertyFilter;

	// TODO output options

	protected Response(String resultName) {
		this.resultName = resultName;
		this.propertyFilter = BeanPropertyFilter.getDefault();
	}

	public BeanPropertyFilter getPropertyFilter() {
		return propertyFilter;
	}

	public static Response ok(String resultName) {
		return Response.build(resultName, OK);
	}

	public static Response ok(String resultName, Object entity) {
		return Response.build(resultName, OK).entity(entity);
	}

	public static Response invalid(String resultName, Object validationEntity) {
		return Response.build(resultName, BAD_REQUEST).entity(validationEntity).avoidCaching();
	}

	public static Response exception(Exception ex) {
		return Response.exception(ex.getClass().getSimpleName(), ex);
	}
	
	public static Response exception(String resultName, Exception ex) {
		return Response.build(resultName, INTERNAL_SERVER_ERROR).entity(
				new ValidationBean(ex)).avoidCaching().noPropertyFiltering();
	}

	public static Response build(String resultName, Status status) {
		return new Response(resultName).withStatusCode(status);
	}

	public Response entity(Object entity) {
		this.entity = entity;
		return this;
	}

	public Response withStatusCode(Status status) {
		this.status = status;
		return this;
	}

	public Response avoidCaching() {
		this.cacheEnabled = false;
		return this;
	}

	public Response wrap(String name) {
		this.wrap = name;
		return this;
	}

	public Response wrap() {
		return wrap("responseEntity");
	}

	public Response propertyFilter(BeanPropertyFilter propertyFilter) {
		this.propertyFilter = propertyFilter;
		return this;
	}

	public Response noPropertyFiltering() {
		this.propertyFilter = null;
		return this;
	}

	/**
	 * 
	 * <strong>Note:</strong> this implementation was inspired by the
	 * <code>DefaultHttpHeaders#apply(HttpServletRequest, HttpServletResponse, Object)</code>
	 * from the <strong>struts2-rest-plugin</strong>.
	 * 
	 * @param req
	 *            The current {@link HttpServletRequest} object.
	 * @param res
	 *            The current {@link HttpServletResponse} object.
	 */
	public void apply(HttpServletRequest req, HttpServletResponse res) {

		if (!cacheEnabled) {
			res.setDateHeader(EXPIRES, EXPIRES_ALREADY_EXPIRED);
			res.setHeader(PRAGMA_HEADER, CACHE_CONTROL_VALUE_NO_CACHE_1_0);
			res.setHeader(CACHE_CONTROL, CACHE_CONTROL_VALUE_NO_CACHE_1_1);
			res.addHeader(CACHE_CONTROL, CACHE_CONTROL_VALUE_NO_CACHE_IE);
		}

		if (lastModified != null) {
			res.setDateHeader(LAST_MODIFIED, lastModified.getTime());
		}

		String etag = null;
		if (useETag && entity != null) {
			etag = String.valueOf(entity.hashCode());
			res.setHeader(ETAG, etag);
		}

		if (status.equals(OK) && cacheEnabled) {
			// TODO caching...
			if (expires != null) {
				res.setDateHeader(EXPIRES, System.currentTimeMillis() + expires);
			}
		}

		// TODO other Headers

		res.setStatus(status.getStatusCode());
	}

	// getters

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public String getResultName() {
		return resultName;
	}

	public String getWrap() {
		return wrap;
	}

	public Status getStatus() {
		return status;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public boolean isUseETag() {
		return useETag;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Long getExpires() {
		return expires;
	}

}

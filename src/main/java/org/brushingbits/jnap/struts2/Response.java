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
package org.brushingbits.jnap.struts2;

import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.*;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

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

	// TODO output options

	protected Response(String resultName) {
		this.resultName = resultName;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>entity</code>.
	 */
	public Object getEntity() {
		return entity;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>entity</code>.
	 * This method is package access and should be called only within the
	 * framework.
	 */
	void setEntity(Object entity) {
		this.entity = entity;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property
	 * <code>resultName</code>.
	 */
	public String getResultName() {
		return resultName;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>wrap</code>.
	 */
	public String getWrap() {
		return wrap;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>status</code>.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property
	 * <code>cacheEnabled</code>.
	 */
	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>useETag</code>
	 * .
	 */
	public boolean isUseETag() {
		return useETag;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property
	 * <code>lastModified</code>.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>expires</code>
	 * .
	 */
	public Long getExpires() {
		return expires;
	}

	public static Response ok(String resultName) {
		return new Response(resultName).withStatusCode(OK);
	}

	public static Response ok(String resultName, Object entity) {
		return new Response(resultName).withStatusCode(OK).entity(entity);
	}

	public static Response invalid(String resultName, Object validationEntity) {
		return new Response(resultName).withStatusCode(BAD_REQUEST).entity(
				validationEntity).avoidCaching();
	}

	public static Response create(String resultName, Status status) {
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

}


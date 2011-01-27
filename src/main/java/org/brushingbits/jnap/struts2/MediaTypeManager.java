/*
 * MediaTypeManager.java created on 31/03/2010
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.brushingbits.jnap.web.Response;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class MediaTypeManager {

	protected Map<String, MediaTypeHandler> handlersByExtension;

	protected Map<MediaType, MediaTypeHandler> handlersByMediaType;

	@Inject
	public MediaTypeManager(@Inject Container container) {
		this.handlersByExtension = new HashMap<String, MediaTypeHandler>();
		this.handlersByMediaType = new HashMap<MediaType, MediaTypeHandler>();
		Set<String> names = container.getInstanceNames(MediaTypeHandler.class);
		for (String beanName : names) {
			MediaTypeHandler handler = container.getInstance(MediaTypeHandler.class, beanName);
			
			// handlers by extension mapping
			String[] extensions = handler.getExtensions();
			for (String extension : extensions) {
				this.handlersByExtension.put(extension, handler);
			}

			// handlers by media type (content type) mapping
			MediaType[] mediaTypes = handler.getSupportedMediaTypes();
			for (MediaType mediaType : mediaTypes) {
				this.handlersByMediaType.put(mediaType, handler);
			}
		}
	}

	public String handle(Response response) {

		final HttpServletRequest req = ServletActionContext.getRequest();
		final HttpServletResponse res = ServletActionContext.getResponse();
		response.apply(req, res);
		final String httpMethod = StrutsHelper.getHttpMethod(req);
		if (!Status.OK.equals(response.getStatus())) {
			
		}

		String resultName = response.getResultName();
		MediaTypeHandler handler = getHandlerForResponse();
		if (handler != null) {
			StringWriter tempWriter = new StringWriter();
			resultName = handler.write(response, tempWriter);
			String responseBody = tempWriter.toString();
			if (StringUtils.isNotBlank(responseBody)) {
				try {
					byte[] content = responseBody.getBytes("utf-8");
					res.setContentType(handler.getDefaultMediaType().toString());
					res.setContentLength(content.length);
					res.getOutputStream().write(content);
				} catch (IOException ioe) {
					// TODO ex handling
					throw new RuntimeException(ioe);
				}
			}
		}
		return resultName;
	}

	protected MediaTypeHandler getHandlerForRequest() {
		MediaTypeHandler mediaTypeHandler = null;
		String requestContentType = ServletActionContext.getRequest().getContentType();
		if (requestContentType != null) {
			try {
				MediaType mediaType = MediaType.valueOf(requestContentType);
				mediaTypeHandler = getHandlerForMediaType(mediaType);
			} catch (IllegalArgumentException e) {
				// do nothing on invalid content type, try by extension next...
			}
		}

		// if the content type is unknown or there is no handler for it, then try requested extension
		if (mediaTypeHandler == null) {
			mediaTypeHandler = this.handlersByExtension.get(getRequestUriExtension());
		}
		return mediaTypeHandler;
	}

	protected MediaTypeHandler getHandlerForResponse() {
		return this.handlersByExtension.get(getRequestUriExtension());
	}

	protected String getRequestUriExtension() {
		ActionMapping mapping = (ActionMapping) ActionContext.getContext().get(ServletActionContext.ACTION_MAPPING);
		return mapping.getExtension();
	}

	protected MediaTypeHandler getHandlerForMediaType(MediaType mediaType) {
		MediaTypeHandler handler = null;
		for (MediaType key : this.handlersByMediaType.keySet()) {
			if (key.isCompatible(mediaType)) {
				handler = this.handlersByMediaType.get(key);
				break;
			}
		}
		return handler;
	}

}

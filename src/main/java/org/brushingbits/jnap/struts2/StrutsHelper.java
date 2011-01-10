/*
 * StrutsHelper.java created on 2010-04-01
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.RequestUtils;
import org.brushingbits.jnap.struts2.mapper.RestActionMapper;


/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public final class StrutsHelper {

	private static final String REQ_ATTR_SERVLET_PATH = "javax.servlet.include.servlet_path";

	/**
	 * This class contains only <code>static</code> methods and cannot be
	 * directly instantiated.
	 */
	private StrutsHelper() {}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	public static String getUriExtension(String uri) {
		// TODO validate extension (is mapped?)
		String extension = StringUtils.EMPTY;
		int index = uri.lastIndexOf('.');
		if (index != -1
				&& uri.indexOf('/', index) == -1
				&& (index < (uri.length() - 1) && !Character.isDigit(uri.charAt(index + 1)))) {
			extension = uri.substring(index + 1);
		}
		return extension;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(REQ_ATTR_SERVLET_PATH);
		if (uri != null) {
			return uri;
		}

		uri = RequestUtils.getServletPath(request);
		if (StringUtils.isNotBlank(uri)) {
			return uri;
		}

		uri = request.getRequestURI();
		return uri.substring(request.getContextPath().length());
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getHttpMethod(HttpServletRequest request) {
		String httpMethod = request.getMethod().toUpperCase();
		if (HttpMethod.POST.equals(httpMethod)) {
			final String httpMethodOverride = request.getHeader(RestActionMapper.X_HTTP_METHOD_OVERRIDE_HEADER);
			final String altHttpMethod = request.getParameter(RestActionMapper.HTTP_METHOD_PARAM_NAME);
			if (StringUtils.isNotBlank(httpMethodOverride)) {
				httpMethod = httpMethodOverride.trim().toUpperCase();
			} else if (StringUtils.isNotBlank(altHttpMethod)) {
				httpMethod = altHttpMethod.trim().toUpperCase();
			}
		}
		return httpMethod;
	}

}

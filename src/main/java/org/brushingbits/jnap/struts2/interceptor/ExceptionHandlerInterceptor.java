/*
 * ExceptionHandlerInterceptor.java created on 2011-01-07
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
package org.brushingbits.jnap.struts2.interceptor;

import org.brushingbits.jnap.struts2.MediaTypeManager;
import org.brushingbits.jnap.web.Response;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class ExceptionHandlerInterceptor extends ExceptionMappingInterceptor {

	protected MediaTypeManager mediaTypeManager;

	@Inject(required = true)
	public void setMediaTypeManager(MediaTypeManager mediaTypeManager) {
		this.mediaTypeManager = mediaTypeManager;
	}

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor#intercept(com.opensymphony.xwork2.ActionInvocation)
	 */
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		String result = super.intercept(invocation);
		Object topObject = invocation.getStack().peek();
		if (topObject != null && topObject instanceof ExceptionHolder) {
			result = mediaTypeManager.handle(Response.exception(result,
					((ExceptionHolder) topObject).getException()));
		}
		return result;
	}

}

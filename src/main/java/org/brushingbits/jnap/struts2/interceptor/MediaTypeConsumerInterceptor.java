/*
 * MediaTypeConsumerInterceptor.java created on 2010-04-08
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

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * @author Daniel Rochetti
 * @version 1.0
 */
public class MediaTypeConsumerInterceptor extends AbstractInterceptor {

	protected MediaTypeManager mediaTypeManager;

	@Inject(required = true)
	public void setMediaTypeManager(MediaTypeManager mediaTypeManager) {
		this.mediaTypeManager = mediaTypeManager;
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		return invocation.invoke();
	}

}

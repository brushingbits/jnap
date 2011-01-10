/*
 * InputResultInterceptor.java created on 2010-03-31
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

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class InputResultInterceptor extends AbstractInterceptor {

	public static final String INPUT_RESULT_NAME = "__jnap_last_result__";

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork2.interceptor.AbstractInterceptor#intercept(com.opensymphony.xwork2.ActionInvocation)
	 */
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		String resultName = invocation.invoke();
		if (StringUtils.isNotBlank(resultName)) {
			Map<String, Object> session = ActionContext.getContext().getSession();
			session.put(INPUT_RESULT_NAME, resultName);
		}
		return resultName;
	}

}

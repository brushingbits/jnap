/*
 * RestWorkflowInterceptor.java created on 2010-03-31
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.brushingbits.jnap.struts2.MediaTypeManager;
import org.brushingbits.jnap.struts2.Response;
import org.brushingbits.jnap.validation.ValidationBean;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.DefaultWorkflowInterceptor;
import com.opensymphony.xwork2.interceptor.ValidationWorkflowAware;
import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestWorkflowInterceptor extends DefaultWorkflowInterceptor {

	protected MediaTypeManager mediaTypeManager;

	@Inject(required = true)
	public void setMediaTypeManager(MediaTypeManager mediaTypeManager) {
		this.mediaTypeManager = mediaTypeManager;
	}

	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {
		
		Object action = invocation.getAction();
		if (action instanceof ValidationAware) {
			ValidationAware validationAware = (ValidationAware) action;
			if (validationAware.hasErrors()) {

				String resultName = findResultName(invocation, action);

				ValidationBean validationEntity = new ValidationBean(
						validationAware.getActionErrors(),
						validationAware.getActionMessages(),
						validationAware.getFieldErrors());
                
                Response response = Response.invalid(resultName, validationEntity);
                return mediaTypeManager.handle(response);
			}
		}
		return invocation.invoke();
	}

	/**
	 * @param invocation
	 * @param action
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected String findResultName(ActionInvocation invocation, Object action)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		String resultName = Action.INPUT;
		Map<String, Object> session = ActionContext.getContext().getSession();
		Method method = action.getClass().getMethod(invocation.getProxy().getMethod(),
				ArrayUtils.EMPTY_CLASS_ARRAY);
		if (action instanceof ValidationWorkflowAware) {
		    resultName = ((ValidationWorkflowAware) action).getInputResultName();
		} else if (method.isAnnotationPresent(InputConfig.class)) {
			InputConfig annotation = method.getAnnotation(InputConfig.class);
		    if (StringUtils.isNotBlank(annotation.methodName())) {
		        Method inputMethod = action.getClass().getMethod(annotation.methodName());
		        resultName = (String) inputMethod.invoke(action);
		    } else {
		        resultName = annotation.resultName();
		    }
		} else if (session.containsKey(InputResultInterceptor.INPUT_RESULT_NAME)) {
			resultName = (String) session.get(InputResultInterceptor.INPUT_RESULT_NAME);
		}
		return resultName;
	}

	
}

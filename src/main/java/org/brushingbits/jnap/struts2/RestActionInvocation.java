/*
 * RestActionInvocation.java created on 06/04/2010
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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.ArrayUtils;
import org.brushingbits.jnap.web.Response;

import com.opensymphony.xwork2.DefaultActionInvocation;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Daniel
 * 
 */
public class RestActionInvocation extends DefaultActionInvocation {

	protected MediaTypeManager mediaTypeManager;

	public RestActionInvocation(Map<String, Object> extraContext,
			boolean pushAction) {
		super(extraContext, pushAction);
	}

	@Inject(required = true)
	public void setMediaTypeManager(MediaTypeManager mediaTypeManager) {
		this.mediaTypeManager = mediaTypeManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.xwork2.DefaultActionInvocation#invokeAction(java.lang
	 * .Object, com.opensymphony.xwork2.config.entities.ActionConfig)
	 */
	@Override
	protected String invokeAction(Object action, ActionConfig actionConfig)
			throws Exception {
		String methodName = actionConfig.getMethodName();
		Object methodResult = null;
		try {
//			try {
				methodResult = MethodUtils.invokeMethod(action, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY);
//			} catch (NoSuchMethodException ex) {
//				if (unknownHandlerManager.hasUnknownHandlers()) {
//					try {
//						methodResult = unknownHandlerManager.handleUnknownMethod(action, methodName);
//					} catch (NoSuchMethodException nestedEx) {
//						throw ex;
//					}
//				} else {
//					throw ex;
//				}
//			}
			
			String resultCode = null;
			if (methodResult != null) {
				if (methodResult instanceof Result) {
					container.inject((Result) methodResult);
				} else {
					Response response = null;
					if (methodResult.getClass().equals(String.class)) {
						response = Response.ok((String) methodResult);
					} else if (methodResult instanceof Response) {
						response = (Response) methodResult;
					} else {
						// TODO should never happen, once the mapper is doing its job
					}
					
					Object target = action;
					if (response.getEntity() != null) {
						target = response.getEntity();
					} else if (action instanceof ModelDriven) {
						target = ((ModelDriven) action).getModel();
					}
					response.setEntity(target);
					resultCode = mediaTypeManager.handle(response);
				}
			}
			return resultCode;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("The " + methodName + "() is not defined in action " + getAction().getClass() + "");
		} catch (InvocationTargetException e) {
			Throwable sourceException = e.getTargetException();

			if (actionEventListener != null) {
				String result = actionEventListener.handleException(sourceException, getStack());
				if (result != null) {
					return result;
				}
			}
			if (sourceException instanceof Exception) {
				throw (Exception) sourceException;
			} else {
				throw e;
			}
		}
	}

}

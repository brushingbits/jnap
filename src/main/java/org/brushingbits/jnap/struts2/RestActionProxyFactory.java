/*
 * RestActionProxyFactory.java created on 08/04/2010
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

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.DefaultActionProxyFactory;

/**
 * @author Daniel
 * 
 */
public class RestActionProxyFactory extends DefaultActionProxyFactory {


	/**
	 * 
	 */
	@Override
	public ActionProxy createActionProxy(String namespace, String actionName,
			String methodName, Map<String, Object> extraContext,
			boolean executeResult, boolean cleanupContext) {
		ActionInvocation actionInvocation = new RestActionInvocation(
				extraContext, true);
		container.inject(actionInvocation);
		return createActionProxy(actionInvocation, namespace, actionName,
				methodName, executeResult, cleanupContext);
	}

}

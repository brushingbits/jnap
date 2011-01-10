/*
 * BeanMediaTypeHandler.java created on 2010-04-01
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
package org.brushingbits.jnap.struts2.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.StrutsConstants;
import org.brushingbits.jnap.struts2.Response;

import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public abstract class BeanMediaTypeHandler extends DefaultMediaTypeHandler {

	protected boolean devMode = false;

	@Inject(StrutsConstants.STRUTS_DEVMODE)
	public void setDevMode(String devMode) {
		this.devMode = Boolean.valueOf(devMode);
	}

	protected Object transformResponse(Response response) {
		Object obj = response.getEntity();
		if (StringUtils.isNotBlank(response.getWrap())) {
			obj = new HashMap<String, Object>();
			((Map<String, Object>) obj).put(response.getWrap(), response.getEntity());
		}
		return obj;
	}
}

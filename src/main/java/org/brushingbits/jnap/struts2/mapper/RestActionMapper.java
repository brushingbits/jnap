/*
 * RestActionMapper.java created on 2010-03-11
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
package org.brushingbits.jnap.struts2.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;
import org.brushingbits.jnap.JnapConstants;
import org.brushingbits.jnap.struts2.StrutsHelper;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.inject.Inject;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class RestActionMapper extends DefaultActionMapper {

	public static final String HTTP_METHOD_PARAM_NAME = "_http_method";

	public static final String X_HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";

	protected List<ActionConfigMapping> actionMappings;

	/**
	 * For true conventions and SEO urls, we're not considering dynamic method calls.
	 */
	@Override
	public void setAllowDynamicMethodCalls(String allow) {
		super.setAllowDynamicMethodCalls(Boolean.toString(false));
	}

	@Inject
	public RestActionMapper(@Inject Configuration config) {
		super();

		// Building the UriTemplate/ActionConfig map
		this.actionMappings = new LinkedList<ActionConfigMapping>();
		
		Set<String> pkgConfigNames = config.getPackageConfigNames();
		Map<String, ActionConfig> actionConfigs = null;
		for (String pkgName : pkgConfigNames) {
			PackageConfig pkgConfig = config.getPackageConfig(pkgName);
			actionConfigs = pkgConfig.getActionConfigs();
			for (ActionConfig actionConfig : actionConfigs.values()) {
				this.actionMappings.add(new ActionConfigMapping(actionConfig));
			}
		}
		
		// sort the templates according to the JAX-RS (JSR-311) specification
		Collections.sort(this.actionMappings);
	}

	@Override
	public ActionMapping getMapping(HttpServletRequest request,
			ConfigurationManager configManager) {

        ActionMapping mapping = new ActionMapping();
        String uri = StrutsHelper.getRequestUri(request);
        String extension = StrutsHelper.getUriExtension(uri);
        mapping.setExtension(extension);
        if (StringUtils.isNotBlank(extension)) {
        	uri = uri.substring(0, uri.lastIndexOf("." + extension));
        }

        if (uri != null) {
        	if (!uri.endsWith("/")) {
        		uri += "/";
        	}
        	final String httpMethod = StrutsHelper.getHttpMethod(request);
        	ActionConfigMapping matchedConfig = null;
        	Map<String, String> uriVariables = new HashMap<String, String>();
        	// TODO needs optimization before v1.0
        	// maybe some simple 'smart' namespace check before match the full URI
        	// ideas?
        	for (ActionConfigMapping mappedAction : this.actionMappings) {
        		if (mappedAction.getHttpMethod().equals(httpMethod)
        				&& mappedAction.getUriTemplate().match(uri, uriVariables)) {
        			matchedConfig = mappedAction;
        			break;
        		}
        	}

        	if (matchedConfig != null) {
        		mapping.setMethod(matchedConfig.getActionConfig().getMethodName());
        		mapping.setName(matchedConfig.getActionConfig().getName());
        		mapping.setNamespace(matchedConfig.getNamespace());
        		if (!uriVariables.isEmpty()) {
        			mapping.setParams(new HashMap<String, Object>(uriVariables));
        		}
        	} else {
        		mapping = null;
        	}

        } else {
        	mapping = null;
        }

        return mapping;
	}

	/**
	 * 
	 */
	class ActionConfigMapping implements Comparable<ActionConfigMapping> {

		private String httpMethod;
		private String namespace;
		private UriTemplate uriTemplate;
		private ActionConfig actionConfig;

		public ActionConfigMapping(ActionConfig actionConfig) {
			this.actionConfig = actionConfig;
			this.httpMethod = actionConfig.getParams().get(JnapConstants.ACTION_HTTP_METHOD);
			this.namespace = actionConfig.getParams().get(JnapConstants.ACTION_NAMESPACE);
			this.uriTemplate = new UriTemplate(actionConfig.getParams().get(JnapConstants.ACTION_ACTION_URI));
		}

		public String getHttpMethod() {
			return httpMethod;
		}

		public String getNamespace() {
			return namespace;
		}

		public UriTemplate getUriTemplate() {
			return uriTemplate;
		}

		public ActionConfig getActionConfig() {
			return actionConfig;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result	+ ((httpMethod == null) ? 0 : httpMethod.hashCode());
			result = prime * result	+ ((namespace == null) ? 0 : namespace.hashCode());
			result = prime * result	+ ((uriTemplate == null) ? 0 : uriTemplate.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ActionConfigMapping other = (ActionConfigMapping) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (httpMethod == null) {
				if (other.httpMethod != null) {
					return false;
				}
			} else if (!httpMethod.equals(other.httpMethod)) {
				return false;
			}
			if (namespace == null) {
				if (other.namespace != null) {
					return false;
				}
			} else if (!namespace.equals(other.namespace)) {
				return false;
			}
			if (uriTemplate == null) {
				if (other.uriTemplate != null) {
					return false;
				}
			} else if (!uriTemplate.equals(other.uriTemplate)) {
				return false;
			}
			return true;
		}

		public int compareTo(ActionConfigMapping other) {
			return UriTemplate.COMPARATOR.compare(getUriTemplate(), other.getUriTemplate());
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder(getClass().getName());
			str.append("{").append("httpMethod: ").append(this.httpMethod);
			str.append(", namespace: ").append(this.namespace);
			str.append(", uriTemplate: ").append(this.uriTemplate.toString());
			str.append(", ActionConfig.className: ").append(this.actionConfig.getClassName());
			str.append(", ActionConfig.methodName: ").append(this.actionConfig.getMethodName());
			str.append("}");
			return str.toString();
		}

		private RestActionMapper getOuterType() {
			return RestActionMapper.this;
		}

	}
}

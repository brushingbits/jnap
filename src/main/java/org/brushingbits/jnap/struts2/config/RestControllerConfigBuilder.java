/*
 * RestControllerConfigBuilder.java created on 10/03/2010
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
package org.brushingbits.jnap.struts2.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.StringTools;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ExceptionMappings;
import org.apache.struts2.convention.annotation.Namespace;
import org.brushingbits.jnap.JnapConstants;
import org.brushingbits.jnap.util.ReflectionUtils;
import org.brushingbits.jnap.web.Response;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * 
 * @author Daniel Rochetti
 *
 */
public class RestControllerConfigBuilder extends DefaultPackageBasedActionConfigBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RestControllerConfigBuilder.class);

	protected ApplicationContext applicationContext;

	@Inject
	public RestControllerConfigBuilder(
			Configuration configuration,
			Container container,
			ObjectFactory objectFactory,
			@Inject("struts.convention.redirect.to.slash") String redirectToSlash,
			@Inject("struts.convention.default.parent.package") String defaultParentPackage,
			@Inject ServletContext servletContext) {

		super(configuration, container, objectFactory, redirectToSlash, defaultParentPackage);
		setAlwaysMapExecute(Boolean.toString(false));

		// finding the web application context
		try {
			this.applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		} catch (IllegalStateException e) {
			// TODO
		}
		
	}

	/**
	 * TODO correct doc
	 * @param packageLocatorsBasePackage
	 *            (Optional) If set, only packages that start with this name
	 *            will be scanned for actions.
	 */
	@Inject(value = "struts.convention.package.locators.basePackage", required = true)
	public void setPackageLocatorsBase(String packageLocatorsBasePackage) {
		this.packageLocatorsBasePackage = packageLocatorsBasePackage;
	}

	@Override
	protected Set<Class> findActions() {
		Set<Class> classes = new HashSet<Class>();

		final ApplicationContext ac = this.applicationContext;
		String[] beanNames = ac.getBeanDefinitionNames();
		for (String beanName : beanNames) {

			// don't care for the bean itself right now, just it's class
			Class beanClass = ac.getType(beanName);

			// first of all, check for the @Controller annotation...
			// then, if it's inside the right package (base controllers package)
			if (beanClass.isAnnotationPresent(Controller.class)
					&& beanClass.getPackage().getName().startsWith(this.packageLocatorsBasePackage)) {
				// we must warn in case of a singleton scoped controller
				if (ac.isSingleton(beanName)) {
					LOG.warn(""); // TODO
				}
				classes.add(beanClass);
			}

		}
		return classes;
	}

	@Override
	protected void buildConfiguration(Set<Class> classes) {

		Map<String, PackageConfig.Builder> packageConfigs = new HashMap<String, PackageConfig.Builder>();

		for (Class controllerClass : classes) {

			// Determine the controller package
			String controllerPackage = controllerClass.getPackage().getName();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing class [#0] in package [#1]", controllerClass.getName(), controllerPackage);
			}
			
			String namespace = determineControllerNamespace(controllerClass);
			String actionName = determineActionName(controllerClass);
			
			PackageConfig.Builder defaultPackageConfig = getPackageConfig(
					packageConfigs, namespace, controllerPackage, controllerClass, null);
			Map<String, ActionMethodConfigBean> methods = findActionMethods(controllerClass);
			for (Iterator<String> iter = methods.keySet().iterator(); iter.hasNext();) {
				String methodName = iter.next();
				ActionMethodConfigBean method = methods.get(methodName);
				method.setNamespace(namespace);
				method.setActionName(actionName);
				createActionConfig(defaultPackageConfig, controllerClass, method);
			}
		}

		// Add the new actions to the configuration
		Set<String> packageNames = packageConfigs.keySet();
		for (String packageName : packageNames) {
			configuration.addPackageConfig(packageName, packageConfigs.get(packageName).build());
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.convention.PackageBasedActionConfigBuilder#determineActionName(java.lang.Class)
	 */
	@Override
	protected String determineActionName(Class<?> actionClass) {
		String actionName = StringUtils.EMPTY;
		if (actionClass.isAnnotationPresent(Path.class)) {
			actionName = actionClass.getAnnotation(Path.class).value().trim();
			// clean surrounding slashes
			actionName = actionName.replaceAll("^\\/|\\/$", StringUtils.EMPTY);
		} else {
			actionName = super.determineActionName(actionClass);
		}
		return actionName;
	}

	/**
	 * TODO
	 * 
	 * @param controllerClass The <code>Controller</code> class to search methods for.
	 * @return a map containing the method's name as the key and its configuration as the value.
	 * 
	 * @see org.apache.struts2.convention.annotation.Action
	 * @see javax.ws.rs.Path
	 * @see javax.ws.rs.HttpMethod
	 * @see javax.ws.rs.GET
	 * @see javax.ws.rs.POST
	 * @see javax.ws.rs.PUT
	 * @see javax.ws.rs.DELETE
	 * @see javax.ws.rs.Consumes
	 * @see javax.ws.rs.Produces
	 */
	protected Map<String, ActionMethodConfigBean> findActionMethods(Class<?> controllerClass) {
		Map<String, ActionMethodConfigBean> map = new HashMap<String, ActionMethodConfigBean>();
		Method[] methods = controllerClass.getMethods();
		for (Method method : methods) {

			// first of all, lets validate the candidate...
			Class<?> returnType = method.getReturnType();
			if (returnType == null
					|| (!returnType.equals(String.class) && !returnType.isAssignableFrom(Response.class))
					|| method.getParameterTypes().length > 0
					|| Modifier.isStatic(method.getModifiers())) {
				continue;
			}

			ActionMethodConfigBean methodConfig = new ActionMethodConfigBean();
			methodConfig.setName(method.getName());
			
			// Guessing http method
			String httpMethod = this.guessHttpMethod(method).value();
			methodConfig.setHttpMethod(httpMethod);

			// if a @Action annotation is found then keep it for results, interceptors and so to be configured properly
			final Action actionAnnotation = ReflectionUtils.getAnnotation(Action.class, method);
			if (actionAnnotation != null) {
				methodConfig.setAction(actionAnnotation);
				methodConfig.setUriTemplate(actionAnnotation.value().trim());
			}

			// if a @Path annotation is found then its value overwrites the @Action one (if found too)
			final Path pathAnnotation = ReflectionUtils.getAnnotation(Path.class, method);
			if (pathAnnotation != null) {
				methodConfig.setUriTemplate(pathAnnotation.value().trim());
			}

			// are there any restrinctions for response type
//			methodConfig.setProduces(method.getAnnotation(Produces.class));
			// TODO @Consumes

			// if no URI template found, then guess based on method's name
			if (StringUtils.isBlank(methodConfig.getUriTemplate())) {
				// TODO filter methods
				//methodConfig.setUriTemplate(actionNameBuilder.build(method.getName()));
			} else {
				map.put(method.getName(), methodConfig);
			}
			
//			map.put(method.getName(), methodConfig); TODO remove 'else' above after impl method filtering
		}
		return map;
	}

	/**
	 * @param method
	 * @return
	 */
	protected HttpMethod guessHttpMethod(Method method) {
		HttpMethod httpMethodAnnotation = ReflectionUtils.getAnnotation(HttpMethod.class, method);
		if (ReflectionUtils.getAnnotation(POST.class, method) != null) {
			httpMethodAnnotation = POST.class.getAnnotation(HttpMethod.class);
		} else if (ReflectionUtils.getAnnotation(PUT.class, method) != null) {
			httpMethodAnnotation = PUT.class.getAnnotation(HttpMethod.class);
		} else if (ReflectionUtils.getAnnotation(DELETE.class, method) != null) {
			httpMethodAnnotation = DELETE.class.getAnnotation(HttpMethod.class);
		} else {
			httpMethodAnnotation = GET.class.getAnnotation(HttpMethod.class);
		}
		return httpMethodAnnotation;
	}

	protected void createActionConfig(PackageConfig.Builder pkgCfg,
			Class<?> controllerClass, ActionMethodConfigBean methodConfig) {

		final String actionName = methodConfig.getActionName();
		final String fullActionName = methodConfig.getHttpMethod() + "," + methodConfig.getActionUri();
		// TODO validate duplicated ActionName
		ActionConfig.Builder actionConfig = new ActionConfig.Builder(pkgCfg.getName(),
				fullActionName, controllerClass.getName());
		actionConfig.methodName(methodConfig.getName());
		actionConfig.addParam(JnapConstants.ACTION_HTTP_METHOD, methodConfig.getHttpMethod());
		actionConfig.addParam(JnapConstants.ACTION_NAMESPACE, methodConfig.getNamespace());
		actionConfig.addParam(JnapConstants.ACTION_ACTION_URI, methodConfig.getActionUri());
		// TODO handle the @Produces annotation
		// TODO handle the @Consumes annotation

		// The code below is from the default config builder
		final Action annotation = methodConfig.getAction();
		// build interceptors
		List<InterceptorMapping> interceptors = interceptorMapBuilder.build(
				controllerClass, pkgCfg, actionName, annotation);
		actionConfig.addInterceptors(interceptors);

		// build results
		Map<String, ResultConfig> results = resultMapBuilder.build(controllerClass,
				annotation, actionName, pkgCfg.build());
		actionConfig.addResultConfigs(results);

		// add params
		if (annotation != null) {
			actionConfig.addParams(StringTools.createParameterMap(annotation.params()));
		}

		// add exception mappings from annotation
		if (annotation != null && annotation.exceptionMappings() != null) {
			actionConfig.addExceptionMappings(buildExceptionMappings(annotation.exceptionMappings(), actionName));
		}

		// add exception mapping from class
		ExceptionMappings exceptionMappings = controllerClass.getAnnotation(ExceptionMappings.class);
		if (exceptionMappings != null) {
			actionConfig.addExceptionMappings(buildExceptionMappings(exceptionMappings.value(), actionName));
		}

        // add
        pkgCfg.addActionConfig(fullActionName, actionConfig.build());
	}
	
	@Override
	protected List<String> determineActionNamespace(Class<?> actionClass) {
		List<String> namespace = new ArrayList<String>(1);
		namespace.add(determineControllerNamespace(actionClass));
		return namespace;
	}

	/**
	 * TODO
	 * 
	 * @param controllerClass
	 * @return
	 */
	protected String determineControllerNamespace(Class<?> controllerClass) {
		String controllerPkgName = controllerClass.getPackage().getName();
		String namespace = "";
		if (controllerClass.isAnnotationPresent(Namespace.class)) {
			namespace = controllerClass.getAnnotation(Namespace.class).value();
		} else if (controllerPkgName.length() > this.packageLocatorsBasePackage.length()) {
			namespace = controllerPkgName.substring(this.packageLocatorsBasePackage.length());
			namespace = namespace.replaceAll("\\.", "/");
		}
		return namespace;
	}

}

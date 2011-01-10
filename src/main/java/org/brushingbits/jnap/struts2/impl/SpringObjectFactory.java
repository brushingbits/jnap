package org.brushingbits.jnap.struts2.impl;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.struts2.StrutsConstants;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * 
 * @author Daniel Rochetti
 * 
 */
public class SpringObjectFactory extends
		com.opensymphony.xwork2.spring.SpringObjectFactory {

	private static final Logger LOG = LoggerFactory.getLogger(SpringObjectFactory.class);

	@Inject
	public SpringObjectFactory(
			@Inject(value = StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_AUTOWIRE, required = false) String autoWire,
			@Inject(value = StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_AUTOWIRE_ALWAYS_RESPECT, required = false) String alwaysAutoWire,
			@Inject(value = StrutsConstants.STRUTS_OBJECTFACTORY_SPRING_USE_CLASS_CACHE, required = false) String useClassCacheStr,
			@Inject ServletContext servletContext,
			@Inject(StrutsConstants.STRUTS_DEVMODE) String devMode,
			@Inject Container container) {
		super();
		boolean useClassCache = "true".equals(useClassCacheStr);
		LOG.info("Initializing Struts-Spring integration...");

		Object rootWebApplicationContext = servletContext
				.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

		if (rootWebApplicationContext instanceof RuntimeException) {
			RuntimeException runtimeException = (RuntimeException) rootWebApplicationContext;
			LOG.fatal(runtimeException.getMessage());
			return;
		}

		ApplicationContext appContext = (ApplicationContext) rootWebApplicationContext;
		if (appContext == null) {
			// uh oh! looks like the lifecycle listener wasn't installed. Let's inform the user
			String message = "********** FATAL ERROR STARTING UP STRUTS-SPRING INTEGRATION **********\n"
					+ "Looks like the Spring listener was not configured for your web app! \n"
					+ "Nothing will work until WebApplicationContextUtils returns a valid ApplicationContext.\n"
					+ "You might need to add the following to web.xml: \n"
					+ "    <listener>\n"
					+ "        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>\n"
					+ "    </listener>";
			LOG.fatal(message);
			return;
		}

		this.setApplicationContext(appContext);

		int type = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME; // default
		if ("name".equals(autoWire)) {
			type = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
		} else if ("type".equals(autoWire)) {
			type = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
		} else if ("constructor".equals(autoWire)) {
			type = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
		} else if ("no".equals(autoWire)) {
			type = AutowireCapableBeanFactory.AUTOWIRE_NO;
		}
		this.setAutowireStrategy(type);
		this.setUseClassCache(useClassCache);
		this.setAlwaysRespectAutowireStrategy("true".equalsIgnoreCase(alwaysAutoWire));

		LOG.info("... initialized Struts-Spring integration successfully");
	}

	/**
	 * @see com.opensymphony.xwork2.spring.SpringObjectFactory#buildBean(String, Map, boolean)
	 */
	@Override
	public Object buildBean(String beanName, Map<String, Object> extraContext,
			boolean injectInternal) throws Exception {

		Object bean = null;

		// if the bean's name is a valid class name then try to "guess" the bean
		// name using the default bean name generator strategy when using classpath
		// component scan (class annotated by @Component, and its subset:
		// @Controller, @Service and @Repository).
		if (isValidClass(beanName)) {
			String guessedBeanName = beanName.substring(beanName.lastIndexOf(".") + 1);
			guessedBeanName = Character.toLowerCase(guessedBeanName.charAt(0))
					+ guessedBeanName.substring(1);
			if (appContext.containsBean(guessedBeanName)) {
				bean = appContext.getBean(guessedBeanName);
			}
		}

		// if t he bean still null, then fallback to default lookup...
		if (bean == null) {
			bean = super.buildBean(beanName, extraContext, injectInternal);
		}

		return bean;
	}

	/**
	 * TODO
	 * 
	 * @param beanName
	 * @return
	 */
	protected boolean isValidClass(String beanName) {
		boolean valid = true;
		try {
			Class.forName(beanName);
		} catch (ClassNotFoundException e) {
			valid = false;
		}
		return valid;
	}

}

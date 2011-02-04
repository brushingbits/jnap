/*
 * BeanLocator.java created on 2010-01-15
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
package org.brushingbits.jnap.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
@Component
public final class BeanLocator implements ApplicationContextAware {

	private static BeanLocator instance = null;

	private ApplicationContext applicationContext;

	private BeanLocator() {
	}

	public static BeanLocator get() {
		if (instance == null) {
			instance = new BeanLocator();
		}
		return instance;
	}

	public boolean isInitialized() {
		return this.applicationContext != null;
	}

	/**
	 * It's a simple delegate to {@link ApplicationContext#getBean(String, Class)}.
	 */
	public <T> T getBean(String name, Class<T> requiredType) {
		return (T) applicationContext.getBean(name, requiredType);
	}

	/**
	 * It's a simple delegate to {@link ApplicationContext#getBean(String)}.
	 */
	public Object getBeanByName(String name) {
		return applicationContext.getBean(name);
	}

	public <T> T getBean(Class<T> requiredType) {
		String[] beanNames = applicationContext.getBeanNamesForType(requiredType);
		if (beanNames == null || beanNames.length == 0) {
			throw new NoSuchBeanDefinitionException(requiredType, "No bean of the required type was found.");
		}
		if (beanNames.length > 1) {
			throw new BeanDefinitionValidationException("There are more than " + beanNames.length
					+ "beans implementing the required type. "
					+ "Use 'getBeanByName(String)' or getBean(String, Class) instead.");
		}
		return (T) getBean(beanNames[0], requiredType);
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * 
	 * @param unmanagedBean
	 */
	public void injectBeans(Object unmanagedBean) {
		AutowireCapableBeanFactory beanFactory = this.applicationContext.getAutowireCapableBeanFactory();
		beanFactory.autowireBeanProperties(unmanagedBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
	}

}

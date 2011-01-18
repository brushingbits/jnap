/*
 * AbstractConstraintValidator.java created on 2011-01-15
 *
 * Copyright 2011 Brushing Bits, Inc.
 * http://www.brushingbits.com
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
package org.brushingbits.jnap.validation.constraints;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.validation.ConstraintValidator;

import org.apache.commons.lang.ArrayUtils;
import org.brushingbits.jnap.util.BeanLocator;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public abstract class AbstractConstraintValidator<A extends Annotation, T>
		implements ConstraintValidator<A, T> {

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	public void initialize(A constraintAnnotation) {
		bindParameters(constraintAnnotation);
		injectBeans();
		validateParameters();
	}

	/**
	 * This method copies all the annotation properties to fields with the same name.
	 * 
	 * @param constraintAnnotation This constraint annotation.
	 * @see PropertyAccessorFactory
	 */
	protected void bindParameters(A constraintAnnotation) {
		Class<?> annotationClass = constraintAnnotation.getClass();
		Method[] methods = annotationClass.getDeclaredMethods();
		ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(this);
		try {
			for (Method method : methods) {
				String methodName = method.getName();
				if (accessor.isWritableProperty(methodName)) {
					accessor.setPropertyValue(methodName, method.invoke(
							constraintAnnotation, ArrayUtils.EMPTY_OBJECT_ARRAY));
				}
			}
		} catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
		}
	}

	/**
	 * 
	 * @see BeanLocator#injectBeans(Object)
	 */
	private void injectBeans() {
		BeanLocator beanLocator = BeanLocator.get();
		if (beanLocator.isInitialized()) {
			beanLocator.injectBeans(this);
		}
	}

	/**
	 * Validates the parameters to assure everything is well set for the validation
	 */
	protected void validateParameters() {
		// do nothing
	}

}

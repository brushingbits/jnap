/*
 * ReflectionUtils.java created on 2010-03-29
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.struts2.convention.ReflectionTools;

/**
 * Utility methods for reflection.
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public final class ReflectionUtils {

	/**
	 * This class is a helper class, so it should not be instantiated.
	 */
	private ReflectionUtils() { }

	/**
	 * 
	 * @param clazz
	 * @param index
	 * @return
	 */
	public static Class<?> getParametrizedType(Class<?> clazz, int index) {
		ParameterizedType generic = (ParameterizedType) clazz.getGenericSuperclass();
		Type[] genericParams = generic.getActualTypeArguments();
		Class<?> genericParamType = null;
		if (genericParams != null && (index < genericParams.length)) {
			genericParamType = (Class<?>) genericParams[index];
		}
		return genericParamType;
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class<?> getParametrizedType(Class<?> clazz) {
		return getParametrizedType(clazz, 0);
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(); // TODO especific exception
		}
	}

	/**
	 * TODO
	 * 
	 * @param <A> must be an {@link Annotation}
	 * @param annotationClass Especific type.
	 * @param targetMethod
	 * @return
	 */
	public static <A extends Annotation> A getAnnotation(Class<A> annotationClass, Method targetMethod) {
		A annotation = targetMethod.getAnnotation(annotationClass);
		if (annotation == null) {
			// search in superclasses, maybe the method was overwritten
			List<Class<?>> hierarchy = ReflectionTools.getClassHierarchy(targetMethod.getDeclaringClass());
			for (int i = hierarchy.size() - 2; i > 0; i--) {
				Class<?> superClass = hierarchy.get(i);
				try {
					Method method = superClass.getMethod(targetMethod.getName(),
							targetMethod.getParameterTypes());
					if (method != null
							&& method.getReturnType().equals(targetMethod.getReturnType())
							&& method.isAnnotationPresent(annotationClass)) {
						annotation = method.getAnnotation(annotationClass);
						break;
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return annotation;
	}

	public static <A extends Annotation> List<A> extractAnnotations(Class<A> annotationType, Class<?> source) {
		// TODO
		return null;
	}

	/**
	 * TODO get methods sorted by class hierarchy
	 * @param targetClass
	 * @return
	 */
	public static List<Method> getPublicMethods(Class<?> targetClass) {
		List<Method> methods = new LinkedList<Method>();
		List<Class<?>> classHierarchy = ReflectionTools.getClassHierarchy(targetClass);
		for (Class<?> classs : classHierarchy) {
			Method[] declaredMethods = classs.getDeclaredMethods();
			for (Method method : declaredMethods) {
				if (Modifier.isPublic(method.getModifiers())) {
					methods.add(method);
				}
			}
		}
		return Collections.unmodifiableList(methods);
	}

}

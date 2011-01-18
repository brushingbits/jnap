/*
 * ResponseFilter.java created on 2010-04-09
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
package org.brushingbits.jnap.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseFilter {

	/**
	 * 
	 */
	public static enum FilterStyle {
		DEFAULT,
		SHALLOW,
		DEEP;
	}

	/**
	 * 
	 */
	public FilterStyle style() default FilterStyle.DEFAULT;

	/**
	 * 
	 */
	public String wrap();

	/**
	 * 
	 */
	public int depth() default -1;

	/**
	 * 
	 */
	public String[] excludeProperties() default {};

	/**
	 * 
	 */
	public String[] excludeTypes() default {};

	/**
	 * 
	 */
	public Class<?>[] excludeAssignableTypes() default {};


	/**
	 * 
	 */
	public boolean ignoreUninitializedProxies() default true;
}

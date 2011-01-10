/*
 * ValidationConfig.java created on 2010-03-26
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
package org.brushingbits.jnap.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.groups.Default;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidationConfig {

	/**
	 * Which groups should be considered during validation. Defaults to {@link Default}.
	 */
	Class<?>[] groups() default { Default.class };

	/**
	 * Indicates if the parameters that are not present in the request should be validated or not.
	 * <br />Example: if the parameter {@code "model.param"} is not present in the request, 
	 * but the property represented by {@code getModel().getParam()} is annotated with
	 * {@link javax.validation.constraints.NotNull} the validation will fail if set to
	 * {@code false} and ignored if set to {@code true}.
	 * <br />Defaults to {@code true}.
	 */
	boolean ignoreNonexistentParams() default true;

	/**
	 * Which parameters to exclude from validation, even if they exist and are in a included group
	 * (see {@link #groups()}).
	 * TODO doc expressions
	 */
	String[] excludes() default {};

	/**
	 * If set to {@code true} is the same as {@link org.apache.struts2.interceptor.validation.SkipValidation},
	 * else nothing changes and the validation will occur. Setting it explicitly to {@code false}
	 * (the default) has no effect other than explicit code documentation.
	 */
	boolean skip() default false;

}

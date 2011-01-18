/*
 * ExpressionAssertValidator.java created on 2010-05-01
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
package org.brushingbits.jnap.validation.constraints.impl;

import java.text.MessageFormat;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.brushingbits.jnap.validation.constraints.ExpressionAssert;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class ExpressionAssertValidator implements ConstraintValidator<ExpressionAssert, Object> {

	protected String expression;
	protected String objectAlias;

	public void initialize(ExpressionAssert constraintAnnotation) {
		this.expression = constraintAnnotation.value();
		this.objectAlias = constraintAnnotation.alias();
	}

	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return evaluateExpression(value);
	}

	protected Boolean evaluateExpression(Object object) {
		ActionContext ctx = ActionContext.getContext();
		ValueStack stack = ctx.getValueStack();

		stack.set(this.objectAlias, object);
		Object test = stack.findValue(this.expression);

		if (!(test instanceof Boolean)) {
			throw new ConstraintDeclarationException(MessageFormat.format(
					"The expression {0} must return a boolean value", this.expression));
		}
		return (Boolean) test;
	}


}

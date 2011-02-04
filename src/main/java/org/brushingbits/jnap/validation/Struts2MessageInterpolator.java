/*
 * Struts2MessageInterpolator.java created on 2011-01-12
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
package org.brushingbits.jnap.validation;

import java.util.Locale;
import java.util.Map;

import javax.validation.MessageInterpolator;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.validator.DelegatingValidatorContext;
import com.opensymphony.xwork2.validator.ValidatorContext;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class Struts2MessageInterpolator implements MessageInterpolator {

	protected ValidatorContext validatorContext;
	protected MessageInterpolator fallbackMessageInterpolator;

	public Struts2MessageInterpolator(Object action) {
		this.validatorContext = new DelegatingValidatorContext(action);
		this.fallbackMessageInterpolator = new ResourceBundleMessageInterpolator();
	}

	/* (non-Javadoc)
	 * @see javax.validation.MessageInterpolator#interpolate(java.lang.String, javax.validation.MessageInterpolator.Context)
	 */
	public String interpolate(String messageTemplate, Context context) {
		// add validation properties to the stack
		final ValueStack stack = ActionContext.getContext().getValueStack();
		final ConstraintDescriptor<?> constraintDescriptor = context.getConstraintDescriptor();
		for (Map.Entry<String, Object> attr : constraintDescriptor.getAttributes().entrySet()) {
			stack.set(attr.getKey(), attr.getValue());
		}
		final String strutsMsgKey = messageTemplate.replaceAll("^\\{|\\}$", StringUtils.EMPTY);
		String message = validatorContext.getText(strutsMsgKey);
		
		// if message is not present on Struts2 text provider, then fallback to default interpolator
		if (StringUtils.isBlank(message) || strutsMsgKey.equals(message)) {
			message = this.fallbackMessageInterpolator.interpolate(messageTemplate, context,
					validatorContext.getLocale());
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see javax.validation.MessageInterpolator#interpolate(java.lang.String, javax.validation.MessageInterpolator.Context, java.util.Locale)
	 */
	public String interpolate(String messageTemplate, Context context, Locale locale) {
		return interpolate(messageTemplate, context);
	}

}

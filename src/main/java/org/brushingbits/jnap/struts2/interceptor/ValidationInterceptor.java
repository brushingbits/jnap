/*
 * ValidationInterceptor.java created on 2010-03-31
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
package org.brushingbits.jnap.struts2.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import javax.ws.rs.HttpMethod;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.brushingbits.jnap.struts2.StrutsHelper;
import org.brushingbits.jnap.util.ReflectionUtils;
import org.brushingbits.jnap.validation.Struts2MessageInterpolator;
import org.brushingbits.jnap.validation.ValidationConfig;
import org.springframework.util.AntPathMatcher;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.Validateable;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.opensymphony.xwork2.interceptor.PrefixMethodInvocationUtil;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class ValidationInterceptor extends MethodFilterInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(ValidationInterceptor.class);

	private final static String VALIDATE_PREFIX = "validate";

	protected ValidatorFactory validatorFactory;
	protected AntPathMatcher pathMatcher;

	@Override
	public void init() {
		super.init();
		this.pathMatcher = new AntPathMatcher();
	}

	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {
		this.validatorFactory = Validation.buildDefaultValidatorFactory();
		doBeforeInvocation(invocation);
		return invocation.invoke();
	}

	/**
	 * 
	 * @param invocation
	 * @return
	 * @throws Exception
	 */
	protected void doBeforeInvocation(ActionInvocation invocation) throws Exception {
		Object action = invocation.getAction();
		Method method = action.getClass().getMethod(invocation.getProxy().getMethod(), new Class[] {});

		if (shouldValidate(method)) {
			
			// i18n text provider, if present will be checked for validation message
			TextProvider textProvider = null;
			if (action instanceof TextProvider) {
				textProvider = (TextProvider) action;
			}

			Class<?>[] groups = { Default.class };
			boolean ignoreNonexistentParams = true;
			
			ValidationConfig validationConfig = ReflectionUtils.getAnnotation(ValidationConfig.class, method);
			if (validationConfig != null) {
				groups = validationConfig.groups();
				ignoreNonexistentParams = validationConfig.ignoreNonexistentParams();
			}

			Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
			Map<String, Object> parameters = ActionContext.getContext().getParameters();
			
			// Build the validator TODO customize message and tranversable
			Validator validator = this.validatorFactory.usingContext().messageInterpolator(
					new Struts2MessageInterpolator(action)).getValidator();
			Set<ConstraintViolation<Object>> violations = validator.validate(action, groups);
			for (ConstraintViolation<Object> constraintViolation : violations) {
				String propertyName = constraintViolation.getPropertyPath().toString();
				if (parameters.containsKey(propertyName) || !ignoreNonexistentParams) {
					// TODO test exclude patterns (AntPathMatcher)
					List<String> messages = fieldErrors.get(propertyName);
					if (messages == null) {
						messages = new ArrayList<String>();
					}
					messages.add(getValidationMessage(invocation, textProvider, constraintViolation));
//					if (constraintViolation.getConstraintDescriptor().) {
//						
//					} TODO NonFieldConstraint and MultiFieldConstraint checking
					fieldErrors.put(propertyName, messages);
				}
			}
			
			if (fieldErrors.size() > 0) {
				if (action instanceof ValidationAware) {
					ValidationAware validationAwareAction = (ValidationAware) action;
					validationAwareAction.setFieldErrors(fieldErrors);
				} else {
					// TODO log
					LOG.warn("");
				}
			}

			if (action instanceof Validateable) {

				Exception exception = null;
				try {
					PrefixMethodInvocationUtil.invokePrefixMethod(invocation, new String[] { VALIDATE_PREFIX });
				} catch (Exception e) {
					LOG.warn("an exception occured while executing the prefix method", e); // TODO msg
					exception = e;
				}

				Validateable validateableAction = (Validateable) action;
				validateableAction.validate();

				if (exception != null) {
					throw exception;
				}
			}

		}
	}

	private boolean shouldValidate(Method method) {
		boolean isGetHttpMethod = HttpMethod.GET.equalsIgnoreCase(
				StrutsHelper.getHttpMethod(ServletActionContext.getRequest()));
		boolean noSkipValidation = ReflectionUtils.getAnnotation(SkipValidation.class, method) == null;
		boolean hasValidationConfig = ReflectionUtils.getAnnotation(ValidationConfig.class, method) != null;
		return noSkipValidation && (!isGetHttpMethod || hasValidationConfig);
	}

	/**
	 * @param invocation
	 * @param textProvider
	 * @param constraintViolation
	 * @return
	 */
	protected String getValidationMessage(ActionInvocation invocation,
			TextProvider textProvider, ConstraintViolation<Object> constraintViolation) {
		String message = constraintViolation.getMessage();

//		// test if the message is present on Struts I18N too
//		final String templateMessage = constraintViolation.getMessageTemplate();
//		String strutsMessage = StringUtils.EMPTY;
//		if (textProvider != null) {
//			strutsMessage = textProvider.getText(templateMessage, templateMessage);
//		} else {
//			strutsMessage = TextProviderHelper.getText(templateMessage, templateMessage, invocation.getStack());
//		}
//		
//		// if a message if found on Struts I18N, then overwrite
//		if (!templateMessage.equals(strutsMessage)) {
//			message = strutsMessage;
//		}
		return message;
	}

}

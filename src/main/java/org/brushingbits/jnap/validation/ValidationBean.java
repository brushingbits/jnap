/*
 * ValidationBean.java created on 2010-04-09
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.ValidationAware;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
@XStreamAlias("validation")
public class ValidationBean {

	private String exceptionMsg;
	private String exceptionType;
	private List<String> errors;
	private List<String> messages;
	private List<FieldError> fieldErrors;

	/**
	 * Default constructor. All properties will be initialized to empty collections.
	 */
	public ValidationBean() {
		this.errors = new ArrayList<String>();
		this.messages = new ArrayList<String>();
		this.fieldErrors = new ArrayList<FieldError>();
	}

	/**
	 * Constructor that will make easier to create the {@code ValidationBean}
	 * with {@link ValidationAware} data.
	 * 
	 * @param errors
	 * @param messages
	 * @param fieldErrors
	 */
	public ValidationBean(Collection<String> errors, Collection<String> messages,
			Map<String, List<String>> fieldErrors) {
		this();
		this.errors.addAll(errors);
		this.messages.addAll(messages);
		if (fieldErrors != null) {
			for (String fieldName : fieldErrors.keySet()) {
				this.fieldErrors.add(new FieldError(fieldName, fieldErrors.get(fieldName)));
			}
		}
	}

	public ValidationBean(Exception ex) {
		this();
		this.setException(ex);
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>errors</code>.
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>errors</code>.
	 */
	public void setErrors(List<String> actionErrors) {
		this.errors = actionErrors;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>messages</code>.
	 */
	public List<String> getMessages() {
		return messages;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>messages</code>.
	 */
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>fieldErrors</code>.
	 */
	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}

	/**
	 * <code>Mutator</code> ("setter") method for property <code>fieldErrors</code>.
	 */
	public void setFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	public void addError(String errorMsg) {
		this.errors.add(errorMsg);
	}

	public String getError() {
		return getErrors().isEmpty() ? null : getErrors().get(0);
	}

	public void setException(Throwable t) {
		this.exceptionMsg = t.getMessage();
		this.exceptionType = t.getClass().getName();
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>exceptionMsg</code>.
	 */
	public String getExceptionMsg() {
		return exceptionMsg;
	}

	/**
	 * <code>Accessor</code> ("getter") method for property <code>exceptionType</code>.
	 */
	public String getExceptionType() {
		return exceptionType;
	}

	/**
	 * 
	 */
	@XStreamAlias("fieldError")
	public static class FieldError {
		
		private String fieldName;
		private List<String> messages;

		public FieldError(String fieldName, List<String> messages) {
			this.fieldName = fieldName;
			this.messages = messages;
		}

		public FieldError(String fieldName, String message) {
			this.fieldName = fieldName;
			this.messages = new ArrayList<String>();
			this.messages.add(message);
		}

		/**
		 * <code>Accessor</code> ("getter") method for property <code>fieldName</code>.
		 */
		public String getFieldName() {
			return fieldName;
		}

		/**
		 * <code>Mutator</code> ("setter") method for property <code>fieldName</code>.
		 */
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		/**
		 * <code>Accessor</code> ("getter") method for property <code>messages</code>.
		 */
		public List<String> getMessages() {
			return messages;
		}

		/**
		 * <code>Mutator</code> ("setter") method for property <code>messages</code>.
		 */
		public void setMessages(List<String> messages) {
			this.messages = messages;
		}

	}

}

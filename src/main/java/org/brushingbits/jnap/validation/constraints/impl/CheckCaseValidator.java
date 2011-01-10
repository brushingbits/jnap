/*
 * CheckCaseValidator.java created on 2010-03-28
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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.brushingbits.jnap.validation.constraints.CheckCase;
import org.brushingbits.jnap.validation.constraints.CheckCase.CaseMode;

/**
 * TODO docme
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class CheckCaseValidator implements ConstraintValidator<CheckCase, String> {
	
	private CaseMode caseMode;

	public void initialize(CheckCase constraintAnnotation) {
		this.caseMode = constraintAnnotation.value();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean valid = false;
		if (value == null) {
			valid = true;
		} else {
			valid = CaseMode.LOWER.equals(caseMode)
				? value.equals(value.toLowerCase())
				: value.equals(value.toUpperCase());
		}
		return valid;
	}

}

/*
 * EmailFactory.java created on 2010-10-16
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
package org.brushingbits.jnap.email;

import java.text.MessageFormat;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mail.MailPreparationException;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class EmailFactory implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private String idPrefix = "";

	/**
	 * 
	 * @param <E>
	 * @param id
	 * @param type
	 * @return
	 */
	public <E extends Email> E getEmail(String id, Class<E> type) {
		E email = null;
		final String emailId = getIdPrefix() + id;
		try {
			email = applicationContext.getBean(emailId, type);
		} catch (NoSuchBeanDefinitionException e) {
			throw new MailPreparationException(MessageFormat.format(
					"The e-mail with id '{0}' could not be found!", emailId), e);
		}
		return email;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Email getEmail(String id) {
		return getEmail(id, Email.class);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public TemplateEmail getTemplateEmail(String id) {
		return getEmail(id, TemplateEmail.class);
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

}

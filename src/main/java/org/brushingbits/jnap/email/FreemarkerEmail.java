/*
 * FreemarkerEmail.java created on 2010-10-16
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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.mail.MailPreparationException;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class FreemarkerEmail extends TemplateEmail {

	// Freemarker
	private Configuration freemarkerConfiguration;

	/**
	 * Freemarker implementation of {@link TemplateEmail#processTemplate()}
	 */
	@Override
	protected void processTemplate() {
		try {
			// Email content
			StringBuilder htmlMsg = new StringBuilder();

			// Header
			if (StringUtils.isNotBlank(getHeaderTemplateName())) {
				Template header = getTemplate(getHeaderTemplateName());
				htmlMsg.append(FreeMarkerTemplateUtils.processTemplateIntoString(header, values));
			}

			// Body
			Template body = getTemplate(getTemplateName());
			String bodyContent = FreeMarkerTemplateUtils.processTemplateIntoString(body, values);
			htmlMsg.append(bodyContent);
			if (isMixedContent()) {
				setText(extractTextFromHtml(bodyContent));
			}

			// Footer
			if (StringUtils.isNotBlank(getFooterTemplateName())) {
				Template footer = getTemplate(getFooterTemplateName());
				htmlMsg.append(FreeMarkerTemplateUtils.processTemplateIntoString(footer, values));
			}

			// Setting the content
			if (isMixedContent()) {
				setHtmlText(htmlMsg.toString());
			} else {
				setText(htmlMsg.toString());
			}
		} catch (IOException e) {
			throw new MailPreparationException("Error trying to open template file.", e);
		} catch (TemplateException e) {
			throw new MailPreparationException("Error loading/processing template file.", e);
		}
	}

	protected Template getTemplate(String name) {
		try {
			return freemarkerConfiguration.getTemplate(name, getLocale());
		} catch (IOException e) {
			throw new MailPreparationException("Error loading email template: " + name, e);
		}
	}
	
	public void setFreemarkerConfiguration(Configuration freemarkerConfiguration) {
		this.freemarkerConfiguration = freemarkerConfiguration;
	}

}

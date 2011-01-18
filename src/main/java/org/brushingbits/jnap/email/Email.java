/*
 * Email.java created on 2011-01-16
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

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.brushingbits.jnap.i18n.I18nTextProvider;
import org.brushingbits.jnap.i18n.struts2.I18nTextProviderImpl;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class Email extends SimpleMailMessage implements MimeMessagePreparator {

	private String htmlText;
	private boolean mixedContent;
	private EmailAccountInfo accountInfo;
	private Map<String, Resource> inlineResources;
	private Map<String, Resource> attachments;
	private I18nTextProvider i18nTextProvider;
	private Locale locale;

	public Email() {
		this.i18nTextProvider = new I18nTextProviderImpl();
		this.locale = this.i18nTextProvider.getLocale();
		this.mixedContent = false;
	}

	public void addTo(String to) {
		setTo((String[]) ArrayUtils.add(getTo(), to));
	}

	public void addCc(String cc) {
		setCc((String[]) ArrayUtils.add(getCc(), cc));
	}

	public void addBcc(String bcc) {
		setBcc((String[]) ArrayUtils.add(getBcc(), bcc));
	}

	public void prepare(MimeMessage mimeMessage) throws Exception {
		boolean multipart = StringUtils.isNotBlank(getHtmlText())
				|| (getInlineResources() != null && getInlineResources().size() > 0)
				|| (getAttachments() != null && getAttachments().size() > 0);
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, multipart);
		helper.setTo(getTo());
		if (getCc() != null) {
			helper.setCc(getCc());
		}
		if (getBcc() != null) {
			helper.setBcc(getBcc());
		}
		helper.setSentDate(new Date());
		helper.setSubject(i18nTextProvider.getText(getSubject()));
		
		final boolean hasHtmlText = StringUtils.isNotBlank(getHtmlText());
		final boolean hasText = StringUtils.isNotBlank(getText());
		if (!hasText && !hasHtmlText) {
			// TODO msg
			throw new MailPreparationException("");
		}
		if (hasHtmlText && hasText) {
			helper.setText(getText(), getHtmlText());
		} else {
			helper.setText(hasHtmlText ? getHtmlText() : getText());
		}

		// add inline resources
		final Map<String, Resource> inlineRes = this.getInlineResources();
		if (inlineRes != null) {
			for (String cid : inlineRes.keySet()) {
				helper.addInline(cid, inlineRes.get(cid));
			}
		}
		// add attachments
		final Map<String, Resource> attachments = this.getAttachments();
		if (attachments != null) {
			for (String attachmentName : attachments.keySet()) {
				helper.addAttachment(attachmentName, attachments.get(attachmentName));
			}
		}
	}

	/**
	 * Simple RegEx replace used to remove all HTML tags.
	 * 
	 * @param html Text with HTML content.
	 * @return the plain text, without the HTML tags.
	 */
	protected String extractTextFromHtml(String html) {
		return html.replaceAll("\\<.*?>", StringUtils.EMPTY);
	}

	public EmailAccountInfo getAccountInfo() {
		return accountInfo;
	}
	
	public void setAccountInfo(EmailAccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}

	public void setI18nTextProvider(I18nTextProvider i18nTextProvider) {
		this.i18nTextProvider = i18nTextProvider;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getHtmlText() {
		return htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	public Map<String, Resource> getInlineResources() {
		return inlineResources;
	}

	public void setInlineResources(Map<String, Resource> inlineItems) {
		this.inlineResources = inlineItems;
	}

	public Map<String, Resource> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Resource> attachments) {
		this.attachments = attachments;
	}

	public boolean isMixedContent() {
		return mixedContent;
	}

	public void setMixedContent(boolean mixedContent) {
		this.mixedContent = mixedContent;
	}

}

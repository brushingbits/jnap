/*
 * EmailSender.java created on 2010-10-16
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class EmailSender {

	private static Log logger = LogFactory.getLog(EmailSender.class);

	private static final int DEFAULT_NUM_RETRIES = 10;

	private JavaMailSenderImpl javaMailSender;
	private EmailAccountInfo defaultEmailAccount;
	private int numberOfRetries = DEFAULT_NUM_RETRIES;
	private List<Email> toSendList;
	private Map<Email, Integer> toRetryList;

	public EmailSender() {
		this(null);
	}

	public EmailSender(EmailAccountInfo defaultEmailAccount) {
		this.toSendList = Collections.synchronizedList(new ArrayList<Email>());
		this.toRetryList = new HashMap<Email, Integer>();
		this.defaultEmailAccount = defaultEmailAccount;
		this.javaMailSender = new JavaMailSenderImpl();
	}

	public void send(Email email) {
		EmailAccountInfo accountInfo = defaultEmailAccount;
		if (email.getAccountInfo() != null) {
			accountInfo = email.getAccountInfo();
		}
		this.javaMailSender.setJavaMailProperties(accountInfo.getJavaMailProperties());
		if (accountInfo.getUsername() != null) {
			this.javaMailSender.setUsername(accountInfo.getUsername());
			this.javaMailSender.setPassword(accountInfo.getPassword());
		}
		this.javaMailSender.send((MimeMessagePreparator) email);
	}

	public void sendAsync(Email email) {
		synchronized (toSendList) {
			toSendList.add(email);
		}
	}

	/**
	 * 
	 */
	@Scheduled(fixedDelay = 30000)
	public void dispatchMessages() {
		List<Email> emails = new ArrayList<Email>();
		synchronized (toSendList) {
			if (toSendList.isEmpty()) {
				return;
			}
			emails.addAll(toSendList);
			toSendList.clear();
			emails.addAll(toRetryList.keySet());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Preparing to send " + emails.size() + " emails.");
		}
		for (Email email : emails) {
			try {
				this.send(email);
				toRetryList.remove(email);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				Integer retries = toRetryList.get(email);
				retries = retries == null ? 0 : retries;
				if (retries < numberOfRetries) {
					toRetryList.put(email, ++retries);
				} else {
					logger.info("Email discarded. Couldn't send after " + numberOfRetries + " retries.");
					toRetryList.remove(email);
				}
			}
		}
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	public EmailAccountInfo getDefaultEmailAccount() {
		return defaultEmailAccount;
	}

	public void setDefaultEmailAccount(EmailAccountInfo defaultEmailAccount) {
		this.defaultEmailAccount = defaultEmailAccount;
	}

}

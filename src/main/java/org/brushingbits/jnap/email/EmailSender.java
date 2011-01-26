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
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


/**
 * @author Daniel Rochetti
 * @since 1.0
 */
@Component
public class EmailSender implements Runnable {

	private static Log logger = LogFactory.getLog(EmailSender.class);

	private static final int DEFAULT_NUM_RETRIES = 10;

	private JavaMailSenderImpl defaultMailSender;
	private Map<EmailAccountInfo, JavaMailSenderImpl> mailSenderMap; 
	private List<Email> toSendList;
	private Map<Email, Integer> toRetryList;
	private ThreadPoolTaskScheduler taskScheduler;
	private ScheduledFuture scheduledAsyncSender;

	private EmailAccountInfo defaultEmailAccount;
	private int numberOfRetries = DEFAULT_NUM_RETRIES;
	private long asyncSendInterval = 30000;
	private int asyncThreadPoolSize = 4;

	public EmailSender() {
		this(null);
	}

	public EmailSender(EmailAccountInfo defaultEmailAccount) {
		this.toSendList = Collections.synchronizedList(new ArrayList<Email>());
		this.toRetryList = new HashMap<Email, Integer>();
		this.defaultEmailAccount = defaultEmailAccount;
		this.defaultMailSender = new JavaMailSenderImpl();
		this.mailSenderMap = Collections.synchronizedMap(new HashMap<EmailAccountInfo, JavaMailSenderImpl>());
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.defaultEmailAccount, "You must provide a default email account configuration.");
		this.defaultMailSender.setJavaMailProperties(this.defaultEmailAccount.getJavaMailProperties());
		if (StringUtils.isNotBlank(this.defaultEmailAccount.getUsername())) {
			this.defaultMailSender.setUsername(this.defaultEmailAccount.getUsername());
			this.defaultMailSender.setPassword(this.defaultEmailAccount.getPassword());
		}
		this.taskScheduler = new ThreadPoolTaskScheduler();
		this.taskScheduler.setPoolSize(this.asyncThreadPoolSize);
		this.taskScheduler.initialize();
		this.scheduledAsyncSender = this.taskScheduler.scheduleWithFixedDelay(this, this.asyncSendInterval);
	}

	@PreDestroy
	public void destroy() throws Exception {
		this.scheduledAsyncSender.cancel(false);
		this.taskScheduler.destroy();
	}

	public void send(Email email) {
		EmailAccountInfo accountInfo = defaultEmailAccount;
		JavaMailSenderImpl sender = this.defaultMailSender;
		if (email.getAccountInfo() != null) {
			accountInfo = email.getAccountInfo();
			synchronized (this.mailSenderMap) {
				sender = this.mailSenderMap.get(accountInfo);
				if (sender == null) {
					sender = new JavaMailSenderImpl();
					Properties props = new Properties(this.defaultEmailAccount.getJavaMailProperties());
					props.putAll(accountInfo.getJavaMailProperties());
					sender.setJavaMailProperties(props);
					sender.setUsername(accountInfo.getUsername());
					sender.setPassword(accountInfo.getPassword());
					this.mailSenderMap.put(accountInfo, sender);
				}
			}
		}
		sender.send((MimeMessagePreparator) email);
	}

	public void sendAsync(Email email) {
		synchronized (toSendList) {
			toSendList.add(email);
		}
	}

	/**
	 * 
	 */
	public void run() {
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

	public long getAsyncSendInterval() {
		return asyncSendInterval;
	}

	public void setAsyncSendInterval(long asyncSendInterval) {
		this.asyncSendInterval = asyncSendInterval;
	}

	public EmailAccountInfo getDefaultEmailAccount() {
		return defaultEmailAccount;
	}

	public void setDefaultEmailAccount(EmailAccountInfo defaultEmailAccount) {
		this.defaultEmailAccount = defaultEmailAccount;
	}

}

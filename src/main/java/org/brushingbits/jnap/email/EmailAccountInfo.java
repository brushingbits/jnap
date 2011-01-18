/*
 * EmailAccountInfo.java created on 2011-01-16
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

import java.util.Properties;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class EmailAccountInfo {

	public static final String SMTP = "smtp";

	public static final String SMTPS = "smtps";

	private String username;
	private String password;
	private String hostName;
	private Integer port;
	private String replyToEmailAddress;
	private String replyToName;
	private String fromEmailAddress;
	private String fromName;
	private String protocol = SMTP;
	private boolean startTls = true;

	private Properties javaMailProperties;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer smtpPort) {
		this.port = smtpPort;
	}

	public String getReplyToEmailAddress() {
		return replyToEmailAddress;
	}

	public void setReplyToEmailAddress(String replyToEmailAddress) {
		this.replyToEmailAddress = replyToEmailAddress;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	public String getFromEmailAddress() {
		return fromEmailAddress;
	}

	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean isStartTls() {
		return startTls;
	}

	public void setStartTls(boolean startTls) {
		this.startTls = startTls;
	}

	/**
	 * 
	 * @return
	 */
	public Properties getJavaMailProperties() {
		if (this.javaMailProperties == null) {
			Properties props = new Properties();
			props.put("mail.transport.protocol", getProtocol());
			props.put("mail.smtp.from", getFromEmailAddress());
			props.put("mail.smtp.host", getHostName());
			if (getPort() != null) {
				props.put("mail.smtp.port", getPort());
			}
			if (getUsername() != null) {
				props.put("mail.smtp.auth", Boolean.TRUE.toString());
			}
			props.put("mail.smtp.starttls.enable", Boolean.toString(isStartTls()));

			this.javaMailProperties = props;
		}
		return this.javaMailProperties;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}

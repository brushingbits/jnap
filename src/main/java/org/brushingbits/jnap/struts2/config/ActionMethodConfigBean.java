package org.brushingbits.jnap.struts2.config;

import org.apache.struts2.convention.annotation.Action;

/**
 * 
 * @author Daniel Rochetti
 * 
 */
class ActionMethodConfigBean {

	private String httpMethod;
	private Action action;
	private String uriTemplate;
//	private Produces produces;
//	private Consumes consumes; 
	private String name;
	private String namespace;
	private String actionName;


	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	/**
	 * 
	 * @return
	 */
	public String getActionUri() {
		final Character slashChar = '/';
		final String slash = Character.toString(slashChar);

		StringBuilder actionUri = new StringBuilder();
		actionUri.append(getNamespace()).append(slash);
		actionUri.append(getActionName()).append(slash);
		actionUri.append(getUriTemplate()).append(slash);

		// wrap in '/' if necessary
		if (!(actionUri.charAt(0) == slashChar)) {
			actionUri.insert(0, slash);
		}

		// normalize duplicated slashes
		return actionUri.toString().replaceAll("\\/{2,}", slash);
	}

}
package org.brushingbits.jnap.struts2.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.struts2.convention.ActionNameBuilder;
import org.apache.struts2.convention.ConventionConstants;
import org.apache.struts2.convention.InterceptorMapBuilder;
import org.apache.struts2.convention.PackageBasedActionConfigBuilder;
import org.apache.struts2.convention.ResultMapBuilder;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.classloader.ReloadingClassLoader;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public class DefaultPackageBasedActionConfigBuilder extends
		PackageBasedActionConfigBuilder {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultPackageBasedActionConfigBuilder.class);

	protected static final String DEFAULT_METHOD = "execute";

	protected final Configuration configuration;
	protected final ActionNameBuilder actionNameBuilder;
	protected final ResultMapBuilder resultMapBuilder;
	protected final InterceptorMapBuilder interceptorMapBuilder;
	protected final ObjectFactory objectFactory;
	protected final String defaultParentPackage;
	protected final boolean redirectToSlash;
	protected String[] actionPackages;
	protected String[] excludePackages;
	protected String[] packageLocators;
	protected String[] includeJars;
	protected String packageLocatorsBasePackage;
	protected boolean disableActionScanning = false;
	protected boolean disablePackageLocatorsScanning = false;
	protected String actionSuffix = "Action";
	protected boolean checkImplementsAction = true;
	protected boolean mapAllMatches = false;
	protected Set<String> loadedFileUrls = new HashSet<String>();
	protected boolean devMode;
	protected ReloadingClassLoader reloadingClassLoader;
	protected boolean reload;
	protected Set<String> fileProtocols;
	protected boolean alwaysMapExecute;
	protected boolean excludeParentClassLoader;

	@Inject
	public DefaultPackageBasedActionConfigBuilder(
			Configuration configuration,
			Container container,
			ObjectFactory objectFactory,
			@Inject("struts.convention.redirect.to.slash") String redirectToSlash,
			@Inject("struts.convention.default.parent.package") String defaultParentPackage) {
		
		super(configuration, container, objectFactory, redirectToSlash, defaultParentPackage);

		// Validate that the parameters are okay
		this.configuration = configuration;
		this.actionNameBuilder = container.getInstance(ActionNameBuilder.class,
				container.getInstance(String.class,	ConventionConstants.CONVENTION_ACTION_NAME_BUILDER));
		this.resultMapBuilder = container.getInstance(ResultMapBuilder.class,
				container.getInstance(String.class,	ConventionConstants.CONVENTION_RESULT_MAP_BUILDER));
		this.interceptorMapBuilder = container.getInstance(InterceptorMapBuilder.class,
						container.getInstance(String.class, ConventionConstants.CONVENTION_INTERCEPTOR_MAP_BUILDER));
		this.objectFactory = objectFactory;
		this.redirectToSlash = Boolean.parseBoolean(redirectToSlash);

		if (LOG.isTraceEnabled()) {
			LOG.trace("Setting action default parent package to [#0]", defaultParentPackage);
		}

		this.defaultParentPackage = defaultParentPackage;
	}

}

/*
 * DefaultMediaTypeHandler.java created on 01/04/2010
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
package org.brushingbits.jnap.struts2.handler;

import java.io.Reader;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.brushingbits.jnap.struts2.MediaTypeHandler;
import org.brushingbits.jnap.struts2.Response;


/**
 * TODO
 * 
 * @author Daniel Rochetti
 * @since 1.0
 */
public class DefaultMediaTypeHandler implements MediaTypeHandler {

	private static final String[] EXTENSIONS = { StringUtils.EMPTY, "xhtml", "html" };

	private static final MediaType[] MEDIA_TYPES = {
		MediaType.APPLICATION_FORM_URLENCODED_TYPE,
		MediaType.MULTIPART_FORM_DATA_TYPE,
		MediaType.TEXT_HTML_TYPE,
		MediaType.APPLICATION_XHTML_XML_TYPE
	};

	public String[] getExtensions() {
		return EXTENSIONS;
	}

	public MediaType[] getSupportedMediaTypes() {
		return MEDIA_TYPES;
	}

	public void read(Object obj, Reader reader) {
	}

	public String write(Response response, Writer writer) {
		return response.getResultName();
	}

	public MediaType getDefaultMediaType() {
		final MediaType[] supportedMediaTypes = getSupportedMediaTypes();
		return supportedMediaTypes != null && supportedMediaTypes.length > 0
				? supportedMediaTypes[0] : null;
	}

}

/*
 * XmlMediaTypeHandler.java created on 2010-04-01
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

import org.brushingbits.jnap.struts2.Response;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

/**
 * @author Daniel Rochetti
 * @since 1.0
 */
public class XmlMediaTypeHandler extends BeanMediaTypeHandler {

	private static final String[] EXTENSIONS = { "xml" };

	private static final MediaType[] MEDIA_TYPES = {
		MediaType.TEXT_XML_TYPE,
		MediaType.APPLICATION_XML_TYPE
	};

	@Override
	public String[] getExtensions() {
		return EXTENSIONS;
	}

	@Override
	public MediaType[] getSupportedMediaTypes() {
		return MEDIA_TYPES;
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.struts2.dispatcher.handler.DefaultMediaTypeHandler#read(java.lang.Object, java.io.Reader)
	 */
	@Override
	public void read(Object obj, Reader reader) {
		// TODO Auto-generated method stub
		super.read(obj, reader);
	}

	/* (non-Javadoc)
	 * @see org.brushingbits.jnap.struts2.dispatcher.handler.DefaultMediaTypeHandler#write(org.brushingbits.jnap.struts2.Response, java.lang.Object, java.io.Writer)
	 */
	@Override
	public String write(Response response, Writer writer) {
		if (response.getEntity() != null) {
			XStream xstream = new XStream();
//			xstream.autodetectAnnotations(true); // TODO should we use this or register only the framework classes? I'm concerned about performance...
			HierarchicalStreamWriter xmlWriter = null;
			if (devMode) {
				xmlWriter = new PrettyPrintWriter(writer);
			} else {
				xmlWriter = new CompactWriter(writer);
			}
			xstream.marshal(transformResponse(response), xmlWriter);
		}
		return null;
	}

	
}

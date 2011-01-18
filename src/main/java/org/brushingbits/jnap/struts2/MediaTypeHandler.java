package org.brushingbits.jnap.struts2;

import java.io.Reader;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.brushingbits.jnap.web.Response;


/**
 * 
 * @author Daniel Rochetti
 *
 */
public interface MediaTypeHandler {

	public void read(Object obj, Reader reader);

	public String write(Response response, Writer writer);

	public MediaType[] getSupportedMediaTypes();

	public String[] getExtensions();

	public MediaType getDefaultMediaType();
}

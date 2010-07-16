/* HttpAuWriter.java

	Purpose:
		
	Description:
		
	History:
		Mon Dec  3 16:18:18     2007, Created by tomyeh

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.http;

import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.json.*;
import org.zkoss.web.servlet.http.Https;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.MarshalledResponse;
import org.zkoss.zk.au.AuWriter;
import org.zkoss.zk.au.AuWriters;

/**
 * The writer used to write the output back to the client.
 *
 * @author tomyeh
 * @since 3.0.1
 */
public class HttpAuWriter implements AuWriter{
	/** The writer used to generate the output to.
	 */
	private JSONObject _out;
	private JSONArray _rs;
	private boolean _compress = true;

	public HttpAuWriter() {
	}

	/** Returns whether to compress the output.
	 * @since 3.6.3
	 */
	public boolean isCompress() {
		return _compress;
	}

	//AuWriter//
	public void setCompress(boolean compress) {
		_compress = compress;
	}
	/** Opens the connection.
	 *
	 * <p>Default: it creates an object to store the responses.
	 *
	 * <p>This implementation doesn't support the timeout argument.
	 */
	public AuWriter open(Object request, Object response, int timeout)
	throws IOException {
		((HttpServletResponse)response).setContentType(AuWriters.CONTENT_TYPE);
			//Bug 1907640: with Glassfish v1, we cannot change content type
			//in another thread, so we have to do it here

		_out = new JSONObject();
		_out.put("rs", _rs = new JSONArray());
		return this;
	}
	/** Closes the connection.
	 */
	public void close(Object request, Object response)
	throws IOException {
		final HttpServletRequest hreq = (HttpServletRequest)request;
		final HttpServletResponse hres = (HttpServletResponse)response;

		//Use OutputStream due to Bug 1528592 (Jetty 6)
		byte[] data = getResult().getBytes("UTF-8");
		if (_compress && data.length > 200) {
			byte[] bs = Https.gzip(hreq, hres, null, data);
			if (bs != null) data = bs; //yes, browser support compress
		}

		hres.setContentType(AuWriters.CONTENT_TYPE);
			//we have to set content-type again. otherwise, tomcat might
			//fail to preserve what is set in open()
		hres.setContentLength(data.length);
		hres.getOutputStream().write(data);
		hres.flushBuffer();
	}
	/** Returns the result of responses that will be sent to client
	 * (never null).
	 * It is called by {@link #close} to retrieve the output.
	 * After invocation, the writer is reset.
	 * @since 5.0.0
	 */
	protected String getResult() {
		final String data = _out.toString();
		_out = null;
		_rs = null;
		return data;
	}
	public void writeResponseId(int resId) throws IOException {
		_out.put("rid", new Integer(resId));
	}
	public void write(AuResponse response) throws IOException {
		write(new MarshalledResponse(response));
	}
	public void write(MarshalledResponse response) throws IOException {
		final JSONArray r = new JSONArray();
		r.add(response.command);
		r.add(response.encodedData);
		_rs.add(r);
	}
	public void write(Collection responses) throws IOException {
		for (Iterator it = responses.iterator(); it.hasNext();) {
			final Object o = it.next();
			if (o instanceof AuResponse)
				write((AuResponse)o);
			else
				write((MarshalledResponse)o);
		}
	}
}

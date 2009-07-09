/* WcsExtendlet.java

	Purpose:
		
	Description:
		
	History:
		Mon Jun 29 16:25:12     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
package org.zkoss.zk.ui.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.util.logging.Log;
import org.zkoss.util.resource.ResourceCache;
import org.zkoss.idom.Element;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.idom.util.IDOMs;

import org.zkoss.web.servlet.Servlets;
import org.zkoss.web.servlet.http.Https;
import org.zkoss.web.servlet.http.HttpBufferedResponse;
import org.zkoss.web.util.resource.ExtendletConfig;
import org.zkoss.web.util.resource.ExtendletContext;
import org.zkoss.web.util.resource.ExtendletLoader;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;

/**
 * The extendlet to handle WCS (widget CSS).
 * Each language (such as zul) must have exactly one of WCS file to
 * generate all CSS for all widgets of a langauge.
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class WcsExtendlet extends AbstractExtendlet {
	public void init(ExtendletConfig config) {
		init(config, new WcsLoader());
	}
	public void service(HttpServletRequest request,
	HttpServletResponse response, String path, String extra)
	throws ServletException, java.io.IOException {
		final WcsInfo wi = (WcsInfo)_cache.get(path);
		if (wi == null) {
			if (Servlets.isIncluded(request))
				log.error("Failed to load the resource: "+path);
				//It might be eaten, so log the error
			response.sendError(response.SC_NOT_FOUND, path);
			return;
		}

		final StringWriter sw = new StringWriter();
		for (int j = 0; j < wi.items.length; ++j) {
			final Object o = wi.items[j];
			if (o instanceof String) {
				try {
					_webctx.include(request, HttpBufferedResponse.getInstance(response, sw), (String)o, null);
				} catch (Throwable ex) {
					log.realCauseBriefly("Unable to load "+wi.items[j], ex);
				}
			} else { //static method
				sw.write(invoke((Method)o));
			}
			sw.write('\n');
		}
		for (Iterator it = wi.langdef.getCSSURIs().iterator(); it.hasNext();) {
			final String uri = (String)it.next();
			try {
				_webctx.include(request, HttpBufferedResponse.getInstance(response, sw), uri, null);
			} catch (Throwable ex) {
				log.realCauseBriefly("Unable to load "+uri, ex);
			}
		}

		response.setContentType("text/css;charset=UTF-8");
		byte[] data = sw.getBuffer().toString().getBytes("UTF-8");
		if (data.length > 200) {
			byte[] bs = Https.gzip(request, response, null, data);
			if (bs != null) data = bs; //yes, browser support compress
		}
		response.setContentLength(data.length);
		response.getOutputStream().write(data);
		response.flushBuffer();
	}

	/*package*/ Object parse(InputStream is, String path) throws Exception {
		final Element root = new SAXBuilder(true, false, true).build(is).getRootElement();
		final String lang = IDOMs.getRequiredAttributeValue(root, "language");
		if (lang.length() == 0)
			throw new UiException("The language attribute must be specified, "+root.getLocator());

		final List items = new LinkedList();
		for (Iterator it = root.getElements().iterator(); it.hasNext();) {
			final Element el = (Element)it.next();
			final String elnm = el.getName();
			if ("stylesheet".equals(elnm)) {
				final String href = IDOMs.getRequiredAttributeValue(el, "href");
				if (href.length() != 0)
					items.add(href);
				else
					log.warning("Ingored stylesheet: href required, " + el.getLocator());
			} else if ("function".equals(elnm)) {
				final Method mtd = getMethod(el);
				if (mtd != null) items.add(mtd);
			} else
				log.warning("Ignored unknown element, " + el.getLocator());
		}
		return new WcsInfo(lang, items);
	}

	private class WcsLoader extends ExtendletLoader {
		private WcsLoader() {
		}

		//-- super --//
		protected Object parse(InputStream is, String path) throws Exception {
			return WcsExtendlet.this.parse(is, path);
		}
		protected ExtendletContext getExtendletContext() {
			return _webctx;
		}
	}
	/*package*/ static class WcsInfo {
		/*package*/ final LanguageDefinition langdef;
		/** A list of URI or static method. */
		/*package*/ final Object[] items;
		private WcsInfo(String lang, List items) {
			this.langdef = LanguageDefinition.lookup(lang);
			this.items = (Object[])items.toArray(new Object[items.size()]);
		}
	}
}

/* DHtmlUpdateServlet.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon May 30 21:11:28     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.http;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.io.StringWriter;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import org.zkoss.mesg.Messages;
import org.zkoss.lang.Exceptions;
import org.zkoss.util.logging.Log;

import org.zkoss.web.servlet.Servlets;
import org.zkoss.web.servlet.http.Https;
import org.zkoss.web.servlet.http.Encodes;
import org.zkoss.web.util.resource.ClassWebResource;

import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zk.ui.sys.UiEngine;
import org.zkoss.zk.ui.sys.FailoverManager;
import org.zkoss.zk.ui.http.ExecutionImpl;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.http.I18Ns;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.AuObsolete;
import org.zkoss.zk.au.AuAlert;
import org.zkoss.zk.au.AuSendRedirect;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.CommandNotFoundException;

/**
 * Used to receive command from the server and send result back to client.
 * Though it is called
 * DHtmlUpdateServlet, it is used to serve all kind of HTTP-based clients,
 * including html, mul and others (see {@link Desktop#getClientType}.
 *
 * <p>Design decision: it is better to use independent servlets for
 * /web, /upload and /view. However, to simplify the configuration,
 * we choose not to.
 *
 * @author tomyeh
 */
public class DHtmlUpdateServlet extends HttpServlet {
	private static final Log log = Log.lookup(DHtmlUpdateServlet.class);

	private ServletContext _ctx;
	private long _lastModified;

	public void init(ServletConfig config) throws ServletException {
		if (log.debugable()) log.debug("Starting DHtmlUpdateServlet at "+config.getServletContext());
		_ctx = config.getServletContext();
	}
	public ServletContext getServletContext() {
		return _ctx;
	}

	//-- super --//
	protected long getLastModified(HttpServletRequest request) {
		final String pi = Https.getThisPathInfo(request);
		if (pi != null && pi.startsWith(ClassWebResource.PATH_PREFIX)
		&& !pi.endsWith(".dsp") && !Servlets.isIncluded(request)) {
			if (_lastModified == 0) _lastModified = new Date().getTime();
				//Hard to know when it is modified, so cheat it..
			return _lastModified;
		}
		return -1;
	}
	protected
	void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		final Session sess = WebManager.getSession(_ctx, request);
		final Object old = I18Ns.setup(sess, request, response, "UTF-8");
		try {
			final String pi = Https.getThisPathInfo(request);
			if (pi != null && pi.length() != 0) {
				//if (log.finerable()) log.finer("Path info: "+pi);
				if (pi.startsWith(ClassWebResource.PATH_PREFIX)) {
					WebManager.getWebManager(_ctx).
						getClassWebResource().doGet(request, response);
				} else if (pi.startsWith("/upload")) {
					Uploads.process(sess, _ctx, request, response);
				} else if (pi.startsWith("/view")) {
					DynaMedias.process(
						sess, _ctx, request, response, pi.substring(5));
				} else {
					log.warning("Unknown path info: "+pi);
				}
				return;
			}

			process(sess, request, response);
		} finally {
			I18Ns.cleanup(request, old);
		}
	}
	protected
	void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		doGet(request, response);
	}

	//-- ASYNC-UPDATE --//
	/** Process update requests from the client. */
	private void process(Session sess,
	HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		final WebApp wapp = sess.getWebApp();
		final WebAppCtrl wappc = (WebAppCtrl)wapp;
		final UiEngine uieng = wappc.getUiEngine();
		final List aureqs = new LinkedList();

		//parse desktop ID
		final String dtid = request.getParameter("dtid");
		if (dtid == null) {
			log.warning("dtid not found: CP="+request.getContextPath()+" and "+Https.getThisContextPath(request)
				+", SP="+request.getServletPath()+" and "+Https.getThisServletPath(request)
				+", QS="+request.getQueryString()+" and "+Https.getThisQueryString(request)
				+", params="+request.getParameterMap().keySet());
			//responseError(uieng, response, "Illegal request: dtid is required");
			//Tom M. Yeh: 20060922: Unknown reason to get here but it is annoying
			//to response it back to users
			return;
		}

		response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate,proxy-revalidate,max-age=0");
		response.setHeader("Pragma", "no-cache,no-store");

		Desktop desktop = wappc.getDesktopCache(sess).getDesktopIfAny(dtid);
		if (desktop == null) {
			desktop = recover(sess, request, response, wappc, dtid);

			if (desktop == null) {
				final StringWriter out = getXMLWriter();

				final String scmd = request.getParameter("cmd.0");
				if (!"rmDesktop".equals(scmd) && !"onRender".equals(scmd)
				&& !"onTimer".equals(scmd)) {//possible in FF due to cache
					String uri = wapp.getConfiguration().getTimeoutURI();
					final AuResponse resp;
					if (uri != null) {
						if (uri.length() != 0)
							uri = Encodes.encodeURL(_ctx, request, response, uri);
						resp = new AuSendRedirect(uri, null);
					} else {
						resp = new AuObsolete(
							dtid, Messages.get(MZk.UPDATE_OBSOLETE_PAGE, dtid));
					}
					uieng.response(resp, out);
				}

				flushXMLWriter(response, out);
				return;
			}
		}
		WebManager.setDesktop(request, desktop);
			//reason: a new page might be created (such as include)

		//parse commands
		try {
			for (int j = 0;; ++j) {
				final String scmd = request.getParameter("cmd."+j);
				if (scmd == null)
					break;

				final Command cmd = AuRequest.getCommand(scmd);
				final String uuid = request.getParameter("uuid."+j);
				final String[] data = request.getParameterValues("data."+j);
				if (data != null) {
					for (int k = data.length; --k >= 0;)
						if ("zk_null~q".equals(data[k]))
							data[k] = null;
				}
				if (uuid == null || uuid.length() == 0) {
					aureqs.add(new AuRequest(desktop, cmd, data));
				} else {
					aureqs.add(new AuRequest(desktop, uuid, cmd, data));
				}
			}
			if (aureqs.isEmpty()) {
				responseError(uieng, response, "Illegal request: cmd is required");
				return;
			}
		} catch (CommandNotFoundException ex) {
			responseError(uieng, response, Exceptions.getMessage(ex));
			return;
		}

		//if (log.debugable()) log.debug("AU request: "+aureqs);
		final StringWriter out = getXMLWriter();

		uieng.execUpdate(
			new ExecutionImpl(_ctx, request, response, desktop, null),
			aureqs, out);

		flushXMLWriter(response, out);
	}

	/** Returns the writer for output XML.
	 * @param withrs whether to output <rs> first.
	 */
	private static StringWriter getXMLWriter() {
		final StringWriter out = new StringWriter();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rs>\n");
		return out;
	}
	/** Flushes all content in out to the response.
	 * Don't write the response thereafter.
	 * @param withrs whether to output </rs> first.
	 */
	private static final
	void flushXMLWriter(HttpServletResponse response, StringWriter out)
	throws IOException {
		out.write("\n</rs>");

		//Use OutputStream due to Bug 1528592 (Jetty 6)
		final byte[] bs = out.toString().getBytes("UTF-8");
		response.setContentType("text/xml;charset=UTF-8");
		response.setContentLength(bs.length);
		response.getOutputStream().write(bs);
		response.flushBuffer();
	}

	/** Recovers the desktop if possible.
	 */
	private Desktop recover(Session sess,
	HttpServletRequest request, HttpServletResponse response,
	WebAppCtrl wappc, String dtid) {
		final FailoverManager failover = wappc.getFailoverManager();
		if (failover != null) {
			Desktop desktop = null;
			try {
				if (failover.isRecoverable(sess, dtid)) {
					desktop = WebManager.getWebManager(_ctx)
						.getDesktop(sess, request, null, true);
					wappc.getUiEngine().execRecover(
						new ExecutionImpl(_ctx, request, response, desktop, null),
						failover);
					return desktop; //success
				}
			} catch (Throwable ex) {
				log.error("Unable to recover "+dtid, ex);
				if (desktop != null)
					((DesktopCtrl)desktop).recoverDidFail(ex);
			}
		}
		return null;
	}

	/** Generates a response for an error message.
	 */
	private static
	void responseError(UiEngine uieng, HttpServletResponse response,
	String errmsg) throws IOException {
		log.debug(errmsg);

		//Don't use sendError because Browser cannot handle UTF-8
		final StringWriter out = getXMLWriter();
		uieng.response(new AuAlert(errmsg), out);
		flushXMLWriter(response, out);
	}
}

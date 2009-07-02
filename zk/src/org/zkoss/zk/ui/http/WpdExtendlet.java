/* WpdExtendlet.java

	Purpose:
		
	Description:
		
	History:
		Mon Oct  6 10:47:11     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
*/
package org.zkoss.zk.ui.http;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Strings;
import org.zkoss.lang.Exceptions;
import org.zkoss.io.Files;
import org.zkoss.util.logging.Log;
import org.zkoss.util.resource.ResourceCache;
import org.zkoss.idom.Element;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.idom.util.IDOMs;
import org.zkoss.io.Files;

import org.zkoss.web.servlet.Servlets;
import org.zkoss.web.servlet.http.Https;
import org.zkoss.web.servlet.http.Encodes;
import org.zkoss.web.util.resource.Extendlet;
import org.zkoss.web.util.resource.ExtendletContext;
import org.zkoss.web.util.resource.ExtendletConfig;
import org.zkoss.web.util.resource.ExtendletLoader;

import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.ui.metainfo.WidgetDefinition;
import org.zkoss.zk.ui.util.Configuration;

/**
 * The extendlet to handle WPD (Widget Package Descriptor).
 *
 * <p>Note: it assumes all JavaScript files are encoded in UTF-8.
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class WpdExtendlet implements Extendlet {
	private static final Log log = Log.lookup(WpdExtendlet.class);

	private ExtendletContext _webctx;
	/** DSP Interpretation cache. */
	private ResourceCache _cache;
	private Boolean _debugJS;
	/** The locator. */
	private ThreadLocal _loc = new ThreadLocal();

	public void init(ExtendletConfig config) {
		_webctx = config.getExtendletContext();
		final WpdLoader loader = new WpdLoader();
		_cache = new ResourceCache(loader, 16);
		_cache.setMaxSize(1024);
		_cache.setLifetime(60*60*1000); //1hr
		final int checkPeriod = loader.getCheckPeriod();
		_cache.setCheckPeriod(checkPeriod >= 0 ? checkPeriod: 60*60*1000); //1hr
	}
	public boolean getFeature(int feature) {
		return feature == ALLOW_DIRECT_INCLUDE;
	}
	public void service(HttpServletRequest request,
	HttpServletResponse response, String path, String extra)
	throws ServletException, IOException {
		byte[] data;
		_loc.set(new Locator(request, response));
		try {
			final Object rawdata = _cache.get(path);
			if (rawdata == null) {
				if (Servlets.isIncluded(request))
					log.error("Failed to load the resource: "+path);
					//It might be eaten, so log the error
				response.sendError(response.SC_NOT_FOUND, path);
				return;
			}

			data = rawdata instanceof byte[] ? (byte[])rawdata:
				((WpdContent)rawdata).toByteArray();
		} finally {
			_loc.set(null);
		}

		response.setContentType("text/javascript;charset=UTF-8");
		if (data.length > 200) {
			byte[] bs = Https.gzip(request, response, null, data);
			if (bs != null) data = bs; //yes, browser support compress
		}
		response.setContentLength(data.length);
		response.getOutputStream().write(data);
		response.flushBuffer();
	}
	/** Sets whether to generate JS files that is easy to debug. */
	public void setDebugJS(boolean debugJS) {
		_debugJS = Boolean.valueOf(debugJS);
	}
	/** Returns whether to generate JS files that is easy to debug. */
	public boolean isDebugJS() {
		if (_debugJS == null) {
			final WebApp wapp = getWebApp();
			if (wapp == null) return true; //zk lighter
			_debugJS = Boolean.valueOf(wapp.getConfiguration().isDebugJS());
		}
		return _debugJS.booleanValue();
	}
	private WebApp getWebApp() {
		final WebManager wm = WebManager.getWebManager(_webctx.getServletContext());
		return wm != null ? wm.getWebApp(): null;
	}
	/** Parses and return the specified file.
	 * It is used by ZK Lighter to generate JavaScript files.
	 */
	public byte[] service(File fl) throws Exception {
		_loc.set(new FileLocator(fl));
		try {
			final Object rawdata = parse(new FileInputStream(fl), fl.getPath());
			return rawdata instanceof byte[] ? (byte[])rawdata:
				((WpdContent)rawdata).toByteArray();
		} finally {
			_loc.set(null);
		}
	}
	private Object parse(InputStream is, String path) throws Exception {
		final Element root = new SAXBuilder(true, false, true).build(is).getRootElement();
		final String name = IDOMs.getRequiredAttributeValue(root, "name");
		if (name.length() == 0)
			throw new UiException("The name attribute must be specified, "+root.getLocator());
		final boolean zk = "zk".equals(name);
		final String lang = root.getAttributeValue("language");
		final LanguageDefinition langdef = //optional
			lang != null ? LanguageDefinition.lookup(lang): null;
		final String dir = path.substring(0, path.lastIndexOf('/') + 1);
		final WpdContent wc =
			"false".equals(root.getAttributeValue("cacheable")) ?
				new WpdContent(dir): null;

		final ByteArrayOutputStream out = new ByteArrayOutputStream(1024*8);
		String depends = null;
		if (zk) {
			write(out, "//ZK, Copyright (C) 2009 Potix Corporation. Distributed under GPL 3.0\n"
				+ "//jQuery, Copyright (c) 2009 John Resig\n"
				+ "if(!window.zk){");//may be loaded multiple times because specified in lang.xml
		} else {
			write(out, "_z='");
			write(out, name);
			write(out, "';try{_zkpk=zk.$package(_z,false);\n");

			depends = root.getAttributeValue("depends");
			if (depends != null)
				if (depends.length() == 0) {
					depends = null;
				} else if (depends != null) {
					write(out, "zPkg.load('");
					write(out, depends);
					write(out, "',function(){\n_zkpk=");
					write(out, name);
					write(out, ";\n");
				}
		}

		final Map moldInfos = new HashMap();
		for (Iterator it = root.getElements().iterator(); it.hasNext();) {
			final Element el = (Element)it.next();
			final String elnm = el.getName();
			if ("widget".equals(elnm)) {
				final String wgtnm = IDOMs.getRequiredAttributeValue(el, "name");
				final String jspath = wgtnm + ".js"; //eg: /js/zul/wgt/Div.js
				if (!writeResource(out, jspath, dir, false)) {
					log.error("Failed to load widget "+wgtnm+": "+jspath+" not found, "+el.getLocator());
					continue;
				}

				final String wgtflnm = name + "." + wgtnm;
				write(out, "zkreg(_zkwg=");
				write(out, zk ? "zk.": "_zkpk.");
				write(out, wgtnm);
				write(out, ",'");
				write(out, wgtflnm);
				write(out, '\'');
				WidgetDefinition wgtdef = langdef != null ? langdef.getWidgetDefinitionIfAny(wgtflnm): null;
				if (wgtdef != null && wgtdef.isBlankPreserved())
					write(out, ",true");
				write(out, ");");
				if (wgtdef == null)
					continue;

				try {
					boolean first = true;
					for (Iterator e = wgtdef.getMoldNames().iterator(); e.hasNext();) {
						final String mold = (String)e.next();
						final String uri = wgtdef.getMoldURI(mold);
						if (uri == null) continue;

						if (first) {
							first = false;
							write(out, "_zkmd={};\n");
						}
							
						write(out, "_zkmd['");
						write(out, mold);
						write(out, "']=");

						String[] info = (String[])moldInfos.get(uri);
						if (info != null) { //reuse
							write(out, "[_zkpk.");
							write(out, info[0]);
							write(out, ",'");
							write(out, info[1]);
							write(out, "'];");
						} else {
							moldInfos.put(uri, new String[] {wgtnm, mold});
							if (!writeResource(out, uri, dir, true)) {
								write(out, "zk.$void;zk.error('");
								write(out, uri);
								write(out, " not found')");
								log.error("Failed to load mold "+mold+" for widget "+wgtflnm+". Cause: "+uri+" not found");
							}
							write(out, ';');
						}
					}
					if (!first) write(out, "zkmld(_zkwg,_zkmd);");
				} catch (Throwable ex) {
					log.error("Failed to load molds for widget "+wgtflnm+".\nCause: "+Exceptions.getMessage(ex));
				}
			} else if ("script".equals(elnm)) {
				String browser = el.getAttributeValue("browser");
				if (browser != null && wc == null)
					log.error("browser attribute not called in a cachable WPD, "+el.getLocator());
				String jspath = el.getAttributeValue("src");
				if (jspath != null && jspath.length() > 0) {
					if (wc != null
					&& (browser != null || jspath.indexOf('*') >= 0)) {
						move(wc, out);
						wc.add(jspath, browser);
					} else {
						if (browser != null) {
							final Locator loc = (Locator)_loc.get();
							if (loc != null && !Servlets.isBrowser(loc.request, browser))
								continue;
						}
						if (!writeResource(out, jspath, dir, true))
							log.error("Failed to load script "+jspath+", "+el.getLocator());
					}
				}

				String s = el.getText(true);
				if (s != null && s.length() > 0) {
					write(out, s);
					write(out, '\n'); //might terminate with //
				}
			} else if ("function".equals(elnm)) {
				final String clsnm = IDOMs.getRequiredAttributeValue(el, "class");
				final String mtdnm = IDOMs.getRequiredAttributeValue(el, "name");
				final Class cls;
				try {
					cls = Classes.forNameByThread(clsnm);
				} catch (ClassNotFoundException ex) {
					log.error("Class not found: "+clsnm+", "+el.getLocator());
					continue; //to report as many errors as possible
				}

				final Method mtd;
				try {
					mtd = cls.getMethod(mtdnm, new Class[0]);
					if ((mtd.getModifiers() & Modifier.STATIC) == 0) {
						log.error("Not a static method: "+mtd);
						continue;
					}
				} catch (NoSuchMethodException ex) {
					log.error("Method not found in "+clsnm+": "+mtdnm+" "+el.getLocator());
					continue;
				}

				if (wc != null) {
					move(wc, out);
					wc.add(mtd);
				} else {
					write(out, mtd);
				}
			} else {
				log.warning("Unknown element "+elnm+", "+el.getLocator());
			}
		}
		if (zk) {
			final WebApp wapp = getWebApp();
			if (wapp != null)
				writeAppInfo(out, wapp);
			write(out, '}'); //end of if
		} else {
			if (depends != null)
				write(out, "\n});");
			write(out, "\n}finally{zPkg.end(_z);}");
		}

		if (wc != null) {
			move(wc, out);
			return wc;
		}
		return out.toByteArray();
	}
	private boolean writeResource(OutputStream out, String path,
	String dir, boolean locate)
	throws IOException, ServletException {
		if (path.startsWith("~./")) path = path.substring(2);
		else if (path.charAt(0) != '/')
			path = Files.normalize(dir, path);

		final InputStream is =
			((Locator)_loc.get()).getResourceAsStream(path, locate);
		if (is == null) {
			final boolean debugJS = isDebugJS();
			if (debugJS) write(out, "zk.log(");
			write(out, "'Failed to load ");
			write(out, path);
			write(out, '\'');
			if (debugJS) write(out, ')');
			write(out, ';');
			return false;
		}

		Files.copy(out, is);
		write(out, '\n'); //might terminate with //
		return true;
	}
	private void write(OutputStream out, String s) throws IOException {
		if (s != null) {
			final byte[] bs = s.getBytes("UTF-8");
			out.write(bs, 0, bs.length);
		}
	}
	/*private static void write(OutputStream out, StringBuffer sb)
	throws IOException {
		final byte[] bs = sb.toString().getBytes("UTF-8");
		out.write(bs, 0, bs.length);
		sb.setLength(0);
	}*/
	private void writeln(OutputStream out) throws IOException {
		write(out, '\n');
	}
	private void write(OutputStream out, char cc) throws IOException {
		assert cc < 128;
		final byte[] bs = new byte[] {(byte)cc};
		out.write(bs, 0, 1);
	}
	private void write(OutputStream out, Method mtd) throws IOException {
		try {
			write(out, (String)mtd.invoke(null, new Object[0]));
		} catch (IOException ex) {
			throw ex;
		} catch (Throwable ex) { //log and eat ex
			log.error("Unable to invoke "+mtd, ex);
		}
	}
	private void move(WpdContent wc, ByteArrayOutputStream out) {
		final byte[] bs = out.toByteArray();
		if (bs.length > 0) {
			wc.add(bs);
			out.reset();
		}
	}

	private void writeAppInfo(OutputStream out, WebApp wapp)
	throws IOException, ServletException {
		final StringBuffer sb = new StringBuffer(256);
		sb.append("\nzkver('").append(wapp.getVersion())
			.append("','").append(wapp.getBuild());
		final Locator loc = (Locator)_loc.get();
		if (loc != null)
			sb.append("','")
				.append(Encodes.encodeURL(
					_webctx.getServletContext(),
					loc.request, loc.response, wapp.getUpdateURI(false)));
		sb.append('\'');

		for (Iterator it = LanguageDefinition.getByDeviceType("ajax").iterator();
		it.hasNext();) {
			final LanguageDefinition langdef = (LanguageDefinition)it.next();
			final Set mods = langdef.getJavaScriptModules().entrySet();
			if (!mods.isEmpty())
				for (Iterator e = mods.iterator(); e.hasNext();) {
					final Map.Entry me = (Map.Entry)e.next();
					sb.append(",'").append(me.getKey())
					  .append("','").append(me.getValue()).append('\'');
				}
		}

		sb.append(");");
		final int jdot = sb.length();

		if (WebApps.getFeature("enterprise"))
			sb.append(",ed:'e'");
		else if (WebApps.getFeature("professional"))
			sb.append(",ed:'p'");

		final Configuration config = wapp.getConfiguration();
		int v = config.getProcessingPromptDelay();
		if (v != 900) sb.append(",pd:").append(v);
		v = config.getTooltipDelay();
		if (v != 800) sb.append(",td:").append(v);
		v = config.getResendDelay();
		if (v >= 0) sb.append(",rd:").append(v);
		v = config.getClickFilterDelay();
		if (v >= 0) sb.append(",cd:").append(v);
		if (config.isDebugJS()) sb.append(",dj:1");
		if (config.isKeepDesktopAcrossVisits())
			sb.append(",kd:1");
		if (config.getPerformanceMeter() != null)
			sb.append(",pf:1");
		if (sb.length() > jdot) {
			sb.replace(jdot, jdot + 1, "zkopt({");
			sb.append("});\n");
		}

		final int[] cers = config.getClientErrorReloadCodes();
		if (cers.length > 0) {
			final int k = sb.length();
			for (int j = 0; j < cers.length; ++j) {
				final String uri = config.getClientErrorReload(cers[j]);
				if (uri != null) {
					if (k != sb.length()) sb.append(',');
					sb.append(cers[j]).append(",'")
						.append(Strings.escape(uri, Strings.ESCAPE_JAVASCRIPT))
						.append('\'');
				}
			}
			if (k != sb.length()) {
				sb.insert(k, "zAu.setErrorURI(");
				sb.append(");\n");
			}
		}
		write(out, sb.toString());
	}

	private class WpdLoader extends ExtendletLoader {
		private WpdLoader() {
		}

		//-- super --//
		protected Object parse(InputStream is, String path) throws Exception {
			return WpdExtendlet.this.parse(is, path);
		}
		protected ExtendletContext getExtendletContext() {
			return _webctx;
		}
	}
	private class WpdContent {
		private final String _dir;
		private final List _cnt = new LinkedList();
		private WpdContent(String dir) {
			_dir = dir;
		}
		private void add(byte[] bs) {
			_cnt.add(bs);
		}
		private void add(Method mtd) {
			_cnt.add(mtd);
		}
		private void add(String jspath, String browser) {
			_cnt.add(new String[] {jspath, browser});
		}
		private byte[] toByteArray() throws ServletException, IOException {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (Iterator it = _cnt.iterator(); it.hasNext();) {
				final Object o = it.next();
				if (o instanceof byte[])
					out.write((byte[])o);
				else if (o instanceof Method)
					write(out, (Method)o);
				else {
					final String[] inf = (String[])o;
					if (inf[1] != null) {
						final Locator loc = (Locator)_loc.get();
						if (loc != null && !Servlets.isBrowser(loc.request, inf[1]))
							continue;
					}
					if (!writeResource(out, inf[0], _dir, true))
						log.error("Failed to load script "+inf[0]);
				}
			}
			return out.toByteArray();
		}
	}
	/*package*/ class Locator { //don't use private since WpdContent needs it
		private HttpServletRequest request;
		private HttpServletResponse response;
		private Locator(HttpServletRequest request, HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}

		/*package*/
		InputStream getResourceAsStream(String path, boolean locate)
		throws IOException, ServletException {
			if (locate)
				path = Servlets.locate(_webctx.getServletContext(),
					this.request, path, _webctx.getLocator());

			if (_cache.getCheckPeriod() >= 0) {
				//Due to Web server might cache the result, we use URL if possible
				try {
					URL url = _webctx.getResource(path);
					if (url != null)
						return url.openStream();
				} catch (Throwable ex) {
					log.warningBriefly("Unable to read from URL: "+path, ex);
				}
			}

			//Note: _webctx will handle the renaming for debugJS (.src.js)
			return _webctx.getResourceAsStream(path);
		}
	}
	class FileLocator extends Locator {
		private String _parent;
		private FileLocator(File file) {
			super(null, null);
			_parent = file.getParent();
		}
		InputStream getResourceAsStream(String path, boolean locate)
		throws IOException {
			if (isDebugJS()) {
				final int j = path.lastIndexOf('.');
				if (j >= 0)
					path = 	path.substring(0, j) + ".src" + path.substring(j);
			}
			final File file = new File(_parent, path);
			return locate ? new FileInputStream(Files.locate(file.getPath())):
				new FileInputStream(file);
		}
	}
}

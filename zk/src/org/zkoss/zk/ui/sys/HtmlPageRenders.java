/* HtmlPageRenders.java

	Purpose:
		
	Description:
		
	History:
		Tue Oct 14 19:06:37     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.zkoss.lang.Strings;
import org.zkoss.io.Files;
import org.zkoss.util.logging.Log;
import org.zkoss.web.fn.ServletFns;
import org.zkoss.web.servlet.JavaScript;
import org.zkoss.web.servlet.StyleSheet;
import org.zkoss.xml.HTMLs;
import org.zkoss.xml.XMLs;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zk.ui.util.ThemeProvider;
import org.zkoss.zk.ui.sys.PageCtrl;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.ui.impl.Attributes;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.device.Devices;
import org.zkoss.zk.device.Device;

/**
 * Utilities for implementing HTML-based {@link PageRenderer}.
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class HtmlPageRenders {
//	private static final Log log = Log.lookup(HtmlPageRenders.class);

	/** Denotes whether style sheets are generated for this request. */
	private static final String ATTR_LANG_CSS_GENED
		= "javax.zkoss.zk.lang.css.generated";
		//Naming with javax to be able to shared among portlets
	/** Denotes whether JavaScripts are generated for this request. */
	private static final String ATTR_LANG_JS_GENED
		= "javax.zkoss.zk.lang.js.generated";
		//Naming with javax to be able to shared among portlets
	/** Denotes whether the unavailable message is generated for this request. */
	private static final String ATTR_UNAVAILABLE_GENED
		= "javax.zkoss.zk.unavail.generated";

	/** The render context. */
	private static final String ATTR_RENDER_CONTEXT
		= "org.zkoss.zk.ui.renderContext";

	/** Sets the content type to the specified execution for the given page.
	 * @param exec the execution (never null)
	 */
	public static final void setContentType(Execution exec, Page page) {
		String contentType = ((PageCtrl)page).getContentType();
		if (contentType == null) {
			contentType = page.getDesktop().getDevice().getContentType();
			if (contentType == null) contentType = "";
		}

		final int j = contentType.indexOf(';');
		if (j < 0) {
			final String cs = page.getDesktop().getWebApp()
				.getConfiguration().getResponseCharset();
			if (cs != null && cs.length() > 0)
				contentType += ";charset=" + cs;
		}

		((ExecutionCtrl)exec).setContentType(contentType);
	}
	/** Returns the doc type, or null if not available.
	 * It is null or &lt;!DOCTYPE ...&gt;.
	 */
	public static final String outDocType(Page page) {
		final String docType = ((PageCtrl)page).getDocType();
		return trimAndLF(docType != null ?
			docType: page.getDesktop().getDevice().getDocType());
	}
	/** Trims and appends a linefeed if necessary.
	 */
	private static final String trimAndLF(String s) {
		if (s != null) {
			s = s.trim();
			final int len = s.length();
			if (len > 0 && s.charAt(len-1) != '\n')
				s += '\n';
		}
		return s;
	}
	/** Generates the unavailable message in HTML tags, if any.
	 * @param exec the execution (never null)
	 */
	public static String outUnavailable(Execution exec) {
		if (exec.getAttribute(ATTR_UNAVAILABLE_GENED) != null)
			return ""; //nothing to generate
		exec.setAttribute(ATTR_UNAVAILABLE_GENED, Boolean.TRUE);

		final Device device = exec.getDesktop().getDevice();
		String s = device.getUnavailableMessage();
		return s != null ?
			"<noscript>\n" + s + "\n</noscript>": "";
	}
	/** Returns the first line to be generated to the output,
	 * or null if no special first line.
	 */
	public static final String outFirstLine(Page page) {
		return trimAndLF(((PageCtrl)page).getFirstLine());
	}

	/** Returns JavaScript for handling the specified response.
	 * @param exec the execution (never null)
	 */
	public static final
	String outResponseJavaScripts(Execution exec) {
		final ExecutionCtrl execCtrl = (ExecutionCtrl)exec;
		final Collection responses = execCtrl.getResponses();
		if (responses == null || responses.isEmpty()) return "";
		execCtrl.setResponses(null);

		final StringBuffer sb = new StringBuffer(256)
			.append("\n<script>zk.afterMount(function(){\n");
		for (Iterator it = responses.iterator(); it.hasNext();) {
			final AuResponse response = (AuResponse)it.next();
			sb.append("zAu.process('").append(response.getCommand())
				.append("'");

			final List encdata = response.getEncodedData();
			if (encdata != null)
				sb.append(",'")
					.append(Strings.escape(
						org.zkoss.json.JSONArray.toJSONString(encdata),
						Strings.ESCAPE_JAVASCRIPT))
					.append('\'');
			sb.append(");\n");
		}
		return sb.append("});\n</script>\n").toString();
	}

	/** Returns HTML tags to include all JavaScript files and codes that are
	 * required when loading a ZUML page (never null).
	 *
	 * <p>FUTURE CONSIDERATION: we might generate the inclusion on demand
	 * instead of all at once.
	 * @param exec the execution (never null)
	 * @param wapp the Web application.
	 * If null, exec.getDesktop().getWebApp() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 * @param deviceType the device type, such as ajax.
	 * If null, exec.getDesktop().getDeviceType() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 */
	public static final
	String outLangJavaScripts(Execution exec, WebApp wapp, String deviceType) {
		if (exec.getAttribute(ATTR_LANG_JS_GENED) != null)
			return ""; //nothing to generate
		exec.setAttribute(ATTR_LANG_JS_GENED, Boolean.TRUE);

		final Desktop desktop = exec.getDesktop();
		if (wapp == null) wapp = desktop.getWebApp();
		if (deviceType == null)
			deviceType = desktop != null ? desktop.getDeviceType(): "ajax";

		final Device device = Devices.getDevice(deviceType);
		final StringBuffer sb = new StringBuffer(1536);

		final Set jses = new LinkedHashSet(32);
		for (Iterator it = LanguageDefinition.getByDeviceType(deviceType).iterator();
		it.hasNext();)
			jses.addAll(((LanguageDefinition)it.next()).getJavaScripts());
		for (Iterator it = jses.iterator(); it.hasNext();)
			append(sb, (JavaScript)it.next());

		sb.append("\n<!-- ZK ").append(wapp.getVersion());
		if (WebApps.getFeature("enterprise"))
			sb.append(" ENT");
		else if (WebApps.getFeature("professional"))
			sb.append(" PRO");
		sb.append(' ').append(wapp.getBuild())
			.append(" -->\n");

		final Boolean autoTimeout = getAutomaticTimeout(desktop);
		int tmout = 0;
		if (autoTimeout != null ?
		autoTimeout.booleanValue(): device.isAutomaticTimeout()) {
			tmout = desktop.getSession().getMaxInactiveInterval();
			if (tmout > 0) { //unit: seconds
				int extra = tmout / 8;
				tmout += extra > 180 ? 180: extra;
					//Add extra seconds to ensure it is really timeout
			}
		}
		final boolean keepDesktop = exec.getAttribute(Attributes.NO_CACHE) == null;
		if (tmout > 0 || keepDesktop) {
			sb.append("<script>zkopt({");

			if (keepDesktop)
				sb.append("kd:1,");
			if (tmout > 0)
				sb.append("to:").append(tmout);

			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("});</script>");
		}		

		final String s = device.getEmbedded();
		if (s != null)
			sb.append(s).append('\n');

		return sb.toString();
	}
	private static Boolean getAutomaticTimeout(Desktop desktop) {
		if (desktop != null)
			for (Iterator it = desktop.getPages().iterator(); it.hasNext();) {
				Boolean b = ((PageCtrl)it.next()).getAutomaticTimeout();
				if (b != null) return b;
			}
		return null;
	}
	/** Returns HTML tags to include all style sheets that are
	 * defined in all languages of the specified device (never null).
	 *
	 * <p>In addition to style sheets defined in lang.xml and lang-addon.xml,
	 * it also include:
	 * <ol>
	 * <li>The style sheet specified in the theme-uri parameter.</li>
	 * </ol>
	 *
	 * <p>FUTURE CONSIDERATION: we might generate the inclusion on demand
	 * instead of all at once.
	 * @param exec the execution (never null)
	 * @param wapp the Web application.
	 * If null, exec.getDesktop().getWebApp() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 * @param deviceType the device type, such as ajax.
	 * If null, exec.getDesktop().getDeviceType() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 */
	public static final String outLangStyleSheets(Execution exec,
	WebApp wapp, String deviceType) {
		if (exec.getAttribute(ATTR_LANG_CSS_GENED) != null)
			return ""; //nothing to generate
		exec.setAttribute(ATTR_LANG_CSS_GENED, Boolean.TRUE);

		final StringBuffer sb = new StringBuffer(512);
		for (Iterator it = getStyleSheets(exec, wapp, deviceType).iterator();
		it.hasNext();)
			append(sb, (StyleSheet)it.next(), exec, null);

		if (sb.length() > 0) sb.append('\n');
		return sb.toString();
	}

	/** Returns a list of {@link StyleSheet} that shall be generated
	 * to the client for the specified execution.
	 * @param exec the execution (never null)
	 * @param wapp the Web application.
	 * If null, exec.getDesktop().getWebApp() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 * @param deviceType the device type, such as ajax.
	 * If null, exec.getDesktop().getDeviceType() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 */
	public static final
	List getStyleSheets(Execution exec, WebApp wapp, String deviceType) {
		if (wapp == null) wapp = exec.getDesktop().getWebApp();
		if (deviceType == null) deviceType = exec.getDesktop().getDeviceType();

		final Configuration config = wapp.getConfiguration();
		final Set disabled = config.getDisabledThemeURIs();
		final List sses = new LinkedList(); //a list of StyleSheet
		for (Iterator it = LanguageDefinition.getByDeviceType(deviceType).iterator();
		it.hasNext();) {
			final LanguageDefinition langdef = (LanguageDefinition)it.next();
			for (Iterator e = langdef.getStyleSheets().iterator(); e.hasNext();) {
				final StyleSheet ss = (StyleSheet)e.next();
				if (!disabled.contains(ss.getHref()))
					sses.add(ss);
			}
		}

		//Process configuration
		final ThemeProvider themeProvider = config.getThemeProvider();
		if (themeProvider != null) {
			final List org = new LinkedList();
			for (Iterator it =  sses.iterator(); it.hasNext();) {
				final StyleSheet ss = (StyleSheet)it.next();
				org.add(ss.getHref()); //we don't support getContent
			}

			final String[] hrefs = config.getThemeURIs();
			for (int j = 0; j < hrefs.length; ++j)
				org.add(hrefs[j]);

			sses.clear();
			final Collection res = themeProvider.getThemeURIs(exec, org);
			if (res != null) {
				for (Iterator it = res.iterator(); it.hasNext();)
					sses.add(new StyleSheet((String)it.next(), "text/css"));
			}
		} else {
			final String[] hrefs = config.getThemeURIs();
			for (int j = 0; j < hrefs.length; ++j)
				sses.add(new StyleSheet(hrefs[j], "text/css"));
		}
		return sses;
	}

	private static void append(StringBuffer sb, StyleSheet ss,
	Execution exec, Page page) {
		String href = ss.getHref();
		if (href != null) {
			try {
				if (exec != null)
					href = (String)exec.evaluate(page, href, String.class);

				if (href != null && href.length() > 0)
					sb.append("\n<link rel=\"stylesheet\" type=\"")
						.append(ss.getType()).append("\" href=\"")
						.append(ServletFns.encodeURL(href))
						.append("\"/>");
			} catch (javax.servlet.ServletException ex) {
				throw new UiException(ex);
			}
		} else {
			sb.append("\n<style");
			if (ss.getType() != null)
				sb.append(" type=\"").append(ss.getType()).append('"');
			sb.append(">\n").append(ss.getContent()).append("\n</style>");
		}
	}
	private static void append(StringBuffer sb, JavaScript js) {
		sb.append("\n<script type=\"text/javascript\"");
		if (js.getSrc() != null) {
			String url;
			try {
				url = ServletFns.encodeURL(js.getSrc());
			} catch (javax.servlet.ServletException ex) {
				throw new UiException(ex);
			}

			//Note: Jetty might encode jessionid into URL, which
			//Dojo cannot handle, so we have to remove it
			int j = url.lastIndexOf(';');
			if (j > 0 && url.indexOf('.', j + 1) < 0
			&& url.indexOf('/', j + 1) < 0)
				url = url.substring(0, j);

			sb.append(" src=\"").append(url).append('"');
			final String charset = js.getCharset();
			if (charset != null)
				sb.append(" charset=\"").append(charset).append('"');
			sb.append('>');
		} else {
			sb.append(">\n").append(js.getContent());
		}
		sb.append("\n</script>");
	}

	/** Returns the render context, or null if not available.
	 * It is used to render the content that will appear after the content
	 * generated by {@link ContentRenderer}, such as crawlable content.
	 *
	 * @param exec the execution. If null, {@link Executions#getCurrent}
	 * is assumed.
	 */
	public static final
	RenderContext getRenderContext(Execution exec) {
		if (exec == null) exec = Executions.getCurrent();
		return exec != null ?
			(RenderContext)exec.getAttribute(ATTR_RENDER_CONTEXT): null;
	}
	private static final
	void setRenderContext(Execution exec, RenderContext rc) {
		exec.setAttribute(ATTR_RENDER_CONTEXT, rc);
	}

	/** Returns the HTML content representing a page.
	 * @param au whether it is caused by aynchrous update
	 * @param exec the execution (never null)
	 */
	public static final
	void outPageContent(Execution exec, Page page, Writer out, boolean au)
	throws IOException {
		final Desktop desktop = page.getDesktop();
		final PageCtrl pageCtrl = (PageCtrl)page;
		final Component owner = pageCtrl.getOwner();
		boolean contained = owner == null && exec.isIncluded();
			//a standalong page (i.e., no owner), and being included by
			//non-ZK page (e.g., JSP).
			//
			//Revisit Bug 2001707: OK to use exec.isIncluded() since
			//we use PageRenderer now (rather than Servlet's include)
			//TODO: test again

		//prepare style
		String style = page.getStyle();
		if (style == null || style.length() == 0) {
			style = null;
			String wd = null, hgh = null;
			if (owner instanceof HtmlBasedComponent) {
				final HtmlBasedComponent hbc = (HtmlBasedComponent)owner;
				wd = hbc.getWidth(); //null if not set
				hgh = hbc.getHeight(); //null if not set
			}

			if (wd != null || hgh != null || contained) {
				final StringBuffer sb = new StringBuffer(32);
				HTMLs.appendStyle(sb, "width", wd != null ? wd: "100%");
				HTMLs.appendStyle(sb, "height",
					hgh != null ? hgh: contained ? null: "100%");
				style = sb.toString();
			}
		}

		RenderContext rc = null;
		final boolean standalone = !au && owner == null;
		if (standalone) {
			rc = new RenderContext(
				desktop.getWebApp().getConfiguration().isCrawlable());
			setRenderContext(exec, rc);

			out.write("\n<script>zkblbg();try{");
		}
	
		out.write("zkpgbg('");
		out.write(page.getUuid());
		out.write('\'');
		if (style != null || owner == null) {
			out.write(",'");
			if (style != null) out.write(style);
			out.write('\'');

			if (owner == null) {
				out.write(",'");
				out.write(desktop.getId());
				out.write("',");
				out.write(contained ? '1': '0');
				out.write(",'");
				out.write(getContextURI(exec));
				out.write("','");
				out.write(desktop.getUpdateURI(null));
				out.write('\'');
			}
		}
		out.write(");");

		for (Iterator it = page.getRoots().iterator(); it.hasNext();)
			((ComponentCtrl)it.next()).redraw(out);

		out.write("\nzkpge();");

		if (standalone) {
			setRenderContext(exec, null);

			out.write("}finally{zkble();}</script>\n");

			out.write("<div");
			writeAttr(out, "id", page.getUuid());
			out.write(">");
			Files.write(out, ((StringWriter)rc.extra).getBuffer());
			out.write("</div>");
			Files.write(out, ((StringWriter)rc.perm).getBuffer());
		}
	}
	/** Generates the content of a standalone componnent that
	 * the peer widget is not a child of the page widget at the client.
	 */
	public static final void outStandalone(Execution exec,
	Component comp, Writer out) throws java.io.IOException {
		out.write("<div id=\"");
		out.write(comp.getUuid());
		out.write("\"></div><script>\nzkblbg();try{\n");

		((ComponentCtrl)comp).redraw(out);

		out.write("\n}finally{zkble();}\n</script>\n");
	}
	private static final void writeAttr(Writer out, String name, String value)
	throws IOException {
		out.write(' ');
		out.write(name);
		out.write("=\"");
		out.write(XMLs.encodeAttribute(value));
		out.write('"');
	}

	/** Generates and returns the ZK specific HTML tags such as stylesheet
	 * and JavaScript.
	 *
	 * <p>For each desktop, we have to generate a set of HTML tags
	 * to load ZK Client engine, style sheets and so on.
	 * For ZUL pages, it is generated automatically by page.dsp.
	 * However, for ZHTML pages, we have to generate these tags
	 * with special component such as org.zkoss.zhtml.Head, such that
	 * the result HTML page is legal.
	 *
	 * @param exec the execution (never null)
	 * @return the string holding the HTML tags, or null if already generated.
	 * @param wapp the Web application.
	 * If null, exec.getDesktop().getWebApp() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 * @param deviceType the device type, such as ajax.
	 * If null, exec.getDesktop().getDeviceType() is used.
	 * So you have to specify it if the execution is not associated
	 * with desktop (a fake execution).
	 * @see #outZkTags(Execution,Desktop)
	 */
	public static
	String outZkTags(Execution exec, WebApp wapp, String deviceType) {
		if (exec.getAttribute("zkHtmlTagsGened") != null)
			return null;
		exec.setAttribute("zkHtmlTagsGened", Boolean.TRUE);

		final StringBuffer sb = new StringBuffer(512).append('\n')
			.append(outLangStyleSheets(exec, wapp, deviceType))
			.append(outLangJavaScripts(exec, wapp, deviceType));

		final Desktop desktop = exec.getDesktop();
		if (desktop != null) {
			sb.append("<script>zkdtbg('")
				.append(desktop.getId()).append("','")
				.append(getContextURI(exec))
				.append("','").append(desktop.getUpdateURI(null))
				.append("');</script>\n");
		}

		sb.append(outResponseJavaScripts(exec));
		return sb.toString();
	}
	private static String getContextURI(Execution exec) {
		if (exec != null) {
			String s = exec.encodeURL("/");
			int j = s.lastIndexOf('/'); //might have jsessionid=...
			return j >= 0 ? s.substring(0, j) + s.substring(j + 1): s;
		}
		return "";
	}

	/** Generates and returns the ZK specific HTML tags such as stylesheet
	 * and JavaScript for a desktop.
	 *
	 * @param exec the execution (never null)
	 * @param desktop the desktop. Ignored if null.
	 * @see #outZkTags(Execution,WebApp,String)
	 */
	public static String outZkTags(Execution exec, Desktop desktop) {
		return outZkTags(exec, desktop != null ? desktop.getWebApp(): null,
			desktop != null ? desktop.getDeviceType(): null);
	}

	/** The render context.
	 * @see HtmlPageRenders#getRenderContext
	 */
	public static class RenderContext {
		/** The extra writer used to generate crawlable content.
		 * It is never null.
		 */
		public final Writer extra;
		/** The extra writer used to generate the content that exists
		 * after the other being replaced with ZK widgets (never null).
		 * It is currenlty used only to generate CSS style.
		 */
		public final Writer perm;
		/** Indicates whether to generate crawlable content.
		 */
		public final boolean crawlable;
		private RenderContext(boolean crawlable) {
			this.extra = new StringWriter();
			this.perm = new StringWriter();
			this.crawlable = crawlable;
		}
	}
}

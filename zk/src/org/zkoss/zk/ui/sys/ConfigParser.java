/* ConfigParser.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sun Mar 26 18:09:10     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import java.util.Iterator;
import java.util.Map;
import java.net.URL;

import javax.servlet.jsp.el.ExpressionEvaluator;

import org.zkoss.lang.Classes;
import org.zkoss.util.resource.Locator;
import org.zkoss.util.logging.Log;
import org.zkoss.idom.Element;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.idom.util.IDOMs;

import org.zkoss.zk.ui.sys.UiEngine;
import org.zkoss.zk.ui.sys.DesktopCacheProvider;
import org.zkoss.zk.ui.sys.UiFactory;
import org.zkoss.zk.ui.sys.FailoverManager;
import org.zkoss.zk.ui.sys.LocaleProvider;
import org.zkoss.zk.ui.sys.TimeZoneProvider;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zk.ui.metainfo.DefinitionLoaders;
import org.zkoss.zk.scripting.Interpreters;

/**
 * Used to parse WEB-INF/zk.xml into {@link Configuration}.
 *
 * @author tomyeh
 */
public class ConfigParser {
	private static final Log log = Log.lookup(ConfigParser.class);

	/** Parses zk.xml, specified by url, into the configuration.
	 *
	 * @param url the URL of zk.xml.
	 */
	public void parse(URL url, Configuration config, Locator locator)
	throws Exception {
		if (url == null || config == null)
			throw new IllegalArgumentException("null");
		log.info("Parsing "+url);

		final Element root = new SAXBuilder(false, false, true)
			.build(url).getRootElement();
		for (Iterator it = root.getElements().iterator(); it.hasNext();) {
			final Element el = (Element)it.next();
			final String elnm = el.getName();
			if ("listener".equals(elnm)) {
				final String clsnm =
					IDOMs.getRequiredElementValue(el, "listener-class");
				try {
					final Class cls = Classes.forNameByThread(clsnm);
					config.addListener(cls);
				} catch (Throwable ex) {
					throw new UiException("Unable to load "+clsnm+", at "+el.getLocator(), ex);
				}
			} else if ("richlet".equals(elnm)) {
				final String clsnm =
					IDOMs.getRequiredElementValue(el, "richlet-class");
				final String path =
					IDOMs.getRequiredElementValue(el, "richlet-url");
				final Map params =
					IDOMs.parseParams(el, "init-param", "param-name", "param-value");
				try {
					config.addRichlet(path, clsnm, params);
				} catch (Throwable ex) {
					throw new UiException("Wrong richlet definition at "+el.getLocator(), ex);
				}
			} else if ("desktop-config".equals(elnm)) {
			//desktop-config
			//  disable-default-theme
			//	theme-uri
			//	desktop-timeout
			//	file-check-period
			//  processing-prompt-delay
			//	tooltip-delay
			//  keep-across-visits
				parseThemeUri(config, el);

				Element subel = el.getElement("disable-default-theme");
				if (subel != null) {
					String s = subel.getText(true);
					if (s.length() == 0)
						throw new UiException("The language name, such as xul/html, is required, "+subel.getLocator());
					config.enableDefaultTheme(s, false); //disable it
				}

				Integer v = parseInteger(el, "desktop-timeout", false);
				if (v != null) config.setDesktopMaxInactiveInterval(v.intValue());

				v = parseInteger(el, "file-check-period", true);
				if (v != null) System.setProperty("org.zkoss.util.resource.checkPeriod", v.toString());
					//System-wide property

				v = parseInteger(el, "processing-prompt-delay", true);
				if (v != null) config.setProcessingPromptDelay(v.intValue());
				v = parseInteger(el, "tooltip-delay", true);
				if (v != null) config.setTooltipDelay(v.intValue());

				String s = el.getElementValue("keep-across-visits", true);
				if (s != null)
					config.setKeepDesktopAcrossVisits(!"false".equals(s));
			} else if ("session-config".equals(elnm)) {
			//session-config
			//	session-timeout
			//	max-desktops-per-session
			//	timeout-uri
				Integer v = parseInteger(el, "session-timeout", false);
				if (v != null) config.setSessionMaxInactiveInterval(v.intValue());

				v = parseInteger(el, "max-desktops-per-session", true);
				if (v != null) config.setMaxDesktops(v.intValue());

				final String s = el.getElementValue("timeout-uri", true);
				if (s != null) config.setTimeoutURI(s);
			} else if ("language-config".equals(elnm)) {
			//language-config
			//	addon-uri
				parseLangAddon(locator, el);
			} else if ("system-config".equals(elnm)) {
			//system-config
			//	max-spare-threads
			//  max-suspended-threads
			//  max-upload-size
			//	response-charset
			//  cache-provider-class
			//  ui-factory-class
			//  failover-manager-class
			//	engine-class
			//  web-app-class
			//  locale-provider-class
			//	time-zone-provider-class
				Integer v = parseInteger(el, "max-spare-threads", true);
				if (v != null) config.setMaxSpareThreads(v.intValue());
				
				v = parseInteger(el, "max-suspended-threads", true);
				if (v != null) config.setMaxSuspendedThreads(v.intValue());

				v = parseInteger(el, "max-upload-size", true);
				if (v != null) config.setMaxUploadSize(v.intValue());

				final String s = el.getElementValue("response-charset", true);
				if (s != null) config.setCharset(s);

				Class cls = parseClass(el, "cache-provider-class",
					DesktopCacheProvider.class);
				if (cls != null) config.setDesktopCacheProviderClass(cls);

				cls = parseClass(el, "ui-factory-class", UiFactory.class);
				if (cls != null) config.setUiFactoryClass(cls);

				cls = parseClass(el, "failover-manager-class", FailoverManager.class);
				if (cls != null) config.setFailoverManagerClass(cls);

				cls = parseClass(el, "engine-class", UiEngine.class);
				if (cls != null) config.setUiEngineClass(cls);

				cls = parseClass(el, "web-app-class", UiEngine.class);
				if (cls != null) config.setWebAppClass(cls);

				cls = parseClass(el, "locale-provider-class", LocaleProvider.class);
				if (cls != null) config.setLocaleProviderClass(cls);

				cls = parseClass(el, "time-zone-provider-class", TimeZoneProvider.class);
				if (cls != null) config.setTimeZoneProviderClass(cls);
			} else if ("el-config".equals(elnm)) {
			//el-config
			//	evaluator-class
				Class cls = parseClass(el, "evaluator-class", ExpressionEvaluator.class);
				if (cls != null) System.setProperty("org.zkoss.el.ExpressionEvaluator.class", cls.getName());
					//System-wide property; reason: used in zcommon.jar
			} else if ("zscript-config".equals(elnm)) {
			//zscript-config
				Interpreters.add(el);
					//Note: zscript-config is applied to the whole system, not just langdef
			} else if ("log".equals(elnm)) {
				final String base = el.getElementValue("log-base", true);
				if (base != null)
					org.zkoss.util.logging.LogService.init(base, null); //start the log service
			} else if ("error-page".equals(elnm)) {
				final String clsnm =
					IDOMs.getRequiredElementValue(el, "exception-type");
				final String loc =
					IDOMs.getRequiredElementValue(el, "location");
				final Class cls;
				try {
					cls = Classes.forNameByThread(clsnm);
				} catch (Throwable ex) {
					throw new UiException("Unable to load "+clsnm+", at "+el.getLocator(), ex);
				}
				config.addErrorPage(cls, loc);
			} else if ("preference".equals(elnm)) {
				final String nm = IDOMs.getRequiredElementValue(el, "name");
				final String val = IDOMs.getRequiredElementValue(el, "value");
				config.setPreference(nm, val);
			} else {
				throw new UiException("Unknown element: "+elnm+", at "+el.getLocator());
			}
		}
	}

	/** Parse desktop-config/theme-uri. */
	private static void parseThemeUri(Configuration config, Element conf) {
		for (Iterator it = conf.getElements("theme-uri").iterator();
		it.hasNext();) {
			final Element el = (Element)it.next();
			String uri = el.getText().trim();
			if (uri.length() != 0) config.addThemeURI(uri);
		}
	}

	/** Parse language-config/addon-uri. */
	private static void parseLangAddon(Locator locator, Element conf) {
		for (Iterator it = conf.getElements("addon-uri").iterator();
		it.hasNext();) {
			final Element el = (Element)it.next();
			final String path = el.getText().trim();

			final URL url = locator.getResource(path);
			if (url == null)
				log.error("File not found: "+path+", at "+el.getLocator());
			else
				DefinitionLoaders.addLanguage(locator, url);
		}
	}
	/** Parse a class, if specified, whether it implements cls.
	 */
	private static Class parseClass(Element el, String elnm, Class cls) {
		final String clsnm = el.getElementValue(elnm, true);
		if (clsnm != null && clsnm.length() != 0) {
			try {
				final Class klass = Classes.forNameByThread(clsnm);
				if (cls != null && !cls.isAssignableFrom(klass))
					throw new UiException(clsnm+" must implement "+cls.getName()+", "+el.getLocator());
				log.info("Using "+clsnm+" for "+cls);
				return klass;
			} catch (Throwable ex) {
				throw new UiException("Unable to load "+clsnm+", at "+el.getLocator());
			}
		}
		return null;
	}

	/** Configures an integer. */
	private static Integer parseInteger(Element el, String subnm,
	boolean positiveOnly) throws UiException {
		String val = el.getElementValue(subnm, true);
		if (val != null && val.length() > 0) {
			try { 
				final int v = Integer.parseInt(val);
				if (positiveOnly && v <= 0)
					throw new UiException("The "+subnm+" element must be a positive number, not "+val+", at "+el.getLocator());
				return new Integer(v);
			} catch (NumberFormatException ex) { //eat
				throw new UiException("The "+subnm+" element must be a number, not "+val+", at "+el.getLocator());
			}
		}
		return null;
	}
}

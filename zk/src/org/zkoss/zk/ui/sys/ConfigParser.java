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

import org.zkoss.lang.Classes;
import org.zkoss.util.resource.Locator;
import org.zkoss.util.logging.Log;
import org.zkoss.idom.Element;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.idom.util.IDOMs;
import org.zkoss.xel.ExpressionFactory;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zk.ui.metainfo.DefinitionLoaders;
import org.zkoss.zk.scripting.Interpreters;
import org.zkoss.zk.device.Devices;

/**
 * Used to parse WEB-INF/zk.xml into {@link Configuration}.
 *
 * @author tomyeh
 */
public class ConfigParser {
	private static final Log log = Log.lookup(ConfigParser.class);

	/** Used to provide backward compatibility to 2.3.0's richlet definition. */
	private int _richletnm;

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
				final Map params =
					IDOMs.parseParams(el, "init-param", "param-name", "param-value");

				String path = el.getElementValue("richlet-url", true);
				if (path != null) {
				//deprecated since 2.4.0, but backward compatible
					final int cnt;
					synchronized (this) {
						cnt = _richletnm++;
					}
					final String name = "z_obs_" + Integer.toHexString(cnt);
					try {
						config.addRichlet(name, clsnm, params);
						config.addRichletMapping(name, path);
					} catch (Throwable ex) {
						throw new UiException("Illegal richlet definition at "+el.getLocator(), ex);
					}
				} else { //syntax since 2.4.0
					final String nm =
						IDOMs.getRequiredElementValue(el, "richlet-name");
					try {
						config.addRichlet(nm, clsnm, params);
					} catch (Throwable ex) {
						throw new UiException("Illegal richlet definition at "+el.getLocator(), ex);
					}
				}
			} else if ("richlet-mapping".equals(elnm)) { //syntax since 2.4.0
				final String nm =
					IDOMs.getRequiredElementValue(el, "richlet-name");
				final String path =
					IDOMs.getRequiredElementValue(el, "url-pattern");
				try {
					config.addRichletMapping(nm, path);
				} catch (Throwable ex) {
					throw new UiException("Illegal richlet mapping at "+el.getLocator(), ex);
				}
			} else if ("desktop-config".equals(elnm)) {
			//desktop-config
			//	desktop-timeout
			//  disable-default-theme
			//	file-check-period
			//	theme-uri
				parseDesktopConfig(config, el);
				parseClientConfig(config, el); //backward compatible with 2.4

			} else if ("client-config".equals(elnm)) { //since 2.5
			//client-config
			//  disable-behind-modal
			//  keep-across-visits
			//  processing-prompt-delay
			//	error-reload
			//	tooltip-delay
				parseClientConfig(config, el);

			} else if ("session-config".equals(elnm)) {
			//session-config
			//	session-timeout
			//	max-desktops-per-session
			//  timer-keep-alive
			//	timeout-uri (deprecated)
				Integer v = parseInteger(el, "session-timeout", false);
				if (v != null) config.setSessionMaxInactiveInterval(v.intValue());

				v = parseInteger(el, "max-desktops-per-session", false);
				if (v != null) config.setMaxDesktops(v.intValue());

				String s = el.getElementValue("timer-keep-alive", true);
				if (s != null) config.setTimerKeepAlive("true".equals(s));

				//deprecated since 2.4.0, but backward compatible
				s = el.getElementValue("timeout-uri", true);
				if (s != null) Devices.setTimeoutURI("ajax", s);
			} else if ("language-config".equals(elnm)) {
			//language-config
			//	addon-uri
				parseLangAddon(locator, el);
			} else if ("language-mapping".equals(elnm)) {
			//language-mapping
			//	language-name/extension
				DefinitionLoaders.addExtension(
					IDOMs.getRequiredElementValue(el, "extension"),
					IDOMs.getRequiredElementValue(el, "language-name"));
				//Note: we don't add it to LanguageDefinition now
				//since addon-uri might be specified later
				//(so we cannot load definitions now)
			} else if ("system-config".equals(elnm)) {
			//system-config
			//  disable-event-thread
			//	max-spare-threads
			//  max-suspended-threads
			//  max-upload-size
			//  max-process-time
			//	response-charset
			//  cache-provider-class
			//  ui-factory-class
			//  failover-manager-class
			//	engine-class
			//	id-generator-class
			//  web-app-class
			//  locale-provider-class
			//	time-zone-provider-class
			//  server-push-class
				String s = el.getElementValue("disable-event-thread", true);
				if (s != null) config.enableEventThread("false".equals(s));

				Integer v = parseInteger(el, "max-spare-threads", false);
				if (v != null) config.setMaxSpareThreads(v.intValue());
				
				v = parseInteger(el, "max-suspended-threads", false);
				if (v != null) config.setMaxSuspendedThreads(v.intValue());

				v = parseInteger(el, "max-upload-size", false);
				if (v != null) config.setMaxUploadSize(v.intValue());

				v = parseInteger(el, "max-process-time", true);
				if (v != null) config.setMaxProcessTime(v.intValue());

				s = el.getElementValue("upload-charset", true);
				if (s != null) config.setUploadCharset(s);

				s = el.getElementValue("response-charset", true);
				if (s != null) config.setResponseCharset(s);

				Class cls = parseClass(el, "cache-provider-class",
					DesktopCacheProvider.class);
				if (cls != null) config.setDesktopCacheProviderClass(cls);

				cls = parseClass(el, "ui-factory-class", UiFactory.class);
				if (cls != null) config.setUiFactoryClass(cls);

				cls = parseClass(el, "failover-manager-class", FailoverManager.class);
				if (cls != null) config.setFailoverManagerClass(cls);

				cls = parseClass(el, "engine-class", UiEngine.class);
				if (cls != null) config.setUiEngineClass(cls);

				cls = parseClass(el, "id-generator-class", IdGenerator.class);
				if (cls != null) config.setIdGeneratorClass(cls);

				cls = parseClass(el, "web-app-class", WebApp.class);
				if (cls != null) config.setWebAppClass(cls);

				cls = parseClass(el, "server-push-class", WebApp.class);
				if (cls != null) config.setServerPushClass(cls);

			} else if ("xel-config".equals(elnm)) {
			//xel-config
			//	evaluator-class
				Class cls = parseClass(el, "evaluator-class", ExpressionFactory.class);
				if (cls != null) System.setProperty("org.zkoss.xel.ExpressionFactory.class", cls.getName());
					//System-wide property; reason: used in zcommon.jar
			} else if ("zscript-config".equals(elnm)) {
			//zscript-config
				Interpreters.add(el);
					//Note: zscript-config is applied to the whole system, not just langdef
			} else if ("device-config".equals(elnm)) {
			//device-config
				Devices.add(el);
					//Note: device-config is applied to the whole system, not just langdef
			} else if ("log".equals(elnm)) {
				final String base = el.getElementValue("log-base", true);
				if (base != null)
					org.zkoss.util.logging.LogService.init(base, null); //start the log service
			} else if ("error-page".equals(elnm)) {
			//error-page
				final String clsnm =
					IDOMs.getRequiredElementValue(el, "exception-type");
				final String loc =
					IDOMs.getRequiredElementValue(el, "location");
				String devType = el.getElementValue("device-type", true);
				if (devType == null) devType = "ajax";
				else if (devType.length() == 0)
					throw new UiException("device-type not specified at "+el.getLocator());

				final Class cls;
				try {
					cls = Classes.forNameByThread(clsnm);
				} catch (Throwable ex) {
					throw new UiException("Unable to load "+clsnm+", at "+el.getLocator(), ex);
				}

				config.addErrorPage(devType, cls, loc);
			} else if ("preference".equals(elnm)) {
				final String nm = IDOMs.getRequiredElementValue(el, "name");
				final String val = IDOMs.getRequiredElementValue(el, "value");
				config.setPreference(nm, val);
			} else {
				throw new UiException("Unknown element: "+elnm+", at "+el.getLocator());
			}
		}
	}

	/** Parses desktop-config. */
	private static void parseDesktopConfig(Configuration config, Element conf) {
		//theme-uri
		for (Iterator it = conf.getElements("theme-uri").iterator();
		it.hasNext();) {
			final Element el = (Element)it.next();
			String uri = el.getText().trim();
			if (uri.length() != 0) config.addThemeURI(uri);
		}

		//disable-default-theme
		Element subel = conf.getElement("disable-default-theme");
		if (subel != null) {
			String s = subel.getText(true);
			if (s.length() == 0)
				throw new UiException("The language name, such as xul/html, is required, "+subel.getLocator());
			config.enableDefaultTheme(s, false); //disable it
		}

		//desktop-timeout
		Integer v = parseInteger(conf, "desktop-timeout", false);
		if (v != null) config.setDesktopMaxInactiveInterval(v.intValue());

		//file-check-period
		v = parseInteger(conf, "file-check-period", true);
		if (v != null) System.setProperty("org.zkoss.util.resource.checkPeriod", v.toString());
			//System-wide property

	}
	/** Parses client-config. */
	private static void parseClientConfig(Configuration config, Element conf) {
		Integer v = parseInteger(conf, "processing-prompt-delay", true);
		if (v != null) config.setProcessingPromptDelay(v.intValue());

		v = parseInteger(conf, "tooltip-delay", true);
		if (v != null) config.setTooltipDelay(v.intValue());

		String s = conf.getElementValue("keep-across-visits", true);
		if (s != null)
			config.setKeepDesktopAcrossVisits(!"false".equals(s));

		s = conf.getElementValue("disable-behind-modal", true);
		if (s != null) config.enableDisableBehindModal(!"false".equals(s));

		//error-reload
		for (Iterator it = conf.getElements("error-reload").iterator();
		it.hasNext();) {
			final Element el = (Element)it.next();

			v = parseInteger(el, "error-code", true);
			if (v == null)
				throw new UiException("error-code is required, "+el.getLocator());
			String uri = IDOMs.getRequiredElementValue(el, "reload-uri");
			if ("false".equals(uri)) uri = null;

			config.addClientErrorReload(v.intValue(), uri);
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
				DefinitionLoaders.addAddon(locator, url);
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

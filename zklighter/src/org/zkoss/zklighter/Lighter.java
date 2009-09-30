/* Lighter.java

	Purpose:
		
	Description:
		
	History:
		Thu Jan 22 11:06:33     2009, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
package org.zkoss.zklighter;

import java.util.*;
import java.io.*;

import org.zkoss.lang.SystemException;
import org.zkoss.util.Locales;
import org.zkoss.util.logging.Log;
import org.zkoss.io.Files;
import org.zkoss.io.FileWriter;
import org.zkoss.io.FileReader;
import org.zkoss.idom.Element;
import org.zkoss.idom.input.SAXBuilder;
import org.zkoss.idom.util.IDOMs;

import org.zkoss.zk.ui.http.FileWpdExtendlet;
import org.zkoss.zk.ui.http.FileWcsExtendlet;

/**
 * The entry of ZK Lighter
 *
 * @author tomyeh
 */
public class Lighter {
	private static int _cnt = 0;

	public static void main(String[] args) throws Exception {
		Log.setHierarchy(false);
		Log.lookup("org.zkoss").setLevel(Log.ERROR);

		if (args.length != 1) {
			System.err.println("ZK Lighter - ZK Light JavaScript/CSS Generator\n\n"
			+"Usage:\n\tjava -classpath $CP org.zkoss.zklighter.Lighter zklighter.xml\n");
			System.exit(-1);
		}
		final FileWpdExtendlet wpd = new FileWpdExtendlet();
		final FileWcsExtendlet wcs = new FileWcsExtendlet();
		for (Iterator it = new SAXBuilder(true, false, true).build(args[0])
		.getRootElement().getElements().iterator(); it.hasNext();) {
			final Element el = (Element)it.next();
			final String nm = el.getName();
			if ("javascript".equals(nm)) genJS(wpd, el);
			else if ("css".equals(nm)) genCSS(wcs, el);
			else if ("copy".equals(nm)) copy(el);
			else throw new IOException("Unknown "+nm+", "+el.getLocator());
		}
		System.out.println(_cnt+" files are processed successfully");
	}

	//copy//
	private static void copy(Element el) throws IOException {
		final File dst = new File(IDOMs.getRequiredElementValue(el, "destination"));
		for (Iterator it = el.getElements("source").iterator(); it.hasNext();) {
			final File src = new File(((Element)it.next()).getText(true));
			if (dst.isFile())
				throw new IOException("Directory required: "+dst);
			dst.mkdirs();
			++_cnt;
			Files.copy(dst, src, Files.CP_UPDATE|Files.CP_SKIP_SVN);
		}
	}

	//JS//
	private static void genJS(FileWpdExtendlet wpd, Element el) throws IOException {
		final File dst = new File(IDOMs.getRequiredElementValue(el, "destination"));
		final List srcs = new LinkedList();
		for (Iterator it = el.getElements("source").iterator(); it.hasNext();) {
			final Element e = (Element)it.next();
			final File fl = new File(e.getText(true));
			if (!fl.exists())
				throw new FileNotFoundException("Not found: "+fl+", "+e.getLocator());
			srcs.add(fl);
		}

		boolean done = false;
		for (Iterator it = el.getElements("locale").iterator(); it.hasNext();) {
			final Element e = (Element)it.next();
			final String locale = e.getText(true);
			Locales.setThreadLocal(new Locale(locale));
			outJS(wpd, dst, srcs, locale, false);
			outJS(wpd, dst, srcs, locale, true);
			Locales.setThreadLocal(null);
			done = true;
		}
		if (!done) {
			outJS(wpd, dst, srcs, null, false);
			outJS(wpd, dst, srcs, null, true);
		}
	}
	private static void outJS(FileWpdExtendlet wpd, File dst, List srcs,
	String locale, boolean debugJS) throws IOException {
		wpd.setDebugJS(debugJS);
		if (debugJS || locale != null) {
			final String dstnm = dst.getName();
			final int j = dstnm.lastIndexOf('.');
			final StringBuffer sb = new StringBuffer();
			if (debugJS) sb.append("src/");

			sb.append(j >= 0 ? dstnm.substring(0, j): dstnm);
			if (locale != null && locale.length() > 0)
				sb.append('_').append(locale);
			if (j >= 0)
				sb.append(dstnm.substring(j));

			dst = new File(dst.getParent(), sb.toString());
		}

		++_cnt;
		dst.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(dst);
		try {
			for (Iterator it = srcs.iterator(); it.hasNext();)
				out.write(wpd.service((File)it.next()));
		} catch (Throwable ex) {
			if (ex instanceof IOException) throw (IOException)ex;
			throw SystemException.Aide.wrap(ex);
		} finally {
			out.close();
		}
	}

	//CSS//
	private static void genCSS(FileWcsExtendlet wcs, Element el) throws IOException {
		final File dst = new File(IDOMs.getRequiredElementValue(el, "destination"));
		final List srcs = new LinkedList();
		for (Iterator it = el.getElements("source").iterator(); it.hasNext();) {
			final Element e = (Element)it.next();
			final File fl = new File(e.getText(true));
			if (!fl.exists())
				throw new FileNotFoundException("Not found: "+fl+", "+e.getLocator());
			srcs.add(new Object[] {
				fl, IDOMs.getRequiredAttributeValue(e, "class-web-path")});
		}

		//merge all browser CSS into one
		outCSS(wcs, new CSSInfo(el), dst, srcs);
	}
	private static
	void outCSS(FileWcsExtendlet wcs, CSSInfo ci, File dst, List srcs)
	throws IOException {
		++_cnt;
		Writer out = new FileWriter(dst, "UTF-8");
		try {
			for (Iterator it = srcs.iterator(); it.hasNext();) {
				final Object[] inf = (Object[])it.next();
				ci.source = (File)inf[0];
				ci.classWebPath = (String)inf[1];
				out.write(wcs.service(ci.source, new Includer(ci)));
			}
		} catch (Throwable ex) {
			if (ex instanceof IOException) throw (IOException)ex;
			throw SystemException.Aide.wrap(ex);
		} finally {
			out.close();
		}
	}
	private static
	void outCSS(Writer out, CSSInfo ci, File src)
	throws IOException {
		ci.source = src;
		ci.lineno = 1;
		final String in = Files.readAll(new FileReader(src, "UTF-8")).toString();
		for (int j = 0, len = in.length(); j < len; ++j) {
			char cc = in.charAt(j);
			if (cc == '$' && j + 1 < len && in.charAt(j + 1) == '{') {
				int k = in.indexOf('}', j += 2);
				if (k < 0)
					throw new IOException(ci.message("Non-terminated EL"));
				outEL(out, in.substring(j, k), ci);
				j = k;
			} else if (cc == '<' && j + 2 < len) {
				cc = in.charAt(++j);
				int k = in.indexOf('>', ++j);
				if (k < 0)
					throw new IOException(ci.message("Non-terminated <"));

				if (cc == 'c' && in.charAt(j++) == ':') { //restrict but safer
					outDirective(out, ci, in.substring(j, k));
				} else if (cc == '%') {
					if (in.charAt(j + 1) == '-') {
						k = in.indexOf("--%>", ++j);
						if (k < 0)
							throw new IOException("Non-terminated <%--");
						k += 3;
					}
				} else if (cc != '/')
					throw new IOException(ci.message("Unknown <"+cc));
				j = k;
			} else {
				if (cc == '\n') ++ci.lineno;
				out.write(cc);
			}
		}
	}
	private static void outEL(Writer out, String cnt, CSSInfo ci)
	throws IOException {
		int j = cnt.indexOf("encodeURL");
		if (j < 0) {
			String s = (String)ci.vars.get(cnt);
			if (s != null)
				out.write(s);
			else if (isFormula(cnt)) { //formula
				out.write('?'); //mark error
				out.write(cnt);
				out.write('?');
			} else
				throw new IOException(ci.message("Unknown EL, ${"+ cnt+"}"));
			return;
		}

		//encodeURL
		j = cnt.indexOf('\'', j);
		int k = cnt.indexOf('\'', ++j);
		if (j <= 0 || k < 0)
			throw new IOException(ci.message("Unknown EL, ${"+ cnt+"}: '...' not found"));
		cnt = cnt.substring(j, k);
		for (Iterator it = ci.translates.entrySet().iterator(); it.hasNext();) {
			final Map.Entry me = (Map.Entry)it.next();
			final String nm = (String)me.getKey();
			j = cnt.indexOf(nm);
			if (j >= 0) {
				cnt = cnt.substring(0, j) + me.getValue() + cnt.substring(j + nm.length());
				break; //done
			}
		}
		out.write(cnt);
	}
	private static boolean isFormula(String s) {
		for (int j = s.length(); --j >= 0;) {
			char cc = s.charAt(j);
			if ((cc > 'z' || cc < 'a') && (cc > 'Z' || cc < 'A')
			&& (cc > '9' || cc < '0') && cc != '_' && cc != '$')
				return true;
		}
		return false;
	}
	private static void outDirective(Writer out, CSSInfo ci, String cnt)
	throws IOException {
		int j = cnt.indexOf(' ');
		if (j < 0) j = cnt.length();

		String nm = cnt.substring(0, j);
		if ("include".equals(nm)) {
			j = cnt.indexOf("page=\"", j);
			if (j < 0)
				throw new IOException(ci.message("The page attribute not found"));
			include(out, ci, cnt.substring(j += 6, cnt.indexOf('"', j)));
		} else if ("choose".equals(nm) || "when".equals(nm) || "otherwise".equals(nm)
		|| "if".equals(nm) || "set".equals(nm)) {
			out.write("//?");
			out.write(cnt);
		} else
			throw new IOException(ci.message("Unknown <c:"+nm));
	}
	private static void include(Writer out, CSSInfo ci, String uri)
	throws IOException {
		if (!uri.startsWith("~./"))
			throw new IOException(ci.message("Unknown URI: "+uri));

		int oldln = ci.lineno;
		File oldsrc = ci.source;
		try {
			outCSS(out, ci, new File(ci.classWebPath + uri.substring(2)));
		} finally {
			ci.lineno = oldln;
			ci.source = oldsrc;
		}
	}

	private static class Includer implements FileWcsExtendlet.Includer {
		private final CSSInfo _ci;
		private Includer(CSSInfo ci) {
			_ci = ci;
		}
		public void include(String uri, Writer out) throws IOException {
			Lighter.include(out, _ci, uri);
		}
	}
	private static class CSSInfo {
		private final Map vars = new HashMap();
		private final Map translates = new LinkedHashMap();
		/** The current line number in {@link #source}. */
		private int lineno;
		/** The source file to parse. */
		private File source;
		private String classWebPath;

		private CSSInfo(Element el) {
			for (Iterator it = el.getElements("variable").iterator(); it.hasNext();) {
				final Element e = (Element)it.next();
				this.vars.put(IDOMs.getRequiredAttributeValue(e, "name"), e.getText(true));
			}

			el = el.getElement("encodeURL");
			if (el == null)
				return;

			for (Iterator it = el.getElements("translate").iterator(); it.hasNext();) {
				final Element e = (Element)it.next();
				this.translates.put(IDOMs.getRequiredAttributeValue(e, "from"),
					IDOMs.getRequiredAttributeValue(e, "to"));
			}
		}
		private String message(String msg) {
			return msg + " at line "+lineno+", "+source;
		}
	}
}

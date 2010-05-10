/* HtmlNativeComponent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jul 19 18:05:01     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui;

import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.Writer;

import org.zkoss.xml.HTMLs;
import org.zkoss.xml.XMLs;
import org.zkoss.idom.Namespace;

import org.zkoss.zk.ui.sys.ComponentsCtrl;
import org.zkoss.zk.ui.ext.DynamicTag;
import org.zkoss.zk.ui.ext.Native;
import org.zkoss.zk.ui.ext.render.PrologAllowed;
import org.zkoss.zk.ui.impl.NativeHelpers;
import org.zkoss.zk.fn.ZkFns;

/**
 * A comonent used to represent XML elements that are associated
 * with the inline namespace (http://www.zkoss.org/2005/zk/inline).
 *
 * <p>It contains the content that shall be sent directly to client.
 * It has three parts: prolog, children and epilog.
 * The prolog ({@link #getPrologContent}) and epilog ({@link #getEpilogContent})
 * are both {@link String}.
 *
 * <p>When this component is renderred ({@link #redraw}), it generates
 * the prolog first, the children and then the epilog.
 *
 * @author tomyeh
 * @since 3.0.0
 */
public class HtmlNativeComponent extends AbstractComponent
implements DynamicTag, Native {
	private static Helper _helper = new HtmlHelper();
	private static final String ATTR_ZKHEAD_FOUND = "org.zkoss.zk.ui.zkheadFound";

	private String _tag;
	private String _prolog = "", _epilog = "";
	/** The text before the tag name. */
	private String _prefix;
	private Map _props;
	/** Declared namespaces ({@link Namespace}). */
	private List _dns;

	/** Contructs a {@link HtmlNativeComponent} component.
	 * 
	 */
	public HtmlNativeComponent() {
	}
	/** Constructs a {@link HtmlNativeComponent} component with the specified tag name.
	 *
	 * @param tag the tag name. If null or empty, plain text is assumed.
	 */
	public HtmlNativeComponent(String tag) {
		setTag(tag);
	}

	/** Contructs a {@link HtmlNativeComponent} component with the specified
	 * prolog ad epilog.
	 */
	public HtmlNativeComponent(String tag, String prolog, String epilog) {
		this(tag);

		_prolog = prolog != null ? prolog: "";
		_epilog = epilog != null ? epilog: "";
	}

	/** Returns the tag name, or null if plain text.
	 */
	public String getTag() {
		return _tag;
	}

	//Native//
	public List getDeclaredNamespaces() {
		return _dns != null ? _dns: Collections.EMPTY_LIST;
	}
	public void addDeclaredNamespace(Namespace ns) {
		if (ns == null)
			throw new IllegalArgumentException();

		if (_dns == null)
			_dns = new LinkedList();
		_dns.add(ns);
	}
	public String getPrologContent() {
		return _prolog;
	}
	public void setPrologContent(String prolog) {
		_prolog = prolog != null ? prolog: "";
	}
	public String getEpilogContent() {
		return _epilog;
	}
	public void setEpilogContent(String epilog) {
		_epilog = epilog != null ? epilog: "";
	}

	public Helper getHelper() {
		return _helper;
	}

	//-- Component --//
	public void setId(String id) {
		super.setId(id);
		setDynamicProperty("id", id);
	}
	public boolean setVisible(boolean visible) {
		throw new UnsupportedOperationException("Use client-dependent attribute, such as display:none");
	}

	public void redraw(Writer out) throws java.io.IOException {
		final StringBuffer sb = new StringBuffer(128);
		final Helper helper = getHelper();
			//don't use _helper directly, since the derive might override it

		if (_prefix != null)
			sb.append(_prefix);

		//first half
		helper.getFirstHalf(sb, _tag, _props, _dns);

		//prolog
		sb.append(_prolog); //no encoding
		boolean zktagGened = replaceZkhead(sb, false);
		final String tn = _tag != null ? _tag.toLowerCase(): "";
		if (!zktagGened && ("html".equals(tn) || "body".equals(tn)
		|| "head".equals(tn))) {//<head> might be part of _prolog
			final int j = indexOfHead(sb);
			if (j >= 0) {
				zktagGened = true;
				final String zktags = ZkFns.outZkHeadHtmlTags(getPage());
				if (zktags != null)
					sb.insert(j, zktags);
			}
		}
		write(out, sb);

		//children
		for (Iterator it = getChildren().iterator(); it.hasNext();)
			((Component)it.next()).redraw(out);

		//epilog
		sb.append(_epilog);

		//second half
		helper.getSecondHalf(sb, _tag);
		zktagGened = replaceZkhead(sb, zktagGened);
		if (!zktagGened && ("html".equals(tn) || "body".equals(tn))) {
			final int j = sb.lastIndexOf("</" + _tag);
			if (j >= 0) {
				final String zktags = ZkFns.outZkHeadHtmlTags(getPage());
				if (zktags != null)
					sb.insert(j, zktags);
			}
		}
		out.write(sb.toString());
	}
	private boolean replaceZkhead(StringBuffer sb, boolean zktagGened) {
		final Execution exec = Executions.getCurrent();
		if (exec == null || exec.getAttribute(ATTR_ZKHEAD_FOUND) == null) {
			final int j = sb.indexOf("<zkhead/>");
			if (j >= 0) {
				if (exec != null)
					exec.setAttribute(ATTR_ZKHEAD_FOUND, Boolean.TRUE);
					//Note: we allow only one zkhead (for better performance)

				if (!zktagGened) {
					final String zktags = ZkFns.outZkHeadHtmlTags(getPage());
					if (zktags != null) {
						sb.replace(j, j + 9, zktags);
						return true;
					}
					zktagGened = true;
				}
				sb.delete(j, j + 9);
			}
		}
		return zktagGened;
	}
	/** Writes the content of stringbuffer to the writer.
	 * After written, stringbuffer is reset.
	 */
	private static void write(Writer out, StringBuffer sb)
	throws java.io.IOException {
		out.write(sb.toString());
		sb.setLength(0);
	}
	/** Search <head> case-insensitive. */
	private static int indexOfHead(StringBuffer sb) {
		for (int j = 0, len = sb.length(); (j = sb.indexOf("<", j)) >= 0;) {
			if (j + 6 > len) break; //not found

			char cc = sb.charAt(++j);
			if (cc != 'h' && cc != 'H') continue;
			cc = sb.charAt(++j);
			if (cc != 'e' && cc != 'E') continue;
			cc = sb.charAt(++j);
			if (cc != 'a' && cc != 'A') continue;
			cc = sb.charAt(++j);
			if (cc != 'd' && cc != 'D') continue;
			j = sb.indexOf(">", j + 1);
			return j >= 0 ? j + 1: -1;
		}
		return -1;
	}

	//DynamicTag//
	/** Sets the tag name.
	 *
	 * @param tag the tag name. If null or empty, plain text is assumed.
	 */
	public void setTag(String tag) throws WrongValueException {
		_tag = tag != null && tag.length() > 0 ? tag: null;
	}
	public boolean hasTag(String tag) {
		return true; //accept anything
	}
	public boolean hasDynamicProperty(String name) {
		return ComponentsCtrl.isReservedAttribute(name);
	}
	public Object getDynamicProperty(String name) {
		return _props != null ? _props.get(name): null;
	}
	public void setDynamicProperty(String name, Object value)
	throws WrongValueException {
		if (name == null)
			throw new WrongValueException("name required");

		if (value == null) {
			if (_props != null) _props.remove(name);
		} else {
			if (_props == null)
				_props = new LinkedHashMap();
			_props.put(name, value);
		}
	}

	/** The HTML helper.
	 */
	public static class HtmlHelper implements Helper {
		public Component newNative(String text) {
			final HtmlNativeComponent nc = new HtmlNativeComponent();
			if (text != null)
				nc.setPrologContent(text);
			return nc;
		}
		public void getFirstHalf(StringBuffer sb, String tag,
		Map props, Collection namespaces) {
			if (tag != null)
				sb.append('<').append(tag);

			NativeHelpers.getAttributes(sb, props, namespaces);

			if (tag != null) {
				final String tn = tag.toLowerCase();
				if ("zkhead".equals(tn) || HTMLs.isOrphanTag(tn))
					sb.append('/');
				sb.append('>');
			}
		}
		public void getSecondHalf(StringBuffer sb, String tag) {
			if (tag != null) {
				final String tn = tag.toLowerCase();
				if ("zkhead".equals(tn) || HTMLs.isOrphanTag(tn))
					return;

				sb.append("</").append(tag).append('>');
			}
		}
		public void appendText(StringBuffer sb, String text) {
			sb.append(text); //don't encode (bug 2689443)
		}
	}

	protected Object newExtraCtrl() {
		return new ExtraCtrl();
	}
	protected class ExtraCtrl implements PrologAllowed {
		//-- PrologAware --//
		public void setPrologContent(String prolog) {
			_prefix = prolog;
				//Notice: it is used as prefix (shown before the tag and children)
				//while _prolog is the text shown after the tag and before the children
		}
	}
}

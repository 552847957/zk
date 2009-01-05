/* RenderContext.java

	Purpose:
		
	Description:
		
	History:
		Mon Jan  5 11:48:18     2009, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
package org.zkoss.zhtml.impl;

import java.util.Iterator;
import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;

/**
 * The render context used to render the additional part (JavaScript
 * code snippet).
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class RenderContext {
	/** The writer to output JavaScript codes.
	 */
	private final StringBuffer _jsout = new StringBuffer();

	/** Whether to generate HTML tags directly.
	 *
	 * <p>If true, the HTML tag shall be generated directly to the writer
	 * provided by {@link org.zkoss.zk.ui.sys.ComponentCtrl#redraw},
	 * and generates JavaScript code snippet in {@link #renderBegin}.
	 *
	 * <p>If false, ZHTML components shall generate properties by use of
	 * {@link org.zkoss.zk.ui.sys.ContentRenderer}.
	 */
	public boolean directContent = true;

	/** Completes the rendering by returning what are generated
	 * by {@link #renderBegin} and {@link #renderEnd} (never null).
	 * After rendering, the context is reset.
	 */
	public String complete() {
		if (_jsout.length() > 0) {
			_jsout.insert(0, "<script>\nzkblbg(true);try{");
			_jsout.append("\n}finally{zkble();}</script>");
			final String txt = _jsout.toString();
			_jsout.setLength(0); //reset
			return txt;
		}
		return "";
	}
	/** Renders the beginning JavaScript code snippet for the component.
	 * It must be called before rendering the children.
	 *
	 * @param clientEvents a collection of client events.
	 * It is ignored if lookup is true.
	 * @param lookup whether to look up instead of creating a widget.
	 * Specifies true if the widget is created somewhere else.
	 */
	public void
	renderBegin(Component comp, Collection clientEvents, boolean lookup) {
		_jsout.append("\nzkb2('")
			.append(comp.getUuid());

		final String wgtcls = lookup ? "zk.RefWidget": comp.getWidgetClass();
		boolean wgtclsGened = false;
		if (!"zhtml.Widget".equals(wgtcls)) {
			wgtclsGened = true;
			_jsout.append("','").append(wgtcls);
		}

		_jsout.append('\'');

		if (!lookup && clientEvents != null) {
			boolean first = true;
			for (Iterator it = clientEvents.iterator(); it.hasNext();) {
				final String evtnm = (String)it.next();
				if (Events.isListened(comp, evtnm, false)) {
					_jsout.append(',');
					if (first) {
						first = false;
						if (!wgtclsGened) _jsout.append("null,");
						_jsout.append('{');
					}
					_jsout.append('$').append(evtnm).append(':')
						.append(Events.isListened(comp, evtnm, true));
						//$onClick and so on
				}
			}
			if (!first) _jsout.append('}');
		}

		_jsout.append(");");
	}
	/** Renders the ending JavaScript code snippet for the component.
	 * It must be called after rendering the children.
	 */
	public void renderEnd(Component comp) {
		_jsout.append("zke();");
	}
}

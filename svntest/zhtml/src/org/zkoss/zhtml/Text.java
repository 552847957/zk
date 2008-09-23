/* Text.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Nov 24 15:17:07     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zhtml;

import java.io.Writer;
import java.io.IOException;

import org.zkoss.lang.Objects;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.ext.RawId;
import org.zkoss.zk.ui.metainfo.LanguageDefinition;
import org.zkoss.zk.ui.sys.ComponentCtrl;

/**
 * Represents a piece of text (of DOM).
 *
 * @author tomyeh
 */
public class Text extends AbstractComponent implements RawId {
	private String _value = "";

	public Text() {
	}
	public Text(String value) {
		setValue(value);
	}

	/** Returns the value.
	 * <p>Default: "".
	 */
	public String getValue() {
		return _value;
	}
	/** Sets the value.
	 */
	public void setValue(String value) {
		if (value == null)
			value = "";
		if (!Objects.equals(_value, value)) {
			_value = value;
			invalidate();
		}
	}

	/** Whether to generate the value directly without ID. */
	private boolean isIdRequired() {
		final Component p = getParent();
		return p == null || !isVisible()
			|| !Components.isAutoId(getId()) || !isRawLabel(p);
	}
	private static boolean isRawLabel(Component comp) {
		final LanguageDefinition langdef =
			comp.getDefinition().getLanguageDefinition();
		return langdef != null && langdef.isRawLabel();
	}

	//-- Component --//
	public void setParent(Component parent) {
		if (!isIdRequired()) {
			final Component old = getParent();
			if (old != parent) {
				if (old != null) old.invalidate();
				if (parent != null) parent.invalidate();
			}
		}
		super.setParent(parent);
	}
	public void invalidate() {
		if (isIdRequired()) super.invalidate();
		else getParent().invalidate();
	}
	public void redraw(Writer out) throws IOException {
		final boolean idRequired = isIdRequired();
		if (idRequired) {
			out.write("<span id=\"");
			out.write(getUuid());
			out.write("\">");
		}

		out.write(_value);

		if (idRequired)
			out.write("</span>");
	}
	public boolean isChildable() {
		return false;
	}
}

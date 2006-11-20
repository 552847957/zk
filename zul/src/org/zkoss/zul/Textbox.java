/* Textbox.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jun 14 15:48:28     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import org.zkoss.lang.Objects;
import org.zkoss.xml.HTMLs;

import org.zkoss.zk.ui.WrongValueException;

import org.zkoss.zul.impl.InputElement;

/**
 * A textbox.
 *
 * <p>See <a href="package-summary.html">Specification</a>.</p>
 *
 * @author tomyeh
 */
public class Textbox extends InputElement {
	private String _type = "text";
	private int _rows = 1;
	private boolean _multiline;

	public Textbox() {
	}
	public Textbox(String value) throws WrongValueException {
		setValue(value);
	}

	/** Returns the value.
	 * The same as {@link #getText}.
	 * <p>Default: "".
	 * @exception WrongValueException if user entered a wrong value
	 */
	public String getValue() throws WrongValueException {
		return getText();
	}
	/** Sets the value.
	 *
	 * @param value the value; If null, it is considered as empty.
	 * @exception WrongValueException if value is wrong
	 */
	public void setValue(String value) throws WrongValueException {
		setText(value);
	}

	//-- super --//
	/** Coerces the value passed to {@link #setValue}.
	 *
	 * <p>Default: convert null to an empty string.
	 */
	protected Object coerceFromString(String value) throws WrongValueException {
		return value != null ? value: "";
	}
	/** Coerces the value passed to {@link #setValue}.
	 *
	 * <p>Default: convert null to an empty string.
	 */
	protected String coerceToString(Object value) {
		return value != null ? (String)value: "";
	}

	/** Returns the type.
	 * <p>Default: text.
	 */
	public String getType() {
		return _type;
	}
	/** Sets the type.
	 * @param type the type. Acceptable values are "text" and "password".
	 * Unlike XUL, "timed" is redudant because it is enabled as long as
	 * onChanging is added.
	 */
	public void setType(String type) throws WrongValueException {
		if (!"text".equals(type) && !"password".equals(type))
			throw new WrongValueException("Illegal type: "+type);

		if (!_type.equals(type)) {
			_type = type;
			invalidate();
		}
	}

	/** Returns the rows.
	 * <p>Default: 1.
	 */
	public int getRows() {
		return _rows;
	}
	/** Sets the rows.
	 */
	public void setRows(int rows) throws WrongValueException {
		if (rows <= 0)
			throw new WrongValueException("Illegal rows: "+rows);

		if (_rows != rows) {
			_rows = rows;
			if (_rows > 1)
				setMultiline(true); //auto-enable

			smartUpdate("rows", Integer.toString(_rows));
		}
	}
	/** Returns whether it is multiline.
	 * <p>Default: false.
	 */
	public boolean isMultiline() {
		return _multiline;
	}
	/** Sets whether it is multiline.
	 */
	public void setMultiline(boolean multiline) {
		if (_multiline != multiline) {
			_multiline = multiline;
			invalidate(); //different element
		}
	}

	//-- super --//
	public String getInnerAttrs() {
		final String attrs = super.getInnerAttrs();
		if (!_multiline) return attrs;

		final StringBuffer sb = new StringBuffer(64).append(attrs);
		HTMLs.appendAttribute(sb, "rows",  _rows);
		return sb.toString();
	}
	public String getOuterAttrs() {
		final String attrs = super.getOuterAttrs();
		return _multiline ? attrs + " z.skipOK=\"true\"": attrs;
	}
}

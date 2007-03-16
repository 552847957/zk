/* Intbox.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jun 28 13:39:37     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.Locale;

import org.zkoss.zk.ui.WrongValueException;

import org.zkoss.zul.mesg.MZul;
import org.zkoss.zul.impl.FormatInputElement;

/**
 * An edit box for holding an integer.
 *
 * @author tomyeh
 */
public class Intbox extends FormatInputElement {
	public Intbox() {
		setCols(11);
	}
	public Intbox(int value) throws WrongValueException {
		this();
		setValue(new Integer(value));
	}

	/** Returns the value (in Integer), might be null unless
	 * a constraint stops it.
	 * @exception WrongValueException if user entered a wrong value
	 */
	public Integer getValue() throws WrongValueException {
		return (Integer)getRawValue();
	}
	/** Returns the value in int. If null, zero is returned.
	 */
	public int intValue() throws WrongValueException {
		final Object val = getRawValue();
		return val != null ? ((Integer)val).intValue(): 0;
	}
	/** Sets the value (in Integer).
	 * @exception WrongValueException if value is wrong
	 */
	public void setValue(Integer value) throws WrongValueException {
		validate(value);
		setRawValue(value);
	}

	//-- super --//
	protected Object coerceFromString(String value) throws WrongValueException {
		final Object[] vals = toNumberOnly(value);
		final String val = (String)vals[0];
		if (val == null || val.length() == 0)
			return null;

		try {
			int v = Integer.parseInt(val);
			int divscale = vals[1] != null ? ((Integer)vals[1]).intValue(): 0;
			while (v != 0 && --divscale >= 0)
				v /= 10;
			return new Integer(v);
		} catch (NumberFormatException ex) {
			throw new WrongValueException(this, MZul.NUMBER_REQUIRED, value);
		}
	}
	protected String coerceToString(Object value) {
		return value != null && getFormat() == null ?
			value.toString(): formatNumber(value, null);
	}
}

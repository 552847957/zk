/* Datebox.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jun 28 13:41:01     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.zkoss.lang.Objects;
import org.zkoss.util.Locales;
import org.zkoss.util.TimeZones;
import org.zkoss.xml.HTMLs;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;

import org.zkoss.zul.mesg.MZul;
import org.zkoss.zul.impl.FormatInputElement;

/**
 * An edit box for holding a date.
 *
 * <p>Default {@link #getSclass}: datebox.
 *
 * <p>The default format ({@link #getFormat}) depends on JVM's setting
 * and the current user's locale. That is,
 * <code>DateFormat.getDateInstance(DateFormat,DEFAULT, Locales.getCurrent).</code>
 * You might override {@link #getDefaultFormat} to provide your own default
 * format.
 *
 * @author tomyeh
 */
public class Datebox extends FormatInputElement {
	private static final String DEFAULT_IMAGE = "~./zul/img/caldrbtn.gif";
	private String _img;
	private TimeZone _tzone;
	private boolean _lenient = true;
	private boolean _compact, _btnVisible = true;

	public Datebox() {
		setFormat(getDefaultFormat());
		setSclass("datebox");
		setCols(11);
		_compact = "zh".equals(Locales.getCurrent().getLanguage());
	}
	public Datebox(Date date) throws WrongValueException {
		this();
		setValue(date);
	}
	/** Returns the default format, which is used when contructing
	 * a datebox.
	 * <p>The default format ({@link #getFormat}) depends on JVM's setting
	 * and the current user's locale. That is,
	 * <code>DateFormat.getDateInstance(DateFormat,DEFAULT, Locales.getCurrent).</code>
	 *
	 * <p>You might override this method to provide your own default format.
	 */
	protected String getDefaultFormat() {
		final DateFormat df = DateFormat.getDateInstance(
			DateFormat.DEFAULT, Locales.getCurrent());
		if (df instanceof SimpleDateFormat) {
			final String fmt = ((SimpleDateFormat)df).toPattern();
			if (fmt != null && !"M/d/yy h:mm a".equals(fmt))
				return fmt; //note: JVM use "M/d/yy h:mm a" if not found!
		}
		return "yyyy/MM/dd";
	}

	/** Returns whether or not date/time parsing is to be lenient.
	 *
	 * <p>With lenient parsing, the parser may use heuristics to interpret
	 * inputs that do not precisely match this object's format.
	 * With strict parsing, inputs must match this object's format.
	 */
	public boolean isLenient() {
		return _lenient;
	}
	/** Returns whether or not date/time parsing is to be lenient.
	 * <p>Default: true.
	 *
	 * <p>With lenient parsing, the parser may use heuristics to interpret
	 * inputs that do not precisely match this object's format.
	 * With strict parsing, inputs must match this object's format.
	 */
	public void setLenient(boolean lenient) {
		if (_lenient != lenient) {
			_lenient = lenient;
			smartUpdate("z.lenient", _lenient);
		}
	}
	/** Returns whether to use a compact layout.
	 * <p>Default: true if zh_TW or zh_CN; false otherwise.
	 */
	public boolean isCompact() {
		return _compact;
	}
	/** Sets whether to use a compact layout.
	 */
	public void setCompact(boolean compact) {
		if (_compact != compact) {
			_compact = compact;
			invalidate();
		}
	}

	/** Returns whether the button (on the right of the textbox) is visible.
	 * <p>Default: true.
	 * @since 2.4.1
	 */
	public boolean isButtonVisible() {
		return _btnVisible;
	}
	/** Sets whether the button (on the right of the textbox) is visible.
	 * @since 2.4.1
	 */
	public void setButtonVisible(boolean visible) {
		if (_btnVisible != visible) {
			_btnVisible = visible;
			smartUpdate("z.btnVisi", visible);
		}
	}
	/** Returns the URI of the button image.
	 * @since 2.5.0
	 */
	public String getImage() {
		return _img != null ? _img: DEFAULT_IMAGE;
	}
	/** Sets the URI of the button image.
	 *
	 * @param img the URI of the button image. If null or empty, the default
	 * URI is used.
	 * @since 2.5.0
	 */
	public void setImage(String img) {
		if (img != null && (img.length() == 0 || DEFAULT_IMAGE.equals(img)))
			img = null;
		if (!Objects.equals(_img, img)) {
			_img = img;
			invalidate();
		}
	}

	/** Returns the value (in Date), might be null unless
	 * a constraint stops it.
	 * @exception WrongValueException if user entered a wrong value
	 */
	public Date getValue() throws WrongValueException {
		return (Date)getTargetValue();
	}
	/** Sets the value (in Date).
	 * @exception WrongValueException if value is wrong
	 */
	public void setValue(Date value) throws WrongValueException {
		validate(value);
		setRawValue(value);
	}

	public void setFormat(String format) throws WrongValueException {
		if (format == null || format.length() == 0)
			format = getDefaultFormat();
		super.setFormat(format);
	}

	/** Returns the time zone that this date box belongs to, or null if
	 * the default time zone is used.
	 * <p>The default time zone is determined by {@link TimeZones#getCurrent}.
	 */
	public TimeZone getTimeZone() {
		return _tzone;
	}
	/** Sets the time zone that this date box belongs to, or null if
	 * the default time zone is used.
	 * <p>The default time zone is determined by {@link TimeZones#getCurrent}.
	 */
	public void setTimeZone(TimeZone tzone) {
		_tzone = tzone;
	}

	//-- super --//
	protected Object coerceFromString(String value) throws WrongValueException {
		if (value == null || value.length() == 0)
			return null;

		final String fmt = getFormat();
		final DateFormat df = getDateFormat(fmt);
		df.setLenient(_lenient);
		final Date date;
		try {
			date = df.parse(value);
		} catch (ParseException ex) {
			throw showCustomError(
				new WrongValueException(this, MZul.DATE_REQUIRED,
					new Object[] {value, fmt}));
		}
/*
		if (date.compareTo(_min) < 0 || date.compareTo(_max) > 0)
			throw showCustomError(
				new WrongValueException(
					MZul.DATE_OUT_OF_RANGE,
					new Object[] {value, df.format(_min), df.format(_max), fmt}));
*/
		return date;
	}
	protected String coerceToString(Object value) {
		final DateFormat df = getDateFormat(getFormat());
		return value != null ? df.format((Date)value): "";
	}
	/** Returns the date format of the specified format
	 *
	 * <p>Default: it uses SimpleDateFormat to format the date.
	 *
	 * @param fmt the pattern.
	 */
	protected DateFormat getDateFormat(String fmt) {
		final DateFormat df = new SimpleDateFormat(fmt, Locales.getCurrent());
		final TimeZone tz = _tzone != null ? _tzone: TimeZones.getCurrent();
		df.setTimeZone(tz);
		return df;
	}

	public String getOuterAttrs() {
		final String attrs = super.getOuterAttrs();
		if (_lenient && !_compact) return attrs;

		final StringBuffer sb = new StringBuffer(80).append(attrs);
		if (!_lenient) sb.append(" z.lenient=\"false\"");
		if (_compact) sb.append(" z.compact=\"true\"");
		return sb.toString();
	}
	public String getInnerAttrs() {
		final String attrs = super.getInnerAttrs();
		final String style = getInnerStyle();
		return style.length() > 0 ? attrs+" style=\""+style+'"': attrs;
	}
	private String getInnerStyle() {
		final StringBuffer sb = new StringBuffer(32)
			.append(HTMLs.getTextRelevantStyle(getRealStyle()));
		HTMLs.appendStyle(sb, "width", getWidth());
		HTMLs.appendStyle(sb, "height", getHeight());
		return sb.toString();
	}
	/** Returns RS_NO_WIDTH|RS_NO_HEIGHT.
	 */
	protected int getRealStyleFlags() {
		return super.getRealStyleFlags()|RS_NO_WIDTH|RS_NO_HEIGHT;
	}
}

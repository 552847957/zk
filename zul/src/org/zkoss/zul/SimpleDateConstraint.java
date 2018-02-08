/* SimpleDateConstraint.java

	Purpose:
		
	Description:
		
	History:
		Tue Dec 25 12:06:30     2007, Created by tomyeh

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.zkoss.util.Dates;
import org.zkoss.util.Locales;
import org.zkoss.util.TimeZones;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.mesg.MZul;

/**
 * A simple date constraint.
 *
 * @author tomyeh
 * @since 3.0.2
 */
public class SimpleDateConstraint extends SimpleConstraint {
	private Date _beg, _end;
	private TimeZone _tzone = TimeZones.getCurrent();

	public SimpleDateConstraint(int flags) {
		super(flags);
		fixConstraint();
	}

	/** Constraints a constraint.
	 *
	 * @param flags a combination of {@link #NO_POSITIVE}, {@link #NO_NEGATIVE},
	 * {@link #NO_ZERO}, and so on.
	 * @param errmsg the error message to display. Ignored if null or empty.
	 */
	public SimpleDateConstraint(int flags, String errmsg) {
		super(flags, errmsg);
		fixConstraint();
	}

	/** Constructs a regular-expression constraint.
	 *
	 * @param regex ignored if null or empty. Unlike constraint, the regex doesn't need to enclose with '/'.
	 * @param errmsg the error message to display. Ignored if null or empty.
	 * @deprecated As of release 8.0.1, replaced with {@link #SimpleDateConstraint(Pattern, String)}
	 */
	public SimpleDateConstraint(String regex, String errmsg) {
		super(regex == null || regex.length() == 0 ? null : Pattern.compile(regex), errmsg);
		fixConstraint();
	}

	/** Constructs a regular-expression constraint.
	 *
	 * @param regex ignored if null or empty
	 * @param errmsg the error message to display. Ignored if null or empty.
	 * @since 8.0.1
	 */
	public SimpleDateConstraint(Pattern regex, String errmsg) {
		super(regex, errmsg);
		fixConstraint();
	}

	/** Constructs a constraint combining regular expression.
	 *
	 * @param flags a combination of {@link #NO_POSITIVE}, {@link #NO_NEGATIVE},
	 * {@link #NO_ZERO}, and so on.
	 * @param regex ignored if null or empty. Unlike constraint, the regex doesn't need to enclose with '/'.
	 * @param errmsg the error message to display. Ignored if null or empty.
	 * @deprecated As of release 8.0.1, replaced with {@link #SimpleDateConstraint(int, Pattern, String)}
	 */
	public SimpleDateConstraint(int flags, String regex, String errmsg) {
		super(flags, regex == null || regex.length() == 0 ? null : Pattern.compile(regex), errmsg);
		fixConstraint();
	}

	/** Constructs a constraint combining regular expression.
	 *
	 * @param flags a combination of {@link #NO_POSITIVE}, {@link #NO_NEGATIVE},
	 * {@link #NO_ZERO}, and so on.
	 * @param regex ignored if null or empty
	 * @param errmsg the error message to display. Ignored if null or empty.
	 * @since 8.0.1
	 */
	public SimpleDateConstraint(int flags, Pattern regex, String errmsg) {
		super(flags, regex, errmsg);
		fixConstraint();
	}

	/** Constructs a constraint with beginning and ending date.
	 *
	 * @param flags a combination of {@link #NO_POSITIVE}, {@link #NO_NEGATIVE},
	 * {@link #NO_ZERO}, and so on.
	 * @param begin the beginning date, or null if no constraint at the beginning
	 * date.
	 * @param end the ending date, or null if no constraint at the ending
	 * date.
	 * @param errmsg the error message to display. Ignored if null or empty.
	 */
	public SimpleDateConstraint(int flags, Date begin, Date end, String errmsg) {
		super(flags, errmsg);
		_beg = begin;
		_end = end;
		fixConstraint();
	}

	/** Constructs a constraint with a list of constraints separated by comma.
	 *
	 * @param constraint a list of constraints separated by comma.
	 * Example: "between 20071012 and 20071223", "before 20080103"
	 */
	public SimpleDateConstraint(String constraint) {
		super(constraint);
		fixConstraint();
	}

	/**
	 * Sets time zone that this date constraint belongs to
	 */
	public void setTimeZone(TimeZone tzone) {
		this._tzone = tzone;
		this._finishParseCst = false;
	}

	private void fixConstraint() {
		if ((_flags & NO_FUTURE) != 0 && _end == null)
			_end = Dates.today();
		if ((_flags & NO_PAST) != 0 && _beg == null)
			_beg = Dates.today();
	}

	/** Returns the beginning date, or null if there is no constraint of
	 * the beginning date.
	 */
	public Date getBeginDate() {
		return _beg;
	}

	/** Returns the ending date, or null if there is no constraint of
	 * the ending date.
	 */
	public Date getEndDate() {
		return _end;
	}

	//super//
	protected int parseConstraint(String constraint) throws UiException {
		if (constraint.startsWith("between")) {
			final int j = constraint.indexOf("and", 7);
			if (j < 0)
				throw new UiException("Constraint syntax error: " + constraint);
			_beg = parseDate(constraint.substring(7, j), _tzone);
			_end = parseDate(constraint.substring(j + 3), _tzone);
			if (_beg.compareTo(_end) > 0) {
				final Date d = _beg;
				_beg = _end;
				_end = d;
			}
			return 0;
		} else if (constraint.startsWith("before") && !constraint.startsWith("before_")) {
			_end = parseDate(constraint.substring(6), _tzone);
			return 0;
		} else if (constraint.startsWith("after") && !constraint.startsWith("after_")) {
			_beg = parseDate(constraint.substring(5), _tzone);
			return 0;
		}
		return super.parseConstraint(constraint);
	}

	private static Date parseDate(String val, TimeZone tzone) throws UiException {
		try {
			return getDateFormat(tzone).parse(val.trim());
		} catch (ParseException ex) {
			throw new UiException("Not a date: " + val + ". Format: yyyyMMdd", ex);
		}
	}

	private static SimpleDateFormat getDateFormat(TimeZone tzone) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locales.getCurrent());
		df.setTimeZone(tzone != null ? tzone : TimeZones.getCurrent());
		return df;
	}

	public void validate(Component comp, Object value) throws WrongValueException {
		super.validate(comp, value);
		if (value instanceof Date) {
			final Date d = Dates.beginOfDate((Date) value, _tzone);
			if (_beg != null && _beg.compareTo(d) > 0) {
				throw outOfRangeValue(comp);
			}
			if (_end != null && _end.compareTo(d) < 0) {
				throw outOfRangeValue(comp);
			}
		}
	}

	private WrongValueException outOfRangeValue(Component comp) {
		final String errmsg = getErrorMessage(comp);
		if (errmsg != null)
			return new WrongValueException(comp, errmsg);

		final String s = _beg != null ? _end != null ? dateToString(comp, _beg, _tzone) + " ~ " + dateToString(comp, _end, _tzone)
				: ">= " + dateToString(comp, _beg, _tzone) : "<= " + dateToString(comp, _end, _tzone);
		return new WrongValueException(comp, MZul.OUT_OF_RANGE, s);
	}

	private static String dateToString(Component comp, Date d, TimeZone tzone) {
		if (d == null)
			return "";
		if (comp instanceof Datebox)
			return ((Datebox) comp).coerceToString(d);
		return getDateFormat(tzone).format(d);
	}
}

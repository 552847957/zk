/* ColSizeEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Dec  7 10:25:43     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.event;

import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;

/**
 * Used to notify that the widths of two adjacent column are changed.
 *
 * <p>When an user drags the border of sizable columns, the width of the
 * adjacent columns are changed accordingly -
 * one is enlarged, the other is shrinked and the total width is not changed.
 *
 * <p>The event is sent to the parent (e.g., {@link org.zkoss.zul.Columns}
 * and {@link org.zkoss.zul.Treecols}).
 * 
 * @author tomyeh
 */
public class ColSizeEvent extends Event {
	private final Component _col;
	private final int _icol, _keys;
	private String _width;

	/** Indicates whether the Alt key is pressed.
	 * It might be returned as part of {@link #getKeys}.
	 */
	public static final int ALT_KEY = MouseEvent.ALT_KEY;
	/** Indicates whether the Ctrl key is pressed.
	 * It might be returned as part of {@link #getKeys}.
	 */
	public static final int CTRL_KEY = MouseEvent.CTRL_KEY;
	/** Indicates whether the Shift key is pressed.
	 * It might be returned as part of {@link #getKeys}.
	 */
	public static final int SHIFT_KEY = MouseEvent.SHIFT_KEY;

	/** Converts an AU request to a size event.
	 * @since 5.0.0
	 */
	public static final ColSizeEvent getColSizeEvent(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, request);
		final Map data = request.getData();
		if (data == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
				new Object[] {data, request});

		return new ColSizeEvent(request.getCommand(), comp,
			AuRequests.getInt(data, "index", 0),
			request.getDesktop().getComponentByUuid((String)data.get("column")),
			(String)data.get("width"), AuRequests.parseKeys(data));
	}
	
	/** Constructs an instance of {@link ColSizeEvent}.
	 *
	 * @see #ColSizeEvent(String, Component, int, Component, String, int)
	 */
	public ColSizeEvent(String evtnm, Component target, int icol,
	Component col, int keys) {
		this(evtnm, target, icol, col, null, keys);
	}
	
	/**
	 * Constructs an instance of {@link ColSizeEvent}.
	 * @param icol the index of the first colum whose width is changed.
	 * @param col the component of the column
	 * @param width the width of the column
	 * @since 5.0.0
	 */
	public ColSizeEvent(String evtnm, Component target, int icol,
	Component col, String width, int keys) {
		super(evtnm, target);
		_icol = icol;
		_col = col;
		_width = width;
		_keys = keys;
	}
	/**
	 * Returns the column width
	 * @since 5.0.0
	 */
	public String getWidth() {
		return _width;
	}
	/** Return the column index of the first column whose width is changed.
	 * The other column is the returned index plus one.
	 * <p>In other words, it is the index (starting from 0) of {@link #getColumn}.
	 */
	public int getColIndex() {
		return _icol;
	}
	/** Returns the column whose width is changed.
	 * @since 3.0.0
	 */
	public Component getColumn() {
		return _col;
	}

	/** Returns what keys were pressed when the column is resized, or 0 if
	 * none of them was pressed.
	 * It is a combination of {@link #CTRL_KEY}, {@link #SHIFT_KEY}
	 * and {@link #ALT_KEY}.
	 */
	public final int getKeys() {
		return _keys;
	}
}

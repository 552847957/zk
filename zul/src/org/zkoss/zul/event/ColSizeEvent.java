/* ColSizeEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Dec  7 10:25:43     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.event;

import org.zkoss.zk.ui.Component;
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
	private final Component _col1, _col2;
	private final int _icol, _keys;

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

	/**
	 * @param icol the index of the first colum whose width is changed.
	 * The second column is icol+1.
	 */
	public ColSizeEvent(String evtnm, Component target, int icol,
	Component col1, Component col2, int keys) {
		super(evtnm, target);
		_icol = icol;
		_col1 = col1; _col2 = col2;
		_keys = keys;
	}
	/** Return the column index of the first column whose width is changed.
	 * The other column is the returned index plus one.
	 * <p>In other words, it is the index (starting from 0) of {@link #getColumn1}.
	 */
	public int getColIndex() {
		return _icol;
	}
	/** Returns the first column whose width is changed.
	 */
	public Component getColumn1() {
		return _col1;
	}
	/** Returns the second column whose width is changed.
	 */
	public Component getColumn2() {
		return _col2;
	}

	/** Returns what keys were pressed when the mouse is clicked, or 0 if
	 * none of them was pressed.
	 * It is a combination of {@link #CTRL_KEY}, {@link #SHIFT_KEY}
	 * and {@link #ALT_KEY}.
	 */
	public final int getKeys() {
		return _keys;
	}
}

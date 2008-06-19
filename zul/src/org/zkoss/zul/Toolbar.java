/* Toolbar.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun 23 11:33:31     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import org.zkoss.lang.Objects;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.impl.XulElement;

/**
 * A toolbar.
 * 
 * <p>Mold:
 * <ol>
 * <li>default</li>
 * <li>panel: since 3.1.0, this mold is used for {@link Panel} component as its
 * foot toolbar.</li>
 * </ol>
 * 
 * <p>Default {@link #getSclass}: toolbox, if the mold is panel, 
 * "<i>sclass</i>-panel" is assumed.
 *
 * @author tomyeh
 */
public class Toolbar extends XulElement {
	private String _orient = "horizontal";
	private String _align = "start";

	public Toolbar() {
		setSclass("toolbar");
	}
	
	/** 
	 * Returns the alignment of any children added to this toolbar. Valid values
	 * are "start", "end" and "center".
	 * <p>Default: "start"
	 * @since 3.1.0
	 */
	public String getAlign() {
		return _align;
	}
	
	/**
	 * Sets the alignment of any children added to this toolbar. Valid values
	 * are "start", "end" and "center".
	 * <p>Default: "start", if null, "start" is assumed.
	 * 
	 * @since 3.1.0
	 */
	public void setAlign(String align) {
		if (align == null) align = "start";
		if (!"start".equals(align) && !"center".equals(align) && !"end".equals(align))
			throw new WrongValueException("align cannot be "+align);
		if (!Objects.equals(_align, align)) {
			_align = align;
			invalidate();
		}
	}
	/*package*/ final boolean inPanelMold() {
		return "panel".equals(getMold());
	}
	
	public boolean insertBefore(Component newChild, Component refChild) {
		if (super.insertBefore(newChild, refChild)) {
			if (inPanelMold()) invalidate();
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the CSS class. If the mold is panel, the class that returned will
	 * be concatenated with "-panel", that is, "<i>sclass</i>-panel" is assumed.
	 * @since 3.1.0
	 */
	public String getSclass() {
		String scls = super.getSclass();
		return scls + (inPanelMold() ? "-panel" : "");
	}
	/**
	 * @param orient either "horizontal" or "vertical".
	 */
	public Toolbar(String orient) {
		this();
		setOrient(orient);
	}

	/** Returns the orient.
	 * <p>Default: "horizontal".
	 */
	public String getOrient() {
		return _orient;
	}
	/** Sets the orient.
	 * @param orient either "horizontal" or "vertical".
	 */
	public void setOrient(String orient) throws WrongValueException {
		if (!"horizontal".equals(orient) && !"vertical".equals(orient))
			throw new WrongValueException("orient cannot be "+orient);

		if (!Objects.equals(_orient, orient)) {
			_orient = orient;
			invalidate();
		}
	}

	//-- super --//
	public String getOuterAttrs() {
		final String attrs = super.getOuterAttrs();
		final String clkattrs = getAllOnClickAttrs();
		return clkattrs == null ? attrs: attrs + clkattrs;
	}
}

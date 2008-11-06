/* Tabpanels.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jul 12 10:43:08     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

import org.zkoss.zul.impl.XulElement;

/**
 * A collection of tab panels.
 *
 * <p>Default {@link #getZclass}: z-tabpanels. (since 3.5.0)
 * @author tomyeh
 */
public class Tabpanels extends XulElement {
	public Tabpanels() {
	}

	/** Returns the tabbox owns this component.
	 * <p>It is the same as {@link #getParent}.
	 */
	public Tabbox getTabbox() {
		return (Tabbox)getParent();
	}

	//-- Component --//
	public void setParent(Component parent) {
		if (parent != null && !(parent instanceof Tabbox))
			throw new UiException("Wrong parent: "+parent);
		super.setParent(parent);
	}
	public boolean insertBefore(Component child, Component insertBefore) {
		if (!(child instanceof Tabpanel))
			throw new UiException("Unsupported child for tabpanels: "+child);
		return super.insertBefore(child, insertBefore);
	}
	/** Returns the style class.
	 * @since 3.5.0
	 * <p>Note: the default style class is always "z-tabpanels".
	 */
	public String getZclass() {
		if (_zclass != null) return super.getZclass();
		final Tabbox tabbox = getTabbox();
		final String added = tabbox != null ? tabbox.inAccordionMold() ? "-" + tabbox.getMold() :
			tabbox.isVertical() ? "-ver" : "" : "";
		return "z-tabpanels" + added;
	}	
}

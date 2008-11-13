/* Listhead.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Aug  5 13:06:38     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import org.zkoss.xml.HTMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;

import org.zkoss.zul.impl.HeadersElement;

/**
 * A list headers used to define multi-columns and/or headers.
 *
 * <p>Difference from XUL:
 * <ol>
 * <li>There is no listcols in ZUL because it is merged into {@link Listhead}.
 * Reason: easier to write Listbox.</li>
 * </ol>
 *  <p>Default {@link #getZclass}: z-list-head.(since 3.5.0)
 *
 * @author tomyeh
 */
public class Listhead extends HeadersElement implements org.zkoss.zul.api.Listhead {
	/** Returns the list box that it belongs to.
	 * <p>It is the same as {@link #getParent}.
	 */
	public Listbox getListbox() {
		return (Listbox)getParent();
	}
	/** Returns the list box that it belongs to.
	 * <p>It is the same as {@link #getParent}.
	 * @since 3.5.2
	 */
	public org.zkoss.zul.api.Listbox getListboxApi() {
		return getListbox();
	}

	//super//
	public boolean setVisible(boolean visible) {
		final boolean vis = super.setVisible(visible);
		final Listbox listbox = getListbox();
		if (listbox != null)
			listbox.invalidate();
		return vis;
	}

	public String getZclass() {
		return _zclass == null ? "z-list-head" : super.getZclass();
	}

	public void setParent(Component parent) {
		if (parent != null && !(parent instanceof Listbox))
			throw new UiException("Wrong parent: "+parent);
		super.setParent(parent);
	}
	public boolean insertBefore(Component child, Component insertBefore) {
		if (!(child instanceof Listheader))
			throw new UiException("Unsupported child for listhead: "+child);
		return super.insertBefore(child, insertBefore);
	}
}

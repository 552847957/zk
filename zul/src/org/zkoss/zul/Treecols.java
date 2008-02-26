/* Treecols.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Jul  6 18:55:52     2005, Created by tomyeh
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
 * A treecols.
 *
 * @author tomyeh
 */
public class Treecols extends HeadersElement {
	/** Returns the tree that it belongs to.
	 * <p>It is the same as {@link #getParent}.
	 */
	public Tree getTree() {
		return (Tree)getParent();
	}

	public boolean setVisible(boolean visible) {
		final boolean vis = super.setVisible(visible);
		final Tree tree = getTree();
		if (tree != null)
			tree.invalidate();
		return vis;
	}
	public String getOuterAttrs() {
		final StringBuffer sb =
			new StringBuffer(80).append(super.getOuterAttrs());
			HTMLs.appendAttribute(sb, "z.rid", getTree().getUuid());
		return sb.toString();
	}
	//-- Component --//
	public void setParent(Component parent) {
		if (parent != null && !(parent instanceof Tree))
			throw new UiException("Wrong parent: "+parent);
		super.setParent(parent);
	}
	public boolean insertBefore(Component child, Component insertBefore) {
		if (!(child instanceof Treecol))
			throw new UiException("Unsupported child for treecols: "+child);
		return super.insertBefore(child, insertBefore);
	}
}

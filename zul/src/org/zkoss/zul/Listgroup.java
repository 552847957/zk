/* Listgroup.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 23, 2008 10:34:35 AM , Created by jumperchen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.xml.HTMLs;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.client.Openable;
import org.zkoss.zul.impl.XulElement;

/**
 * Adds the ability for single level grouping to the Listbox.
 * 
 * <p>Event:
 * <ol>
 * 	<li>onOpen is sent when this listgroup is opened or closed by user.</li>
 * </ol>
 * 
 * <p>Default {@link #getMoldSclass}: z-list-group.
 * @author jumperchen
 * @since 3.5.0
 */
public class Listgroup extends Listitem {
	private boolean _open = true;
	private transient List _items;
	public Listgroup() {
		init();
	}
	public Listgroup(String label) {
		this();
		setLabel(label);
	}
	public Listgroup(String label, Object value) {
		this();
		setLabel(label);
		setValue(value);
	}
	private void init() {
		_items =  new AbstractList() {
			public int size() {
				return getItemCount();
			}
			public Iterator iterator() {
				return new IterItems();
			}
			public Object get(int index) {
				final Listbox lb = getListbox();
				if (lb == null)
					throw new IndexOutOfBoundsException("Index: "+index);
				return lb.getItemAtIndex(getIndex() + index + 1);
			}
		};
	}
	private void applyImageIfAny() {
		if (getFirstChild() != null) {
			final Listcell lc = (Listcell)getFirstChild();
			if (lc.getImage() == null)
				lc.setImageDirectly(isOpen() ? "~./zul/img/tree/open.png" : "~./zul/img/tree/close.png");
		}
	}
	/** 
	 * Returns a list of all {@link Listitem} are grouped by this listgroup.
	 */
	public List getItems() {
		return _items;
	}
	/** Returns the number of items.
	 */
	public int getItemCount() {
		final Listbox lb = getListbox();
		if (lb != null) {
			int[] g = lb.getGroupsInfoAt(getIndex(), true);
			if (g != null) {
				if (g[2] == -1)
					return g[1] - 1;
				else
					return g[1] - 2;
			}
		}
		return 0;
	}
	/**
	 * Returns the index of Listgroupfoot
	 * <p> -1: no Listgroupfoot
	 */
	public int getListgroupfootIndex(){
		final Listbox lb = (Listbox)getParent();
		if (lb != null) {			
			int[] g = lb.getGroupsInfoAt(getIndex(), true);
			if (g != null) return g[2];
		}
		return -1;
	}
	/**
	 * Returns the Listfoot, if any. Otherwise, null is returned.
	 */
	public Listfoot getListfoot() {
		int index = getListgroupfootIndex();
		if (index < 0) return null;
		final Listbox lb = (Listbox)getParent();
		return (Listfoot) lb.getChildren().get(index);
	}
	/** Returns whether this container is open.
	 * <p>Default: true.
	 */
	public boolean isOpen() {
		return _open;
	}
	/** Sets whether this container is open.
	 */
	public void setOpen(boolean open) {
		if (_open != open) {
			_open = open;
			smartUpdate("z.open", _open);
		}
	}
	public String getMoldSclass() {
		return _moldSclass == null ? "z-list-group" : super.getMoldSclass();
	}
	public String getOuterAttrs() {
		applyImageIfAny();
		final StringBuffer sb = new StringBuffer(64).append( super.getOuterAttrs());
		HTMLs.appendAttribute(sb, "z.open", isOpen());
		HTMLs.appendAttribute(sb, "z.nostripe", true);
		appendAsapAttr(sb, Events.ON_OPEN);
		return sb.toString();
	}
	public void onChildAdded(Component child) {
		super.onChildAdded(child);
		invalidate();
	}
	public void onChildRemoved(Component child) {
		super.onChildRemoved(child);
		invalidate();
	}
	//-- ComponentCtrl --//
	protected Object newExtraCtrl() {
		return new ExtraCtrl();
	}
	/** A utility class to implement {@link #getExtraCtrl}.
	 * It is used only by component developers.
	 */
	protected class ExtraCtrl extends XulElement.ExtraCtrl
	implements Openable {
		//-- Openable --//
		public void setOpenByClient(boolean open) {
			_open = open;
		}
	}
	/**
	 * An iterator used by _items.
	 */
	private class IterItems implements Iterator {
		private final Iterator _it = getListbox().getItems().listIterator(getIndex()+1);
		private int _j;

		public boolean hasNext() {
			return _j < getItemCount();
		}
		public Object next() {
			final Object o = _it.next();
			++_j;
			return o;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}

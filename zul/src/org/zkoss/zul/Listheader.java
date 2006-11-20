/* Listheader.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Aug  5 13:06:59     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.Iterator;
import java.util.List;
import java.util.Comparator;

import org.zkoss.lang.Objects;
import org.zkoss.xml.HTMLs;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;

import org.zkoss.zul.impl.HeaderElement;

/**
 * The list header which defines the attributes and header of a columen
 * of a list box.
 * Its parent must be {@link Listhead}.
 *
 * <p>Difference from XUL:
 * <ol>
 * <li>There is no listcol in ZUL because it is merged into {@link Listheader}.
 * Reason: easier to write Listbox.</li>
 * </ol>
 *
 * @author tomyeh
 */
public class Listheader extends HeaderElement {
    private static final long serialVersionUID = 20060731L;

	private String _sortDir = "natural";
	private transient Comparator _sortAsc, _sortDsc;
	private int _maxlength;

	public Listheader() {
	}
	public Listheader(String label) {
		setLabel(label);
	}
	public Listheader(String label, String src) {
		setLabel(label);
		setImage(src);
	}

	/** Returns the listbox that this belongs to.
	 */
	public Listbox getListbox() {
		final Component comp = getParent();
		return comp != null ? (Listbox)comp.getParent(): null;
	}
	/** Returns the listhead that this belongs to.
	 */
	public Listhead getListhead() {
		return (Listhead)getParent();
	}

	/** Returns the sort direction.
	 * <p>Default: "natural".
	 */
	public String getSortDirection() {
		return _sortDir;
	}
	/** Sets the sort direction. This does not sort the data, it only serves
	 * as an indicator as to how the list is sorted.
	 *
	 * <p>If you use {@link #sort(boolean)} to sort list items,
	 * the sort direction is maintained automatically.
	 * If you want to sort it in customized way, you have to set the
	 * sort direction manaully.
	 *
	 * @param sortDir one of "ascending", "descending" and "natural"
	 */
	public void setSortDirection(String sortDir) throws WrongValueException {
		if (sortDir == null || (!"ascending".equals(sortDir)
		&& !"descending".equals(sortDir) && !"natural".equals(sortDir)))
			throw new WrongValueException("Unknown sort direction: "+sortDir);
		if (!Objects.equals(_sortDir, sortDir)) {
			_sortDir = sortDir;
			smartUpdate("z.sort", _sortDir); //don't use null because sel.js assumes it
		}
	}

	/** Sets the type of the sorter.
	 * You might specify either "auto" or "none".
	 *
	 * <p>If "auto" is specified, it will call
	 * {@link #setSortAscending} and/or {@link #setSortDescending} 
	 * are called with {@link ListitemComparator}, if
	 * {@link #getSortDescending} and/or {@link #getSortAscending} are null.
	 * If you assigned a comparator to them, it won't be affected.
	 * The auto created comparator is case-insensitive.
	 *
	 * <p>If "none" is specified, both {@link #setSortAscending} and
	 * {@link #setSortDescending} are called with null.
	 * Therefore, no more sorting is available to users for this column.
	 */
	public void setSort(String type) {
		if ("auto".equals(type)) {
			if (getSortAscending() == null)
				setSortAscending(new ListitemComparator(this, true, true));
			if (getSortDescending() == null)
				setSortDescending(new ListitemComparator(this, false, true));
		} else if ("none".equals(type)) {
			setSortAscending((Comparator)null);
			setSortDescending((Comparator)null);
		}
	}

	/** Returns the ascending sorter, or null if not available.
	 */
	public Comparator getSortAscending() {
		return _sortAsc;
	}
	/** Sets the ascending sorter, or null for no sorter for
	 * the ascending order.
	 */
	public void setSortAscending(Comparator sorter) {
		if (!Objects.equals(_sortAsc, sorter)) {
			if (sorter == null) smartUpdate("z.asc", null);
			else if (_sortAsc == null) smartUpdate("z.asc", "true");
			_sortAsc = sorter;
		}
	}
	/** Sets the ascending sorter with the class name, or null for
	 * no sorter for the ascending order.
	 */
	public void setSortAscending(String clsnm)
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException {
		setSortAscending(toComparator(clsnm));
	}

	/** Returns the descending sorter, or null if not available.
	 */
	public Comparator getSortDescending() {
		return _sortDsc;
	}
	/** Sets the descending sorter, or null for no sorter for the
	 * descending order.
	 */
	public void setSortDescending(Comparator sorter) {
		if (!Objects.equals(_sortDsc, sorter)) {
			if (sorter == null) smartUpdate("z.dsc", null);
			else if (_sortDsc == null) smartUpdate("z.dsc", "true");
			_sortDsc = sorter;
		}
	}
	/** Sets the descending sorter with the class name, or null for
	 * no sorter for the descending order.
	 */
	public void setSortDescending(String clsnm)
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException {
		setSortDescending(toComparator(clsnm));
	}

	private Comparator toComparator(String clsnm)
	throws ClassNotFoundException, InstantiationException,
	IllegalAccessException {
		if (clsnm == null || clsnm.length() == 0) return null;

		final Class cls = getClass(clsnm);
		if (!Comparator.class.isAssignableFrom(cls))
			throw new UiException("Comparator must be implemented: "+clsnm);
		return (Comparator)cls.newInstance();
	}

	/** Returns the maximal length of each item's label.
	 * Default: 0 (no limit).
	 */
	public int getMaxlength() {
		return _maxlength;
	}
	/** Sets the maximal length of each item's label.
	 */
	public void setMaxlength(int maxlength) {
		if (maxlength < 0) maxlength = 0;
		if (_maxlength != maxlength) {
			_maxlength = maxlength;
			invalidateCells();
		}
	}

	/** Returns the column index, starting from 0.
	 */
	public int getColumnIndex() {
		int j = 0;
		for (Iterator it = getParent().getChildren().iterator();
		it.hasNext(); ++j)
			if (it.next() == this)
				break;
		return j;
	}

	/** Invalidates the relevant cells. */
	private void invalidateCells() {
		final Listbox listbox = getListbox();
		if (listbox == null || listbox.inSelectMold())
			return;

		final int jcol = getColumnIndex();
		for (Iterator it = listbox.getItems().iterator(); it.hasNext();) {
			final Listitem li = (Listitem)it.next();
			final List chs = li.getChildren();
			if (jcol < chs.size())
				((Component)chs.get(jcol)).invalidate();
		}
	}

	/** Sorts the list items based on {@link #getSortAscending}
	 * and {@link #getSortDescending}, if {@link #getSortDirection} doesn't
	 * matches the ascending argument.
	 *
	 * <p>It checks {@link #getSortDirection} to see whether sorting
	 * is required, and update {@link #setSortDirection} after sorted.
	 * For example, if {@link #getSortDirection} returns "ascending" and
	 * the ascending argument is false, nothing happens.
	 * To enforce the sorting, you can invoke {@link #setSortDirection}
	 * with "natural" before invoking this method.
	 * Alternatively, you can invoke {@link #sort(boolean, boolean)} instead.
	 *
	 * <p>It sorts the listitem by use of {@link Components#sort}.
	 *
	 * <p>Note: it invokes {@link Listbox#renderAll} to load all list items.
	 * If you want to use different way (such as issuing SQL with different ORDER BY)
	 * you have to override this method or listen to the onSort event.
	 *
	 * @param ascending whether to use {@link #getSortAscending}.
	 * If the corresponding comparator is not set, it returns false
	 * and does nothing.
	 * @return whether the list items are sorted.
	 */
	public boolean sort(boolean ascending) {
		final String dir = getSortDirection();
		if (ascending) {
			if ("ascending".equals(dir)) return false;
		} else {
			if ("descending".equals(dir)) return false;
		}

		final Comparator cpr = ascending ? _sortAsc: _sortDsc;
		if (cpr == null) return false;

		final Listbox box = getListbox();
		if (box == null) return false;

		box.renderAll(); //make sure all items are loaded before sorting
		Components.sort(box.getItems(), cpr);

		//maintain
		for (Iterator it = box.getListhead().getChildren().iterator();
		it.hasNext();) {
			final Listheader hd = (Listheader)it.next();
			hd.setSortDirection(
				hd != this ? "natural": ascending ? "ascending": "descending");
		}
		return true;
	}
	/** Sorts the list items based on {@link #getSortAscending}
	 * and {@link #getSortDescending}.
	 *
	 * @param ascending whether to use {@link #getSortAscending}.
	 * If the corresponding comparator is not set, it returns false
	 * and does nothing.
	 * @param force whether to enforce the sorting no matter what the sort
	 * direction ({@link #getSortDirection}) is.
	 * If false, this method is the same as {@link #sort(boolean)}.
	 * @return whether the rows are sorted.
	 */
	public boolean sort(boolean ascending, boolean force) {
		if (force) setSortDirection("natural");
		return sort(ascending);
	}

	//-- event listener --//
	/** It invokes {@link #sort(boolean)} to sort list items and maintain
	 * {@link #getSortDirection}.
	 */
	public void onSort() {
		final String dir = getSortDirection();
		if ("ascending".equals(dir)) sort(false);
		else if ("descending".equals(dir)) sort(true);
		else if (!sort(true)) sort(false);
	}

	//-- super --//
	/** Returns the style class.
	 * If the style class is not defined ({@link #setSclass} is not called
	 * or called with null or empty), it returns "sort" if sortable,
	 * or null if not sortable.
	 * <p>By sortable we mean that {@link #setSortAscending}
	 * or {@link #setSortDescending}
	 * was called with a non-null comparator
	 */
	public String getSclass() {
		final String scls = super.getSclass();
		if (scls != null) return scls;
		return _sortAsc != null || _sortDsc != null ? "sort": null;
	}

	public String getOuterAttrs() {
		final StringBuffer sb = new StringBuffer(80);
		if (_sortAsc != null) sb.append(" z.asc=\"true\"");
		if (_sortDsc != null) sb.append(" z.dsc=\"true\"");

		if (!"natural".equals(_sortDir))
			HTMLs.appendAttribute(sb, "z.sort", _sortDir);

		final String clkattrs = getAllOnClickAttrs(false);
		if (clkattrs != null) sb.append(clkattrs);

		final String attrs = super.getOuterAttrs();
		if (sb.length() == 0) return attrs;
		return sb.insert(0, attrs).toString();
	}

	/** Invalidates the whole box. */
	protected void invalidateWhole() {
		final Listbox box = getListbox();
		if (box != null) box.invalidate();
	}

	//-- Component --//
	public void setParent(Component parent) {
		if (parent != null && !(parent instanceof Listhead))
			throw new UiException("Wrong parent: "+parent);
		super.setParent(parent);
	}

	//Cloneable//
	public Object clone() {
		final Listheader clone = (Listheader)super.clone();
		clone.fixClone();
		return clone;
	}
	private void fixClone() {
		if (_sortAsc instanceof ListitemComparator) {
			final ListitemComparator c = (ListitemComparator)_sortAsc;
			if (c.getListheader() == this && c.isAscending())
				_sortAsc =
					new ListitemComparator(this, true, c.shallIgnoreCase());
		}
		if (_sortDsc instanceof ListitemComparator) {
			final ListitemComparator c = (ListitemComparator)_sortDsc;
			if (c.getListheader() == this && !c.isAscending())
				_sortDsc =
					new ListitemComparator(this, false, c.shallIgnoreCase());
		}
	}

	//Serializable//
	//NOTE: they must be declared as private
	private synchronized void writeObject(java.io.ObjectOutputStream s)
	throws java.io.IOException {
		s.defaultWriteObject();

		boolean written = false;
		if (_sortAsc instanceof ListitemComparator) {
			final ListitemComparator c = (ListitemComparator)_sortAsc;
			if (c.getListheader() == this && c.isAscending()) {
				s.writeBoolean(true);
				s.writeBoolean(c.shallIgnoreCase());
				s.writeBoolean(c.byValue());
				written = true;
			}
		}
		if (!written) {
			s.writeBoolean(false);
			s.writeObject(_sortAsc);
		}

		written = false;
		if (_sortDsc instanceof ListitemComparator) {
			final ListitemComparator c = (ListitemComparator)_sortDsc;
			if (c.getListheader() == this && !c.isAscending()) {
				s.writeBoolean(true);
				s.writeBoolean(c.shallIgnoreCase());
				s.writeBoolean(c.byValue());
				written = true;
			}
		}
		if (!written) {
			s.writeBoolean(false);
			s.writeObject(_sortDsc);
		}
	}
	private synchronized void readObject(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		boolean b = s.readBoolean();
		if (b) {
			final boolean igcs = s.readBoolean();
			final boolean byval = s.readBoolean();
			_sortAsc = new ListitemComparator(this, true, igcs, byval);
		} else {
			_sortAsc = (ListitemComparator)s.readObject();
		}

		b = s.readBoolean();
		if (b) {
			final boolean igcs = s.readBoolean();
			final boolean byval = s.readBoolean();
			_sortDsc = new ListitemComparator(this, false, igcs, byval);
		} else {
			_sortDsc = (ListitemComparator)s.readObject();
		}
	}
}

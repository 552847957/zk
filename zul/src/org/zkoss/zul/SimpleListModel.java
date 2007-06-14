/* SimpleListModel.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 18 15:40:14     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

import org.zkoss.util.ArraysX;
import org.zkoss.zul.event.ListDataEvent;

/**
 * A simple implementation of {@link ListModel}.
 * Note: It assumes the content is immutable. If not, use {@link ListModelList}
 * or {@link ListModelArray} nstead.
 *
 * @author tomyeh
 * @see ListModelArray
 * @see ListModelSet
 * @see ListModelList
 * @see ListModelMap
 */
public class SimpleListModel extends AbstractListModel
implements ListModelExt, java.io.Serializable {
    private static final long serialVersionUID = 20060707L;

	private final Object[] _data;

	/** Constructor.
	 *
	 * @param data the array to represent
	 * @param live whether to have a 'live' {@link ListModel} on top of
	 * the specified list.
	 * If false, the content of the specified list is copied.
	 * If true, this object is a 'facade' of the specified list,
	 * i.e., when you add or remove items from this ListModelList,
	 * the inner "live" list would be changed accordingly.
	 *
	 * However, it is not a good idea to modify <code>data</code>
	 * once it is passed to this method with live is true,
	 * since {@link Listbox} is not smart enough to hanle it.
	 * @since 2.4.1
	 */
	public SimpleListModel(Object[] data, boolean live) {
		if (data == null)
			throw new NullPointerException();
		_data = live ? data: (Object[])ArraysX.clone(data);
	}
	/** Constructor.
	 * It made a copy of the specified array (<code>data</code>).
	 */
	public SimpleListModel(Object[] data) {
		this(data, false);
	}

	/** Constructor.
	 * @since 2.4.1
	 */
	public SimpleListModel(List data) {
		_data = data.toArray(new Object[data.size()]);
	}

	//-- ListModel --//
	public int getSize() {
		return _data.length;
	}
	public Object getElementAt(int j) {
		return _data[j];
	}

	//-- ListModelExt --//
	/** Sorts the data.
	 *
	 * @param cmpr the comparator.
	 * @param ascending whether to sort in the ascending order.
	 * It is ignored since this implementation uses cmprt to compare.
	 */
	public void sort(Comparator cmpr, final boolean ascending) {
		Arrays.sort(_data, cmpr);
		fireEvent(ListDataEvent.CONTENTS_CHANGED, -1, -1);
	}
}

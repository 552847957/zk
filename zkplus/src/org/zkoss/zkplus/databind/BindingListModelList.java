/* BindingListModelList.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Jan 29 21:07:15     2007, Created by henrichen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkplus.databind;

import org.zkoss.zul.ListModelList;

import java.util.List;

/**
 * <p>This is the {@link BindingListModel} as a {@link java.util.List} to be used with {@link org.zkoss.zul.Listbox} 
 * and {@link DataBinder}.
 * Add or remove the contents of this model as a List would cause the associated Listbox to change accordingly.</p> 
 *
 * @author Henri Chen
 * @see BindingListModel
 * @see org.zkoss.zul.ListModel
 * @see org.zkoss.zul.ListModelList
 */
public class BindingListModelList extends ListModelList implements BindingListModel {
	/* package */BindingListModelList(List list) {
		super(list);
	}
}


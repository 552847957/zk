/* BindingListModelArray.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Feb 26 17:37:25     2007, Created by henrichen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkplus.databind;

import org.zkoss.zul.ListModelArray;

/**
 * <p>This is the {@link BindingListModel} as a Object array to be used with 
 * {@link org.zkoss.zul.Listbox}, {@link org.zkoss.zul.Grid},  
 * and {@link DataBinder}.
 * Modify the contents of this model as an Object array would cause the associated Listbox or Grid to change accordingly.</p> 
 *
 * @author Henri Chen
 * @see BindingListModel
 * @see org.zkoss.zul.ListModel
 * @see org.zkoss.zul.ListModelArray
 */
/*package*/ class BindingListModelArray extends ListModelArray
implements BindingListModel, java.io.Serializable {
	/* package */BindingListModelArray(Object[] c, boolean live) {
		super(c, live);
	}
}


/* ListModelConverter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Dec  1 16:55:36     2006, Created by Henri Chen
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zkplus.databind;

import org.zkoss.zul.ListModelSet;
import org.zkoss.zul.ListModelMap;
import org.zkoss.zul.ListModelList;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.Component;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * The {@link TypeConverter} implmentation for converting collection to ListModel and vice versa.
 *
 * @author Henri Chen
 */
public class ListModelConverter implements TypeConverter {
	/** Convert a Set, Map, List, Object[], Enum, or other kind of BindingListModel to associated {@link BindingListModel}.
	 * @param val must be instanceof Set, Map, List, Object[], Enum Class, or other kind of BindingListModel implementation.
	 */
	public Object coerceToUi(Object val, Component comp) {
		if (val == null) {
			val = new ArrayList();
		}
		if (val instanceof BindingListModel) {
			return val;
		} else if (val instanceof Set) {
			return new BindingListModelSet((Set)val, true);
		} else if (val instanceof List) {
			return new BindingListModelList((List)val, true);
		} else if (val instanceof Map) {
			return new BindingListModelMap((Map)val, true);
		} else if (val instanceof Object[]) {
			return new BindingListModelArray((Object[]) val, true);
		} else if ((val instanceof Class) && Enum.class.isAssignableFrom((Class)val)) {
			return new BindingListModelArray((Object[]) ((Class)val).getEnumConstants(), true);
		} else {
			throw new UiException("Expects java.util.Set, java.util.List, java.util.Map, Object[], Enum Class, or BindingListModel only. "+val.getClass());
		}
	}

	/** Convert a {@link BindingListModel} to Set, Map, List, or BindingListModel (itself).
	 * @param val must be BindingListModelSet, BindingListModelList, BindingListModelMap, or other kind of BindingListModel
	 */
	public Object coerceToBean(Object val, Component comp) {
		if (val == null) {
			throw new NullPointerException("val");
		}
		if (val instanceof BindingListModelSet) {
			return ((BindingListModelSet)val).getInnerSet();
		} else if (val instanceof BindingListModelList) {
			return ((BindingListModelList)val).getInnerList();
		} else if (val instanceof BindingListModelMap) {
			return ((BindingListModelMap)val).getInnerMap();
		} else if (val instanceof BindingListModelArray) {
			return ((BindingListModelArray)val).getInnerArray();
		} else if (val instanceof BindingListModel) {
			return val;
		} else {
			throw new UiException("Expects BindingListModelSet, BindingListModelList, BindingListModelMap, or BindingListModel only."+val.getClass());
		}
	}
}

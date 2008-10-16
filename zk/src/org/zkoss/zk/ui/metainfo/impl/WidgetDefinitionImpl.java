/* WidgetDefinitionImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Oct 16 11:06:05     2008, Created by tomyeh
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.metainfo.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.metainfo.*;
import org.zkoss.zk.ui.UiException;

/**
 * An implementation of WidgetDefinition.
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class WidgetDefinitionImpl implements WidgetDefinition {
	/** The widget name. */
	private final String _name;
	/** A map of molds (String mold, String moldURI). */
	private Map _molds;

	public WidgetDefinitionImpl(String name) {
		_name = name;
	}

	//WidgetDefinition//
	public String getName() {
		return _name;
	}
	public void addMold(String name, String moldURI) {
		if (moldURI == null || moldURI.length() == 0 || name == null
		|| name.length() == 0)
			throw new IllegalArgumentException();

		if (_molds == null)
			_molds = new HashMap(2);
		Object old = _molds.put(name, moldURI);
		if (old != null && !Objects.equals(old, moldURI)) {
			_molds.put(name, old);
			throw new UiException("Different mold URIs, "+old+" and "+moldURI+" cannot be mapped to the same widget, "+_name);
			//We assume mold URI can be decided by widget type and mold
			//(no need of component definition), so we must prevent
			//misuse (such as overriding with a diff URI but same widget type)
		}
	}
	public String getMoldURI(String name) {
		if (_molds == null)
			return null;

		return (String)_molds.get(name);
	}
	public boolean hasMold(String name) {
		return _molds != null && _molds.containsKey(name);
	}
	public Collection getMoldNames() {
		return _molds != null ?
			_molds.keySet(): (Collection)Collections.EMPTY_LIST;
	}
}

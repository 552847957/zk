/* InterpretResolver.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sat Sep 17 17:03:58     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.web.servlet.dsp.impl;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.el.ELException;

import org.zkoss.lang.D;
import org.zkoss.lang.SystemException;
import org.zkoss.web.servlet.dsp.*;
import org.zkoss.web.servlet.dsp.action.ActionContext;

/**
 * The resolver used to interpret an {@link Interpretation}.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
class InterpretResolver implements VariableResolver {
	private final VariableResolver _parent;
	private final Map _attrs = new HashMap();

	InterpretResolver(VariableResolver parent) {
		assert D.OFF || !(parent instanceof InterpretResolver);
		_parent = parent;
	}

	/** Returns the attributes of the given scope. */
	Map getAttributes(int scope) {
		if (scope == ActionContext.PAGE_SCOPE)
			return _attrs;

		try {
			Map attrs = null;
			if (_parent != null) {
				switch (scope) {
				case ActionContext.REQUEST_SCOPE:
					attrs = (Map)_parent.resolveVariable("requestScope");
					break;
				case ActionContext.SESSION_SCOPE:
					attrs = (Map)_parent.resolveVariable("sessionScope");
					break;
				case ActionContext.APPLICATION_SCOPE:
					attrs = (Map)_parent.resolveVariable("applicationScope");
					break;
				}
			}
			return attrs != null ? attrs: Collections.EMPTY_MAP;
		} catch (ELException ex) {
			throw new SystemException(ex);
		}
	}

	//-- VariableResolver --//
	public Object resolveVariable(String name) throws ELException {
		if ("pageScope".equals(name))
			return _attrs;
		final Object o = _attrs.get(name);
		if (o != null) return o;
		return _parent != null ? _parent.resolveVariable(name): null;
	}
}

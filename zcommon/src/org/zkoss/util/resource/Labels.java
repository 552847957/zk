/* Labels.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Sep 21 10:55:09     2004, Created by tomyeh
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.util.resource;

import javax.servlet.jsp.el.VariableResolver;
import org.zkoss.util.resource.impl.LabelLoader;

/**
 * Utilities to access labels. A label is a Locale-dependent string
 * that is stored in i3-label*properties.
 *
 * @author tomyeh
 */
public class Labels {
	private Labels() {} //prevent form misuse

	private static final LabelLoader _loader = new LabelLoader();

	/** Returns the label of the specified key based
	 * on the current Locale, or null if no found.
	 *
	 * <p>The current locale is given by {@link org.zkoss.util.Locales#getCurrent}.
	 */
	public static final String getLabel(String key) {
		return _loader.getLabel(key);
	}
	/** Resets all cached labels and next call to {@link #getLabel}
	 * will cause re-loading i3-label*.proerties.
	 */
	public static final void reset() {
		_loader.reset();
	}
	/** Sets the variable resolver, which is used if an EL expression
	 * is specified.
	 *
	 * <p>Default: no resolver at all.
	 *
	 * @return the previous resolver, or null if no resolver.
	 */
	public static final
	VariableResolver setVariableResolver(VariableResolver resolv) {
		return _loader.setVariableResolver(resolv);
	}
	/** Registers a locator which is used to load i3-label*.properties
	 * from other resource, such as servlet contexts.
	 */
	public static final void register(LabelLocator locator) {
		_loader.register(locator);
	}
}

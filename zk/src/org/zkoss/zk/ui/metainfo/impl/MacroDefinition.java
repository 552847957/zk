/* MacroDefinition.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 16 12:31:56     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.metainfo.impl;

import org.zkoss.zk.ui.metainfo.*;

/**
 * The macro component definition.
 *
 * @author tomyeh
 * @since 2.5.0
 * @see ComponentDefinitionImpl#newMacroDefinition
 */
public class MacroDefinition extends ComponentDefinitionImpl {
	private final String _macroURI;
	/** Whether it is an inline macro. */
	private final boolean _inline;

	/*package*/ MacroDefinition(LanguageDefinition langdef, String name,
	Class cls, String macroURI, boolean inline) {
		super(langdef, name, cls);

		if (name == null || cls == null)
			throw new IllegalArgumentException("null");
		if (macroURI == null || macroURI.length() == 0)
			throw new IllegalArgumentException("empty macroURI");

		_macroURI = macroURI;
		_inline = inline;
	}

	public boolean isMacro() {
		return _macroURI != null;
	}
	public String getMacroURI() {
		return _macroURI;
	}
	public boolean isInlineMacro() {
		return _inline;
	}
}

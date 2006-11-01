/* LabelElement.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jun 17 09:45:54     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.impl;

import org.zkoss.lang.Objects;

/**
 * A HTML element with a label.
 *
 * @author tomyeh
 */
abstract public class LabelElement extends XulElement {
	/** The label. */
	private String _label = "";

	/** Returns the label (never null).
	 * <p>Default: "".
	 */
	public String getLabel() {
		return _label;
	}
	/** Sets the label.
	 * <p>If label is changed, the whole component is invalidate.
	 * Thus, you want to smart-update, you have to override this method.
	 */
	public void setLabel(String label) {
		if (label == null) label = "";
		if (!Objects.equals(_label, label)) {
			_label = label;
			invalidate();
		}
	}
}

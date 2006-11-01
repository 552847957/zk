/* Input.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Nov 29 21:59:11     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zhtml;

import java.lang.Object; //since we have org.zkoss.zhtml.Object

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.ext.client.Inputable;
import org.zkoss.zhtml.impl.AbstractTag;

/**
 * The input tag.
 *
 * @author tomyeh
 */
public class Input extends AbstractTag {

	public Input() {
		this("input");
	}
	protected Input(String tagnm) {
		super(tagnm);
	}

	/** Returns the value of this input.
	 */
	public String getValue() {
		return (String)getDynamicProperty("value");
	}
	/** Sets the vallue of this input.
	 */
	public void setValue(String value) throws WrongValueException {
		setDynamicProperty("value", value);
	}

	//-- super --//
	public Object newExtraCtrl() {
		return new Inputable() {
			//-- Inputable --//
			public void setTextByClient(String value) throws WrongValueException {
				setValue(value);
			}
		};
	}
}

/* Select.java

	Purpose:
		
	Description:
		
	History:
		Tue Dec 13 15:04:17     2005, Created by tomyeh

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zhtml;

import org.zkoss.zhtml.impl.AbstractTag;

import org.zkoss.zk.ui.event.Events;

/**
 * The SELECT tag.
 * 
 * @author tomyeh
 */
public class Select extends AbstractTag {
	static {
		addClientEvent(Select.class, Events.ON_CHANGE, 0);
	}

	public Select() {
		super("select");
	}
}

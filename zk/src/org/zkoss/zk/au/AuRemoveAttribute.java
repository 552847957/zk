/* AuRemoveAttribute.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Oct 13 11:26:16     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au;

import org.zkoss.zk.ui.Component;

/**
 * A resonse to remove the attribute of the specified component at the client.
 * <p>data[0]: the uuid of the component<br>
 * data[1]: the attribute name
 * 
 * @author tomyeh
 */
public class AuRemoveAttribute extends AuResponse {
	public AuRemoveAttribute(Component comp, String attr) {
		super("rmAttr", comp, new String[] {comp.getUuid(), attr});
	}
}

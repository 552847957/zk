/* AuFocus.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Oct 13 11:52:43     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.out;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.au.AuResponse;

/**
 * A response to set focus to the specified component at the client.
 * <p>data[0]: the uuid of the component to set focus
 * 
 * @author tomyeh
 * @since 3.0.0
 */
public class AuFocus extends AuResponse {
	public AuFocus(Component comp) {
		super("focus", comp, comp.getUuid());
	}
}

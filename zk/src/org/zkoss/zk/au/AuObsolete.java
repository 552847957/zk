/* AuObsolete.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Oct 12 23:45:30     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au;

import org.zkoss.zk.ui.Component;

/**
 * A response to denote the desktop might become obsolte.
 * <p>data[0]: the desktop ID.<br/>
 * data[1]: the message
 *
 * @author tomyeh
 */
public class AuObsolete extends AuResponse {
	public AuObsolete(String dtid, String message) {
		super("obsolete", new String[] {dtid, message});
	}
}

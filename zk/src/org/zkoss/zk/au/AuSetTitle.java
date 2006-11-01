/* AuSetTitle.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Oct 13 10:31:55     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au;

/**
 * A response to ask the client to set the title (of window).
 *  <p>data[0]: the title
 *
 * @author tomyeh
 */
public class AuSetTitle extends AuResponse {
	public AuSetTitle(String title) {
		super("title", title);
	}
}

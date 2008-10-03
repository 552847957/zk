/* AuOuter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Oct  1 17:18:05     2008, Created by tomyeh
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.out;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.au.AuResponse;

/**
 * A response to ask client to 'outer' the widgets and all its
 * descendants of the associatethe specified component or page.
 *
 * <p>data[0]: the uuid of the component or page to outer
 * data[1]: the new content
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class AuOuter extends AuResponse {
	public AuOuter(Component comp, String content) {
		super("outer", comp, new String[] {comp.getUuid(), content});
	}
	public AuOuter(Page page, String content) {
		super("outer", page, new String[] {page.getUuid(), content});
	}
}

/* Link.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Dec 13 15:22:15     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zhtml;

import org.zkoss.zhtml.impl.AbstractTag;

/**
 * The LINK tag.
 * 
 * @author tomyeh
 */
public class Link extends AbstractTag {
	public Link() {
		super("link");
	}

	//super//
	public void redraw(java.io.Writer out) throws java.io.IOException {
		super.redraw(out);
		out.write('\n');
	}
}

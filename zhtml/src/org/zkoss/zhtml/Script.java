/* Script.java

	Purpose:
		
	Description:
		
	History:
		Tue Dec 13 15:04:35     2005, Created by tomyeh

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zhtml;

/**
 * The SCRIPT tag.
 * 
 * @author tomyeh
 */
public class Script extends org.zkoss.zhtml.impl.ContentTag {
	public Script() {
		super("script");
	}
	public Script(String content) {
		super("script", content);
	}
}

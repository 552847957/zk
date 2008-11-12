/* Area.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Oct 22 09:27:29     2008, Created by Flyworld
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zul.api;

import org.zkoss.zk.ui.Component;

/**
 * An area of a {@link Imagemap}.
 * 
 * @author tomyeh
 * @since 3.5.2
 */
public interface Area {
	/**
	 * Returns the text as the tooltip.
	 * <p>
	 * Default: null.
	 */
	public String getTooltiptext();

	/**
	 * Sets the text as the tooltip.
	 */
	public void setTooltiptext(String tooltiptext);
}

/* Condition.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun  2 22:07:52     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.util;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;

/**
 * A condition to test with. It could be used in any situation, but
 * we mainly use to denote wither an element in ZUL file is effective.
 *
 * @author tomyeh
 */
public interface Condition {
	/** Used to evaluate whether it is effective.
	 *
	 * @param comp used as the self variable. Ignored if null.
	 */
	public boolean isEffective(Component comp);
	/** Used to evaluate whether it is effective.
	 *
	 * @param page used as the self variable. Ignored if null.
	 */
	public boolean isEffective(Page page);
}

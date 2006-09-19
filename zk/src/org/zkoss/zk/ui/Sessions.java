/* Sessions.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon May 30 21:33:01     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui;

/**
 * Utilities to access {@link Session}.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class Sessions {
	/** Used to the store the session for the current thread. */
	protected static final ThreadLocal _sess = new ThreadLocal();

	protected Sessions() {} //prevent from instantiated

	/** Returns the session for the current thread.
	 */
	public static final Session getCurrent() {
		return (Session)_sess.get();
	}
}

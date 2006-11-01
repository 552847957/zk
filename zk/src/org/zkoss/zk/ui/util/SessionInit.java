/* SessionInit.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Mar 22 11:34:09     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.util;

import org.zkoss.zk.ui.Session;

/**
 * Used to initialize a session when it is created.
 *
 * <p>How this interface is used.
 * <ol>
 * <li>First, you specify a class that implements this interface
 * in WEB-INF/zk.xml as a listener.
 * </li>
 * <li>Then, even time ZK loader creates a new session, an instnace of
 * the specified class is instantiated and {@link #init} is called.</li>
 * </ol>
 * 
 * @author tomyeh
 */
public interface SessionInit {
	/** Called when a session is created and initialized.
	 *
	 * <p>If the client is based on HTTP (such as browsers), you could
	 * retrieve the HTTP session by {@link Session#getNativeSession}</p>
	 */
	public void init(Session sess);
}

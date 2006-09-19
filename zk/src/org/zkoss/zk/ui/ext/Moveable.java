/* Moveable.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Aug 12 08:44:39     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.ext;

/**
 * Used to decorate a {@link org.zkoss.zk.ui.Component} object that
 * it is moveable by the client.
 *
 * <p>Once it is moved by the user, {@link #setLeftByClient} and
 * {@link #setTopByClient}
 * are called and {@link org.zkoss.zk.ui.event.MoveEvent} is sent to notify the component.
 * 
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public interface Moveable {
	/** Sets the left of the component, caused by user's activity at
	 * the client.
	 * <p>This method is designed to be used by engine.
	 * Don't invoke it directly. Otherwise, the client and server
	 * might mismatch.
	 */
	public void setLeftByClient(String left);
	/** Sets the top of the component, caused by user's activity at
	 * the client.
	 * <p>This method is designed to be used by engine.
	 * Don't invoke it directly. Otherwise, the client and server
	 * might mismatch.
	 */
	public void setTopByClient(String top);
}

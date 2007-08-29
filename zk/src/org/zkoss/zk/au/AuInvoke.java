/* AuInvoke.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jul  6 22:52:34     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au;

import org.zkoss.zk.ui.Component;

/**
 * A response to ask the client to execute the specified client function.
 * Unlike {@link AuScript},
 *
 * <p>data[0]: the component UUID
 * data[1]: the client function name (i.e., JavaScript function)<br/>
 * data[2]: the second argument<br/>
 * data[3]: the third argument...
 *
 * <p>Note: the first argument is always the component itself.
 * 
 * @author tomyeh
 * @since 3.0.0
 */
public class AuInvoke extends AuResponse {
	/** Construct AuInvoke to call a client function with one argument,
	 * the component itself.
	 *
	 * <p>In other words, it invokes (assume Type is the comp's z.type)<br/>
	 * <code>zkType.function(comp)</code>
	 *
	 * @param comp the component that this script depends on.
	 * It cannot be null.
	 * @param function the function name
	 */
	public AuInvoke(Component comp, String function) {
		super("invoke", comp, new String[] {comp.getUuid(), function});
	}
	/** Construct AuInvoke to call a client function with two arguments,
	 * the component itself and arg.
	 *
	 * <p>In other words, it invokes (assume Type is the comp's z.type)<br/>
	 * <code>zkType.function(comp, arg)</code>
	 *
	 * @param comp the component that this script depends on.
	 * It cannot be null.
	 * @param function the function name
	 * @param arg the additional argument
	 */
	public AuInvoke(Component comp, String function, String arg) {
		super("invoke", comp,
			new String[] {comp.getUuid(), function, arg});
	}
	/** Construct AuInvoke to call a client function with three arguments,
	 * the component itself, arg1 and arg2.
	 *
	 * <p>In other words, it invokes (assume Type is the comp's z.type)<br/>
	 * <code>zkType.function(comp, arg1, arg2)</code>
	 *
	 * @param comp the component that this script depends on.
	 * It cannot be null.
	 * @param function the function name
	 */
	public AuInvoke(Component comp, String function, String arg1, String arg2) {
		super("invoke", comp, new String[] {comp.getUuid(), function,
			arg1, arg2});
	}
	/** Construct AuInvoke to call a client function with four arguments,
	 * the component itself, arg1, arg2 and arg3.
	 *
	 * <p>In other words, it invokes (assume Type is the comp's z.type)<br/>
	 * <code>zkType.function(comp, arg1, arg2, arg3)</code>
	 *
	 * @param comp the component that this script depends on.
	 * It cannot be null.
	 * @param function the function name
	 */
	public AuInvoke(Component comp, String function, String arg1, String arg2,
	String arg3) {
		super("invoke", comp, new String[] {comp.getUuid(), function,
			arg1, arg2, arg3});
	}
}

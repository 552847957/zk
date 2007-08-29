/* AuClientInfo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jul 28 11:39:43     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zk.au;

import org.zkoss.zk.ui.Desktop;

/**
 * A response to ask the browser to send back its information.
 *
 * @author tomyeh
 * @see org.zkoss.zk.ui.event.ClientInfoEvent
 */
public class AuClientInfo extends AuResponse {
	/** Contructs a client-info response with the specified desktop.
	 *
	 * @param desktop the desktop to get the client info back.
	 * If null, the client info is sent back for each desktop in the
	 * same browser window.
	 * @since 3.0.0
	 */
	public AuClientInfo(Desktop desktop) {
		super("clientInfo", desktop != null ? desktop.getId(): null);
	}
	/** Constructs a client-info response for all desktops of the same
	 * browser window.
	 */
	public AuClientInfo() {
		this(null);
	}
}

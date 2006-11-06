/* AbortByRemoveDesktop.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Nov  6 21:46:23     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zk.ui.impl;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.sys.AbortingReason;
import org.zkoss.zk.au.AuResponse;

/**
 * The aborting reason when the remove-desktop command is received.
 *
 * @author tomyeh
 */
public class AbortByRemoveDesktop implements AbortingReason {
	public AbortByRemoveDesktop() {
	}

	//-- AbortingReason --//
	public boolean isAborting() {
		return true;
	}
	public AuResponse getResponse() {
		final Desktop dt = Executions.getCurrent().getDesktop();
		final WebAppCtrl wappc = (WebAppCtrl)dt.getWebApp();
		wappc.getDesktopCache(dt.getSession()).removeDesktop(dt);
		return null;
	}
}

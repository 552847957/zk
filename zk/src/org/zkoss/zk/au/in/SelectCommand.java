/* SelectCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sun Oct  2 13:24:37     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.in;

import java.util.Set;

import org.zkoss.lang.Objects;

import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.ext.client.Selectable;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;

/**
 * Used only by {@link AuRequest} to implement the {@link SelectEvent}
 * relevant command.
 * 
 * @author tomyeh
 * @since 3.0.0
 */
public class SelectCommand extends Command {
	public SelectCommand(String evtnm, int flags) {
		super(evtnm, flags);
	}

	//-- super --//
	protected void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);

		final Set items = Commands.convertToItems(request);
		((Selectable)((ComponentCtrl)comp).getExtraCtrl())
			.selectItemsByClient(items);
		Events.postEvent(new SelectEvent(getId(), comp, items));
	}
}

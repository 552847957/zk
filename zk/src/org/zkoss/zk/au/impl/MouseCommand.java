/* MouseCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sun Oct  2 12:45:36     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.au.impl;

import org.zkoss.lang.Objects;

import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;

/**
 * Used only by {@link AuRequest} to implement the {@link MouseEvent}
 * relevant command.
 *
 * @author tomyeh
 */
public class MouseCommand extends Command {
	public MouseCommand(String evtnm, int flags) {
		super(evtnm, flags);
	}

	//-- super --//
	protected void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		final String[] data = request.getData();
		if (data != null && data.length != 1 && data.length != 2 && data.length != 3)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
				new Object[] {Objects.toString(data), this});

		final MouseEvent event =
		data == null || data.length == 0 ?
			new MouseEvent(getId(), comp):			//no area, no coord
		data.length == 1 ?
			new MouseEvent(getId(), comp, data[0]):	//by area
			new MouseEvent(getId(), comp,			//by coord
				Integer.parseInt(data[0]), Integer.parseInt(data[1]),
				data.length == 2 ? 0: parseKeys(data[2]));
		Events.postEvent(event);
	}
	private static int parseKeys(String flags) {
		int keys = 0;
		if (flags.indexOf("a") >= 0) keys |= MouseEvent.ALT_KEY;
		if (flags.indexOf("c") >= 0) keys |= MouseEvent.CTRL_KEY;
		if (flags.indexOf("s") >= 0) keys |= MouseEvent.SHIFT_KEY;
		return keys;
	}
}

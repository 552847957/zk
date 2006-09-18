/* Event.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sat Jun 11 10:41:14     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2004 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.zk.ui.event;

import com.potix.zk.ui.Component;
import com.potix.zk.ui.Page;

/**
 * An event sent to the event handler of a component.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 * @see Component
 */
public class Event {
	private final String _name;
	private final Component _target;
	private final Object _data;
	private boolean _propagatable = true;

	/** Constructs a simple event.
	 * @param target a component, or null to indicate broadcasting
	 * the event to all root components.
	 */
	public Event(String name, Component target) {
		if (name == null)
			throw new NullPointerException();
		_name = name;
		_target = target;
		_data = null;
	}
	/** Constructs a simple event.
	 * @param target a component, or null to indicate broadcasting
	 * the event to all root components.
	 * @param data an arbitary data
	 */
	public Event(String name, Component target, Object data) {
		if (name == null)
			throw new NullPointerException();
		_name = name;
		_target = target;
		_data = data;
	}

	/** Returns the event name.
	 */
	public final String getName() {
		return _name;
	}
	/** Returns the target, or null if broadcast.
	 */
	public final Component getTarget() {
		return _target;
	}
	/** Returns the page owning this event, or null if broadcast.
	 */
	public final Page getPage() {
		return _target != null ? _target.getPage(): null;
	}
	/** Returns the data accompanies with this event, or null if not available.
	 */
	public Object getData() {
		return _data;
	}

	/** Returns whether this event is propagatable.
	 * <p>Default: true.
	 * <p>It becomes false if {@link #stopPropagation} is called.
	 * If true, the event will be sent to the following event listener
	 * ({@link EventListener}) being registered by {@link Component#addEventListener}
	 * and {@link Page#addEventListener}.
	 */
	public boolean isPropagatable() {
		return _propagatable;
	}
	/** Stops the propagation for this event.
	 */
	public void stopPropagation() {
		_propagatable = false;
	}

	//-- Object --//
	public String toString() {
		final String clsnm = getClass().getName();
		final int j = clsnm.lastIndexOf('.');
		return "["+clsnm.substring(j+1)+' '+_name+' '+_target+']';
	}
}

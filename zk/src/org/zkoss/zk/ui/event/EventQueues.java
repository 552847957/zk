/* EventQueues.java

	Purpose:
		
	Description:
		
	History:
		Fri May  2 15:30:36     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zk.ui.event;

import org.zkoss.lang.Library;
import org.zkoss.lang.Classes;

import org.zkoss.zk.ui.UiException;

import org.zkoss.zk.ui.event.impl.EventQueueProvider;
import org.zkoss.zk.ui.event.impl.EventQueueProviderImpl;

/**
 * Utilities to access the event queue.
 *
 * <h3>Customization:</h3>
 *
 * <p>The implementation of {@link EventQueue} and even the scope
 * are customizable. To customize, specify the name of a class implementing
 * {@link EventQueueProvider} in the library property called
 * "org.zkoss.zk.ui.event.EventQueueProvider.class".
 * For example, you can use JMS to extend the queue to be able to communicate
 * with applications running in different JVM.
 * @author tomyeh
 * @since 5.0.0
 */
public class EventQueues {
	/** Represents the event queue in the desktop scope.
	 * In other words, the events published to this kind of queues
	 * can be passed around only in the same desktop.
	 */
	public static final String DESKTOP = "desktop";
	/** Represenets the event queue in the application scope.
	 * In other words, the events published to this kind of queues
	 * can be passed around to any desktops of the same application.
	 * <p>Notice that this feature requires ZK PE or EE, or you have to
	 * provide your own implementation.
	 */
	public static final String APPLICATION = "application";

	/** Returns the event queue with the specified name in the
	 * specified scope.
	 *
	 * <p>There are two kinds of event scopes: {@link #DESKTOP} and
	 * {@link #APPLICATION}.
	 *
	 * <p>If the desktop scope is selected, the event queue is associated
	 * with the desktop of the current execution.
	 * And, the event queue is gone if the desktop is removed,
	 * or removed manually by {@link #remove}.
	 *
	 * <p>If the application scope is selected, the event queue is
	 * associated with the application, and remains until the application
	 * stops or removed manually by {@link #remove}.
	 * When an execution subscribes an event queue, the server push
	 * is enabled automatically.
	 *
	 * <p>Note:
	 * <ul>
	 * <li>This method can be called only in an activated execution,
	 * i.e., {@link org.zkoss.zk.ui.Executions#getCurrent} not null.</li>
	 * </ul>
	 *
	 * @param name the queue name.
	 * @param scope the scope fo the event queue. Currently,
	 * it supports {@link #DESKTOP} and {@link #APPLICATION}.
	 * @param autoCreate whether to create the event queue if not found.
	 * @return the event queue with the associated name, or null if
	 * not found and autoCreate is false
	 * @exception IllegalStateException if not in an activated execution
	 * @exception UnsupportedOperationException if the scope is not supported
	 */
	public static
	EventQueue lookup(String name, String scope, boolean autoCreate) {
		return getProvider().lookup(name, scope, autoCreate);
	}
	/** Returns the desktop-level event queue with the specified name in the current
	 * desktop.
	 * It is a shortcut of <code>lookup(name, DESKTOP, autoCreate)</code>.
	 */
	public static EventQueue lookup(String name, boolean autoCreate) {
		return lookup(name, DESKTOP, autoCreate);
	}
	/** Returns the desktop-level event queue with the specified name in the current
	 * desktop, or if no such event queue, create one.
	 * It is a shortcut of <code>lookup(name, DESKTOP, true)</code>.
	 */
	public static EventQueue lookup(String name) {
		return lookup(name, DESKTOP, true);
	}
	/** Removes the event queue.
	 * @param name the queue name.
	 * @param scope the scope fo the event queue. Currently,
	 * it supports {@link #DESKTOP} and {@link #APPLICATION}.
	 * @return true if it is removed successfully
	 */
	public static boolean remove(String name, String scope) {
		return getProvider().remove(name, scope);
	}
	private static final EventQueueProvider getProvider() {
		if (_provider == null)
			synchronized (EventQueues.class) {
				if (_provider == null) {
					EventQueueProvider provider;
					String clsnm = Library.getProperty("org.zkoss.zk.ui.event.EventQueueProvider.class");
					if (clsnm == null)
						clsnm = Library.getProperty("org.zkoss.zkmax.ui.EventQueueProvider.class");
							//backward compatible
					try {
						final Object o = Classes.newInstanceByThread(
							clsnm != null ? clsnm:
							"org.zkoss.zkmax.zk.eq.EventQueueProviderImpl");
								//try zkmax first
						if (!(o instanceof EventQueueProvider))
							throw new UiException(o.getClass().getName()+" must implement "+EventQueueProvider.class.getName());
						provider = (EventQueueProvider)o;
					} catch (UiException ex) {
						throw ex;
					} catch (Throwable ex) {
						if (clsnm != null)
							throw UiException.Aide.wrap(ex, "Unable to load "+clsnm);
						provider = new EventQueueProviderImpl();
					}
					_provider = provider;
				}
			}
		return _provider;
	}
	private static EventQueueProvider _provider;
}

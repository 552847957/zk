/* ExecutionCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Jun  6 14:36:47     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.PageDefinition;

/**
 * Additional interface to {@link org.zkoss.zk.ui.Execution}
 * for implementation.
 *
 * <p>Application developers shall never access any of this methods.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public interface ExecutionCtrl {
	/** Returns the current page.
	 * Though an execution might process many pages, it processes update
	 * requests one-by-one and each update request is associated
	 * with a page.
	 *
	 * <p>Design decision: we put it here because user need not to know
	 * about the conccept of the current page.
	 *
	 * @see org.zkoss.zk.ui.Desktop#getPage
	 */
	public Page getCurrentPage();
	/** Sets the current page.
	 * Though an execution might process many pages, it processes update requests
	 * one-by-one and each update request is associated with a page.
	 */
	public void setCurrentPage(Page page);

	/** Returns the current page definition, which is pushed when
	 * evaluating a page (from a page definition).
	 */
	public PageDefinition getCurrentPageDefinition();
	/** Sets the current page definition.
	 * @param pgdef the page definition. If null, it means it is the same
	 * as getCurrentPage().getPageDefinition().
	 */
	public void setCurrentPageDefinition(PageDefinition pgdef);

	/** Returns the next event queued by
	 * {@link org.zkoss.zk.ui.Execution#postEvent}, or null if no event queued.
	 */
	public Event getNextEvent();

	/** Returns whether this execution is activated.
	 */
	public boolean isActivated();
	/** Called when this execution is about to activate.
	 * It is called before any execution.
	 * <p>It is used as callback notification.
	 */
	public void onActivate();
	/** Called when this execution is about to de-activate.
	 * It is called before ending, no matter exception being throw or not.
	 * <p>It is used as callback notification.
	 */
	public void onDeactivate();

	/** Sets the {@link Visualizer} for this execution.
	 * It could be anything that {@link UiEngine} requires.
	 */
	public void setVisualizer(Visualizer ei);
	/** Returns the {@link Visualizer} for this execution
	 * (set by {@link #setVisualizer}.
	 */
	public Visualizer getVisualizer();

	/** Sets the header of response.
	 */
	public void setHeader(String name, String value);

	/** Returns the value of an attribute in the client request
	 * (e.g., HTTP request) that creates this execution.
	 *
	 * <p>Notice that a servlet might include serveral ZK pages,
	 * while an independent execution is created for adding a new page.
	 * It means, each client request might create several executions.
	 * Thus, the attribute set by this method might last for several
	 * executions (until the request ends).
	 */
	public Object getRequestAttribute(String name);
	/** Sets the value of an attribute to the client request
	 * (e.g., HTTP request) that creates this execution.
	 */
	public void setRequestAttribute(String name, Object value);
}

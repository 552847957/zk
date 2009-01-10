/* PageCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Jun  8 13:55:09     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import java.util.Collection;
import java.io.Writer;
import java.io.IOException;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.metainfo.ZScript;

/**
 * Addition interface to {@link Page} for implementation purpose.
 *
 * <p>Application developers shall never access any of this methods.
 *
 * @author tomyeh
 */
public interface PageCtrl {
	/** The execution attribute used to control how to {@link #redraw}
	 * a page. By default, the page draws the desktop with the page if
	 * necessary (such as the top-level page and not being included).
	 * If you prefer to draw the page only, you can set the value to
	 * <code>page</code>.
	 * If you prefer to draw the desktop with the page, you can set the
	 * vallue to <code>desktop</code>.
	 * If the page already has everything that the client expects such
	 * as the HTML and BODY tags, you can set the value to <code>complete</code>.
	 *
	 * <p>Note: if {@link Page#isComplete} is true, it has the same effect
	 * of setting {@link #ATTR_REDRAW_CONTROL} to <code>complete</code>.
	 * 
	 * <p>Default: null (means auto).
	 * @since 5.0.0
	 */
	public static final String ATTR_REDRAW_CONTROL = "org.zkoss.zk.ui.page.redrawCtrl";

	/** Pre-initializes this page.
	 * It initializes {@link org.zkoss.zk.ui.Page#getDesktop},
	 * but it doesn't add this page to the desktop yet
	 * (which is done by {@link #init}).
	 *
	 * <p>Note: it is called before 
	 * {@link org.zkoss.zk.ui.util.Initiator#doInit} and {@link #init}.
	 * Since {@link org.zkoss.zk.ui.Page#getDesktop} is initialized in this
	 * method, it is OK to create components in 
	 * {@link org.zkoss.zk.ui.util.Initiator#doInit}.
	 *
	 * @since 3.5.2
	 */
	public void preInit();
	/** Initializes this page by assigning the info provided by
	 * the specified {@link PageConfig}, and then adds it
	 * to a desktop (by use of {@link Execution#getDesktop}).
	 *
	 * <p>Note: this method is called after {@link #preInit} and
	 * {@link org.zkoss.zk.ui.util.Initiator#doInit}.
	 *
	 * <p>This method shall be called only after the current execution
	 * is activated.
	 *
	 * @param config the info about how to initialize this page
	 * @since 3.0.0
	 */
	public void init(PageConfig config);
	/** Called when this page is about to be detroyed.
	 * It is called by desktop, after removing it from the desktop.
	 */
	public void destroy();

	/** Returns the header elements declared in this page (never null).
	 * An empty string is returned if special header is defined.
	 * <p>For HTML, the header element is the HEAD element.
	 */
	public String getHeaders();
	/** Returns the attributes of the root element declared in this page
	 * (never null).
	 * An empty string is returned if no special attribute is declared.
	 *
	 * <p>For HTML, the root element is the HTML element.
	 * @since 3.0.0
	 */
	public String getRootAttributes();
	/** Set the attributes of the root element declared in this page
	 *
	 * <p>Default: "".
	 */
	public void setRootAttributes(String rootAttributes);
	/** Returns the doc type (&lt;!DOCTYPE&gt;),
	 * or null to use the device default.
	 *
	 * @since 3.0.0
	 */
	public String getDocType();
	/** Sets the doc type (&lt;!DOCTYPE&gt;).
	 *
	 * <p>Default: null (i.e., the device default)
	 * @since 3.0.0
	 */
	public void setDocType(String docType);
	/** Returns the first line to be generated to the output,
	 * or null if nothing to generate.
	 *
	 * <p>For XML devices, it is usually the xml processing instruction:<br/>
	 * <code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;
	 *
	 * @since 3.0.0
	 */
	public String getFirstLine();
	/** Sets the first line to be generated to the output.
	 *
	 * <p>Default: null (i.e., nothing generated)
	 * @since 3.0.0
	 */
	public void setFirstLine(String firstLine);

	/** Returns the content type, or null to use the device default.
	 *
	 * @since 3.0.0
	 */
	public String getContentType();
	/** Sets the content type.
	 *
	 * @since 3.0.0
	 */
	public void setContentType(String contentType);
	/** Returns if the client can cache the rendered result, or null
	 * to use the device default.
	 *
	 * @since 3.0.0
	 */
	public Boolean getCacheable();
	/** Sets if the client can cache the rendered result.
	 *
	 * <p>Default: null (use the device default).
	 * @since 3.0.0
	 */
	public void setCacheable(Boolean cacheable);

	/** Returns the owner of this page, or null if it is not owned by
	 * any component.
	 * A page is included by a component. We say it is owned by the component.
	 */
	public Component getOwner();
	/** Sets the owner of this page.
	 * <p>Used only internally.
	 */
	public void setOwner(Component comp);

	/** Redraws the whole page into the specified output.
	 *
	 * <p>You could use {@link #ATTR_REDRAW_CONTROL} to control
	 * how to render manually.
	 *
	 * @since 5.0.0
	 */
	public void redraw(Writer out) throws IOException;

	/** Adds a deferred zscript.
	 *
	 * @param parent the component that is the parent of zscript (in
	 * the ZUML page), or null if it belongs to the page.
	 * @param zscript the zscript that shall be evaluated as late as
	 * when the interpreter of the same language is being loaded.
	 */
	public void addDeferredZScript(Component parent, ZScript zscript);
 	/** @deprecated As of release 5.0.0, it is removed to simplify ZK.
 	 */
 	public Component getDefaultParent();
 	/** @deprecated As of release 5.0.0, it is removed to simplify ZK.
 	 */
 	public void setDefaultParent(Component comp);

	/** Notification that the session, which owns this page,
	 * is about to be passivated (aka., serialized).
	 */
	public void sessionWillPassivate(Desktop desktop);
	/** Notification that the session, which owns this page,
	 * has just been activated (aka., deserialized).
	 */
	public void sessionDidActivate(Desktop desktop);
}

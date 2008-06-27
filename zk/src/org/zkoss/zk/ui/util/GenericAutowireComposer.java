/* GenericAutowireComposer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 11, 2008 10:56:06 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.zkoss.lang.Classes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WebApp;

/**
 * <p>An abstract composer that you can extend and write intuitive onXxx 
 * event handler methods with "auto-wired" accessible variable objects such
 * as implicit objects, components, and external resolvable variables in a ZK 
 * zuml page; this class will registers onXxx events to the supervised 
 * component and wire all accessible variable objects to this composer by 
 * calling setXxx() method or set xxx field value directly per the variable 
 * name. Since 3.0.7, this composer has wired all implicit objects 
 * such as self, spaceOwner, page, desktop, session, application, 
 * componentScope, spaceScope, pageScope, desktopScope, sessionScope, 
 * applicationScope, and requestScope, so you can use them directly. Besides 
 * that, it also provides alert(String message) method, so you can call alert() 
 * without problems.</p>
 * 
 * <p>Notice that since this composer kept references to the components, single
 * instance composer object cannot be shared by multiple components.</p>
 *  
 * <p>The following is an example. The onOK event listener is registered into 
 * the target window, and the Textbox component with id name "mytextbox" is
 * injected into the "mytextbox" field automatically (so you can use 
 * mytextbox variable directly in onOK).</p>
 * 
 * <pre><code>
 * MyComposer.java
 * 
 * public class MyComposer extends GenericAutowireComposer {
 *     private Textbox mytextbox;
 *     
 *     public void onOK() {
 *         mytextbox.setValue("Enter Pressed");
 *         alert("Hi!");
 *     }
 * }
 * 
 * test.zul
 * 
 * &lt;window id="mywin" apply="MyComposer">
 *     &lt;textbox id="mytextbox"/>
 * &lt;/window>
 * </code></pre>
 * 
 * @author henrichen
 * @since 3.0.6
 * @see org.zkoss.zk.ui.Components#wireFellows
 */
abstract public class GenericAutowireComposer extends GenericComposer {
	/** Implicit Object; the applied component itself. 
	 * @since 3.0.7
	 */ 
	protected Component self;
	/** Implicit Object; the space owner of the applied component.
	 * @since 3.0.7
	 */
	protected IdSpace spaceOwner;
	/** Implicit Object; the page.
	 * @since 3.0.7
	 */
	protected Page page;
	/** Implicit Object; the desktop.
	 * @since 3.0.7
	 */
	protected Desktop desktop;
	/** Implicit Object; the session.
	 * @since 3.0.7
	 */
	protected Session session;
	/** Implicit Object; the web application.
	 * @since 3.0.7
	 */
	protected WebApp application;
	/** Implicit Object; a map of attributes defined in the applied component.
	 * @since 3.0.7
	 */
	protected Map componentScope;
	/** Implicit Object; a map of attributes defined in the ID space contains the applied component.
	 * @since 3.0.7
	 */
	protected Map spaceScope;
	/** Implicit Object; a map of attributes defined in the page.
	 * @since 3.0.7
	 */
	protected Map pageScope;
	/** Implicit Object; a map of attributes defined in the desktop.
	 * @since 3.0.7
	 */
	protected Map desktopScope;
	/** Implicit Object; a map of attributes defined in the session.
	 * @since 3.0.7
	 */
	protected Map sessionScope;
	/** Implicit Object; a map of attributes defined in the web application.
	 * @since 3.0.7
	 */
	protected Map applicationScope;
	/** Implicit Object; a map of attributes defined in the request.
	 * @since 3.0.7
	 */
	protected Map requestScope;
	
	/**
	 * Auto wire accessible variables of the specified component into a 
	 * controller Java object; a subclass that 
	 * override this method should remember to call super.doAfterCompose(comp) 
	 * or it will not work.
	 */
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		//initialize implicit objects
		self = comp;
		spaceOwner = comp.getSpaceOwner();
		page = comp.getPage();
		desktop = comp.getDesktop();
		session = desktop.getSession();
		application = desktop.getWebApp();
		componentScope = comp.getAttributes();
		spaceScope = (spaceOwner instanceof Page) ? 
			pageScope : ((Component)spaceOwner).getAttributes();
		pageScope = page.getAttributes();
		desktopScope = desktop.getAttributes();
		sessionScope = session.getAttributes();
		applicationScope = application.getAttributes();
		requestScope = new RequestScope();
		
		//wire variables to reference fields
		Components.wireVariables(comp, this);
	}
	
	/** Shortcut to call Messagebox.show(String).
	 * @since 3.0.7 
	 */
	private static Method SHOW;
	protected void alert(String m) {
		//zk.jar cannot depends on zul.jar; thus we call Messagebox.show() via
		//reflection. kind of weird :-).
		try {
			if (SHOW == null) {
				final Class mboxcls = Classes.forNameByThread("org.zkoss.zul.Messagebox");
				SHOW = mboxcls.getMethod("show", new Class[] {String.class});
			}
			SHOW.invoke(null, new Object[] {m});
		} catch (InvocationTargetException e) {
			throw UiException.Aide.wrap(e);
		} catch (Exception e) {
			//ignore
		}
	}
	
	//Proxy to read current requestScope
	private static class RequestScope implements Map {
		private static final Map req() {
			return Executions.getCurrent().getAttributes();
		}
		public void clear() {
			req().clear();
		}
		public boolean containsKey(Object key) {
			return req().containsKey(key);
		}
		public boolean containsValue(Object value) {
			return req().containsValue(value);
		}
		public Set entrySet() {
			return req().entrySet();
		}
		public Object get(Object key) {
			return req().get(key);
		}
		public boolean isEmpty() {
			return req().isEmpty();
		}
		public Set keySet() {
			return req().keySet();
		}
		public Object put(Object key, Object value) {
			return req().put(key, value);
		}
		public void putAll(Map arg0) {
			req().putAll(arg0);
		}
		public Object remove(Object key) {
			return req().remove(key);
		}
		public int size() {
			return req().size();
		}
		public Collection values() {
			return req().values();
		}
	}
}

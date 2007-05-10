/* SimpleSession.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jun  3 17:05:30     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.http;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.Serializable;
import java.io.Externalizable;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.zkoss.util.logging.Log;
import org.zkoss.el.impl.AttributesMap;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.sys.SessionCtrl;
import org.zkoss.zk.ui.sys.WebAppCtrl;
import org.zkoss.zk.ui.sys.DesktopCache;
import org.zkoss.zk.ui.util.Monitor;
import org.zkoss.zk.ui.util.Configuration;

/** A non-serializable implementation of {@link org.zkoss.zk.ui.Session}.
 * 
 * <p>Note:<br/>
 * Though this class is serializable, it is meaningless to serialize
 * it directly. Reason: it doesn't serialize any session attributes.
 * Rather, it is serialized when Web server serialized HttpSession.
 * Also, notice that {@link SimpleSession} is stored as an attribute
 * HttpSession.
 *
 * @author tomyeh
 */
public class SimpleSession implements Session, SessionCtrl {
	private static final Log log = Log.lookup(SimpleSession.class);

	/** The attribute used to hold a map of attributes that are written
	 * thru {@link #setAttribute}.
	 *
	 * <p>Note: once a HttpSession is serialized, all serializable attributes
	 * are serialized. However, if ZK's session is not serializable, it causes
	 * inconsistency. For example, if an application has stored a serializable
	 * attribute, say myAttr, in a session. Then, it will be surprised to see
	 * myAttr is available while the session is actually brand-new
	 *
	 * <p>It is used if java.io.Serializable is NOT implemented.
	 */
	private static final String ATTR_PRIVATE = "javax.zkoss.ui.session.private";

	/** The Web application that this session belongs to. */
	private WebApp _wapp;
	/** The HTTP session that this session is associated with. */
	private HttpSession _hsess;
	/** The attributes belonging to this session.
	 * Note: it is the same map of HttpSession's attributes.
	 * Note: No need to serialize attributes since it is done by Web server.
	 */
	private Map _attrs;
	/** The client's IP addression. */
	private String _clientAddr;
	/** The client's host name. */
	private String _clientHost;
	private DesktopCache _cache;
	/** Next available component uuid. */
	private int _nextUuid;
	private boolean _invalid;

	public SimpleSession(WebApp wapp, HttpSession hsess, String clientAddr,
	String clientHost) {
		if (wapp == null || hsess == null)
			throw new IllegalArgumentException();

		cleanSessAttrs(hsess);

		_wapp = wapp;
		_hsess = hsess;
		_clientAddr = clientAddr;
		_clientHost = clientHost;
		init();

		final Configuration config = getWebApp().getConfiguration();
		config.invokeSessionInits(this); //it might throw exception

		final Monitor monitor = config.getMonitor();
		if (monitor != null) {
			try {
				monitor.sessionCreated(this);
			} catch (Throwable ex) {
				log.error(ex);
			}
		}
	}
	/** Called to initialize some members after this object is deserialized.
	 * <p>In other words, it is called by the deriving class if it implements
	 * java.io.Serializable.
	 */
	private final void init() {
		_attrs = new AttributesMap() {
			protected Enumeration getKeys() {
				return _hsess.getAttributeNames();
			}
			protected Object getValue(String key) {
				return _hsess.getAttribute(key);
			}
			protected void setValue(String key, Object val) {
				setAttribute(key, val);
			}
			protected void removeValue(String key) {
				removeAttribute(key);
			}
		};
	}

	public Object getAttribute(String name) {
		return _hsess.getAttribute(name);
	}
	public void setAttribute(String name, Object value) {
		if (name.startsWith("javax.zkoss"))
			throw new IllegalArgumentException("Unable to change the readonly attribute: "+name);

		//Note: we have to handle ATTR_PRIVATE, so cleanSessAttrs knows what to do
		if (!(this instanceof Serializable || this instanceof Externalizable)
		/*&& name != null && !name.startsWith("javax.")*/
		&& (value instanceof Serializable || value instanceof Externalizable)) {
			synchronized (this) {
				_hsess.setAttribute(name, value);

				Map prv = (Map)_hsess.getAttribute(ATTR_PRIVATE);
				if (prv == null)
					_hsess.setAttribute(ATTR_PRIVATE, prv = new HashMap());
				prv.put(name, value);
			}
		} else {
			_hsess.setAttribute(name, value);
		}
	}
	public void removeAttribute(String name) {
		if (name.startsWith("javax.zkoss"))
			throw new IllegalArgumentException("Unable to remove the readonly attribute: "+name);

		//Note: we have to handle ATTR_PRIVATE, so cleanSessAttrs knows what to do
		if (!(this instanceof Serializable || this instanceof Externalizable)
		/*&& name != null && !name.startsWith("javax.")*/) {
			synchronized (this) {
				_hsess.removeAttribute(name);

				Map prv = (Map)_hsess.getAttribute(ATTR_PRIVATE);
				if (prv != null)
					prv.remove(name);
			}
		} else {
			_hsess.removeAttribute(name);
		}
	}
	public Map getAttributes() {
		return _attrs;
	}
	/** Cleans up attributes that shall not available to a brand-new session
	 * See {@link #ATTR_PRIVATE} for details.
	 */
	private static void cleanSessAttrs(HttpSession hsess) {
		final Map prv = (Map)hsess.getAttribute(ATTR_PRIVATE);
		if (prv != null) {
			for (Iterator it = prv.entrySet().iterator(); it.hasNext();) {
				final Map.Entry me = (Map.Entry)it.next();
				final String nm = (String)me.getKey();
				if (hsess.getAttribute(nm) == me.getValue()) //don't use equals
					hsess.removeAttribute(nm);
			}
		}
	}

	public String getClientAddr() {
		return _clientAddr;
	}
	public String getClientHost() {
		return _clientHost;
	}

	public void invalidateNow() {
		_hsess.invalidate();
	}
	public void setMaxInactiveInterval(int interval) {
		_hsess.setMaxInactiveInterval(interval);
	}
	public Object getNativeSession() {
		return _hsess;
	}

	synchronized public int getNextUuidGroup(int groupSize) {
		int uuid = _nextUuid;
		_nextUuid += groupSize;
		return uuid;
	}

	public final WebApp getWebApp() {
		return _wapp;
	}

	public final void invalidate() {
		_invalid = true;
	}
	public final boolean isInvalidated() {
		return _invalid;
	}

	public DesktopCache getDesktopCache() {
		return _cache;
	}
	public void setDesktopCache(DesktopCache cache) {
		_cache = cache;
	}
	public void recover(Object nativeSession) {
		sessionDidActivate((HttpSession)nativeSession);
	}

	public void onDestroyed() {
		final Configuration config = getWebApp().getConfiguration();
		config.invokeSessionCleanups(this);

		final Monitor monitor = config.getMonitor();
		if (monitor != null) {
			try {
				monitor.sessionDestroyed(this);
			} catch (Throwable ex) {
				log.error(ex);
			}
		}
	}

	//--Serializable for deriving classes--//
	/** Used by the deriving class,
	 * only if the deriving class implements java.io.Serializable.
	 */
	protected SimpleSession() {}
	/** Used by the deriving class to write this object,
	 * only if the deriving class implements java.io.Serializable.
	 * <p>Refer to {@link SerializableSession} for how to use this method.
	 */
	protected void writeThis(java.io.ObjectOutputStream s)
	throws java.io.IOException {
		s.writeObject(_clientAddr);
		s.writeObject(_clientHost);
		s.writeObject(_cache);
		s.writeInt(_nextUuid);
	}
	/** Used by the deriving class to read back this object,
	 * only if the deriving class implements java.io.Serializable.
	 * <p>Refer to {@link SerializableSession} for how to use this method.
	 */
	protected void readThis(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		init();

		_clientAddr = (String)s.readObject();
		_clientHost = (String)s.readObject();
		_cache = (DesktopCache)s.readObject();
		_nextUuid = s.readInt();
	}
	/** Used by the deriving class to pre-process a session before writing
	 * the session
	 *
	 * <p>Refer to {@link SerializableSession} for how to use this method.
	 */
	protected void sessionWillPassivate() {
		((WebAppCtrl)_wapp).sessionWillPassivate(this);
	}
	/** Used by the deriving class to post-process a session after
	 * it is read back.
	 *
	 * <p>Application shall not call this method directly.
	 *
	 * <p>Refer to {@link SerializableSession} for how to use this method.
	 */
	protected void sessionDidActivate(HttpSession hsess) {
		//Note: in Tomcat, servlet is activated later, so we have to
		//add listener to WebManager instead of process now

		_hsess = hsess;
		WebManager.addListener(
			_hsess.getServletContext(),
			new ActivationListener() {
				public void onActivated(WebManager webman) {
					_wapp = webman.getWebApp();
					((WebAppCtrl)_wapp)
						.sessionDidActivate(SimpleSession.this);
				}
			});
	}
}

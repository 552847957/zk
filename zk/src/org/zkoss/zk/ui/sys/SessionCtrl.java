/* SessionCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jun  3 15:08:34     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.ComponentNotFoundException;

/**
 * Additional interface of {@link org.zkoss.zk.ui.Session} for implementation.
 * <p>Note: applications shall never access this interface.
 *
 * @author tomyeh
 */
public interface SessionCtrl {
	/** Sets the device type that this session belongs to.
	 *
	 * <p>It is called by the desktop when {@link Desktop#setDeviceType}
	 * is called.
	 * Don't call this method directly. Otherwise, the result is inpredictable.
	 *
	 * @since 2.4.1
	 */
	public void setDeviceType(String deviceType);

	/** Returns the desktop cache, or null if not available.
	 *
	 * <p>Note: this is an utility to implement {@link DesktopCacheProvider},
	 * which might or might not use this method. Other part of ZK shall not
	 * access this method.
	 */
	public DesktopCache getDesktopCache();
	/** Sets the desktop cache.
	 *
	 * <p>Note: this is an utility to implement {@link DesktopCacheProvider},
	 * which might or might not use this method. Other part of ZK shall not
	 * access this method.
	 *
	 * @param cache the desktop cache; null to denote no cache available.
	 */
	public void setDesktopCache(DesktopCache cache);
	
	/** Called when the session is destroyed.
	 * <p>Application shall never access this method.
	 */
	public void onDestroyed();

	/** Called when ZK detects {@link org.zkoss.zk.ui.Session#getNativeSession}
	 * is not initialized properly.
	 *
	 * <p>It is actually a workaround to solve the issue that some Web
	 * contrainer fails to call
	 * HttpSessionActivationListener.sessionDidActivate() unpon failover.
	 */
	public void recover(Object nativeSession);

	/** Returns whether this session is invalidated
	 * (i.e., {@link org.zkoss.zk.ui.Session#invalidate} was called).
	 */
	public boolean isInvalidated();
	/** Really invalidates the session.
	 * <p>Application shall never access this method.
	 */
	public void invalidateNow();

	/** Returns a sequence of UUID for {@link DesktopCtrl#getNextUuid}
	 * to generate component UUID.
	 *
	 * @param groupSize how many number of UUID the caller wants.
	 */
	public int getNextUuidGroup(int groupSize);
}

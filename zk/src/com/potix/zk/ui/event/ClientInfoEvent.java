/* ClientInfoEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jul 25 16:34:05     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.zk.ui.event;

import java.util.TimeZone;
import com.potix.util.TimeZones;

/**
 * The onClientInfo event is used to notify the client's information, such
 * as time zone and screen resolutions.
 *
 * <p>This event is sent if and only if it is registered to a root component.
 *
 * <p>Note: the information returned by this event is not stored in the server.
 * Thus, you might want to store in the session's attribute
 * ({@link com.potix.zk.ui.Session#setAttribute}).
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 * @see com.potix.zk.ui.util.Clients
 */
public class ClientInfoEvent extends Event {
	private final TimeZone _timeZone;
	private final int _scrnwd, _scrnhgh, _colorDepth;
	private final int _dtwd, _dthgh, _dtx, _dty;

	/** Constructs an event to hold the client-info.
	 *
	 * <p>Note: {@link #getTarget} will return null. It means it is a broadcast
	 * event.
	 *
	 * @param scrnwd the screen's width
	 * @param scrnhgh the screen's height
	 * @param dtwd the desktop's width
	 * @param dthgh the desktop's height
	 * @param dtx the desktop's the left offset
	 * @param dty the desktop's the top offset
	 */
	public ClientInfoEvent(String name, int timeZoneOfs,
	int scrnwd, int scrnhgh, int colorDepth,
	int dtwd, int dthgh, int dtx, int dty) {
		super(name, null);

		final StringBuffer sb = new StringBuffer(8).append("GMT");
		//Note: we have to revert the sign
		//see http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Date:getTimezoneOffset
		_timeZone = TimeZones.getTimeZone(-timeZoneOfs);

		_scrnwd = scrnwd;
		_scrnhgh = scrnhgh;
		_colorDepth = colorDepth;

		_dtwd = dtwd;
		_dthgh = dthgh;
		_dtx = dtx;
		_dty = dty;
	}
	/** Returns the time zone of the client.
	 */
	public TimeZone getTimeZone() {
		return _timeZone;
	}
	/** Returns the pixel width of the client's screen.
	 */
	public int getScreenWidth() {
		return _scrnwd;
	}
	/** Returns the pixel height of the client's screen.
	 */
	public int getScreenHeight() {
		return _scrnhgh;
	}
	/** Returns the maximum number of colors the client's screen supports.
	 */
	public int getColorDepth() {
		return _colorDepth;
	}
	/** Returns the pixel width of the client's desktop.
	 */
	public int getDesktopWidth() {
		return _dtwd;
	}
	/** Returns the pixel height of the client's desktop.
	 */
	public int getDesktopHeight() {
		return _dthgh;
	}
	/** The the current horizontal pixel location of the top-left corner of
	 * the document in the window.
	 * It is changed by user when he scrolls the browser.
	 * <p>To change it programmingly, use {@link com.potix.zk.ui.util.Clients#scrollTo}.
	 */
	public int getDesktopXOffset() {
		return _dtx;
	}
	/** The the current vertical pixel location of the top-left corner of
	 * the document in the window.
	 * It is changed by user when he scrolls the browser.
	 * <p>To change it programmingly, use {@link com.potix.zk.ui.util.Clients#scrollTo}.
	 */
	public int getDesktopYOffset() {
		return _dty;
	}
}

/* PollingServerPush.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Aug  3 18:53:21     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkex.ui.impl;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.zkoss.lang.D;
import org.zkoss.lang.Threads;
import org.zkoss.util.logging.Log;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.sys.ServerPush;
import org.zkoss.zk.ui.impl.ExecutionCarryOver;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.au.out.AuScript;

/**
 * A server-push implementation that is based on client-polling.
 *
 * @author tomyeh
 * @since 3.0.0
 */
public class PollingServerPush implements ServerPush {
	private static final Log log = Log.lookup(PollingServerPush.class);
	/** Denote a server-push thread gives up the activation (timeout). */
	private static final int GIVEUP = -99;

	private Desktop _desktop;
	private Configuration _config;
	/** List of ThreadInfo. */
	private final List _pending = new LinkedList();
	/** The active thread. */
	private ThreadInfo _active;
	/** The info to carray over from onPiggyback to the server-push thread. */
	private ExecutionCarryOver _carryOver;
	/** A mutex that is used by this object to wait for the server-push thread
	 * to complete.
	 */
	private final Object _mutex = new Object();

	/** Returns the JavaScript codes to enable (aka., start) the server push.
	 */
	protected String getStartScript() {
		final String start = _config.getPreference("PollingServerPush.start", null);
		if (start != null)
			return start;

		final String dtid = _desktop.getId();
		final StringBuffer sb = new StringBuffer(128)
			.append("zk.invoke('zkex.ui.cpsp',function(){zkCpsp.start('")
			.append(dtid).append('\'');

		final int v1 = getIntPref("PollingServerPush.delay.min"),
			v2 = getIntPref("PollingServerPush.delay.max");
		if (v1 > 0  && v2 > 0)
			sb.append(',').append(v1).append(',').append(v2);

		return sb.append(");},'").append(dtid).append("');").toString();
	}
	private int getIntPref(String key) {
		final String s = _config.getPreference(key, null);
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				log.warning("Not a number specified at "+key);
			}
		}
		return -1;
	}
			
	/** Returns the JavaScript codes to disable (aka., stop) the server push.
	 */
	protected String getStopScript() {
		final String stop =
			_config.getPreference("PollingServerPush.stop", null);
		return stop != null ? stop:
			"zkCpsp.stop('" + _desktop.getId() + "');";
	}


	//ServerPush//
	public void start(Desktop desktop) {
		if (_desktop != null)
			throw new IllegalStateException("Already started");

		_desktop = desktop;
		_config = _desktop.getWebApp().getConfiguration();
		Clients.response(new AuScript(null, getStartScript()));
	}
	public void stop() {
		if (_desktop == null)
			throw new IllegalStateException("Not started");

		if (Executions.getCurrent() != null) //Bug 1815480: don't send if timeout
			Clients.response(new AuScript(null, getStopScript()));

		_desktop = null; //to cause DesktopUnavailableException being thrown
		_config = null;

		synchronized (_pending) {
			for (Iterator it = _pending.iterator(); it.hasNext();) {
				final ThreadInfo info = (ThreadInfo)it.next();
				synchronized (info) {
					info.notify();
				}
			}
			_pending.clear();
		}
	}
	/** Sets the delay between each polling request.
	 * <p>Default: use the preference called
	 * <code>PollingServerPush.delay.min</code>
	 * <code>PollingServerPush.delay.max</code>,
	 * and <code>PollingServerPush.delay.factor</code>.
	 * If not defined, min is 1100, max is 10000, and factor is 5.
	 */
	public void setDelay(int min, int max, int factor) {
		Clients.response(
			new AuScript(null, "zkau.setSPushInfo('" + _desktop.getId()
				+ "',{min:" + min + ",max:" + max + ",factor:" + factor + "})"));
	}

	public void onPiggyback() {
		long tmexpired = 0;
		for (int cnt = 0; !_pending.isEmpty();) {
			//Don't hold the client too long.
			//In addition, an ill-written code might activate again
			//before onPiggyback returns. It causes dead-loop in this case.
			if (tmexpired == 0) { //first time
				tmexpired = System.currentTimeMillis()
					+ (_config.getMaxProcessTime() >> 1);
				cnt = _pending.size() + 3;
			} else if (--cnt < 0 || System.currentTimeMillis() > tmexpired) {
				break;
			}

			final ThreadInfo info;
			synchronized (_pending) {
				if (_pending.isEmpty())
					return; //nothing to do
				info = (ThreadInfo)_pending.remove(0);
			}

			synchronized (_mutex) {
				_carryOver = new ExecutionCarryOver(_desktop);

				synchronized (info) {
					if (info.nActive == GIVEUP)
						continue; //give up and try next
					info.nActive = 1; //granted
					info.notify();
				}

				if (_desktop == null) //just in case
					break;

				try {
					_mutex.wait(); //wait until the server push is done
				} catch (InterruptedException ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
	}

	public boolean activate(long timeout)
	throws InterruptedException, DesktopUnavailableException {
		if (D.ON && Events.inEventListener())
			throw new IllegalStateException("No need to activate in the event listener");

		final Thread curr = Thread.currentThread();
		if (_active != null && _active.thread.equals(curr)) { //re-activate
			++_active.nActive;
			return true;
		}

		final ThreadInfo info = new ThreadInfo(curr);
		synchronized (_pending) {
			if (_desktop != null)
				_pending.add(info);
		}

		synchronized (info) {
			if (_desktop != null) {
				info.wait(timeout);

				if (_desktop != null && info.nActive <= 0) { //not granted (timeout)
					info.nActive = GIVEUP; //denote timeout (and give up)
					synchronized (_pending) { //undo pending
						_pending.remove(info);
					}
					return false; //timeout
				}
			}
		}

		if (_desktop == null)
			throw new DesktopUnavailableException("Stopped");

		_carryOver.carryOver();
		_active = info;
		return true;

		//Note: we don't mimic inEventListener since 1) ZK doesn't assume it
		//2) Window depends on it
	}
	public void deactivate() {
		if (_active != null &&
		Thread.currentThread().equals(_active.thread)) {
			if (--_active.nActive <= 0) {
				_carryOver.cleanup();
				_carryOver = null;
				_active.nActive = 0; //just in case
				_active = null;

				//wake up onPiggyback
				synchronized (_mutex) {
					_mutex.notify();
				}

				Threads.sleep(50);
					//to minimize the chance that the server-push thread
					//activate again, before onPiggback polls next _pending
			}
		}
	}

	/** The info of a server-push thread.
	 * It is also a mutex used to start a pending server-push thread.
	 */
	private static class ThreadInfo {
		private final Thread thread;
		/** # of activate() was called. */
		private int nActive;
		private ThreadInfo(Thread thread) {
			this.thread = thread;
		}
		public String toString() {
			return "[" + thread + ',' + nActive + ']';
		}
	}
}

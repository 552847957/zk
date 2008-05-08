/* ServerPushWindow.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Aug  6 12:37:13     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkdemo.test2;

import org.zkoss.lang.Threads;
import org.zkoss.util.logging.Log;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;

/**
 * Used to test the server-push feature.
 *
 * @author tomyeh
 */
public class ServerPush {
	private static final Log log = Log.lookup(ServerPush.class);
	private static boolean _ceased;

	public static void start(Component info) throws InterruptedException {
		final Desktop desktop = Executions.getCurrent().getDesktop();
		if (desktop.isServerPushEnabled()) {
			Messagebox.show("Already started");
		} else {
			_ceased = false;
			desktop.enableServerPush(true);
			new WorkingThread(info).start();
		}
	}
	public static void stop() throws InterruptedException {
		final Desktop desktop = Executions.getCurrent().getDesktop();
		if (desktop.isServerPushEnabled()) {
			Executions.getCurrent().getDesktop().enableServerPush(false);
			_ceased = true;
		} else {
			Messagebox.show("Already stopped");
		}
	}
	public static void updateInfo(Component info, String postfix) {
		Integer i = (Integer)info.getAttribute("count");
		int v = i == null ? 0: i.intValue() + 1;
		if (info.getChildren().size() >= 10) {
			((Component)info.getChildren().get(0)).detach();
			((Component)info.getChildren().get(0)).detach();
		}
		info.setAttribute("count", new Integer(v));
		info.appendChild(new Label(" " + v + " " + postfix));
		info.appendChild(new Separator());
			//create a new component that is easier to detect bugs
	}

	private static class WorkingThread extends Thread {
		private final Desktop _desktop;
		private final Component _info;
		private WorkingThread(Component info) {
			_desktop = info.getDesktop();
			_info = info;
		}
		public void run() {
			try {
				while (!_ceased) {
					Executions.activate(_desktop);
					try {
						updateInfo(_info, "comet");
					} catch (RuntimeException ex) {
						log.error(ex);
						throw ex;
					} catch (Error ex) {
						log.error(ex);
						throw ex;
					} finally {
						Executions.deactivate(_desktop);
					}
					Threads.sleep(2000); //Update every two seconds
				}
				log.info("The server push thread ceased");
			} catch (InterruptedException ex) {
				log.info("The server push thread interrupted", ex);
			}
		}
	}
}

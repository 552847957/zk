/* DesktopCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jul 29 08:47:19     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import java.util.Collection;

import org.zkoss.util.media.Media;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.UiException;

/**
 * An addition interface to {@link org.zkoss.zk.ui.Desktop}
 * for implementation.
 *
 * <p>Note: applications shall never access this interface.
 *
 * @author tomyeh
 */
public interface DesktopCtrl {
	/** Returns the request queue.
	 */
	public RequestQueue getRequestQueue();

	/** Returns the next available key which is unique in the whole desktop.
	 */
	public int getNextKey();
	/** Returns the next available UUID for a component.
	 * The returned UUID is unique in the desktop.
	 * You can consider it as unique in the whole session, though
	 * it may not be true if {@link org.zkoss.zk.ui.ext.RawId} is used
	 * (developer's responsibility to avoid conflict),
	 * or integer overflow (too many UUID in one session, which
	 * can be considered as impossible).
	 */
	public String getNextUuid();

	/** Adds a component to this page.
	 * <p>It is used internally and developers shall not invoke it
	 * explicityly.
	 */
	public void addComponent(Component comp);
	/** Removes a component to this page.
	 * <p>It is used internally and developers shall not invoke it
	 * explicityly.
	 */
	public void removeComponent(Component comp);

	/** Adds a page to this desktop.
	 * It must be called when a page is created.
	 *
	 * <p>This is one of the only few method you could access
	 * before activating an execution.
	 */
	public void addPage(Page page);
	/** Removes a page from this desktop.
	 * <p>NOTE: once a page is removed, you can NOT add it back.
	 * You shall just GC it.
	 */
	public void removePage(Page page);

	/** Sets the execution (used to represent a lock).
	 * Called only internally (by UIEngine's implementation to activate
	 * an execution).
	 */
	public void setExecution(Execution exec);

	/** Sets the bookmark when receiving the onBookmarkChanged command
	 * from the client.
	 */
	public void setBookmarkByClient(String name);

	/** Sets the desktop identifier.
	 *
	 * <p>It is callable only if it is the recovering phase, i.e.,
	 * {@link ExecutionCtrl#isRecovering} is true.
	 * In other words, callable only in the invocation of
	 * {@link FailoverManager#recover}.
	 *
	 * @exception IllegalStateException if it is NOT in recovering.
	 */
	public void setId(String id);
	/** Called when the recoving failed.
	 */
	public void recoverDidFail(Throwable ex);

	/** Returns the sequence ID of the response.
	 * The client and server uses the sequence ID to make sure
	 * the responses are processed in the correct order.
	 *
	 * <p>The range of the sequence IDs is 0~1023.
	 *
	 * @param advance whether to advance the number before returning.
	 * If true, the ID is increased and then returned.
	 * If false, the previous value is returned
	 */
	public int getResponseSequence(boolean advance);
	/** Sets the sequence ID of the response.
	 *
	 * <p>It is rarely called other than in the recovering mode, i.e.,
	 * {@link ExecutionCtrl#isRecovering} is true.
	 *
	 * @param seqId a value between 0 and 1023.
	 * Since ZK 2.4.1, you can reset the sequence by passing a negative
	 * value to this argument.
	 */
	public void setResponseSequence(int seqId);

	/** Notification that the session, which owns this desktop,
	 * is about to be passivated (aka., serialized) by the Web container.
	 */
	public void sessionWillPassivate(Session sess);
	/** Notification that the session, which owns this desktop,
	 * has just been activated (aka., deserialized) by the Web container.
	 */
	public void sessionDidActivate(Session sess);

	/** Called when the desktop is about to be destroyed.
	 */
	public void destroy();

	/** Returns a collection of suspended event processing threads, or empty
	 * if no suspended thread at all.
	 *
	 * <p>An event processing thread is an instance of
	 * {@link EventProcessingThread}
	 *
	 * <p>Note: if you access this method NOT in an event listener for
	 * the SAME desktop, you have to synchronize the iteration
	 * (though the returned collection is synchronized).
	 * Of course, it is always safe to test whether it is empty
	 * ({@link Collection#isEmpty}).
	 *
	 * <pre><code>
//Use the following pathern IF it is not in the SAME desktop's listener
Collection c = otherDesktop.getSuspendedThreads();
if (c.isEmpty()) {
	//do something accordingly
} else {
  synchronized (c) {
    for (Iterator it = c.iterator(); it.hasNext();) {
      //...
    }
  }
}</code></pre>
	 */
	public Collection getSuspendedThreads();
	/** Ceases the specified event thread.
	 *
	 * @param cause an arbitrary text to describe the cause.
	 * It will be the message of the thrown InterruptedException.
	 * @return true if the event processing thread is ceased successfully;
	 * false if no such thread or it is not suspended.
	 */
	public boolean ceaseSuspendedThread(EventProcessingThread evtthd, String cause);

	/** Returns the media that is associated with
	 * {@link org.zkoss.zk.ui.Desktop#getDownloadMediaURI}, or
	 * null if not found.
	 *
	 * <p>This method is used internally. Developers rarely need to
	 * access this method.
	 *
	 * @param remove whether to remove it from cache once returned.
	 * @return the media or null if not found.
	 */
	public Media getDownloadMedia(String medId, boolean remove);

	/** Called when a component added or removed a listener for
	 * {@link org.zkoss.zk.ui.event.Events#ON_PIGGYBACK}.
	 *
	 * <p>The implementation usualy uses it to optimize whether to
	 * call the listener when {@link #onPiggyback} is called.
	 *
	 * @param comp the component that adds an listener for
	 * {@link org.zkoss.zk.ui.event.Events#ON_PIGGYBACK}.
	 * The component may or may not be a root component.
	 * @param listen whether the listener is added (or removed).
	 * @since 3.0.0
	 */
	public void onPiggybackListened(Component comp, boolean listen);
	/** Called each time ZK Update Engine processes all events.
	 * It is used to implement the piggyback feature
	 * (see {@link org.zkoss.zk.ui.event.Events#ON_PIGGYBACK}).
	 *
	 * <p>Used only internally. Application develepers shall not call it.
	 *
	 * @since 3.0.0
	 */
	public void onPiggyback();
	/** Returns the server-push controller, or null if it is not enabled
	 * yet.
	 */
	public ServerPush getServerPush();
	/** Enables the server-push feature with the specified server-push
	 * controller.
	 *
	 * @param serverpush the server-push controller. If null,
	 * the server-push feature is disabled (for this desktop).
	 * Note: this method will invoke {@link ServerPush#start}, so the
	 * caller doesn't need to do it.
	 * @since 3.0.0
	 * @see org.zkoss.zk.ui.Desktop#enableServerPush
	 */
	public boolean enableServerPush(ServerPush serverpush);
}

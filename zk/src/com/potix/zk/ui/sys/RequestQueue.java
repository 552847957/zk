/* RequestQueue.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Apr 24 11:00:33     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package com.potix.zk.ui.sys;

import java.util.Collection;

import com.potix.zk.au.AuRequest;

/**
 * A queue of {@link AuRequest}.
 * There is one queue for each desktop.
 *
 * <p>Implementation Note:
 * Unlike only of desktop members, this class must be thread-safe.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public interface RequestQueue {
	/** Returns whether any request is available in the queue.
	 *
	 * <p>Note: the in-process flag is cleared automatically.
	 * In other wordsd, this method is designed to be called when
	 * an execution is about to de-activate.
	 *
	 * <p>Typical use:<br/>
	 * Call this method after timeout (there might be pending requests).
	 * If it returns true, the caller sends a response to ask client to
	 * send anther request.
	 */
	public boolean hasRequest();
	/** Returns the next request, or null if no more request.
	 * Onced returned, the request is removed from the queue.
	 *
	 * <p>Note: if no more request, this queue is 
	 * automatically marked a flag to denote no one is processing the queue.
	 *
	 * <p>The call shall invoke this method until null is returned, or timeout.
	 * If timeout, it shall invoke {@link #hasRequest} in a way described
	 * in {@link #hasRequest}.
	 */
	public AuRequest nextRequest();

	/** Marks the queue to denote it is being processing.
	 * Then, {@link #addRequests} will return true to tell the caller
	 * is free to leave without process the pendinig requests.
	 */
	public void setInProcess();

	/** Adds a list of requests.
	 * Usually called by execs, that are not activated yeh, to pass
	 * request to the activated one.
	 *
	 * <p>Note: caller might check the returned value. If false is returned,
	 * it means the no one is processing queue, and the caller is responsible
	 * to process all pending requests. Before processing, it shall call
	 * {@link #setInProcess} to notify following executions free to leave.
	 * On the other hand, if true is returned, it means the queue is
	 * being processed by some one else, and the caller is free to leave.
	 *
	 * @return whether the queue is being processed by another execution.
	 */
	public boolean addRequests(Collection requests);
}

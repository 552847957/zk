/* PagingEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 17 16:18:13     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zul.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import org.zkoss.zul.ext.Pageable;
import org.zkoss.zul.ext.Paginal;

/**
 * Used to notify that a new page is selected by the user, or by
 * {@link Paginal} (such as {@link org.zkoss.zul.Paging}).
 * It is used for paging long content.
 *
 * @author tomyeh
 */
public class PagingEvent extends Event {
	private final Pageable _pgi;
	private final int _actpg;

	/** Construct a paging event.
	 *
	 * @param target the target must be a paginal component, i.e.,
	 * implements {@link Pageable}.
	 * @param actpg the active page
	 */
	public PagingEvent(String name, Component target, int actpg) {
		super(name, target);
		_pgi = (Pageable)target;
		_actpg = actpg;
	}
	/** Construct a paging event that the target is different
	 * from the page controller.
	 *
	 * @param target the event target
	 * @param pageable the paging controller. In other words,
	 * it is usually {@link Paginal}.
	 */
	public PagingEvent(String name, Component target, Pageable pageable,
	int actpg) {
		super(name, target);
		_pgi = pageable;
		_actpg = actpg;
	}
	/** Construct a paging event that the target is different
	 * from the page controller.
	 *
	 * <p>Deprecated since 2.4.1. Use {@link #PagingEvent(String,Component,Paginal,int)}
	 * instead.
	 *
	 * @param target the event target
	 * @param paginal the paging controller.
	 * @deprecated
	 */
	public PagingEvent(String name, Component target, Paginal paginal,
	int actpg) {
		super(name, target);
		_pgi = paginal;
		_actpg = actpg;
	}

	/** Returns the pageable controller.
	 * @since 2.4.1
	 */
	public Pageable getPageable() {
		return _pgi;
	}
	/** Returns the paginal controller.
	 *
	 * <p>Deprecated since 2.4.1. Use {@link #getPageable} instead.
	 *
	 * @deprecated
	 */
	public Paginal getPaginal() {
		return _pgi instanceof Paginal ? (Paginal)_pgi: null;
	}

	/** Returns the active page (starting from 0).
	 * <p>It is the same as {@link #getPageable}'s {@link Pageable#getActivePage}.
	 *
	 * <p>To get the index of the first visible item, use<br/>
	 * <code>{@link #getActivePage} * {@link Pageable#getPageSize}</code>.
	 */
	public int getActivePage() {
		return _actpg;
	}
}

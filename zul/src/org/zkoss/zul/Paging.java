/* Paging.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 17 15:26:06     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zul;

import org.zkoss.mesg.Messages;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.mesg.MZul;
import org.zkoss.zul.event.ZulEvents;
import org.zkoss.zul.event.PagingEvent;
import org.zkoss.zul.impl.XulElement;
import org.zkoss.zul.ext.Paginal;
import org.zkoss.zul.au.impl.PagingCommand;

/**
 * Paging of long content.
 *
 * <p>Default {@link #getSclass}: paging.
 *
 * @author tomyeh
 */
public class Paging extends XulElement implements Paginal {
	static {
		//register commands
		new PagingCommand("onPaging", Command.SKIP_IF_EVER_ERROR);
	}

	/** # of items per page. */
	private int _pgsz = 20;
	/** total # of items. */
	private int _ttsz = 0;
	/** # of pages. */
	private int _npg = 1;
	/** the active page. */
	private int _actpg = 0;
	/** # of page anchors are visible */
	private int _pginc = 10;
	/** Whether to hide automatically if only one page is available. */
	private boolean _autohide;
	/** Whether to show detailed info. */
	private boolean _detailed;

	public Paging() {
		setSclass("paging");
	}
	/** Contructor.
	 *
	 * @param totalsz the total # of items
	 * @param pagesz the # of items per page
	 */
	public Paging(int totalsz, int pagesz) {
		this();
		setTotalSize(totalsz);
		setPageSize(pagesz);
	}

	//Paginal//
	public int getPageSize() {
		return _pgsz;
	}
	public void setPageSize(int size) throws WrongValueException {
		if (size <= 0)
			throw new WrongValueException("positive only");

		if (_pgsz != size) {
			_pgsz = size;
			updatePageNum();
			Events.postEvent(new PagingEvent(ZulEvents.ON_PAGING, this, _actpg));
		}
	}
	public int getTotalSize() {
		return _ttsz;
	}
	public void setTotalSize(int size) throws WrongValueException {
		if (size < 0)
			throw new WrongValueException("non-negative only");

		if (_ttsz != size) {
			_ttsz = size;
			updatePageNum();
			if (_detailed) invalidate();
		}
	}
	private void updatePageNum() {
		int v = (_ttsz - 1) / _pgsz + 1;
		if (v == 0) v = 1;
		if (v != _npg) {
			_npg = v;
			if (_actpg >= _npg)
				_actpg = _npg - 1;

			invalidate();
		}
	}

	public int getPageCount() {
		return _npg;
	}
	public int getActivePage() {
		return _actpg;
	}
	public void setActivePage(int pg) throws WrongValueException {
		if (pg >= _npg || pg < 0)
			throw new WrongValueException("Unable to set active page to "+pg+" since only "+_npg+" pages");
		if (_actpg != pg) {
			_actpg = pg;
			invalidate();
			Events.postEvent(new PagingEvent(ZulEvents.ON_PAGING, this, _actpg));
		}
	}

	public int getPageIncrement() {
		return _pginc;
	}
	public void setPageIncrement(int pginc) throws WrongValueException {
		if (pginc <= 0)
			throw new WrongValueException("Nonpositive is not allowed: "+pginc);
		if (_pginc != pginc) {
			_pginc = pginc;
			invalidate();
		}
	}

	public boolean isDetailed() {
		return _detailed;
	}
	public void setDetailed(boolean detailed) {
		if (_detailed != detailed) {
			_detailed = detailed;
			invalidate();
		}
	}

	//extra//
	/** Returns whether to automatically hide this component if
	 * there is only one page available.
	 * <p>Default: false.
	 */
	public boolean isAutohide() {
		return _autohide;
	}
	/** Sets whether to automatically hide this component if
	 * there is only one page available.
	 */
	public void setAutohide(boolean autohide) {
		if (_autohide != autohide) {
			_autohide = autohide;
			if (_npg == 1) invalidate();
		}
	}

	/** Returns the inner HTML tags of this component.
	 * <p>Used only for component development. Not accessible by
	 * application developers.
	 */
	public String getInnerTags() {
		final StringBuffer sb = new StringBuffer(512);

		int half = _pginc / 2;
		int begin, end = _actpg + half - 1;
		if (end >= _npg) {
			end = _npg - 1;
			begin = end - _pginc + 1;
			if (begin < 0) begin = 0;
		} else {
			begin = _actpg - half;
			if (begin < 0) begin = 0;
			end = begin + _pginc - 1;
			if (end >= _npg) end = _npg - 1;
		}

		if (_actpg > 0) {
			if (begin > 0) //show first
				appendAnchor(sb, Messages.get(MZul.FIRST), 0);
			appendAnchor(sb, Messages.get(MZul.PREV), _actpg - 1);
		}

		boolean bNext = _actpg < _npg - 1;
		for (; begin <= end; ++begin) {
			if (begin == _actpg) {
				sb.append(begin + 1).append("&nbsp;");
			} else {
				appendAnchor(sb, Integer.toString(begin + 1), begin);
			}
		}

		if (bNext) {
			appendAnchor(sb, Messages.get(MZul.NEXT), _actpg + 1);
			if (end < _npg - 1) //show last
				appendAnchor(sb, Messages.get(MZul.LAST), _npg - 1);
		}
		if (_detailed)
			sb.append("<span>[").append(_actpg * _pgsz + 1).append('/')
				.append(_ttsz).append("]</span>");
		return sb.toString();
	}
	private static final
	void appendAnchor(StringBuffer sb, String label, int val) {
		sb.append("<a href=\"javascript:;\" onclick=\"zkPg.go(this,")
			.append(val).append(")\">").append(label).append("</a>&nbsp;");
	}

	//-- Component --//
	public boolean isVisible() {
		return super.isVisible() && (_npg > 1 || !_autohide);
	}
	public boolean isChildable() {
		return false;
	}
}

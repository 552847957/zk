/* Text.java

{{IS_NOTE

$Header: //time/potix/rd/cvs/zk1/pxcommon/src/com/potix/idom/Text.java,v 1.5 2006/02/27 03:41:55 tomyeh Exp $
Purpose: 
Description: 
History:
C2001/10/22 20:48:21, reate, Tom M. Yeh
}}IS_NOTE

Copyright (C) 2001 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.idom;

import com.potix.idom.impl.*;

/**
 * The iDOM Text.
 *
 * @author <a href="mailto:tomyeh@potix.com">Tom M. Yeh</a>
 * @version $Revision: 1.5 $ $Date: 2006/02/27 03:41:55 $
 * @see CData
 */
public class Text extends AbstractTextual implements org.w3c.dom.Text {
	/** Constructor.
	 */
	public Text(String text) {
		super(text);
	}
	/** Constructor.
	 */
	public Text() {
	}

	//-- AbstractTextual --//
	/**
	 * Always returns true to denote it allows to be coalesced
	 * with its siblings with the same type (class).
	 */
	public final boolean isCoalesceable() {
		return true;
	}

	//-- Item --//
	public final String getName() {
		return "#text";
	}

	//-- Node --//
	public final short getNodeType() {
		return TEXT_NODE;
	}
}

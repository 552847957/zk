/* FacadeNodeList.java

{{IS_NOTE

	$Header: //time/potix/rd/cvs/m3/pxcommon/src/com/potix/xml/FacadeNodeList.java,v 1.2 2006/02/27 03:42:08 tomyeh Exp $
	Purpose: 
	Description: 
	History:
	2001/10/02 16:33:29, Create, Tom M. Yeh.
}}IS_NOTE

Copyright (C) 2001 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.xml;

import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The NodeList which is a facade of another java List.
 *
 * @author <a href="mailto:tomyeh@potix.com">Tom M. Yeh</a>
 * @version $Revision: 1.2 $ $Date: 2006/02/27 03:42:08 $
 * @see FacadeList
 */
public class FacadeNodeList implements NodeList {
	/** The java List to facade. */
	protected List _list;

	/** Constructor.
	 *
	 * @param list the list to facade; never null
	 */
	public FacadeNodeList(List list) {
		_list = list;
	}

	//-- NodeList --//
	public final int getLength() {
		return _list.size();
	}
	public final Node item(int j) {
		return j>=0 && j<_list.size() ? (Node)_list.get(j): null;
	}
}

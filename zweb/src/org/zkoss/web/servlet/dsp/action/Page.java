/* Page.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Sep  6 09:35:48     2005, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.web.servlet.dsp.action;

import java.io.IOException;

import org.zkoss.web.mesg.MWeb;
import org.zkoss.web.servlet.ServletException;

/**
 * The page action used to set the page info, such as the content type.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class Page extends AbstractAction {
	private String _ctype;

	/** Returns the content type. */
	public String getContentType() {
		return _ctype;
	}
	/** Sets the content type. */
	public void setContentType(String ctype) {
		_ctype = ctype;
	}

	//-- Action --//
	public void render(ActionContext ac, boolean nested)
	throws javax.servlet.ServletException, IOException {
		if (!isEffective())
			return;
		if (nested)
			throw new ServletException(MWeb.DSP_NESTED_ACTION_NOT_ALLOWED,
				new Object[] {this, new Integer(ac.getLineNumber())});
		if (_ctype != null)
			ac.setContentType(_ctype);
	}

	//-- Object --//
	public String toString() {
		return "page";
	}
}

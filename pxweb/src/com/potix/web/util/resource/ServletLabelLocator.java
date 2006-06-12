/* ServletLabelLocator.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sat Apr  8 19:51:08     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.web.util.resource;

import java.util.Locale;
import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletContext;

import com.potix.util.resource.LabelLocator;

/**
 * Used by Lables to load labels from a servlet context.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class ServletLabelLocator implements LabelLocator {
	private final ServletContext _ctx;
	public ServletLabelLocator(ServletContext ctx) {
		if (ctx == null)
			throw new IllegalArgumentException("null");
		_ctx = ctx;
	}

	//-- LabelLocator --//
	public URL locate(Locale locale) throws IOException {
		return _ctx.getResource("/WEB-INF/"+getI3LabelName(locale));
	}
	/** Returns the filename of i3-label.properties. */
	private static final String getI3LabelName(Locale locale) {
		return locale.equals(Locale.ENGLISH) ?
			"i3-label.properties": "i3-label_" + locale + ".properties";
	}

	//-- Object --//
	public int hashCode() {
		return _ctx.hashCode();
	}
	public boolean equals(Object o) {
		return o instanceof ServletLabelLocator
			&& ((ServletLabelLocator)o)._ctx.equals(_ctx);
	}
}

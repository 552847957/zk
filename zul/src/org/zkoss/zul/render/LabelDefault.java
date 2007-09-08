/* LabelDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Sep  5 11:08:53     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.render;

import java.io.Writer;
import java.io.IOException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Label;

/**
 * {@link Label}'s default mold.
 *
 * @author tomyeh
 * @since 3.0.0
 */
public class LabelDefault implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Label self = (Label)comp;
		final boolean idRequired = self.isIdRequired();
		if (idRequired) {
			wh.write("<span id=\"").write(self.getUuid()).write("\"");
			wh.write(self.getOuterAttrs()).write(self.getInnerAttrs());
			wh.write(">");
		}

		wh.writeln(self.getEncodedText());

		if (idRequired)
			wh.writeln("</span>");
	}
}

/* MenuseparatorDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Sep 6 2007, Created by Jeff.Liu
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.render;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Menuseparator;

/**
 * {@link Menuseparator}'s default mold.
 * @author Jeff Liu
 * @since 3.0.0
 */
public class MenuseparatorDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Menuseparator self = (Menuseparator)comp;
		final String uuid = self.getUuid();
		wh.write("<tr id=\"").write(uuid).write("\" z.type=\"Menusp\"");
		wh.write(self.getOuterAttrs()).write(self.getInnerAttrs()).writeln(">");
		wh.writeln("<td colspan=\"3\"><hr/></td>");
		wh.writeln("</tr>");
	}
}

/* Hbox.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 5, 2007 2:15:45 PM , Created by robbiecheng
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
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Box;
/**
 * {@link Box}'s horizontal mold.
 * @author robbiecheng
 * @since 3.0.0
 */

public class BoxHorizontal implements ComponentRenderer{

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Box self = (Box) comp;
		final String uuid = self.getUuid();		

		wh.write("<table id=\"").write(uuid).write("\" z.type=\"zul.box.Box\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs())
			.writeln(" cellpadding=\"0\" cellspacing=\"0\">")
			.write("<tr valign=\"").write(self.getValign())
			.write("\" id=\"").write(uuid).write("!cave\">");		

		for (Iterator it = self.getChildren().iterator(); it.hasNext();) {
			final Component child = (Component)it.next();
			wh.write("<td id=\"").write(child.getUuid()).write("!chdextr\"")
				.write(self.getChildOuterAttrs(child))
				.write(self.getChildInnerAttrs(child)).write(">");

			child.redraw(out);

			wh.writeln("</td>");
		}		
		wh.write("</tr></table>");
	}
}

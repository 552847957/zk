/* BoxVertical.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 6, 2007 11:51:57 AM , Created by robbiecheng
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkmax.zul.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Box;

/**
 * {@link Box}'s vertical mold.
 * @author robbiecheng
 * @since 3.0.0
 */
public class BoxVertical implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Box self = (Box) comp;
		final String uuid = self.getUuid();
		final String spacing = self.getSpacing();
		String spscls = null, spstyle = null;

		wh.write("<table id=\"").write(uuid).write("\" z.type=\"zul.box.Box\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs())
			.writeln(" cellpadding=\"0\" cellspacing=\"0\">");
		for (Iterator it = self.getChildren().iterator(); it.hasNext();) {
			final Component child = (Component)it.next();
			wh.write("<tr id=\"").write(child.getUuid()).write("!chdextr\"")
				.write(self.getChildOuterAttrs(child)).write(">\n<td")	
				.write(self.getChildInnerAttrs(child))
				.write(">").write(child).writeln("</td></tr>");

			if (child.getNextSibling() != null) {
				if (spscls == null) {
					spscls = self.getSclass();
					spscls = spscls == null || spscls.length() == 0 ? "vbox-sp": spscls + "-sp";
					if (spacing != null)
						spstyle = "height:" + spacing;
				}

				wh.write("<tr id=\"").write(child.getUuid())
					.write("!chdextr2\" class=\"").write(spscls).write("\"");

				//note: we have to hide if spacing is 0 since IE7 shows some space
				if (!child.isVisible() || "0".equals(spacing) || "0px".equals(spacing)) {
					wh.write(" style=\"display:none;");
					if (spstyle != null) wh.write(spstyle);
					wh.write("\"");
				} else if (spstyle != null) {
					wh.write(" style=\"").write(spstyle).write("\"");
				}

				wh.write("><td>");
				if (Executions.getCurrent().isExplorer())
					wh.write("<img style=\"width:0;height:0\"/>");
					//Bug 1899003: we must have something to show border (IE)
				wh.writeln("</td></tr>");
			}
		}		
		wh.write("</table>");
	}
}

/* ListboxDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Sep 6 2007, Created by Jeff.Liu
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkmax.zul.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.fn.ZulFns;

/**
 * {@link Listbox}'s default mold.
 * @author Jeff Liu
 * @since 3.0.0
 */
public class ListboxDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Listbox self = (Listbox)comp;
		final String uuid = self.getUuid();
		
		wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.sel.Libox\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs()).write(">");

		if(self.getListhead() != null){
			wh.write("<div id=\"").write(uuid).write("!head\" class=\"listbox-head\">")
				.write("<table width=\"").write(self.getInnerWidth()).write("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout:fixed\">");
			if(self.getListhead() != null) {
				wh.write("<tbody style=\"visibility:hidden;height:0px\">")
					.write("<tr id=\"").write(self.getListhead().getUuid()).write("!hdfaker\" class=\"listbox-fake\">");
					
				for (Iterator it = self.getListhead().getChildren().iterator(); it.hasNext();) {
					final Listheader child = (Listheader) it.next();
					wh.write("<th id=\"").write(child.getUuid()).write("!hdfaker\"").write(child.getOuterAttrs())
					.write("><div style=\"overflow:hidden\"></div></th>");
				}
				wh.write("</tr></tbody>");
			}
			wh.writeComponents(self.getHeads())
				.write("</table></div>");
		}
		wh.write("<div id=\"").write(uuid).write("!body\" class=\"listbox-body\"");
		if (self.getHeight() != null) wh.write(" style=\"overflow:hidden;height:").write(self.getHeight()).write("\"");
		else if (self.getRows() > 1) wh.write(" style=\"overflow:hidden;height:").write(self.getRows() * 15).write("px\"");
		
		wh.write("><table width=\"").write(self.getInnerWidth()).write("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"")
			.write(uuid).writeln("!cave\" class=\"listbox-btable\"");
		if (self.isFixedLayout())
			wh.write(" style=\"table-layout:fixed\"");
		wh.write(">");
		
		if(self.getListhead() != null) {
			wh.write("<tbody style=\"visibility:hidden;height:0px\">")
				.write("<tr id=\"").write(self.getListhead().getUuid()).write("!bdfaker\" class=\"listbox-fake\">");
				
			for (Iterator it = self.getListhead().getChildren().iterator(); it.hasNext();) {
				final Listheader child = (Listheader) it.next();
				wh.write("<th id=\"").write(child.getUuid()).write("!bdfaker\"").write(child.getOuterAttrs())
				.write("><div style=\"overflow:hidden\"></div></th>");
			}
			wh.write("</tr></tbody>");
		}
		ZulFns.resetStripeClass(self);
		for (Iterator it = self.getItems().iterator(); it.hasNext();) {
			final Component child = (Component) it.next();
			ZulFns.setStripeClass(child);
			child.redraw(out);
		}
		wh.write("\n</table></div>");

		if(self.getListfoot() != null){
			wh.write("<div id=\"").write(uuid).write("!foot\" class=\"listbox-foot\">")
				.write("<table width=\"").write(self.getInnerWidth()).write("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout:fixed\">");
			if(self.getListhead() != null) {
				wh.write("<tbody style=\"visibility:hidden;height:0px\">")
					.write("<tr id=\"").write(self.getListhead().getUuid()).write("!ftfaker\" class=\"listbox-fake\">");
					
				for (Iterator it = self.getListhead().getChildren().iterator(); it.hasNext();) {
					final Listheader child = (Listheader) it.next();
					wh.write("<th id=\"").write(child.getUuid()).write("!ftfaker\"").write(child.getOuterAttrs())
					.write("><div style=\"overflow:hidden\"></div></th>");
				}
				wh.write("</tr></tbody>");
			}
			wh.writeln(self.getListfoot())
				.write("</table></div>");
		}

		wh.write("</div>");
	}
}

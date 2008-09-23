/* PanelDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 26, 2008 11:27:53 AM , Created by jumperchen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zkmax.zul.render;

import java.io.IOException;
import java.io.Writer;

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.Out;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Panel;

/**
 * {@link Panel}'s default mold.
 * @author jumperchen
 * @since 3.5.0
 */
public class PanelDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Panel self = (Panel)comp;
		final String uuid = self.getUuid();
		final String mcls = self.getMoldSclass();
		final String title = self.getTitle();
		final String border = self.getBorder();
		final boolean hasBorder = "normal".equals(border);
		final boolean noTitle = Strings.isBlank(title);
		final Caption caption = self.getCaption();
		
		wh.write("<div id=\"").write(self.getUuid()).write("\" z.type=\"zul.panel.Panel\" z.autoz=\"true\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs()).write(">");
		if (self.isFramable()) {
			wh.write("<div class=\"").write(mcls).write("-tl");
			if (caption == null && noTitle)
				wh.write(' ').write(mcls).write("-header-noheader");
			wh.write("\"><div class=\"").write(mcls).write("-tr\"><div class=\"").write(mcls)
				.write("-tm\">");
		}
		if (caption != null || !noTitle) {
			wh.write("<div id=\"").write(uuid).write("!caption\" class=\"").write(mcls)
				.write("-header");
			if (!self.isFramable() && !hasBorder)
				wh.write(' ').write(mcls).write("-header-noborder");
			wh.write("\">");
			if (caption == null) {
				if (self.isClosable())
					wh.write("<div id=\"").write(uuid).write("!close\" class=\"")
						.write(mcls).write("-tool ").write(mcls).write("-close\"></div>");
				if (self.isMaximizable()) {
					wh.write("<div id=\"").write(uuid).write("!maximize\" class=\"")
						.write(mcls).write("-tool ").write(mcls).write("-maximize");
					if (self.isMaximized())
							wh.write(" ").write(mcls).write("-maximized");
					wh.write("\"></div>");
				}
				if (self.isMinimizable())
					wh.write("<div id=\"").write(uuid).write("!minimize\" class=\"")
						.write(mcls).write("-tool ").write(mcls).write("-minimize\"></div>");
				if (self.isCollapsible())
					wh.write("<div id=\"").write(uuid).write("!toggle\" class=\"")
						.write(mcls).write("-tool ").write(mcls).write("-toggle\"></div>");
				new Out(title).render(out);				
			} else wh.write(caption);
			wh.write("</div>");
		}
		if (self.isFramable()) wh.write("</div></div></div>");
		
		wh.write("<div id=\"").write(uuid).write("!bwrap\" class=\"").write(mcls).write("-body\"");
		if (!self.isOpen()) wh.write("style=\"display:none;\"");
		wh.write('>');
		
		if (self.isFramable())
			wh.write("<div class=\"").write(mcls).write("-cl\"><div class=\"").write(mcls)
				.write("-cr\"><div class=\"").write(mcls).write("-cm\">");
		
		if (self.getTopToolbar() != null) {
			wh.write("<div id=\"").write(uuid).write("!tbar\" class=\"").write(mcls).write("-tbar");
			if (!hasBorder)
				wh.write(' ').write(mcls).write("-tbar-noborder");
			if (self.isFramable() && caption == null && noTitle)
				wh.write(' ').write(mcls).write("-noheader");
			wh.write("\">").write(self.getTopToolbar()).write("</div>");
		}
		wh.write(self.getPanelchildren());

		if (self.getBottomToolbar() != null) {
			wh.write("<div id=\"").write(uuid).write("!bbar\" class=\"").write(mcls).write("-bbar");
			if (!hasBorder)
				wh.write(' ').write(mcls).write("-bbar-noborder");
			if (self.isFramable() && caption == null && noTitle)
				wh.write(' ').write(mcls).write("-noheader");
			wh.write("\">").write(self.getBottomToolbar()).write("</div>");
		}
		if (self.isFramable()) {
			wh.write("</div></div></div><div class=\"").write(mcls).write("-bl");
			if (self.getFootToolbar() == null)
				wh.write(' ').write(mcls).write("-nofbar");
			wh.write("\"><div class=\"").write(mcls).write("-br\"><div class=\"").write(mcls).write("-bm\">");
		}
		if (self.getFootToolbar() != null) {
			wh.write("<div id=\"").write(uuid).write("!fbar\" class=\"").write(mcls)
				.write("-fbar\">").write(self.getFootToolbar()).write("</div>");
		}
		if (self.isFramable()) wh.write("</div></div></div>");
		wh.write("</div></div>");
	}
}

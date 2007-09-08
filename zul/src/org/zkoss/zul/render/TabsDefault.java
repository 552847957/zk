/* TabsDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 6, 2007 6:21:35 PM , Created by robbiecheng
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
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabs;

/**
 * {@link Tabs}'s default mold.
 * It forwards to {@link TabsDefaultV} if the orient is vertical.
 * 
 * @author robbiecheng
 * 
 * @since 3.0.0
 */
public class TabsDefault implements ComponentRenderer {
	private final TabsDefaultV _vtabs = new TabsDefaultV();

	public void render(Component comp, Writer out) throws IOException {
		final Tabs self = (Tabs)comp;
		final Tabbox tabbox = self.getTabbox();

		if(tabbox.getOrient().equals("vertical")) {
			_vtabs.render(comp, out);
			return;
		}

		final SmartWriter wh = new SmartWriter(out);		
		final String tscls = tabbox.getTabSclass() + '-';

		wh.write("<thead id=\"").write(self.getUuid()).write("\" z.type=\"zul.tab.Tabs\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs()).writeln('>')
			.writeln("<tr><td><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
			.write("<tr valign=\"bottom\">");
		
		/* prefix column */
		wh.writeln("<td><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
		wh.write("<tr><td class=\"").write(tscls).writeln("first\"></td></tr></table></td>");				

		wh.writeChildren(self);
		
		wh.write("<td style=\"display:none\" id=\"")
			.write(self.getUuid()).writeln("!child\"></td>"); //bookmark for adding children
		
		/* postfix column */		
		wh.writeln("<td><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
			.write("<tr><td class=\"").write(tscls).write("last1\" id=\"")
			.write(self.getUuid()).writeln("!last\"></td>")
			.write("<td class=\"").write(tscls).writeln("last2\"></td>")
			.writeln("</tr></table></td></tr></table></td></tr></thead>");
	}
}

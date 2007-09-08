/* MenuDefault.java

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
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zk.ui.render.Out;

import org.zkoss.zul.Menu;
import org.zkoss.zul.Menupopup;

/**
 * {@link Menu}'s default mold.
 * @author Jeff Liu
 * @since 3.0.0
 */
public class MenuDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Menu self = (Menu)comp;
		final String uuid = self.getUuid();
		final Execution exec = Executions.getCurrent();
		
		if(self.isTopmost()){
			wh.write("<td id=\"").write(uuid).write("\" align=\"left\" z.type=\"zul.menu.Menu\"");
			wh.write(self.getOuterAttrs()).write(self.getInnerAttrs()).write(">");
			wh.write("<a href=\"javascript:;\" id=\"").write(uuid).write("!a\">").write(self.getImgTag());
			new Out(self.getLabel()).render(out);
			wh.write("</a>")
				.write(self.getMenupopup())
				.writeln("</td>");
		}else{
			wh.write("<tr id=\"").write(uuid).write("\" z.type=\"zul.menu.Menu\"");
			wh.write(self.getOuterAttrs()).write(self.getInnerAttrs())
				.write(">\n<td><img src=\"").write(exec.encodeURL("~./img/spacer.gif"))
				.write("\" width=\"11\"/></td>\n<td align=\"left\"><a href=\"javascript:;\" id=\"")
				.write(uuid).write("!a\">").write(self.getImgTag());

			new Out(self.getLabel()).render(out);

			wh.write("</a>")
				.write(self.getMenupopup())
				.write("</td>\n<td><img src=\"").write(exec.encodeURL("~./zul/img/menu/arrow.gif"))
				.writeln("\" width=\"9\"/></td></tr>");
		}

	}
}

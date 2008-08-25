/* Tabbox2DefaultV.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 22, 2008 6:03:53 PM , Created by RyanWu
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
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabs;

/**
 * {@link Tabbox}'s default mold.
 * 
 * @author RyanWu
 * 
 * @since 3.5.0
 */
public class Tabbox2DefaultV implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Tabbox self = (Tabbox) comp;
		final Tabs tabs = self.getTabs();		
		wh.write("<div id=\"").write(self.getUuid()).write('"')
			.write(self.getOuterAttrs()).write(self.getInnerAttrs());
			if (!Strings.isBlank(self.getHeight())){
				wh.write(" style=\"height:"+self.getHeight()+";\"");
			}
			wh.writeln(" z.type=\"zul.tab2.Tabbox2\">");
			wh.writeln(tabs);
			wh.writeln(self.getTabpanels());
			wh.writeln("<div class=\"z-clear\" ></div>");
		wh.writeln("</div>");		
	}
}

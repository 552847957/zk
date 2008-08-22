/* TabboxDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 6, 2007 7:08:15 PM , Created by robbiecheng
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabs;

/**
 * {@link Tabbox}'s default mold.
 * It forwards the vertical orient to {@link TabboxDefaultV}.
 * 
 * @author robbiecheng
 * 
 * @since 3.0.0
 */
public class TabboxDefault implements ComponentRenderer {
	private final TabboxDefaultV _vtabbox = new TabboxDefaultV();

	public void render(Component comp, Writer out) throws IOException {
		final Tabbox self = (Tabbox) comp;
		if("vertical".equals(self.getOrient())){
			_vtabbox.render(comp, out);
			return;
		}

		final SmartWriter wh = new SmartWriter(out);
		final Tabs tabs = self.getTabs();

			wh.write("<div id=\"").write(self.getUuid()).write("\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs())
			.writeln(" z.type=\"zul.tab.Tabbox\">")
			.writeln(tabs)
			.writeln(self.getTabpanels())
			.write("</div>");
		
	}
}

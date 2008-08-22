/* TabboxAccordion.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 6, 2007 8:04:12 PM , Created by robbiecheng
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

/**
 * {@link Tabbox}'s accordion mold.
 * 
 * @author robbiecheng
 * 
 * @since 3.0.0
 */

public class Tabbox2Accordion implements ComponentRenderer {
	
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Tabbox self = (Tabbox) comp;

		wh.write("<div id=\"").write(self.getUuid()).write("\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs())
			.writeln(" z.accd=\"true\" z.type=\"zul.tab2.Tabbox2\">")
			.writeln(self.getTabpanels())
			.write("</div>");
		
	}
}

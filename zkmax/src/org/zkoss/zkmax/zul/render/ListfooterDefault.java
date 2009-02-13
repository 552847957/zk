/* ListfooterDefault.java

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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zk.ui.render.Out;

import org.zkoss.zul.Listfooter;

/**
 * {@link Listfooter}'s default mold.
 * @author Jeff Liu
 * @since 3.0.0
 */
public class ListfooterDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Listfooter self = (Listfooter)comp;
		wh.write("<td id=\"").write(self.getUuid()).write("\" ")
		.write(self.getOuterAttrs()).write(self.getInnerAttrs())
		.write(" z.type=\"zul.zul.Ftr\"><div id=\"").write(self.getUuid()).write("!cave\" class=\"foot-cell-inner\">");
		wh.write(self.getImgTag());
		new Out(self.getLabel()).render(out);
		wh.writeChildren(self);
		wh.writeln("</div></td>");
	}
}

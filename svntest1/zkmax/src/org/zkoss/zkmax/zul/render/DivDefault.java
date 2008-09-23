/* DivDefault.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Sep 6, 2007 5:50:17 PM , Created by jumperchen
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
import org.zkoss.zul.Div;

/*
 * {@link Div}'s default mold.
 * 
 * @author jumperchen
 * 
 * @since 3.0.0
 */
public class DivDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Div self = (Div) comp;
		final String uuid = self.getUuid();
		wh.write("<div id=\"").write(uuid).write('"');
		wh.write(self.getOuterAttrs()).write(self.getInnerAttrs()).write(">");
		wh.writeChildren(self);
		wh.write("</div>");
	}

}

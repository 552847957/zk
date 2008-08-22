/* ComboitemDefault.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Sep 6, 2007 5:10:32 PM , Created by jumperchen
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

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zk.ui.render.Out;

import org.zkoss.zul.Comboitem;

/*
 * {@link  Comboitem}'s default mold.
 * 
 * @author jumperchen
 * 
 * @since 3.0.0
 */
public class ComboitemDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Comboitem self = (Comboitem) comp;
		final String uuid = self.getUuid();
		final String mcls = self.getMoldSclass();
		wh.write("<tr id=\"").write(uuid).write("\" z.type=\"Cmit\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs()).writeln(">")
			.write("<td class=\"").write(mcls).write("-img\">").write(self.getImgTag())
			.write("</td>\n<td class=\"").write(mcls).write("-text\">");

		new Out(self.getLabel()).render(out);

		String s = self.getDescription();
		if (!Strings.isBlank(s)) {
			wh.write("<br/>\n<span class=\"").write(mcls).write("-inner\">");
			new Out(s).render(out);
			wh.write("</span>");
		}

		s = self.getContent();
		if (!Strings.isBlank(s))
			wh.write("<span class=\"").write(mcls).write("-content\">").write(s).write("</span>");
			//1. don't use Out to encode since content might contain HTML tags
			//2. Feature 1908524: no <br/>

		wh.writeln("</td></tr>");
	}
}

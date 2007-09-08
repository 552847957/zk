/* CaptionDefault.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Wed Sep  5 13:18:53     2007, Created by Dennis.Chen
 }}IS_NOTE

 Copyright (C) 2007 Potix Corporation. All Rights Reserved.

 {{IS_RIGHT
 This program is distributed under GPL Version 2.0 in the hope that
 it will be useful, but WITHOUT ANY WARRANTY.
 }}IS_RIGHT
 */
package org.zkoss.zul.render;

import java.io.Writer;
import java.io.IOException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zk.ui.render.Out;

import org.zkoss.zul.Caption;

/**
 * {@link Caption}'s default mold.
 * 
 * @author dennis.chen
 * @since 3.0.0
 */
public class CaptionDefault implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Caption self = (Caption) comp;
		final String uuid = self.getUuid();
		final Execution exec = Executions.getCurrent();
		final String imgTag = self.getImgTag();

		if (self.isLegend()) {
			wh.write("<legend>").write(imgTag);
			new Out(self.getLabel()).render(out);
			wh.writeChildren(self);
			wh.writeln("</legend>");
		} else {
			wh.write("<table id=\"").write(uuid).write("\" ");
			wh.write("z.type=\"zul.widget.Capt\"").write(self.getOuterAttrs())
					.write(self.getInnerAttrs());
			wh.writeln(" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
			wh.writeln("<tr valign=\"middle\">");
			wh.write("<td align=\"left\" class=\"caption\">").write(imgTag);
			new Out(self.getCompoundLabel()).setNbsp(true).render(out);
			wh.writeln("</td>");

			wh.write("<td align=\"right\" class=\"caption\" id=\"").write(uuid).write("!cave\">")
				.writeChildren(self)
				.writeln("</td>");

			if (self.isClosableVisible()) {
				wh.write("<td width=\"16\"><img id=\"")
					.write(self.getParent().getUuid())
					.write("!close\" src=\"")
					.write(exec.encodeURL("~./zul/img/close-off.gif"))
					.writeln("\"/></td>");
			}

			wh.write("</tr></table>");
		}
	}
}

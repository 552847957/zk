/* Button2Default.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Aug 6, 2008 2:51:53 PM , Created by robbiecheng
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.Out;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zk.fn.ZkFns;
import org.zkoss.zul.Button;

/**
 * 
 * {@link Button}'s default mold.
 * 
 * @author robbiecheng
 * @since 3.5.0
 */
public class Button2Default implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Button self = (Button) comp;
		final String uuid = self.getUuid();
		String mcls = self.getMoldSclass();
		if (mcls == null) mcls = "";
		final String outerattrs = self.getOuterAttrs();
		final Execution exec = Executions.getCurrent();
		wh.write("<span z.type=\"zul.widget.Button\" id=\"").write(uuid)
			.write("\" class=\"").write(mcls).write("\"")
			.write(ZkFns.noCSSAttrs(outerattrs))
			.write('>').write("<table id=\"").write(uuid)
			.write("!box\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"");
		if (self.getTabindex() >= 0) {
			if (!exec.isGecko() && !exec.isSafari())
				wh.write(" tabindex=\"").write(self.getTabindex()).write("\"");
		}
		wh.write(ZkFns.outCSSAttrs(outerattrs))
			.write(self.getInnerAttrs())
			.write(">\n<tr><td class=\"").write(mcls).write("-tl\"><button id=\"")
			.write(uuid).write("!real\" class=\"z ")
			.write(mcls).write("\"");
		if (self.getTabindex() >= 0) {
			if (exec.isGecko() || exec.isSafari())
				wh.write(" tabindex=\"").write(self.getTabindex()).write("\"");
		}
		wh.write("></button></td>");
		wh.write("<td class=\"").write(mcls).write("-tm\"></td><td class=\"")
			.write(mcls).write("-tr\"></td></tr>\n")
			.write("<tr><td class=\"")
			.write(mcls).write("-cl\"></td><td class=\"")
			.write(mcls).write("-cm\">");

		if (self.getDir().equals("reverse")) {
			new Out(self.getLabel()).render(out);
			if (self.isImageAssigned()
				&& self.getOrient().equals("vertical"))
			wh.writeln("<br/>");
			wh.write(self.getImgTag());
		} else {
			wh.write(self.getImgTag());
			if (self.isImageAssigned()
				&& self.getOrient().equals("vertical"))
			wh.writeln("<br/>");
			new Out(self.getLabel()).render(out);
		}		

		wh.write("</td>\n<td class=\"").write(mcls)
			.write("-cr\"><i class=\"z ")
			.write(mcls).write("\"></i></td></tr>\n<tr><td class=\"")
			.write(mcls).write("-bl\"></td><td class=\"").write(mcls)
			.write("-bm\"></td><td class=\"")
			.write(mcls).write("-br\"></td></tr>\n</table></span>");						
	}
}

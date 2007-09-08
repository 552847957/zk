/* TabDefaultV.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Sep 6, 2007 3:59:51 PM , Created by robbiecheng
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

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zk.ui.render.Out;

import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;

/**
 * {@link Tab}'s default mold in vertical only.
 * 
 * @author robbiecheng
 * 
 * @since 3.0.0
 * 
 */
public class TabDefaultV implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final Tab self = (Tab) comp;
		final Tabbox tabbox = self.getTabbox();
		final SmartWriter wh = new SmartWriter(out);
		final Execution exec = Executions.getCurrent();
		final String look = tabbox.getTabLook() + '-';
		final String suffix = self.isSelected() ? "-sel" : "-uns";

		wh.write("<tr id=\"").write(self.getUuid()).write("\" z.type=\"Tab\"")
			.write(self.getOuterAttrs()).write(" z.sel=\"").write(self.isSelected())
			.write("\" z.box=\"").write(tabbox.getUuid()).write("\" z.panel=\"")
			.write(self.getLinkedPanel().getUuid()).writeln("\">");

		wh.write("<td align=\"right\"").writeAttr("width", self.getWidth())
			.writeln("><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">")
			.write("<tr><td class=\"").write(look).write("tl").write(suffix).writeln("\"></td>")
			.write("<td colspan=\"3\" class=\"").write(look).write("tm").write(suffix).writeln("\"></td>")
			.write("<td class=\"").write(look).write("tr").write(suffix).writeln("\"></td>")
			.write("</tr>");

		wh.write("<tr height=\"22\">")
			.write("<td class=\"").write(look).write("ml").write(suffix).writeln("\"></td>")
			.write("<td width=\"3\" class=\"").write(look).write("mm").write(suffix).writeln("\"></td>")
			.write("<td align=\"center\" class=\"").write(look).write("mm").write(suffix)
			.write("\" id=\"").write(self.getUuid()).write("!real\"").write(self.getInnerAttrs())
			.write("><a href=\"javascript:;\" id=\"").write(self.getUuid()).write("!a\">");
			wh.write(self.getImgTag());
			new Out(self.getLabel()).render(out);
			wh.writeln( "</a></td>");		

			wh.write("<td width=\"3\" class=\"").write(look).write("mm").write(suffix).writeln("\"></td>")	
				.write("<td class=\"").write(look).write("mr").write(suffix).writeln("\"></td></tr>");		
		
		if(self.isClosable()){
			wh.writeln("<tr height=\"8\">")
				.write("<td class=\"").write(look).write("ml").write(suffix).writeln("\"></td>")
				.write("<td width=\"3\" class=\"").write(look).write("mm").write(suffix).writeln("\"></td>")
				.write("<td align=\"center\" valign=\"buttom\" class=\"").write(look).write("mm").write(suffix)
				.write("\"><img id=\"").write(self.getUuid()).write("!close\" src=\"")
				.write(exec.encodeURL("~./zul/img/close-off.gif")).writeln("\"/></td>")
				.write("<td width=\"3\" class=\"").write(look).write("mm").write(suffix).writeln("\"></td>")
				.write("<td class=\"").write(look).write("mr").write(suffix).writeln("\"></td></tr>");
		}
		
		wh.write("<tr><td class=\"").write(look).write("bl").write(suffix).writeln("\"></td>")
			.write("<td colspan=\"3\" class=\"").write(look).write("bm").write(suffix).writeln("\"></td>")
			.write("<td class=\"").write(look).write("br").write(suffix).writeln("\"></td>")
			.writeln("</tr></table></td></tr>");
	}
}

/* TreeDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 7, 2007 8:17:26 AM , Created by robbiecheng
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
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecol;

/**
 * {@link Tree}'s default mold.
 * 
 * @author robbiecheng
 * 
 * @since 3.0.0
 */
public class TreeDefault implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Tree self = (Tree) comp;
		final String uuid = self.getUuid();
		final String zcls = self.getZclass();
		final Execution exec = Executions.getCurrent();
		final String tag = exec.isBrowser("ie") || exec.isBrowser("gecko") ? "a" : "button";
		wh.write("<div id=\"").write(uuid).write("\" z.type=\"zul.tree.Tree\"")
			.write(self.getOuterAttrs()).write(self.getInnerAttrs()).write(">");
		if(self.getTreecols() != null){
			wh.write("<div id=\"").write(uuid).write("!head\" class=\"").write(zcls).write("-header\">")
				.write("<table width=\"").write(self.getInnerWidth()).write("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout:fixed\">");
			if(self.getTreecols() != null) {
				wh.write("<tbody style=\"visibility:hidden;height:0px\">")
					.write("<tr id=\"").write(self.getTreecols().getUuid())
					.write("!hdfaker\" class=\"").write(zcls).write("-faker\">");
					
				for (Iterator it = self.getTreecols().getChildren().iterator(); it.hasNext();) {
					final Treecol child = (Treecol) it.next();
					wh.write("<th id=\"").write(child.getUuid()).write("!hdfaker\"").write(child.getOuterAttrs())
					.write("><div style=\"overflow:hidden\"></div></th>");
				}
				wh.write("</tr></tbody>");
			}
			wh.writeComponents(self.getHeads())
				.write("</table></div>");
		}

		wh.write("<div id=\"").write(uuid).write("!body\" class=\"").write(zcls).write("-body\">")
			 .write("<table width=\"").write(self.getInnerWidth()).write("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"");
		if (self.isFixedLayout())
			wh.write(" style=\"table-layout:fixed\"");
		wh.write(">");
		
		if(self.getTreecols() != null) {
			wh.write("<tbody style=\"visibility:hidden;height:0px\">")
				.write("<tr id=\"").write(self.getTreecols().getUuid()).write("!bdfaker\" class=\"").write(zcls).write("-faker\">");
				
			for (Iterator it = self.getTreecols().getChildren().iterator(); it.hasNext();) {
				final Treecol child = (Treecol) it.next();
				wh.write("<th id=\"").write(child.getUuid()).write("!bdfaker\"").write(child.getOuterAttrs())
				.write("><div style=\"overflow:hidden\"></div></th>");
			}
			wh.write("</tr></tbody>");
		}
		wh.writeln(self.getTreechildren())
			.write("</table>");
		wh.write("<").write(tag).write("  z.keyevt=\"true\" id=\"").write(uuid).write("!a\" tabindex=\"-1\" onclick=\"return false;\"")
		.write(" href=\"javascript:;\" style=\"position: absolute;left: 0px; top: 0px;padding:0 !important; margin:0 !important; border:0 !important; background: transparent !important; font-size: 1px !important; width: 1px !important; height: 1px !important;-moz-outline: 0 none; outline: 0 none;	-moz-user-select: text; -khtml-user-select: text;\"></")
		.write(tag).write(">");
		wh.write("</div>");

		if(self.getTreefoot() != null){
			wh.write("<div id=\"").write(uuid).write("!foot\" class=\"").write(zcls).write("-footer\">")
				.write("<table width=\"").write(self.getInnerWidth()).write("\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout:fixed\">");
			if(self.getTreecols() != null) {
				wh.write("<tbody style=\"visibility:hidden;height:0px\">")
					.write("<tr id=\"").write(self.getTreecols().getUuid()).write("!ftfaker\" class=\"").write(zcls).write("-faker\">");
					
				for (Iterator it = self.getTreecols().getChildren().iterator(); it.hasNext();) {
					final Treecol child = (Treecol) it.next();
					wh.write("<th id=\"").write(child.getUuid()).write("!ftfaker\"").write(child.getOuterAttrs())
					.write("><div style=\"overflow:hidden\"></div></th>");
				}
				wh.write("</tr></tbody>");
			}
			wh.writeln(self.getTreefoot())
				.write("</table></div>");
		}
		wh.write("</div>");
	}
}

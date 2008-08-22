/* Bandbox2Default.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 6, 2008 8:53:14 AM , Created by jumperchen
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
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Bandbox;

/**
 * {@link Bandbox}'s default mold.
 * 
 * @author jumperchen
 * @since 3.5.0
 *
 */
public class Bandbox2Default implements ComponentRenderer {
	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Bandbox self = (Bandbox) comp;
		final String uuid = self.getUuid();
		final String mcls = self.getMoldSclass();
		final Execution exec = Executions.getCurrent();

		wh.write("<span id=\"").write(uuid).write("\"")
			.write(self.getOuterAttrs())
			.write(" z.type=\"zul.cb.Bdbox\" z.combo=\"true\"><input id=\"")
			.write(uuid).write("!real\" autocomplete=\"off\"")
			.write(" class=\"").write(mcls).write("-inp\"")
			.write(self.getInnerAttrs()).write("/><span id=\"")
			.write(uuid).write("!btn\" class=\"").write(mcls).write("-btn\"");

		if (!self.isButtonVisible())
			wh.write(" style=\"display:none\"");

		wh.write("><img class=\"").write(mcls).write("-img\"");
		if (self.getImage() != null) 
			wh.write(" style=\"background-image:url(").write(exec.encodeURL(self.getImage()))
				.write(")\"");
		wh.write(" src=\"").write(exec.encodeURL("~./img/spacer.gif")).write("\"")
			.write("\"/></span><div id=\"").write(uuid).write("!pp\" class=\"")
			.write(mcls).write("-pp\" style=\"display:none\" tabindex=\"-1\">")
			.write(self.getDropdown()).write("</div></span>");
	}
}
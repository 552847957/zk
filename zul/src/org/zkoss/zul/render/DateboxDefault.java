/* DateboxDefault.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Sep 6, 2007 5:29:45 PM , Created by jumperchen
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Datebox;

/*
 * {@link Datebox}'s default mold.
 * 
 * @author jumperchen
 * 
 * @since 3.0.0
 */
public class DateboxDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Datebox self = (Datebox) comp;
		final String uuid = self.getUuid();
		final Execution exec = Executions.getCurrent();

		wh.write("<span id=\"").write(uuid).write("\"");
		wh.write(self.getOuterAttrs()).write(" z.type=\"zul.db.Dtbox\" z.combo=\"true\"><input id=\"");
		wh.write(uuid).write("!real\" autocomplete=\"off\"");
		wh.write(self.getInnerAttrs()).write("/><span id=\"");
		wh.write(uuid).write("!btn\" class=\"rbtnbk\"><img src=\"");
		wh.write(exec.encodeURL(self.getImage()));
		wh.write("\"");
		if (self.isButtonVisible())
			wh.write("");
		else wh.write(" style=\"display:none\"");
		wh.write("/></span><div id=\"").write(uuid);
		wh.write("!pp\" class=\"dateboxpp\" style=\"display:none\" tabindex=\"-1\"></div></span>");
		wh.writeln();
	}

}

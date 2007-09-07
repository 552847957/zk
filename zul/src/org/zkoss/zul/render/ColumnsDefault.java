/* ColumnsDefault.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 7, 2007 10:49:13 AM , Created by jumperchen
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
import java.util.Iterator;

import org.zkoss.zk.fn.ZkFns;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.ComponentRenderer;
import org.zkoss.zul.Columns;

/*
 * {@link Columns}'s default mold.
 * 
 * @author jumperchen
 * 
 * @since 3.0.0
 */
public class ColumnsDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final WriterHelper wh = new WriterHelper(out);
		final Columns self = (Columns) comp;
		final String uuid = self.getUuid();
		wh.write("<tr id=\"").write(uuid).write("\"").write(" z.type=\"Cols\"");
		wh.write(self.getOuterAttrs()).write(self.getInnerAttrs()).write("align=\"left\">");
		
		for (Iterator it = self.getChildren().iterator(); it.hasNext();) {
			final Component child = (Component) it.next();
			ZkFns.redraw(child, out);
		}
		wh.writeln("</tr>");
	}

}

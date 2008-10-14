/* AppletDefault.java

 {{IS_NOTE
 Purpose:
 
 Description:
 
 History:
 Sep 7, 2007 9:04:13 AM , Created by davidchen
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
import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.render.ComponentRenderer;
import org.zkoss.zk.ui.render.SmartWriter;
import org.zkoss.zul.Applet;

/**
 * 
 * {@link Applet}'s default mold.
 * 
 * @author davidchen
 * @since 3.0.0
 */
public class AppletDefault implements ComponentRenderer {

	public void render(Component comp, Writer out) throws IOException {
		final SmartWriter wh = new SmartWriter(out);
		final Applet self = (Applet) comp;
		final String uuid = self.getUuid();
		
		wh.write("<applet id=\"").write(uuid).write("\" ");
		wh.write(self.getOuterAttrs()).write(self.getInnerAttrs());
		wh.write(" z.type=\"zul.applet.Applet\">");
		
		List li = self.getParamsHtml();
		for(Iterator iter = li.iterator(); iter.hasNext(); ) {
			Object[] param = (Object[]) iter.next();
			wh.write("<param name=\"").write(param[0].toString()).write("\" ");
			wh.write("value=\"").write(param.toString()).write("\"/>");
		}
		wh.write("</applet>");		
	}
}
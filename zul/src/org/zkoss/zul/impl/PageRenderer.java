/* PageRenderer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Oct 14 17:31:02     2008, Created by tomyeh
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul.impl;

import java.util.Collection;
import java.io.Writer;
import java.io.IOException;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.sys.PageCtrl;
import org.zkoss.zk.ui.sys.HtmlPageRenders;
/**
 * The page render for ZUL pages.
 *
 * @author tomyeh
 * @since 5.0.0
 */
public class PageRenderer implements org.zkoss.zk.ui.sys.PageRenderer {
	public void render(Page page, Writer out) throws IOException {
		final Execution exec = Executions.getCurrent();
		final String ctl =
			(String)exec.getAttribute(PageCtrl.ATTR_REDRAW_CONTROL);
		boolean au = exec.isAsyncUpdate(null);
		if (!au && (page.isComplete() || "complete".equals(ctl))) {
			renderComplete(exec, page, out);
			return;
		}

		boolean pageOnly = au;
		if (!pageOnly)
			pageOnly = (exec.isIncluded() || "page".equals(ctl))
				&& !"desktop".equals(ctl);

		if (pageOnly)
			renderPage(exec, page, out, au);
		else
			renderDesktop(exec, page, out);
	}
	/** Renders the desktop and the page.
	 */
	protected
	void renderDesktop(Execution exec, Page page, Writer out)
	throws IOException {
		HtmlPageRenders.setContentType(exec, page);

		final PageCtrl pageCtrl = (PageCtrl)page;
		write(out, HtmlPageRenders.outFirstLine(page)); //might null
		write(out, HtmlPageRenders.outDocType(page)); //might null
		out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\"");
		write(out, pageCtrl.getRootAttributes());
		out.write(">\n<head>\n"
			+"<meta http-equiv=\"Pragma\" content=\"no-cache\" />\n"
			+"<meta http-equiv=\"Expires\" content=\"-1\" />\n"
			+"<title>");
		write(out, page.getTitle());
		out.write("</title>\n");
		out.write(HtmlPageRenders.outLangStyleSheets(exec));
		out.write(HtmlPageRenders.outLangJavaScripts(exec));
		out.write(pageCtrl.getHeaders());
		out.write("</head>\n");

		out.write("<body>\n");
		writeln(out, HtmlPageRenders.outUnavailable(exec));
		HtmlPageRenders.outPageContent(exec, page, out, false);
		out.write(HtmlPageRenders.outResponseJavaScripts(exec));
		out.write("\n</body>\n</html>\n");
	}
	private static void write(Writer out, String s) throws IOException {
		if (s != null) out.write(s);
	}
	private static void writeln(Writer out, String s) throws IOException {
		if (s != null) {
			out.write(s);
			out.write('\n');
		}
	}
	/** Renders the page if {@link Page#isComplete} is true.
	 *
	 * @param au whether it is caued by an asynchronous update
	 */
	protected void renderPage(Execution exec, Page page, Writer out, boolean au)
	throws IOException {
		if (!au) {
			out.write(HtmlPageRenders.outLangStyleSheets(exec));
			out.write(HtmlPageRenders.outLangJavaScripts(exec));
		}

		HtmlPageRenders.outPageContent(exec, page, out, au);
		out.write(HtmlPageRenders.outResponseJavaScripts(exec));
	}
	/** Renders the page if {@link Page#isComplete} is true.
	 * In other words, the page content contains HTML/BODY tags.
	 */
	protected void renderComplete(Execution exec, Page page, Writer out)
	throws IOException {
		HtmlPageRenders.setContentType(exec, page);
		//TODO
	}
}

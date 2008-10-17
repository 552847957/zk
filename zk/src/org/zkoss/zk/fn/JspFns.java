/* JspFns.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Oct 17 08:57:02     2008, Created by tomyeh
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.fn;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.ExecutionCtrl;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zk.ui.sys.HtmlPageRenders;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.http.ExecutionImpl;

/**
 * Utilities to generate ZK related information in JSP pages.
 *
 * <p>For DSP pages, use {@link DspFns} instead.<br/>
 * For ZUML pages, use {@link ZkFns} instead.
 *
 * @author tomyeh
 * @since 3.5.2
 */
public class JspFns {
	/** Generates and returns the ZK specific HTML tags such as stylesheet
	 * and JavaScript.
	 * If you want to generate HTML HEAD and BODY tags by yourself in
	 * a non-ZUML page (e.g., JSP or DSP), you can invoke this method at
	 * the location you want (such as inside the HTML HEAD tag).
	 *
	 * @return the string holding the HTML tags, or null if already generated.
	 * @param deviceType the device type. If null, ajax is assumed.
	 */
 	public static String outZkHtmlTags(ServletContext ctx,
 	HttpServletRequest request, HttpServletResponse response,
 	String deviceType) {
 		Execution old = Executions.getCurrent();
 		Execution exec = new ExecutionImpl(ctx, request, response, null, null); 
 		((ExecutionCtrl)exec).onActivate();
		ExecutionsCtrl.setCurrent(exec);
		try {
			return HtmlPageRenders.outZkTags(exec,
				WebManager.getWebManager(ctx).getWebApp(),
				deviceType != null ? deviceType: "ajax");
		} finally {
			((ExecutionCtrl)exec).onDeactivate();
			ExecutionsCtrl.setCurrent(old);
		}
 	}
	/** Returns HTML tags to include style sheets of the specified device
	 * of the current application (never null).
	 *
	 * <p>It is the same as {@link JspPage#outDeviceStyleSheets}
	 * except this method is used for JSP pages.
	 * @param deviceType the device type. If null, ajax is assumed.
	 */
	public static final String outDeviceStyleSheets(ServletContext ctx,
 	HttpServletRequest request, HttpServletResponse response,
 	String deviceType) {
 		Execution old = Executions.getCurrent();
 		Execution exec = new ExecutionImpl(ctx, request, response, null, null); 
		ExecutionsCtrl.setCurrent(exec);
		try {
			return HtmlPageRenders.outDeviceStyleSheets(exec,
				WebManager.getWebManager(ctx).getWebApp(),
				deviceType != null ? deviceType: "ajax");
		} finally {
			ExecutionsCtrl.setCurrent(old);
		}
 	}
}

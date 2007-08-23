/* Window.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 8, 2007 5:48:27 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.jsf.zul;

import org.zkoss.jsf.zul.impl.BranchComponent;
import org.zkoss.zk.ui.Component;

/**
 * Window is a JSF component implementation for org.zkoss.zul.Window, 
 * 
 * This component should be declared nested under {@link org.zkoss.jsf.zul.Page}.
 * 
 * To know more ZK component features you can refer to <a href="http://www.zkoss.org/">http://www.zkoss.org/</a>
 *   
 * @author Dennis.Chen
 *
 */
public class Window extends BranchComponent {


	protected Component newComponent(Class use) throws Exception {
		return (Component) (use==null?new org.zkoss.zul.Window():use.newInstance());
	}
	
	
}

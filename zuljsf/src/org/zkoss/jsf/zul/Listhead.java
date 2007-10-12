/* Listhead.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2007/08/16  18:10:17 , Created by Dennis.Chen
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
 * Listhead is a JSF component implementation for {@link org.zkoss.zul.Listhead}, 
 * 
 * This component should be declared nested under {@link org.zkoss.jsf.zul.Page}.
 * 
 * To know more ZK component features you can refer to <a href="http://www.zkoss.org/">http://www.zkoss.org/</a>
 *   
 * @author Dennis.Chen
 *
 */
public class Listhead extends BranchComponent {


	protected Component newComponent(Class use) throws Exception {
		return (Component) (use==null?new org.zkoss.zul.Listhead():use.newInstance());
	}
	
	
}

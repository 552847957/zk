/* XmlDevice.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Aug 23 18:38:54     2007, Created by tomyeh
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.device;

/**
 * Represents the XML output.
 * 
 * @author tomyeh
 * @since 2.5.0
 */
public class XmlDevice extends GenericDevice {
	//Device//
	public String getContentType() {
		return "text/xml;charset=UTF-8";
	}
}

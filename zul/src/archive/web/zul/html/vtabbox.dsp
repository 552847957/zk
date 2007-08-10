<%--
vtabbox.dsp

{{IS_NOTE
	Purpose:
		
	Description:
		Vertical orientation of tabbox
	History:
		May 3 2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<table id="${self.uuid}"${self.outerAttrs}${self.innerAttrs} z.tabs="${self.tabs.uuid}" z.type="zul.tab.Tabbox" border="0" cellpadding="0" cellspacing="0">
<tr valign="top">
${z:redraw(self.tabs, null)}
${z:redraw(self.tabpanels, null)}
</tr>
</table>
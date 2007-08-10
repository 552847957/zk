<%--
tabs.dsp

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Tue Jul 12 10:58:31     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<thead id="${self.uuid}" z.type="zul.tab.Tabs"${self.outerAttrs}${self.innerAttrs}>
<tr><td>
<table border="0" cellpadding="0" cellspacing="0">
<tr valign="bottom">

<%-- prefix column  --%>
<td><table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td class="tab-3d-first"></td>
</tr>
</table></td>

	<c:forEach var="child" items="${self.children}">
	${z:redraw(child, null)}
	</c:forEach>

<td style="display:none" id="${self.uuid}!child"></td><%-- bookmark for adding children --%>

<%-- postfix column  --%>
<td><table border="0" cellpadding="0" cellspacing="0">
<tr>
	<td class="tab-3d-last1" id="${self.uuid}!last"></td>
	<td class="tab-3d-last2"></td>
</tr>
</table></td>
	</tr>
</table>
</td></tr>
</thead>

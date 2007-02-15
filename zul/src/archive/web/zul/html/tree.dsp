<%--
tree.dsp

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jul  7 11:27:10     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/zk/core.dsp.tld" prefix="z" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<div id="${self.uuid}" z.type="zul.tree.Tree"${self.outerAttrs}${self.innerAttrs}>
<c:if test="${!empty self.treecols}">
	<div id="${self.uuid}!head" class="tree-head">
	<table width="100%" border="0" cellpadding="0" cellspacing="0" style="table-layout:fixed">
${z:redraw(self.treecols, null)}
	</table>
	</div>
</c:if>
	<div id="${self.uuid}!body" class="tree-body">
	<table width="100%" border="0" cellpadding="0" cellspacing="0">
${z:redraw(self.treechildren, null)}
	</table>
	</div>
<c:if test="${!empty self.treefoot}">
	<div id="${self.uuid}!foot" class="tree-foot">
	<table width="100%" border="0" cellpadding="0" cellspacing="0" style="table-layout:fixed">
${z:redraw(self.treefoot, null)}
	</table>
	</div>
</c:if>
</div>

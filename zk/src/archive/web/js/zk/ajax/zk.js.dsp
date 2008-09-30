<%@ page contentType="application/x-javascript;charset=UTF-8" %><%--
zk.js.dsp

{{IS_NOTE
	Purpose:
		
	Description:
		Integrate all common js into one file
	History:
		Oct 24 2007 	Dennis Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>
if (!window.zk) {
<c:include page="~./js/zk/ajax/zk.js"/>
<c:include page="~./js/zk/ajax/lang/mesg*.js"/>
<c:include page="~./js/zk/ajax/util.js"/>
<c:include page="~./js/zk/ajax/dom.js"/>
<c:include page="~./js/zk/ajax/widget.js"/>
<c:include page="~./js/zk/ajax/au.js"/>

${z:outLocaleJavaScript()}
}
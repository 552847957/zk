<%--
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
<c:include page="~./js/ext/prototype/prototype.js"/>
<c:include page="~./js/ext/aculo/effects.js"/>
<c:include page="~./js/ext/aculo/dragdrop.js"/>
<c:include page="~./js/zk/html/boot.js"/>
<c:include page="~./js/zk/html/lang/mesg*.js"/>
<c:include page="~./js/zk/html/common.js"/>
<c:include page="~./js/zk/html/au.js"/>
<c:include page="~./js/zk/locale.js.dsp"/>
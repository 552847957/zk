<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=UTF-8" %><%--
fileupload-done.dsp

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jul 21 18:10:25     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/web/html.dsp.tld" prefix="h" %>
<%@ taglib uri="/WEB-INF/tld/zk/core.dsp.tld" prefix="z" %>
<c:set var="arg" value="${requestScope.arg}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Upload Result</title>
<link rel="stylesheet" type="text/css" href="${c:encodeURL('~./zul/css/norm**.css.dsp')}"/>
<%-- We cannot use ${z:outLangStyleSheets()} since Executions.getCurrent()
	is not available for this page.
 --%>
</head>
<body>
 <c:if test="${!empty arg.alert}">
	<h:box color="red">
	<pre><c:out value="${arg.alert}"/></pre>
 	</h:box>
	<input type="button" value="${c:l('mesg:org.zkoss.zul.mesg.MZul:UPLOAD_CANCEL')}" onclick="closeUpload()"/>
 </c:if>
</body>
<script type="text/javascript">
<!--
	parent.zkau.endUpload();

<%-- NOTE: we cannot execute zkau.sendUpdateResult in this frame with Firefox,
	because this frame will be removed and it will cause the following error
	if we try to insert some elements (some kind of NullPointerException
	"Component returned failure code: 0x80004005 (NS_ERROR_FAILURE) [nsIXMLHttpRequest.open]"
--%>
	function doUpdate() {
		parent.setTimeout("zkau.sendUpdateResult('${arg.uuid}', '${arg.contentId}')", 0);
	}
	function closeUpload() {
		parent.setTimeout("zkau.sendRemove('${arg.uuid}')", 0);
	}
	<c:if test="${!empty arg.contentId}">doUpdate(); closeUpload();</c:if>
	<c:if test="${empty arg.contentId and empty arg.alert}">closeUpload();</c:if>
// -->
</script>
</html>

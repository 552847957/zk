<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=UTF-8" %><%--
fileuploaddlg.dsp

{{IS_NOTE
	Purpose:
		The content of the inline frame of the fileupload modal dialog
		(fileuploaddlg.zul)
	Description:
		
	History:
		Thu Jul 21 11:37:28     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="/WEB-INF/tld/web/core.dsp.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/zk/core.dsp.tld" prefix="z" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Upload</title>
<link rel="stylesheet" type="text/css" href="${c:encodeURL('~./zul/css/norm**.css.dsp')}"/>
<%-- We cannot use ${z:outLangStyleSheets()} since Executions.getCurrent()
	is not available for this page.
 --%>
<script type="text/javascript">
<!--
function submitUpload() {
	var wndid = '${param.uuid}';
	var img = parent.$e(wndid + '!img');
	if (img) img.parentNode.removeChild(img);
		<%-- Bug 1578549: we have to remove the closable button first, since
			it might mis-behave if user clicks it after submitting
		--%>

	parent.zkau.beginUpload(wndid);
}
function cancelUpload() {
	parent.setTimeout("zk.focus(window); zkau.sendRemove('${param.uuid}');", 100);
}
function init() {
	var inp = document.getElementById("file");
	if (inp) inp.focus();
}
// -->
</script>
</head>
<body onload="init()">
	<form action="${param.action}?dtid=${param.dtid}&uuid=${param.uuid}" enctype="multipart/form-data" method="POST" onsubmit="submitUpload()">
	<%-- We have to encode dtid and uuid in action rather than hidden fields,
		because 1) dtid must be ready before parsing multi-part requests.
		2) parsing multi-part might fail
	--%>
	<%-- change the following if you want the return URI to be different from the default
	<input type="hidden" name="nextURI" value="~./zul/html/fileuploaddlg-done.dsp"/>
	--%>

	<table border="0">
<c:set var="maxcnt" value="${empty param.max ? 1: param.max}"/>
<c:forEach var="cnt" begin="1" end="${maxcnt}">
	<tr>
		<td align="right"><c:if test="${maxcnt gt 3}">${cnt}</c:if></td>
		<td><input type="file" id="file" name="file"/></td>
	</tr>
</c:forEach>
	<tr align="left">
		<td colspan="2" style="border: outset 1px">
		<input type="submit" value="${c:l('mesg:org.zkoss.zul.mesg.MZul:UPLOAD_SUBMIT')}" onclick="parent.zk.progress()"/>
		<input type="button" value="${c:l('mesg:org.zkoss.zul.mesg.MZul:UPLOAD_CANCEL')}" onclick="cancelUpload()"/>
		</td>
	<tr>
	</table>
	</form>
</body>
</html>

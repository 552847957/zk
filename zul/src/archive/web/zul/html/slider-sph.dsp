<%--
slider-sph.dsp

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Sep 29 21:06:03     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
--%><%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<c:set var="self" value="${requestScope.arg.self}"/>
<table id="${self.uuid}"${self.outerAttrs}${self.innerAttrs} z.type="zul.sld.Sld" cellpadding="0" cellspacing="0">
<tr height="20">
 <td class="slidersph-bkl"></td>
 <td class="slidersph-bk"><img id="${self.uuid}!btn" src="${c:encodeURL('~./zul/img/slider/btnsph.gif')}" title="${c:string(self.curpos)}"/></td>
 <td class="slidersph-bkr"></td>
</tr>
</table>

/* vbox.js

	Purpose:
		
	Description:
		
	History:
		Wed Nov  5 14:10:39     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
function (out) {
	out.push('<table', this.domAttrs_(), zUtl.cellps0, '><tr');
	
	var	p = this.getPack();
	if (p && p != 'stretch') out.push(' valign="', zul.box.Box._toValign(p), '"');
	out.push('><td style="height:100%;width:100%"');
	var v = this.getAlign();
	if (v && v != 'stretch') out.push(' align="', zul.box.Box._toHalign(v), '"');
	out.push('><table id="', this.uuid, '-real"', zUtl.cellps0, 'style="text-align:left');
	if (v == 'stretch') out.push(';width:100%');
	if (p == 'stretch') out.push(';height:100%');
	out.push('">');

	for (var w = this.firstChild; w; w = w.nextSibling)
		this.encloseChildHTML_(w, false, out);

	out.push('</table></td></tr></table>');
}
/* input.js

	Purpose:
		
	Description:
		
	History:
		Fri Jan 16 13:13:15     2009, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
function (out) {
	out.push('<input', this.domAttrs_(), this.innerAttrs_(), '/>');
}
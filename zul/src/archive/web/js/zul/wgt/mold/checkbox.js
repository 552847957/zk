/* checkbox.js

	Purpose:
		
	Description:
		
	History:
		Wed Dec 10 16:51:36     2008, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
function (out) {
	var uuid = this.uuid,
		zcls = this.getZclass();
	out.push('<span', this.domAttrs_(), '>', '<input type="checkbox" id="', uuid,
			'$real"', this.contentAttrs_(), '/><label for="', uuid, '$real"',
			this.labelAttrs_(), ' class="', zcls, '-cnt">', this.domContent_(),
			'</label></span>');	
}
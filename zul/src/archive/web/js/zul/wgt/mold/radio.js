/* radio.js

	Purpose:
		
	Description:
		
	History:
		Tue Dec 16 11:17:47     2008, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
function () {
	var uuid = this.uuid,
		zcls = this.getZclass(),
		rg = this.getRadiogroup();
	return '<span' + this.domAttrs_() + '>'
		 + '<input type="radio" id="' + uuid + '$real"' + this.contentAttrs_()
		 + '/><label for="' + uuid + '$real"' + this.labelAttrs_()
		 + ' class="' + zcls + '-cnt">' + this.domContent_() + '</label>'
		 + (rg && rg.getOrient() == 'vertical' ? '<br/>' : '') + '</span>';
}
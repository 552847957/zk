/* main.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Feb  8 16:07:03     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
function zkRaw() {} //ZHTML components

zkRaw.init = function (cmp) {
	zk.listen(cmp, "change", function () {zkRaw.onchange(cmp);});
};

zkRaw.onchange = function (cmp) {
	if (getZKAttr(cmp, "onChange"))
		zkau.send({uuid: cmp.id, cmd: "onChange", data: [cmp.value]}, 25);
};

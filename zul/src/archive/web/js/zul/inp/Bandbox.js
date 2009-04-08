/* Bandbox.js

	Purpose:
		
	Description:
		
	History:
		Tue Mar 31 14:17:28     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.inp.Bandbox = zk.$extends(zul.inp.ComboWidget, {
	//super
	getPopupSize_: function (pp) {
		var bp = this.firstChild, //bandpopup
			w, h;
		if (bp) {
			w = bp.getWidth();
			h = bp.getHeight();
		}
		return [w||'auto', h||'auto'];
	},
	getZclass: function () {
		var zcs = this._zclass;
		return zcs != null ? zcs: "z-bandbox";
	}
});

/* Toolbar.js

	Purpose:

	Description:

	History:
		Sat Dec 24 12:58:43	 2008, Created by Flyworld

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zk.def(zul.wgt.Toolbar = zk.$extends(zul.Widget, {
	_orient: "horizontal",
	_align: "start",

	// super
	getZclass: function(){
		var zcls = this._zclass;
		return zcls ? zcls : "z-toolbar" +
		(this.inPanelMold() ? "-panel" : "");
	},
	// protected 
	inPanelMold: function(){
		return this._mold == "panel";
	},
	onChildAdded_: function(){
		this.$supers('onChildAdded_', arguments);
		if (this.inPanelMold()) 
			this.rerender();
	},
	onChildRemoved_: function(){
		this.$supers('onChildRemoved_', arguments);
		if (this.inPanelMold()) 
			this.rerender();
	}
	
}), { //zk.def
	align: _zkf = function () {
		this.rerender();
	},
	orient: _zkf
});

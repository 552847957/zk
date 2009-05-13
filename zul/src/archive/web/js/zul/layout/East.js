/* East.js

	Purpose:
		
	Description:
		
	History:
		Wed Jan  7 12:14:59     2009, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.layout.East = zk.$extends(_zkbc = zul.layout.LayoutRegion, {
	setHeight: zk.$void, // readonly
	sanchor: 'r',

	$init: function () {
		this.$supers('$init', arguments);
		this.setCmargins("0,5,5,0");
	},
	getPosition: function () {
		return zul.layout.Borderlayout.EAST;
	},
	getSize: _zkbc.prototype.getWidth,
	setSize: _zkbc.prototype.setWidth
});
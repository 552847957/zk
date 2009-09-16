/* math.js

	Purpose:
		
	Description:
		
	History:
		Sun Dec 14 17:16:17     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zk.BigDecimal = zk.$extends(zk.Object, {
	_prec: 0,
	$define: {
		prec: null
	},
	$init: function (value) {
		value = value ? '' + value: '0';
		var j = value.lastIndexOf('.');
		if (j >= 0) {
			value = value.substring(0, j) + value.substring(j + 1);
			this._prec = value.length - j;
		}
		this._value = value;
	},
	toInternalString: function() {
		var j = this._value.length - this._prec;
		return this._value.substring(0, j) + '.' + this._value.substring(j);
	},
	toString: function() {
		var j = this._value.length - this._prec;
		return this._value.substring(0, j) + zk.DECIMAL + this._value.substring(j);
	}
});

/* WidgetInfo.js

	Purpose:
		
	Description:
		
	History:
		Thu Sep  3 14:30:38     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
(function () {
	var _wgtInfs = {};

	function _load(nm, weavee) {
			if (!zk.isLoaded(nm, true))
				zk.load(nm,
					weavee ? function () {
						if (zk.$package(nm).$wv)
							zk.load(nm + '.wv');
					}: null);
	}

zk.wgt.WidgetInfo = {
	getClassName: function (wgtnm) {
		return _wgtInfs[wgtnm];
	},
	register: function (infs) {
		for (var i = 0, len = infs.length; i < len; ++i) {
			var clsnm = infs[i],
				j = clsnm.lastIndexOf('.'),
				wgtnm = j >= 0 ? clsnm.substring(j + 1): clsnm;
			_wgtInfs[wgtnm.substring(0,1).toLowerCase()+wgtnm.substring(1)] = clsnm;
		}
	},
	loadAll: function (f, weavee) {
		for (var w in _wgtInfs) {
			var clsnm = _wgtInfs[w],
				j = clsnm.lastIndexOf('.');
			_load(clsnm.substring(0, j), weavee);
		}
		if (f) zk.afterLoad(f);
	}
};

})();
/* Column.js

	Purpose:
		
	Description:
		
	History:
		Wed Dec 24 15:25:39     2008, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.grid.Column = zk.$extends(zul.HeaderWidget, {
	_sortDir: "natural",
	_sortAsc: "none",
	_sortDsc: "none",

	preserveBlank: true, //iZuml
	
	$init: function () {
		this.$supers('$init', arguments);
		this.listen('onSort', this, null, -1000);
	},
	getGrid: zul.HeaderWidget.prototype.getMeshWidget,
	setSort: function (type) {
		if (type && type.startsWith('client')) {
			this.setSortAscending(type);
			this.setSortDescending(type);
		} else {
			this.setSortAscending('none');
			this.setSortDescending('none');
		}
	},
	getSortDirection: function () {
		return this._sortDir;
	},
	setSortDirection: function (sortDir) {
		if (this._sortDir != sortDir) {
			this._sortDir = sortDir;
			var n = this.getNode();
			
			if (n) {
				var zcls = this.getZclass();
				zDom.rmClass(n, zcls + "-sort-dsc");
				zDom.rmClass(n, zcls + "-sort-asc");
				switch (sortDir) {
				case "ascending":
					zDom.addClass(n, zcls + "-sort-asc");
					break;
				case "descending":
					zDom.addClass(n, zcls + "-sort-dsc");
					break;
				default: // "natural"
					zDom.addClass(n, zcls + "-sort");
					break;
				}
			}
		}
	},
	isSortable_: function () {
		return this._sortAsc != "none" || this._sortDsc != "none";
	},
	getSortAscending: function () {
		return this._sortAsc;
	},
	setSortAscending: function (sorter) {
		if (!sorter) sorter = "none";
		if (this._sortAsc != sorter) {
			this._sortAsc = sorter;
			var n = this.getNode(),
				zcls = this.getZclass();
			if (n) {
				if (sorter == "none") {
					zDom.rmClass(n, zcls + "-sort-asc");
					if (this._sortDsc == "none")
						zDom.rmClass(n, zcls + "-sort");					
				} else
					zDom.addClass(n, zcls + "-sort");
			}
		}
	},
	getSortDescending: function () {
		return this._sortDsc;
	},
	setSortDescending: function (sorter) {
		if (!sorter) sorter = "none";
		if (this._sortDsc != sorter) {
			this._sortDsc = sorter;
			var n = this.getNode(),
				zcls = this.getZclass();
			if (n) {
				if (sorter == "none") {
					zDom.rmClass(n, zcls + "-sort-dsc");
					if (this._sortAsc == "none")
						zDom.rmClass(n, zcls + "-sort");					
				} else
					zDom.addClass(n, zcls + "-sort");
			}
		}
	},
	sort: function (ascending, evt) {
		var dir = this.getSortDirection();
		if (ascending) {
			if ("ascending" == dir) return false;
		} else {
			if ("descending" == dir) return false;
		}

		var sorter = ascending ? this._sortAsc: this._sortDsc;
		if (sorter == "fromServer")
			return false;
		else if (sorter == "none") {
			evt.stop();
			return false;
		}
		
		var grid = this.getGrid();
		if (!grid || grid.isModel()) return false;
			// if in model, the sort should be done by server
			
		var	rows = grid.rows;		
		if (!rows) return false;
		rows.parent.removeChild(rows);
		evt.stop();
		var d = [], col = this.getChildIndex();
		for (var i = 0, z = 0, row = rows.firstChild; row; row = row.nextSibling, z++)
			for (var k = 0, cell = row.firstChild; cell; cell = cell.nextSibling, k++) 
				if (k == col) {
					d[i++] = {
						wgt: cell,
						index: z
					};
				}
		
		var dsc = dir == "ascending" ? 1 : -1,
			fn = this.sorting,
			isNumber = sorter == "client(number)";
		d.sort(function(a, b) {
			var v = fn(a.wgt, b.wgt, isNumber) * dsc;
			if (v == 0) {
				v = (a.index < b.index ? -1 : 1);
			}
			return v;
		});
		for (var i = 0, k = d.length;  i < k; i++) {
			rows.appendChild(d[i].wgt.parent);
		}
		this._fixDirection(ascending);
		grid.appendChild(rows);
		return true;
	},
	sorting: function(a, b, isNumber) {
		var v1 = a.getValue(), v2 = b.getValue();
			if (isNumber) return v1 - v2;
		return v1 > v2 ? 1 : (v1 < v2 ? -1 : 0);
	},
	_fixDirection: function (ascending) {
		//maintain
		for (var w = this.parent.firstChild; w; w = w.nextSibling) {
			w.setSortDirection(
				w != this ? "natural": ascending ? "ascending": "descending");
		}
	},
	setLabel: function (label) {
		this.$supers('setLabel', arguments);
		// TODO menupopup
	},
	setVisible: function (visible) {
		if (this.isVisible() != visible) {
			this.$supers('setVisible', arguments);
			// TODO menupopup
		}
	},
	onSort: function (evt) {
		var dir = this.getSortDirection();
		if ("ascending" == dir) this.sort(false, evt);
		else if ("descending" == dir) this.sort(true, evt);
		else if (!this.sort(true, evt)) this.sort(false, evt);
	},
	getZclass: function () {
		return this._zclass == null ? "z-column" : this._zclass;
	},
	domClass_: function (no) {
		var scls = this.$supers('domClass_', arguments);
		if (!no || !no.zclass) {
			var added = this._sortAsc != "none" || this._sortDsc != "none" ?  this.getZclass() + '-sort': '';
			return scls != null ? scls + ' ' + added : added;
		}
		return scls;
	}
});

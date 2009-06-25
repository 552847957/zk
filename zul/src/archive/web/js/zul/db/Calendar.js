/* Calendar.js

{{IS_NOTE
	Purpose:

	Description:

	History:
		Fri Jan 23 10:32:34 TST 2009, Created by Flyworld
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
zul.db.Calendar = zk.$extends(zul.Widget, {
	_view : "day", //"day", "month", "year", "decade"
	$define: {
		date: _zkf = function() {
			this.rerender();
		}
	},
	getFormat: function () {
		var fmt = this._fmt;
		return fmt ? fmt: "yyyy/MM/dd";
	},
	getZclass: function () {
		var zcs = this._zclass;
		return zcs != null ? zcs: "z-calendar";
	},
	today: function () {
		var d = new Date();
		return new Date(d.getFullYear(), d.getMonth(), d.getDate());
	},
	bind_: function (){
		this.$supers('bind_', arguments);
		this._dateobj = this._date ? zDateFormat.parseDate(this._date, "yyyy/MM/dd") : new Date();
		var title = this.getSubnode("title"),
			mid = this.getSubnode("mid"),
			ly = this.getSubnode("ly"),
			ry = this.getSubnode("ry");

		this._markCal();
		this.domListen_(title, "onClick", '_changeView');
		this.domListen_(mid, "onClick", '_choiceData');
		this.domListen_(ly, "onClick", '_doclickArrow');
		this.domListen_(ry, "onClick", '_doclickArrow');
		this.domListen_(mid, "onMouseOver", '_doMouseEffect');
		this.domListen_(mid, "onMouseOut", '_doMouseEffect');
	},
	unbind_: function () {
		var title = this.getSubnode("title"),
			mid = this.getSubnode("mid"),
			ly = this.getSubnode("ly"),
			ry = this.getSubnode("ry");
		this.domUnlisten_(title, "onClick", '_changeView');
		this.domUnlisten_(mid, "onClick", '_choiceData');
		this.domUnlisten_(ly, "onClick", '_doclickArrow');
		this.domUnlisten_(ry, "onClick", '_doclickArrow');
		this.domUnlisten_(mid, "onMouseOver", '_doMouseEffect');
		this.domUnlisten_(mid, "onMouseOut", '_doMouseEffect');
		this.$supers('unbind_', arguments);
	},
	_doclickArrow: function (evt) {
		var node = evt.domTarget,
			ofs = node.id.indexOf("-ly") > 0 ? -1 : 1;
		switch(this._view) {
			case "day" :
				this._shiftDate("m", ofs);
				break;
			case "month" :
				this._shiftDate("y", ofs);
				break;
			case "year" :
				this._shiftDate("y", ofs*10);
				break;
			case "decade" :
				this._shiftDate("y", ofs*100);
				break;
		}
		this.rerender();
	},
	_doMouseEffect: function (evt) {
		var node = evt.domTarget.tagName == "TD" ? evt.domTarget : evt.domTarget.parentNode,
			zcls = this.getZclass();
		if (jq(node).is("."+zcls+"-seld")) {
			jq(node).removeClass(zcls + "-over");
			jq(node).toggleClass(zcls + "-over-seld");
		}else {
			jq(node).toggleClass(zcls + "-over");
		}
	},
	_getDateObj : function () {
		return this._date ? zDateFormat.parseDate(this._date, "yyyy/MM/dd") : new Date();
	},
	_setTime : function (y, m, d, hr, mi) {
		var dateobj = this._getDateObj(),
			year = y != null ? y  : dateobj.getFullYear(),
			month = m != null ? m : dateobj.getMonth(),
			day = d != null ? d : dateobj.getDate();
		this._date = zDateFormat.formatDate(new Date(year, month, day), "yyyy/MM/dd");
		this.fire('onChange', {value: this._date});
	},
	_choiceData: function (evt) {
		var target = evt.domTarget.tagName == "TD" ? jq(evt.domTarget).children('a')[0]:
					evt.domTarget.tagName == "A" ? evt.domTarget : null,
			cell = target.parentNode,
			dateobj = this._getDateObj();
		if (target) {
			switch(this._view) {
				case "day" :
					this._setTime(null, cell._monofs != null && cell._monofs != 0 ?
							dateobj.getMonth() + cell._monofs : null, target.textContent);
					this.rerender();
					break;
				case "month" :
					var t = target.parentNode.id.split('-')[1];
					this._setTime(null, zk.parseInt(t.replace('m','')));
					this._setView("day");
					break;
				case "year" :
					this._setTime(target.textContent);
					this._setView("month");
					break;
				case "decade" :
					//Decade mode Set Year Also
					this._setTime(target.textContent.split("-")[0]);
					this._setView("year");
					break;
			}
		}
	},
	_shiftDate: function (opt, ofs) {
		var dateobj = this._getDateObj(),
			year = dateobj.getFullYear(),
			month = dateobj.getMonth(),
			day = dateobj.getDate();
		switch(opt) {
			case "d" :
				day = day + ofs;
				break;
			case "m" :
				month = month + ofs;
				break;
			case "y" :
				year = year + ofs;
				break;
			case "d" :
				year = year + ofs;
				break;
		}
		this._date = zDateFormat.formatDate(new Date(year, month, day), "yyyy/MM/dd");
		this.fire('onChange', {value: this._date});
	},
	_changeView : function (evt) {
		var tm = this.getSubnode("tm"),
			ty = this.getSubnode("ty"),
			tyd = this.getSubnode("tyd");
		if (evt.domTarget == tm)
			this._setView("month");
		else if (evt.domTarget == ty)
			this._setView("year");
		else if (evt.domTarget == tyd )
			this._setView("decade");
	},
	_setView : function (view) {
		if (view != this._view) {
			this._view = view;
			this.rerender();
		}
	},
	_markCal : function () {
		var	zcls = this.getZclass(),
		 	val = this._getDateObj(),
		 	m = val.getMonth(),
			d = val.getDate(),
			y = val.getFullYear(),
			last = new Date(y, m + 1, 0).getDate(), //last date of this month
			prev = new Date(y, m, 0).getDate(), //last date of previous month
			v = new Date(y, m, 1).getDay()- zk.DOW_1ST;
		//hightlight month & year
		for (var j = 0; j < 12; ++j) {
			var mon = this.getSubnode("m" + j),
				year = this.getSubnode("y" + j),
				yy = y % 10 + 1;
			if (mon) {
				if (m == j) {
					jq(mon).addClass(zcls+"-seld");
					jq(mon).removeClass(zcls+"-over");
				} else
					jq(mon).removeClass(zcls+"-seld");
			}
			if (year) {
				if (yy == j) {
					jq(year).addClass(zcls+"-seld");
				    jq(year).removeClass(zcls+"-over");
				} else
					jq(year).removeClass(zcls+"-seld");
			}
		}

		if (v < 0) v += 7;
		for (var j = 0, cur = -v + 1; j < 6; ++j) {
			var week = this.getSubnode("w" + j);
			if ( week != null) {
				for (var k = 0; k < 7; ++k, ++cur) {
					v = cur <= 0 ? prev + cur: cur <= last ? cur: cur - last;
					if (k == 0 && cur > last)
						week.style.display = "none";
					else {
						if (k == 0) week.style.display = "";
						var cell = week.cells[k],
							monofs = cur <= 0 ? -1: cur <= last ? 0: 1,
							sel = cur == d;
						cell.style.textDecoration = "";
						cell._monofs = monofs;
						//hightlight day
						jq(cell).removeClass(zcls+"-over");
						jq(cell).removeClass(zcls+"-over-seld");
						if (sel)
							jq(cell).addClass(zcls+"-seld");
						else
							jq(cell).removeClass(zcls+"-seld");
						jq(cell).html('<a href="javascript:;">' + v + '</a>');
					}
				}
			}
		}
	}

});

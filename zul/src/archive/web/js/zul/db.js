/* db.js

{{IS_NOTE
	Purpose:
		datebox
	Description:
		
	History:
		Mon Oct 17 15:24:01     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
zk.load("zul.zul"); //msgzul
zk.load("zul.widget");

//Calendar//
zkCal = {};

zk.Cal = Class.create();
zk.Cal.prototype = {
	initialize: function (cmp, popup) {
		this.id = cmp.id;
		this.popup = popup;
		this.input = $e(cmp.id + "!real");
		this._newCal();
		this.init();
	},
	_newCal: function() {
		this.element = $e(this.id);
		if (!this.element) return;

		var compact = getZKAttr(this.element, "compact") == "true";
		var html = this.popup ? '<table border="0" cellspacing="0" cellpadding="0" tabindex="-1">': '';
		html += '<tr><td><table class="calyear" width="100%" border="0" cellspacing="0" cellpadding="0"><tr><td width="5"></td><td align="right"><img src="'
			+zk.getUpdateURI('/web/zul/img/cal/arrowL.gif')
			+'" style="cursor:pointer" onclick="zkCal.onyearofs(event,-1)" id="'
			+this.id+'!ly"/></td>';

		if (compact)
			html += '<td align="right"><img src="'+zk.getUpdateURI('/web/zul/img/cal/arrow2L.gif')
				+'" style="cursor:pointer" onclick="zkCal.onmonofs(event,-1)" id="'
				+this.id+'!lm"/></td>';

		html += '<td width="5"></td><td id="'+this.id+'!title"></td><td width="5"></td>';

		if (compact)
			html += '<td align="left"><img src="'+zk.getUpdateURI('/web/zul/img/cal/arrow2R.gif')
				+'" style="cursor:pointer" onclick="zkCal.onmonofs(event,1)" id="'
				+this.id+'!rm"/></td>';

		html += '<td align="left"><img src="'+zk.getUpdateURI('/web/zul/img/cal/arrowR.gif')
			+'" style="cursor:pointer" onclick="zkCal.onyearofs(event,1)" id="'
			+this.id+'!ry"/></td><td width="5"></td></tr></table></td></tr>';

		if (!compact) {
			html += '<tr><td><table class="calmon" width="100%" border="0" cellspacing="0" cellpadding="0"><tr>';
			for (var j = 0 ; j < 12; ++j) {
				html += '<td id="'+this.id+'!m'+j
					+'" onclick="zkCal.onmonclk(event)" onmouseover="zkCal.onover(event)" onmouseout="zkCal.onout(event)">'
					+ zk.S2MON[j] + '</td>';
				if (j == 5) html += '</tr><tr>';
			}
			html += '</tr></table></td></tr>';
		}
		if (this.popup) html += '<tr><td height="3px"></td></tr>';

		html += '<tr><td><table class="calday" width="100%" border="0" cellspacing="0" cellpadding="0"><tr class="caldow">';
		var sun = (7 - zk.DOW_1ST) % 7, sat = (6 + sun) % 7;
		for (var j = 0 ; j < 7; ++j) {
			html += '<td';
			if (j == sun || j == sat) html += ' style="color:red"';
			html += '>' + zk.S2DOW[j] + '</td>';
		}
		html += '</tr>';
		for (var j = 0; j < 6; ++j) { //at most 7 rows
			html += '<tr class="calday" id="'+this.id+'!w'+j
				+'" onclick="zkCal.ondayclk(event)" onmouseover="zkCal.onover(event)" onmouseout="zkCal.onout(event)">';
			for (var k = 0; k < 7; ++k)
				html += '<td></td>';
			html += '</tr>'
		}
		html += '</table></td></tr>';
		if (this.popup) html += '</table>';
		zk.setInnerHTML(this.popup || this.element, html);
	},
	init: function () {
		this.element = $e(this.id);
		if (!this.element) return;

		var val = this.input ? this.input.value: getZKAttr(this.element, "value");
		if (val) val = zk.parseDate(val, this.getFormat());
		this.date = val ? val: this.today();
		this._output();
	},
	getFormat: function () {
		var fmt = getZKAttr(this.element, "fmt");
		return fmt ? fmt: "yyyy/MM/dd";
	},
	today: function () {
		var d = new Date();
		return new Date(d.getFullYear(), d.getMonth(), d.getDate());
	},
	_output: function () {
		//year
		var val = this.date, m = val.getMonth(), d = val.getDate();
		var y = val.getFullYear();
		var el = $e(this.id + "!title");
		zk.setInnerHTML(el, zk.SMON[m] + ', ' + y);

		//month
		for (var j = 0; j < 12; ++j) {
			el = $e(this.id + "!m" + j);
			if (el) { //omitted if compact
				el.className = m == j ? "sel": "";
				el.setAttribute("zk_mon", j);
			}
		}

		var last = new Date(y, m + 1, 0).getDate(), //last date of this month
			prev = new Date(y, m, 0).getDate(); //last date of previous month
		var v = new Date(y, m, 1).getDay()- zk.DOW_1ST;
		if (v < 0) v += 7;
		for (var j = 0, cur = -v + 1; j < 6; ++j) {
			el = $e(this.id + "!w" +j);
			for (var k = 0; k < 7; ++k, ++cur) {
				v = cur <= 0 ? prev + cur: cur <= last ? cur: cur - last;
				if (k == 0 && cur > last) el.style.display = "none";
				else {
					if (k == 0) el.style.display = "";
					var cell = el.cells[k];
					cell.style.textDecoration = "";
					cell.setAttribute("zk_day", v);
					cell.setAttribute("zk_monofs",
						cur <= 0 ? -1: cur <= last ? 0: 1);
					this._outcell(cell, cur == d);
				}
			}
		}
	},
	_outcell: function (cell, sel) {
		if (sel) this.curcell = cell;
		cell.className = sel ? "sel": "";
		var d = cell.getAttribute("zk_day");
		zk.setInnerHTML(cell,
			!sel || this.popup ? d:
			'<a href="javascript:;" onkeyup="zkCal.onup(event)" on'
				+(zk.ie ? "keydown": "keypress")
				+'="zkCal.onkey(event)" onblur="zkCal.onblur(event)">'+d+'</a>');
			//IE: use keydown. otherwise, it causes the window to scroll
	},
	_ondayclk: function (cell) {
		var y = this.date.getFullYear(), m = this.date.getMonth();
		var d = zk.getIntAttr(cell, "zk_day");
		if (cell.className != "sel") { //!selected
			var monofs = zk.getIntAttr(cell, "zk_monofs");
			this.date = new Date(y, m + monofs, d);
			if (!this.popup) {
				if (monofs != 0) this._output();
				else {
					this._outcell(this.curcell, false);
					this._outcell(cell, true);
				}
			}
		}
		this._onupdate(true);
	},
	_onmonclk: function (cell) {
		if (cell.className != "sel") { //!selected
			var y = this.date.getFullYear(), d = this.date.getDate();
			this.date = new Date(y, zk.getIntAttr(cell, "zk_mon"), d);
			this._output();
			this._onupdate(false);
		}
	},
	_onyearofs: function (ofs) {
		var y = this.date.getFullYear(), m = this.date.getMonth(),
			d = this.date.getDate();
		this.date = new Date(y + ofs, m, d);
		this._output();
		this._onupdate(false);
	},
	_onmonofs: function (ofs) {
		var y = this.date.getFullYear(), m = this.date.getMonth(),
			d = this.date.getDate();
		this.date = new Date(y, m + ofs, d);
		this._output();
		this._onupdate(false);
	},
	setDate: function (val) {
		if (val != this.date) {
			var old = this.date;
			if (old.getFullYear() != val.getFullYear()
			|| old.getMonth() != val.getMonth()) {
				this.date = val;
				this._output();
			} else {
				this.date = val;
				this._outcell(this.curcell, false);
	
				var d = val.getDate();
				for (var j = 0; j < 6; ++j) {
					el = $e(this.id + "!w" +j);
					for (var k = 0; k < 7; ++k) {
						var cell = el.cells[k];
						if (zk.getIntAttr(cell, "zk_monofs") == 0
						&& zk.getIntAttr(cell, "zk_day") == d) {
							this._outcell(cell, true);
							break;
						}
					}
				}
			}
		}
	},
	/** Calls selback or onchange depending on this.popup. */
	_onupdate: function (close) {
		this._output();
		if (this.popup) {
			this.selback(close);
			if (this.input) {
				//Request 1551019: better responsive
				this.onchange();
				zk.asyncFocus(this.input.id);
			}
		} else {
			this.onchange();
			zk.asyncFocusDown(this.id, zk.ie ? 50: 0);
		}
	},
	onchange: function () {
		if (this.popup) {
			zkTxbox.updateChange(this.input, false);
		} else {
			var y = this.date.getFullYear(),
				m = this.date.getMonth(), d = this.date.getDate();
			zkau.send({uuid: this.id, cmd: "onChange",
				data: [y+'/'+(m+1)+'/'+d]}, zkau.asapTimeout(this.element, "onChange"));
			this._changed = false;
		}
	},
	selback: function (close) {
		if (this.input) {
			this.input.value = this.getDateString();
			zk.asyncFocus(this.input.id);
			zk.asyncSelect(this.input.id);
		}
		if (close) zkau.closeFloats(this.element);
	},
	getDateString: function () {
		return zk.formatDate(this.date, this.getFormat());
	},
	shift: function (days) {
		var val = this.date;
		this.setDate(new Date(
			val.getFullYear(), val.getMonth(), val.getDate() + days));
	}
};

zkCal.init = function (cmp) {
	var meta = zkau.getMeta(cmp);
	if (meta) meta.init();
	else zkau.setMeta(cmp, new zk.Cal(cmp, null));
};
zkCal.setAttr = function (cmp, nm, val) {
	if ("z.value" == nm) {
		var meta = zkau.getMeta(cmp);
		if (meta) meta.setDate(zk.parseDate(val, "yyyy/MM/dd"));
	}
	zkau.setAttr(cmp, nm, val);
	return true;
};

zkCal.onyearofs = function (evt, ofs) {
	var meta = zkau.getMeta($uuid(Event.element(evt)));
	if (meta) meta._onyearofs(ofs);
};
zkCal.onmonofs = function (evt, ofs) {
	var meta = zkau.getMeta($uuid(Event.element(evt)));
	if (meta) meta._onmonofs(ofs);
};
zkCal.onmonclk = function (evt) {
	var el = Event.element(evt);
	var meta = zkau.getMeta($uuid(el));
	if (meta) meta._onmonclk(el);
};
zkCal.ondayclk = function (evt) {
	var el = Event.element(evt);
	if ($tag(el) == "A") el = el.parentNode;
	var meta = zkau.getMeta($uuid(el));
	if (meta) meta._ondayclk(el);
};
zkCal.onup = function (evt) {
	var meta = zkau.getMeta($uuid(Event.element(evt)));
	if (meta && meta._changed) meta.onchange(); //delay onchange here to avoid too many reqs
	return true;
};
zkCal.onkey = function (evt) {
	if (!evt.altKey && evt.keyCode >= 37 && evt.keyCode <= 40) {
		var meta = zkau.getMeta($uuid(Event.element(evt)));
		if (meta) {
			ofs = evt.keyCode == 37 ? -1: evt.keyCode == 39 ? 1:
				evt.keyCode == 38 ? -7: 7;
			meta.shift(ofs);
			zk.focusDown(meta.element);
			meta._changed = true;
		}

		Event.stop(evt);
		return false;
	}
	return true;
};
zkCal.onblur = function (evt) {
	//onup is not called if onblur happens first
	var meta = zkau.getMeta($uuid(Event.element(evt)));
	if (meta && meta._changed) meta.onchange();
};

zkCal.onover = function (evt) {
	Event.element(evt).style.textDecoration = "underline";
};
zkCal.onout = function (evt) {
	Event.element(evt).style.textDecoration = "";
};

//Datebox//
zkDtbox = {};

zkDtbox.init = function (cmp) {
	if (!zkDtbox.onHide) zkDtbox.onHide = zkTxbox.onHide;
		//we cannot assign it until now, since widget.js might be loaded after this

	var inp = $real(cmp);
	zkTxbox.init(inp);
	zk.listen(inp, zk.ie ? "keydown": "keypress", zkDtbox.onkey);
		//IE: use keydown. otherwise, it causes the window to scroll

	var btn = $e(cmp.id + "!btn");
	if (btn) zk.listen(btn, "click", function () {if (!inp.disabled && !zk.dragging) zkDtbox.onbutton(cmp);});
	btn.align = "absmiddle";
};
zkDtbox.validate = function (cmp) {
	var inp = $e(cmp.id+"!real");
	if (inp.value) {
		var fmt = getZKAttr(cmp, "fmt");
		var d = zk.parseDate(inp.value, fmt, getZKAttr(cmp, "lenient") == "false");
		if (!d) return msgzul.DATE_REQUIRED+fmt;

		inp.value = zk.formatDate(d, fmt); //meta might not be ready
	}
	return null;
};

/** Handles setAttr. */
zkDtbox.setAttr = function (cmp, nm, val) {
	if ("z.fmt" == nm) {
		zkau.setAttr(cmp, nm, val);

		var inp = $real(cmp);
		if (inp) {
			var d = zk.parseDate(inp.value, val);
			if (d) inp.value = zk.formatDate(d, val);
		}
		return true;
	} else if ("style" == nm) {
		var inp = $real(cmp);
		if (inp) zkau.setAttr(inp, nm, zk.getTextStyle(val, true, true));
	} else if ("style.width" == nm) {
		var inp = $real(cmp);
		if (inp) {
			inp.style.width = val;
			return true;
		}
	} else if ("style.height" == nm) {
		var inp = $real(cmp);
		if (inp) {
			inp.style.height = val;
			return true;
		}
	} else if ("z.sel" == nm ) {
		return zkTxbox.setAttr(cmp, nm, val);
	} else if (zkDtbox._inflds.contains(nm)) {
		cmp = $real(cmp);
	}
	zkau.setAttr(cmp, nm, val);
	return true;
};
zkDtbox.rmAttr = function (cmp, nm) {
	if ("style" == nm) {
		var inp = $real(cmp);
		if (inp) zkau.rmAttr(inp, nm);
	} else if ("style.width" == nm) {
		var inp = $real(cmp);
		if (inp) inp.style.width = "";
	} else if ("style.height" == nm) {
		var inp = $real(cmp);
		if (inp) inp.style.height = "";
	} else if (zkDtbox._inflds.contains(nm))
		cmp = $real(cmp);
	zkau.rmAttr(cmp, nm);
	return true;
};
if (!zkDtbox._inflds)
	zkDtbox._inflds = ["name", "value", "defaultValue", "cols", "size",
		"maxlength", "type", "disabled", "readonly", "rows"];

zkDtbox.onkey = function (evt) {
	var inp = Event.element(evt);
	if (!inp) return true;

	var uuid = $uuid(inp.id);
	var pp = $e(uuid + "!pp");
	if (!pp) return true;

	var opened = pp.style.display != "none";
	if (evt.keyCode == 9) { //TAB; IE: close now to show covered SELECT
		if (opened) zkDtbox.close(pp);
		return true; //don't eat
	}

	if (evt.keyCode == 38 || evt.keyCode == 40) {//UP/DN
		if (evt.altKey) {
			if (evt.keyCode == 38) { //UP
				if (opened) zkDtbox.close(pp);
			} else {
				if (!opened) zkDtbox.open(pp);
			}
			//FF: if we eat UP/DN, Alt+UP degenerate to Alt (select menubar)
			if (zk.ie) {
				Event.stop(evt);
				return false;
			}
			return true;
		}
		if (!opened) {
			zkDtbox.open(pp);
			Event.stop(evt);
			return false;
		}
	}

	if (opened) {
		var meta = zkau.getMeta(uuid);
		if (meta) {
			//Request 1551019: better responsive
			if (evt.keyCode == 13) { //ENTER
				meta.onchange();
				return true;
			}

			var ofs = evt.keyCode == 37 ? -1: evt.keyCode == 39 ? 1:
				evt.keyCode == 38 ? -7: evt.keyCode == 40 ? 7: 0;
			if (ofs) {
				meta.shift(ofs);
				inp.value = meta.getDateString();
				zk.asyncSelect(inp.id);
				Event.stop(evt);
				return false;
			}
		}
	}
	return true;
};

/* Whn the button is clicked on button. */
zkDtbox.onbutton = function (cmp) {
	var pp = $e(cmp.id + "!pp");
	if (pp) {
		if (pp.style.display == "none") zkDtbox.open(pp);
		else zkDtbox.close(pp, true);
	}
};

zkDtbox.open = function (pp) {
	pp = $e(pp);
	zkau.closeFloats(pp); //including popups
	zkau._dtbox.setFloatId(pp.id);

	var uuid = $uuid(pp.id);
	var cb = $e(uuid);
	if (!cb) return;

	var meta = zkau.getMeta(cb);
	if (meta) meta.init();
	else zkau.setMeta(cb, new zk.Cal(cb, pp));

	pp.style.width = pp.style.height = "auto";
	pp.style.position = "absolute"; //just in case
	pp.style.overflow = "auto"; //just in case
	pp.style.display = "block";
	pp.style.zIndex = "80000";
	//No special child, so no need to: zk.onVisiAt(pp);

	if (zk.gecko) {
		setZKAttr(pp, "vparent", uuid); //used by zkTxbox._noonblur
		document.body.appendChild(pp); //Bug 1486840
	}

	//fix size
	if (pp.offsetHeight > 200) {
		pp.style.height = "200px";
		pp.style.width = "auto"; //recalc
	} else if (pp.offsetHeight < 10) {
		pp.style.height = "10px"; //minimal
	}
	if (pp.offsetWidth < cb.offsetWidth) {
		pp.style.width = cb.offsetWidth + "px";
	} else {
		var wd = zk.innerWidth() - 20;
		if (wd < cb.offsetWidth) wd = cb.offsetWidth;
		if (pp.offsetWidth > wd) pp.style.width = wd;
	}

	zk.position(pp, cb, "after-start");

	setTimeout("zkDtbox._repos('"+uuid+"')", 3);
		//IE issue: we have to re-position again because some dimensions
		//might not be correct here
};
/** Re-position the popup. */
zkDtbox._repos = function (uuid) {
	var cb = $e(uuid);
	if (!cb) return;

	var pp = $e(uuid + "!pp");
	var inpId = cb.id + "!real";
	var inp = $e(inpId);

	zk.position(pp, cb, "after-start");
	zkau.hideCovered();
	zk.asyncFocus(inpId);
};

zkDtbox.close = function (pp, focus) {
	var uuid = $uuid(pp.id);
	if (zk.gecko) {
		$e(uuid).appendChild(pp); //Bug 1486840
		rmZKAttr(pp, "vparent");
	}

	pp = $e(pp);
	zkau._dtbox.setFloatId(null);
	pp.style.display = "none";
	//No special child, so no need to: zk.onHideAt(pp);
	zkau.hideCovered();

	if (focus)
		zk.asyncFocus(uuid + "!real");
};

zk.FloatDatebox = Class.create();
Object.extend(Object.extend(zk.FloatDatebox.prototype, zk.Float.prototype), {
	_close: function (el) {
		zkDtbox.close(el);
	}
});
if (!zkau._dtbox)
	zkau.floats.push(zkau._dtbox = new zk.FloatDatebox()); //hook to zkau.js

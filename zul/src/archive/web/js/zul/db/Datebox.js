/* Datebox.js

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
(function () {
	function _initPopup () {
		this._pop = new zul.db.CalendarPop();
		this._tm = new zul.db.CalendarTime();
		this.appendChild(this._pop);
		this.appendChild(this._tm);
	}
	function _reposition(wgt) {
		var db = wgt.$n();
		if (!db) return;
		var pp = wgt.$n("pp"),
			inp = wgt.getInputNode();

		if(pp) {
			zk(pp).position(inp, "after_start");
			wgt._pop.syncShadow();
			zk(inp).focus();
		}
	}

zul.db.Datebox = zk.$extends(zul.inp.FormatWidget, {
	_buttonVisible: true,
	_lenient: true,
	$init: function() {
		this.$supers('$init', arguments);
		this.$afterInit(_initPopup);
		this.listen({onChange: this}, -1000);
	},

	$define: {
		buttonVisible: function (v) {
			var n = this.$n('btn');
			if (n) {
				v ? jq(n).show() : jq(n).hide();
				this.onSize();
			}
		},
		format: function () {
			if (this._pop) {
				this._pop.setFormat(this._format);
				if (this._value)
					this._value = this._pop.getTime();
			}
			var inp = this.getInputNode();
			if (inp)
				inp.value = this.coerceToString_(this._value);
		},
		constraint: function (cst) {
			if (typeof cst == 'string' && cst.charAt(0) != '['/*by server*/)
				this._cst = new zul.inp.SimpleDateConstraint(cst);
			else
				this._cst = cst;
			if (this._cst) delete this._lastRawValVld; //revalidate required
			if (this._pop) {
				this._pop.setConstraint(this._constraint);
				this._pop.rerender();
			}
		},
		timeZone: function (timezone) {
			this._timezone = timezone;
			this._setTimeZonesIndex();
		},
		timeZonesReadOnly: function (readonly) {
			this._tzonesReadOnly = readonly;
			var select = this.$n('dtzones');
			if (select) select.disabled = readonly ? "disabled" : "";
		},
		displayedTimeZones: function (dtzones) {
			this._dtzones = dtzones.split(",");
		},
		lenient: null
	},
	setValue: function (val) {
		var args;
		if (val) {
			args = [];
			for (var j = arguments.length; --j > 0;)
				args.unshift(arguments[j]);

			args.unshift((typeof val == 'string') ? this.coerceFromString_(val) : val);
		} else
			args = arguments;
		this.$supers('setValue', args);
	},
	_setTimeZonesIndex: function () {
		var select = this.$n('dtzones');
		if (select && this._timezone) {
			var opts = jq(select).children('option');
			for (var i = opts.length; i--;) {
				if (opts[i].text == this._timezone) select.selectedIndex = i;
			}
		}		
	},
	onSize: _zkf = function () {
		var width = this.getWidth();
		if (!width || width.indexOf('%') != -1)
			this.getInputNode().style.width = '';
		this.syncWidth();
		this._auxb.fixpos();
	},
	onShow: _zkf,
	getZclass: function () {
		var zcs = this._zclass;
		return zcs != null ? zcs: "z-datebox";
	},
	getRawText: function () {
		return this.coerceToString_(this._value);
	},
	getTimeFormat: function () {
		var fmt = this._format,
			aa = fmt.indexOf('a'),
			hh = fmt.indexOf('h'),
			KK = fmt.indexOf('K'),
			HH= fmt.indexOf('HH'),
			kk = fmt.indexOf('k'),
			mm = fmt.indexOf('m'),
			ss = fmt.indexOf('s'),
			hasAM = aa > -1,
			hasHour1 = hasAM ? hh > -1 || KK > -1 : false;

		if (hasHour1) {
			if ((hh != -1 && aa < hh) || (kk != -1 && aa < kk)) {
				var f = hh < KK ? 'a KK' : 'a hh';
				return f + (mm > -1 ? ':mm': '') + (ss > -1 ? ':ss': '');
			} else {
				var f = hh < KK ? 'KK' : 'hh';
				f = f + (mm > -1 ? ':mm': '') + (ss > -1 ? ':ss': '');
				return f + ' a';
			}
		} else {
			var f = HH < kk ? 'kk' : HH > -1 ? 'HH' : '';
			return f + (mm > -1 ? ':mm': '') + (ss > -1 ? ':ss': '');
		}
	},
	getDateFormat: function () {
		return this._format.replace(/[ahKHksm]/g, '');
	},
	setOpen: function(open) {
		var pp = this.$n("pp");
		if (pp) {
			if (!jq(pp).zk.isVisible()) this._pop.open();
			else this._pop.close();
		}
	},
	coerceFromString_: function (val) {
		if (val) {
			var d = zDateFormat.parseDate(val, this.getFormat(), !this._lenient);
			if (!d) return {error: zMsgFormat.format(msgzul.DATE_REQUIRED + this._format)};
			return d;
		} else
			return val;
	},
	coerceToString_: function (val) {
		return val ? zDateFormat.formatDate(val, this.getFormat()) : '';
	},
	getInputNode: function () {
		return this.$n('real');
	},
	syncWidth: function () {
		var node = this.$n();
		if (!zk(node).isRealVisible() || (!this._inplace && !node.style.width))
			return;
		
		if (this._buttonVisible && this._inplace) {
			if (!node.style.width) {
				var $n = jq(node),
					inc = this.getInplaceCSS();
				$n.removeClass(inc);
				if (zk.opera)
					node.style.width = jq.px(zk(node).revisedWidth(node.clientWidth) + zk(node).borderWidth());
				else
					node.style.width = jq.px(zk(node).revisedWidth(node.offsetWidth));
				$n.addClass(inc);
			}
		} 
		var width = zk.opera ? zk(node).revisedWidth(node.clientWidth) + zk(node).borderWidth()
							 : zk(node).revisedWidth(node.offsetWidth),
			btn = this.$n('btn'),
			inp = this.getInputNode();
		inp.style.width = jq.px(zk(inp).revisedWidth(width - (btn ? btn.offsetWidth : 0)));
	},
	doFocus_: function (evt) {
		var n = this.$n();
		if (this._inplace)
			n.style.width = jq.px(zk(n).revisedWidth(n.offsetWidth));
			
		this.$supers('doFocus_', arguments);

		if (this._inplace) {
			if (jq(n).hasClass(this.getInplaceCSS())) {
				jq(n).removeClass(this.getInplaceCSS());
				this.onSize();
			}
		}

		if (this._readonly && this._pop)
			this._pop.open();
	},
	doBlur_: function (evt) {
		var n = this.$n();
		if (this._inplace && this._inplaceout) {
			n.style.width = jq.px(zk(n).revisedWidth(n.offsetWidth));
		}
		this.$supers('doBlur_', arguments);
		if (this._inplace && this._inplaceout) {
			jq(n).addClass(this.getInplaceCSS());
			this.onSize();
			n.style.width = this.getWidth() || '';
		}
	},
	doKeyDown_: function (evt) {
		this._doKeyDown(evt);
		if (!evt.stopped)
			this.$supers('doKeyDown_', arguments);
	},
	_doKeyDown: function (evt) {
		var keyCode = evt.keyCode,
			bOpen = this._pop.isOpen();
		if (keyCode == 9 || (zk.safari && keyCode == 0)) { //TAB or SHIFT-TAB (safari)
			if (bOpen) this._pop.close();
			return;
		}

		if (evt.altKey && (keyCode == 38 || keyCode == 40)) {//UP/DN
			if (bOpen) this._pop.close();
			else this._pop.open();

			//FF: if we eat UP/DN, Alt+UP degenerate to Alt (select menubar)
			var opts = {propagation:true};
			if (zk.ie) opts.dom = true;
			evt.stop(opts);
			return;
		}

		//Request 1537962: better responsive
		if (bOpen && (keyCode == 13 || keyCode == 27)) { //ENTER or ESC
			if (keyCode == 13) this.enterPressed_(evt);
			else this.escPressed_(evt);
			return;
		}

		if (keyCode == 18 || keyCode == 27 || keyCode == 13
		|| (keyCode >= 112 && keyCode <= 123)) //ALT, ESC, Enter, Fn
			return; //ignore it (doc will handle it)
		
		if (this._pop.isOpen()) {
			var ofs = keyCode == 37 ? -1 : keyCode == 39 ? 1 : keyCode == 38 ? -7 : keyCode == 40 ? 7 : 0;
			if (ofs)
				this._pop._shift(ofs);
		}
	},
	enterPressed_: function (evt) {
		this._pop.close();
		this.updateChange_();
		evt.stop();
	},
	escPressed_: function (evt) {
		this._pop.close();
		evt.stop();
	},
	afterKeyDown_: function (evt) {
		if (this._inplace)
			jq(this.$n()).toggleClass(this.getInplaceCSS(),  evt.keyCode == 13 ? null : false);
			
		this.$supers('afterKeyDown_', arguments);
	},
	bind_: function (){
		this.$supers('bind_', arguments);
		var btn = this.$n('btn'),
			inp = this.getInputNode(),
			pp = this.$n('pp');
			
		if (this._inplace)
			jq(inp).addClass(this.getInplaceCSS());
			
		if (btn) {
			this._auxb = new zul.Auxbutton(this, btn, inp);
			this.domListen_(btn, 'onClick', '_doBtnClick');
		}
		

		if (pp && this._dtzones) {
			var html = ['<div class="', this.getZclass(), '-timezone>"'];
			if (this._dtzones) html.push(this.getTimeZoneLabel());
			html.push('<select id="', this.uuid, '-dtzones" class="', this.getZclass(), '-timezone-body">'); 
			if (pp && this._dtzones) {
				for (var i = 0, len = this._dtzones.length; i < len; i++)
					html.push('<option value="', this._dtzones[i], '" class="', this.getZclass(), '-timezone-item">', this._dtzones[i], '</option>');
			}
			html.push('</select><div>');
			jq(pp).append(html.join(''));
			var select = this.$n('dtzones');
			if (select) {
				select.disabled = this._tzonesReadOnly ? "disable" : "";
				this.domListen_(select, 'onChange', '_doTimeZoneChange');
	 			this._setTimeZonesIndex();
			}			
		}
			
		this.syncWidth();
		
		zWatch.listen({onSize: this, onShow: this});
		this._pop.setFormat(this.getDateFormat());
	},
	unbind_: function () {
		var btn = this.$n('btn'),
			select = this.$n('dtzones');
		if (btn) {
			this._auxb.cleanup();
			this._auxb = null;
			this.domUnlisten_(btn, 'onClick', '_doBtnClick');
		}
		if (select)
			this.domUnlisten_(select, 'onChange', '_doTimeZoneChange');
			
		zWatch.unlisten({onSize: this, onShow: this});
		this.$supers('unbind_', arguments);
	},
	_doBtnClick: function (evt) {
		if (!this._disabled)
			this.setOpen();
		evt.stop();
	},
	_doTimeZoneChange: function (evt) {
		var select = this.$n('dtzones'),
			timezone = select.value;
		if (!this.getValue()) {
			this.setValue(this._tm.getValue());
		}
		this.updateChange_();
		this.fire("onTimeZoneChange", {timezone: timezone}, {toServer:true}, 150);
		if (this._pop) this._pop.close();
	},
	onChange: function (evt) {
		if (this._pop)
			this._pop._value = evt.data.value;
	},
	getTimeZoneLabel: function () {
		return "";
	},

	redrawpp_: function (out) {
		out.push('<div id="', this.uuid, '-pp" class="', this.getZclass(),
			'-pp" style="display:none" tabindex="-1">');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</div>');
	}
});

zul.db.CalendarPop = zk.$extends(zul.db.Calendar, {
	$init: function () {
		this.$supers('$init', arguments);
		this.listen({onChange: this}, -1000);
	},
	setFormat: function (fmt) {
		if (fmt != this._fmt) {
			var old = this._fmt;
			this._fmt = fmt;
			if (this.getValue())
				this._value = zDateFormat.formatDate(zDateFormat.parseDate(this.getValue(), old), fmt);
		}
	},
	rerender: function () {
		this.$supers('rerender', arguments);
		if (this.desktop) this.syncShadow();
	},
	close: function (silent) {
		var db = this.parent,
			pp = db.$n("pp");

		if (!pp || !zk(pp).isVisible()) return;
		if (this._shadow) this._shadow.hide();

		var zcls = db.getZclass();
		pp.style.display = "none";
		pp.className = zcls + "-pp";

		jq(pp).zk.undoVParent();

		var btn = this.$n("btn");
		if (btn)
			jq(btn).removeClass(zcls + "-btn-over");

		if (!silent)
			jq(db.getInputNode()).focus();
	},
	isOpen: function () {
		return zk(this.parent.$n("pp")).isVisible();
	},
	open: function() {
		var wgt = this.parent,
			db = wgt.$n(), pp = wgt.$n("pp");
		if (!db || !pp)
			return;
		var zcls = wgt.getZclass();

		pp.className = db.className + " " + pp.className;
		jq(pp).removeClass(zcls);

		pp.style.width = pp.style.height = "auto";
		pp.style.position = "absolute"; //just in case
		pp.style.overflow = "auto"; //just in case
		pp.style.display = "block";
		pp.style.zIndex = "88000";

		//FF: Bug 1486840
		//IE: Bug 1766244 (after specifying position:relative to grid/tree/listbox)
		jq(pp).zk.makeVParent();

		if (pp.offsetHeight > 200) {
			//pp.style.height = "200px"; commented by the bug #2796461
			pp.style.width = "auto"; //recalc
		} else if (pp.offsetHeight < 10) {
			pp.style.height = "10px"; //minimal
		}
		if (pp.offsetWidth < db.offsetWidth) {
			pp.style.width = db.offsetWidth + "px";
		} else {
			var wd = jq.innerWidth() - 20;
			if (wd < db.offsetWidth)
				wd = db.offsetWidth;
			if (pp.offsetWidth > wd)
				pp.style.width = wd;
		}
		zk(pp).position(wgt.getInputNode(), "after_start");
		setTimeout(function() {
			_reposition(wgt);
		}, 150);
		//IE, Opera, and Safari issue: we have to re-position again because some dimensions
		//in Chinese language might not be correct here.
		var fmt = wgt.getTimeFormat(),
			value = wgt.getValue() || new Date();
		if (fmt) {
			var tm = wgt._tm;
			tm.setVisible(true);
			tm.setFormat(fmt);
			tm.setValue(value);
			tm.onShow();
		} else {
			wgt._tm.setVisible(false);
		}

	},
	syncShadow: function () {
		if (!this._shadow)
			this._shadow = new zk.eff.Shadow(this.parent.$n('pp'), {
				left: -4,
				right: 4,
				top: 2,
				bottom: 3,
				stackup: (zk.useStackup === undefined ? zk.ie6_ : zk.useStackup)
			});
		this._shadow.sync();
	},
	onChange: function (evt) {
		var date = this.getTime(),
			oldDate = this.parent.getValue();
		if (oldDate) {
			oldDate.setFullYear(date.getFullYear());
			oldDate.setMonth(date.getMonth());
			oldDate.setDate(date.getDate());
		} else
			this.parent._value = date;
		this.parent.getInputNode().value = evt.data.value = this.parent.getRawText();
		this.parent.fire(evt.name, evt.data);
		if (this._view == 'day' && evt.data.shallClose !== false) {
			this.close();
			this.parent._inplaceout = true;
		}
		this.parent.focus();
		evt.stop();
	},
	onFloatUp: function (ctl) {
		if (!zUtl.isAncestor(this.parent, ctl.origin))
			this.close(true);
	},
	bind_: function () {
		this.$supers('bind_', arguments);
		zWatch.listen({onFloatUp: this});
	},
	unbind_: function () {
		zWatch.unlisten({onFloatUp: this});

		if (this._shadow) {
			this._shadow.destroy();
			this._shadow = null;
		}
		this.$supers('unbind_', arguments);
	},
	_setView: function (val) {
		if (this.parent.getTimeFormat())
			this.parent._tm.setVisible(val == 'day');
		this.$supers('_setView', arguments);
	},
	_choiceData: function (evt) {
		var target = evt.domTarget;
		target = target.tagName == "TD" ? target : target.parentNode;
		if (target && jq(target).hasClass(this.getZclass() + '-disd')) {
			this.close();
		} else
			this.$supers('_choiceData', arguments);
	}
});
zul.db.CalendarTime = zk.$extends(zul.inp.Timebox, {
	$init: function () {
		this.$supers('$init', arguments);
		this.listen({onChanging: this}, -1000);
	},
	onChanging: function (evt) {
		var date = this.coerceFromString_(evt.data.value),
			oldDate = this.parent.getValue();
		if (oldDate) {
			oldDate.setHours(date.getHours());
			oldDate.setMinutes(date.getMinutes());
			oldDate.setSeconds(date.getSeconds());
		} else
			this.parent._value = date;
		this.parent.getInputNode().value = evt.data.value = this.parent.getRawText();
		this.parent.fire(evt.name, evt.data);
		if (this._view == 'day' && evt.data.shallClose !== false) {
			this.close();
			this.parent._inplaceout = true;
		}
		evt.stop();
	}
});

})();

/* ComboWidget.js

	Purpose:
		
	Description:
		
	History:
		Tue Mar 31 14:15:39     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.inp.ComboWidget = zk.$extends(zul.inp.InputWidget, {
	_btnVisible: true,

	isButtonVisible: function () {
		return this._btnVisible;
	},
	setButtonVisible: function (btnVisible) {
		if (this._btnVisible != btnVisible) {
			this._btnVisible = btnVisible;
			var n = this.getSubnode('btn');
			if (n)
				btnVisible ? zDom.show(n): zDom.hide(n);
		}
	},
	isAutodrop: function () {
		return this._autodrop;
	},
	setAutodrop: function (autodrop) {
		this._autodrop = autodrop;
	},

	onSize: _zkf = function () {
		this._auxb.fixpos();
	},
	onShow: _zkf,
	onFloatUp: function (wgt) {
		//TODO
	},

	isOpen: function () {
		return this._open;
	},
	open: function (opts) {
		if (this._open) return;
		this._open = true;
		if (opts && opts.focus)
			this.focus();

		var pp = this.getSubnode('pp');
		if (!pp) return;

		zWatch.fire('onFloatUp', null, this); //notify all
		this.setTopmost();

		var ppofs = this.getPopupSize_(pp);
		pp.style.width = ppofs[0];
		pp.style.height = "auto";
		pp.style.zIndex = 88000; //on-top of everything

		var pp2 = this.getSubnode("cave");
		if (pp2) pp2.style.width = pp2.style.height = "auto";

		pp.style.position = "absolute"; //just in case
		pp.style.display = "block";

		// throw out
		pp.style.visibility = "hidden";
		pp.style.left = "-10000px";

		//FF: Bug 1486840
		//IE: Bug 1766244 (after specifying position:relative to grid/tree/listbox)
		//NOTE: since the parent/child relation is changed, new listitem
		//must be inserted into the popup (by use of uuid!child) rather
		//than invalidate!!
		zDom.makeVParent(pp);

		// throw in
		pp.style.left = "";
		this._fixsz(ppofs);//fix size
		zDom.position(pp, this.getInputNode(), "after_start");
		pp.style.display = "none";
		pp.style.visibility = "";

		zAnima.slideDown(this, pp, {afterAnima: this._afterSlideDown});

		if (!opts || !opts.silent) {
			var n = this.getSubnode('real');
			if (n) n = n.value;
			this.fire('onOpen', {open:true, value: n||''});
		}
	},
	_afterSlideDown: function (n) {
		zWatch.fireDown("onShow", {visible:true}, this);
	},
	close: function (opts) {
		if (!this._open) return;
		this._open = false;
		if (opts && opts.focus)
			this.focus();

		var pp = this.getSubnode('pp');
		if (!pp) return;

		pp.style.display = "none";
		zDom.undoVParent(pp);
		if (this._shadow) {
			this._shadow.destroy();
			this._shadow = null;
		}
		var n = this.getSubnode('btn');
		if (n) zDom.rmClass(n, this.getZclass() + '-btn-over');

		if (!opts || !opts.silent) {
			n = this.getSubnode('real');
			if (n) n = n.value;
			this.fire('onOpen', {open:false, value: n||''});
		}

		zWatch.fireDown("onHide", {visible:true}, this);
	},
	_fixsz: function (ppofs) {
		var pp = this.getSubnode('pp');
		if (!pp) return;

		var pp2 = this.getSubnode('cave');
		if (ppofs[1] == "auto" && pp.offsetHeight > 250) {
			pp.style.height = "250px";
		} else if (pp.offsetHeight < 10) {
			pp.style.height = "10px"; //minimal
		}

		if (ppofs[0] == "auto") {
			var cb = this.getNode();
			if (pp.offsetWidth < cb.offsetWidth) {
				pp.style.width = cb.offsetWidth + "px";
				if (pp2) pp2.style.width = "100%";
					//Note: we have to set width to auto and then 100%
					//Otherwise, the width is too wide in IE
			} else {
				var wd = zk.innerWidth() - 20;
				if (wd < cb.offsetWidth) wd = cb.offsetWidth;
				if (pp.offsetWidth > wd) pp.style.width = wd;
			}
		}
	},

	downPressed_: zk.$void, //function (evt) {}
	upPressed_: zk.$void, //function (evt) {}
	/** Returns [width, height] for the popup if specified by user.
	 * Default: ['auto', 'auto']
	 */
	getPopupSize_: function (pp) {
		return ['auto', 'auto'];
	},

	//super
	getInputNode: function () {
		return this.getSubnode('real');
	},
	bind_: function () {
		this.$supers('bind_', arguments);

		var btn = this.getSubnode('btn'),
			inp = this.getInputNode();
		if (btn) {
			this._auxb = new zul.Auxbutton(this, btn, inp);
			zEvt.listen(btn, 'click', this.proxy(this._domBtn, '_pxBtn'));
		}
		zWatch.listen('onSize', this);
		zWatch.listen('onShow', this);
		zWatch.listen('onFloatUp', this);
	},
	unbind_: function () {
		var btn = this.getSubnode('btn');
		if (btn) {
			this._auxb.cleanup();
			this._auxb = null;
			zEvt.unlisten(btn, 'click', this._pxBtn);
		}

		zWatch.unlisten('onSize', this);
		zWatch.unlisten('onShow', this);
		zWatch.unlisten('onFloatUp', this);


		this.$supers('unbind_', arguments);
	},
	_domBtn: function (devt) {
		if (this._open) this.close({focus:true});
		else this.open({focus:true});
		zEvt.stop(devt);
	},
	doKeyDown_: function (evt) {
		this._doKeyDown(evt);
		if (!evt.stopped)
			this.$supers('doKeyDown_', arguments);
	},
	_doKeyDown: function (evt) {
		var keyCode = evt.keyCode,
			bOpen = this._open;
		if (keyCode == 9 || (zk.safari && keyCode == 0)) { //TAB or SHIFT-TAB (safari)
			if (bOpen) this.close();
			return;
		}

		if (evt.altKey && (keyCode == 38 || keyCode == 40)) {//UP/DN
			if (bOpen) this.close();
			else this.open();

			//FF: if we eat UP/DN, Alt+UP degenerate to Alt (select menubar)
			var opts = {propagation:true};
			if (zk.ie) opts.dom = true;
			evt.stop(opts);
			return;
		}

		//Request 1537962: better responsive
		if (bOpen && keyCode == 13) { //ENTER
			var item = this._autoselback(); //Better usability(Bug 1633335): auto selback
			if (item && this._selId != item.uuid) {
				this._selId = item.uuid;
				this.fire('onSelect', zk.copy({
					items: [item], reference: item
				}, zEvt.filterMetaData(evt)));
			}
			this.updateChange_(); //fire onChange
			return;
		}

		if (keyCode == 18 || keyCode == 27 || keyCode == 13
		|| (keyCode >= 112 && keyCode <= 123)) //ALT, ESC, Enter, Fn
			return; //ignore it (doc will handle it)

		if (keyCode == 38) this.upPressed_();
		else if (keyCode == 40) this.downPressed_();
	}
});
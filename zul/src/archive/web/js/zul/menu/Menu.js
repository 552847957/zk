/* Menu.js

	Purpose:

	Description:

	History:
		Thu Jan 15 09:02:33     2009, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.menu.Menu = zk.$extends(zul.LabelImageWidget, {
	domContent_: function () {
		var label = zUtl.encodeXML(this.getLabel()),
			img = '<span class="' + this.getZclass() + '-img"' +
				(this._image ? ' style="background-image:url(' + this._image + ')"' : '')
				+ '></span>';
		return label ? img + ' ' + label: img;
	},
	isTopmost: function () {
		return this._topmost;
	},
	beforeParentChanged_: function (newParent) {
		this._topmost = newParent && !(newParent.$instanceof(zul.menu.Menupopup));
	},
	getZclass: function () {
		return this._zclass == null ? "z-menu" : this._zclass;
	},
	domStyle_: function (no) {
		var style = this.$supers('domStyle_', arguments);
		return this.isTopmost() ?
			style + 'padding-left:4px;padding-right:4px;': style;
	},
	onChildAdded_: function (child) {
		this.$supers('onChildAdded_', arguments);
		if (child.$instanceof(zul.menu.Menupopup))
			this.menupopup = child;
	},
	onChildRemoved_: function (child) {
		this.$supers('onChildRemoved_', arguments);
		if (child == this.menupopup)
			this.menupopup = null;
	},
	getMenubar: function () {
		for (var p = this.parent; p; p = p.parent)
			if (p.$instanceof(zul.menu.Menubar))
				return p;
		return null;
	},
	/** Removes the extra space (IE only) */
	_fixBtn: function () {
		var btn = this.getSubnode('b');
		if (btn) {
			var txt = btn.innerHTML, $btn = zk(btn);
			btn.style.width = ($btn.textSize(txt)[0] + $btn.padBorderWidth()) + "px";
		}
	},
	bind_: function () {
		this.$supers('bind_', arguments);

		if (!this.isTopmost()) {
			var anc = this.getSubnode('a'),
				n = this.getNode();
			this.domListen_(anc, "onFocus", "doFocus_");
			this.domListen_(anc, "onBlur", "doBlur_");
			this.domListen_(n, "onMouseOver");
			this.domListen_(n, "onMouseOut");
		} else {
			if (zk.ie) this._fixBtn();

			var anc = this.getSubnode('a');
			this.domListen_(anc, "onMouseOver");
			this.domListen_(anc, "onMouseOut");
		}
	},
	unbind_: function () {
		if (!this.isTopmost()) {
			var anc = this.getSubnode('a'),
				n = this.getNode();
			this.domUnlisten_(anc, "onFocus", "doFocus_");
			this.domUnlisten_(anc, "onBlur", "doBlur_");
			this.domUnlisten_(n, "onMouseOver");
			this.domUnlisten_(n, "onMouseOut");
		} else {
			var anc = this.getSubnode('a');
			this.domUnlisten_(anc, "onMouseOver");
			this.domUnlisten_(anc, "onMouseOut");
		}

		this.$supers('unbind_', arguments);
	},
	doClick_: function (evt) {
		var $a = zk(this.getSubnode('a'));
		if (this.isTopmost() && !$a.isAncestor(evt.domTarget)) return;

		$a.jq.addClass(this.getZclass() + '-body-seld');
		if (this.menupopup) {
			this.menupopup._shallClose = false;
			if (this.isTopmost())
				this.getMenubar()._lastTarget = this;
			if (!this.menupopup.isOpen()) this.menupopup.open();
		}
		this.fireX(evt);
	},
	_doMouseOver: function (evt) { //not zk.Widget.doMouseOver_
		if (this.$class._isActive(this)) return;

		var	topmost = this.isTopmost();
		if (topmost && zk.ie && !zk(this.getSubnode('a')).isAncestor(evt.domTarget))
				return; // don't activate

		this.$class._addActive(this);
		if (!topmost) {
			if (this.menupopup) this.menupopup._shallClose = false;
			zWatch.fire('onFloatUp', null, this); //notify all
			if (this.menupopup && !this.menupopup.isOpen()) this.menupopup.open();
		} else {
			var menubar = this.getMenubar();
			if (this.menupopup && menubar.isAutodrop()) {
				menubar._lastTarget = this;
				this.menupopup._shallClose = false;
				zWatch.fire('onFloatUp', null, this); //notify all
				if (!this.menupopup.isOpen()) this.menupopup.open();
			} else {
				var target = menubar._lastTarget;
				if (target && target != this && menubar._lastTarget.menupopup
						&& menubar._lastTarget.menupopup.isVisible()) {
					menubar._lastTarget.menupopup.close({sendOnOpen:true});
					this.$class._rmActive(menubar._lastTarget);
					menubar._lastTarget = this;
					if (this.menupopup) this.menupopup.open();
				}
			}
		}
	},
	_doMouseOut: function (evt) { //not zk.Widget.doMouseOut_
		if (zk.ie) {
			var n = this.getSubnode('a'),
				xy = zk(n).revisedOffset(),
				x = evt.pageX,
				y = evt.pageY,
				diff = this.isTopmost() ? 1 : 0,
				vdiff = this.isTopmost() && 'vertical' == this.parent.getOrient() ? 1 : 0;
			if (x - diff > xy[0] && x <= xy[0] + n.offsetWidth && y - diff > xy[1] &&
					y - vdiff <= xy[1] + n.offsetHeight)
				return; // don't deactivate;
		}
		var	topmost = this.isTopmost();
		if (topmost) {
			if (this.menupopup && this.getMenubar().isAutodrop()) {
				this.$class._rmActive(this);
				if (this.menupopup.isOpen()) this.menupopup._shallClose = true;
				zWatch.fire('onFloatUp', {
					timeout: 10
				}, this); //notify all
			}
		} else if (this.menupopup && !this.menupopup.isOpen())
			this.$class._rmActive(this);
	}
}, {
	_isActive: function (wgt) {
		var top = wgt.isTopmost(),
			n = top ? wgt.getSubnode('a') : wgt.getNode(),
			cls = wgt.getZclass() + (top ? '-body-over' : '-over');
		return jq(n).hasClass(cls);
	},
	_addActive: function (wgt) {
		var top = wgt.isTopmost(),
			n = top ? wgt.getSubnode('a') : wgt.getNode(),
			cls = wgt.getZclass() + (top ? '-body-over' : '-over');
		jq(n).addClass(cls);
		if (!top && wgt.parent.parent.$instanceof(zul.menu.Menu))
			this._addActive(wgt.parent.parent);
	},
	_rmActive: function (wgt) {
		var top = wgt.isTopmost(),
			n = top ? wgt.getSubnode('a') : wgt.getNode(),
			cls = wgt.getZclass() + (top ? '-body-over' : '-over');
		jq(n).removeClass(cls);
	}
});

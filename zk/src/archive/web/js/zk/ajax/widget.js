/* widget.js

	Purpose:
		Widget - the UI object at the client
	Description:
		
	History:
		Tue Sep 30 09:23:56     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zk.Widget = zk.$extends(zk.Object, {
	_visible: true,

	$init: function (uuid, mold) {
		this._lsns = {}; //listeners Map(evtnm,listener)
		this._$lsns = {}; //listners registered by server Map(evtnm, fn)
		this.uuid = uuid ? uuid: zk.Widget.nextUuid();
		this._mold = mold ? mold: "default";
	},

	getMold: function () {
		return this._mold;
	},
	setMold: function (mold) {
		if (mold != this._mold) {
			if (!this.$class.molds[mold])
				throw 'Unknown mold: ' + mold;
			this._mold = mold;
			var n = this.node;
			if (n) this.rerender();
		}
	},

	getSpaceOwner: function () {
		for (var w = this; w; w = w.parent)
			if (w._fellows) return w;
		return null;
	},
	getFellow: function (id) {
		var ow = this.getSpaceOwner();
		return ow != null ? ow._fellows[id]: null;
	},
	getId: function () {
		return this._id;
	},
	setId: function (id) {
		var ow = this.getSpaceOwner();
		var old = this._id;
		if (ow && old) delete ow._fellows[id];
		this._id = id;
		if (ow && id) ow._fellows[id] = this;
	},
	_addIdSpaceDown: function (spowner, wgt) {
		if (wgt._id)
			spowner._fellows[wgt._id] = wgt;
		if (!wgt._fellows)
			for (wgt = wgt.firstChild; wgt; wgt = wgt.nextSibling)
				this._addIdSpaceDown(spowner, wgt);
	},

	appendChild: function (child) {
		if (child == this.lastChild)
			return false;

		if (child.parent != this)
			child.beforeParentChange_(this);

		if (child.parent) {
			if (this._moveChild(child)) return true; //done
			child.parent.removeChild(child);
		}

		child.parent = this;
		var p = this.lastChild;
		if (p) {
			p.nextSibling = child;
			child.previousSibling = p;
			this.lastChild = child;
		} else {
			this.firstChild = this.lastChild = child;
		}

		var spowner = this.getSpaceOwner();
		if (spowner != null)
			this._addIdSpaceDown(spowner, child);

		var dt = this.desktop;
		if (dt) this.insertChildHTML_(child, null, dt);	
		return true;
	},
	insertBefore: function (child, sibling) {
		if (!sibling || sibling.parent != this)
			return this.appendChild(child);

		if (child == sibling || child.nextSibling == sibling)
			return false;

		if (child.parent != this)
			child.beforeParentChange_(this);

		if (child.parent) {
			if (this._moveChild(child, sibling)) return true;
			child.parent.removeChild(child);
		}

		child.parent = this;
		var p = sibling.previousSibling;
		if (p) {
			child.previousSibling = p;
			p.nextSibling = child;
		} else this.firstChild = child;

		sibling.previousSibling = child;
		child.nextSibling = sibling;

		var spowner = this.getSpaceOwner();
		if (spowner != null)
			this._addIdSpaceDown(spowner, child);

		var dt = this.desktop;
		if (dt) this.insertChildHTML_(child, sibling, dt);
		return true;
	},
	removeChild: function (child) {
		if (!child.parent)
			return false;
		if (this != child.parent)
			return false;

		child.beforeParentChange_(null);

		var p = child.previousSibling, n = child.nextSibling;
		if (p) p.nextSibling = n;
		else this.firstChild = n;
		if (n) n.previousSibling = p;
		else this.lastChild = p;
		child.nextSibling = child.previousSibling = child.parent = null;

		if (child.desktop)
			this.removeChildHTML_(child, p);
		return true;
	},
	beforeParentChange_: function () {
	},
	domMovable_: function () {
		return true;
	},
	_moveChild: function (child, moveBefore) {
		var node, kidnode; 
		if (child._floating || !child.domMovable_() || !this.domMovable_()
		|| !(node = this.node) || !(kidnode = child.node))
			return false;

		var dt = this.desktop, kiddt = child.desktop;
		child.node = this.node = child.desktop = this.desktop = null; //to avoid bind_ and unbind_
		try {
			child.parent.removeChild(child);
			this.insertBefore(child, moveBefore);

			zDom.remove(kidnode);
			node.parentNode.insertBefore(kidnode, moveBefore ? moveBefore.node: null);
		} finally {
			this.desktop = dt; child.desktop = kiddt;
			this.node = node; child.node = kidnode;
		}
		return true;
	},

	isRealVisible: function () {
		for (var wgt = this; wgt; wgt = wgt.parent) {
			if (!wgt.isVisible()) return false;
			var n = wgt.node;
			if (n && !zDom.isVisible(n)) return false; //possible (such as in a hbox)
		}
		return true;
	},
	isVisible: function () {
		return this._visible;
	},
	setVisible: function (visible, fromServer) {
		if (this._visible != visible) {
			this._visible = visible;

			var p = this.parent;
			if (p && visible) p.onChildVisible_(this, true); //becoming visible
			if (this.node) this._setVisible(visible);
			if (p && !visible) p.onChildVisible_(this, false); //become invisible
		}
	},
	_setVisible: function (visible) {
		var parent = this.parent,
			parentVisible = !parent || parent.isRealVisible(),
			node = this.node,
			floating = this._floating;

		if (!parentVisible) {
			if (!floating) this.setDomVisible_(node, visible);
			return;
		}

		if (visible) {
			var zi;
			if (floating)
				this._setZIndex(zi = this._topZIndex(), true);

			this.setDomVisible_(node, true);

			//from parent to child
			for (var fs = zk.Widget._floating, j = 0, fl = fs.length; j < fl; ++j) {
				var w = fs[j].widget;
				if (this != w && this._floatVisibleDependent(w)) {
					zi = zi >= 0 ? ++zi: w._topZIndex();
					var n = fs[j].node;
					if (n != w.node) w.setFloatZIndex_(n, zi); //only a portion
					else w._setZIndex(zi, true);

					w.setDomVisible_(n, true, {visibility:1});
				}
			}

			zWatch.fireDown('onVisible', -1, this);
		} else {
			zWatch.fireDown('onHide', -1, this);

			for (var fs = zk.Widget._floating, j = fs.length; --j >= 0;) {
				var w = fs[j].widget;
				if (this != w && this._floatVisibleDependent(w))
					w.setDomVisible_(fs[j].node, false, {visibility:1});
			}

			this.setDomVisible_(node, false);
		}
	},
	/** Returns if the specified widget's visibility depends this widget. */
	_floatVisibleDependent: function (wgt) {
		for (; wgt; wgt = wgt.parent)
			if (wgt == this) return true;
			else if (!wgt.isVisible()) break;
		return false;
	},
	setDomVisible_: function (n, visible, opts) {
		if (!opts || opts.display)
			n.style.display = visible ? '': 'none';
		if (opts && opts.visibility)
			n.style.visibility = visible ? 'visible': 'hidden';
	},
	onChildVisible_: function (child, visible) {
	},
	setTopmost: function () {
		var n = this.node;
		if (n && this._floating) {
			var zi = this._topZIndex();
			this._setZIndex(zi, true);

			for (var fs = zk.Widget._floating, j = 0, fl = fs.length; j < fl; ++j) {
				var w = fs[j].widget;
				if (this != w && zUtl.isAncestor(this, w) && w.isVisible()) {
					var n = fs[j].node
					if (n != w.node) w.setFloatZIndex_(n, ++zi); //only a portion
					else w._setZIndex(++zi, true);
				}
			}
		}
	},
	/** Returns the topmost z-index for this widget.*/
	_topZIndex: function () {
		var zi = 0;
		for (var fs = zk.Widget._floating, j = fs.length; --j >= 0;) {
			var w = fs[j].widget;
			if (w._zIndex >= zi && !zUtl.isAncestor(this, w) && w.isVisible())
				zi = w._zIndex + 1;
		}
		return zi;
	},
	isFloating_: function () {
		return this._floating;
	},
	setFloating_: function (floating, opts) {
		if (this._floating != floating) {
			var $Widget = zk.Widget;
			if (floating) {
				var inf = {widget: this, node: opts && opts.node? opts.node: this.node};
				$Widget._floating.$add(inf, $Widget._floatOrder); //parent first
				this._floating = true;
			} else {
				for (var fs = $Widget._floating, j = fs.length; --j >= 0;)
					if (fs[j].widget == this)
						fs.$removeAt(j);
				this._floating = false;
			}
		}
	},

	getWidth: function () {
		return this._width;
	},
	setWidth: function (width) {
		this._width = width;
		var n = this.node;
		if (n) n.style.width = width ? width: '';
	},
	getHeight: function () {
		return this._height;
	},
	setHeight: function (height) {
		this._height = height;
		var n = this.node;
		if (n) n.style.height = height ? height: '';
	},
	getZIndex: _zkf = function () {
		return this._zIndex;
	},
	getZindex: _zkf,
	setZIndex: _zkf = function (zIndex) { //2nd arg is fromServer
		this._setZIndex(zIndex);
	},
	setZindex: _zkf,
	_setZIndex: function (zIndex, fire) {
		if (this._zIndex != zIndex) {
			this._zIndex = zIndex;
			var n = this.node;
			if (n) {
				n.style.zIndex = zIndex >= 0 ? zIndex: '';
				if (fire) this.fire('onZIndex', zIndex, {ignorable: true});
			}
		}
	},
	getLeft: function () {
		return this._left;
	},
	setLeft: function (left) {
		this._left = left;
		var n = this.node;
		if (n) n.style.left = left ? left: '';
	},
	getTop: function () {
		return this._top;
	},
	setTop: function (top) {
		this._top = top;
		var n = this.node;
		if (n) n.style.top = top ? top: '';
	},
	getTooltiptext: function () {
		return this._tooltiptext;
	},
	setTooltiptext: function (tooltiptext) {
		this._tooltiptext = tooltiptext;
		var n = this.node;
		if (n) n.title = tooltiptext ? tooltiptext: '';
	},

	getStyle: function () {
		return this._style;
	},
	setStyle: function (style) {
		if (this._style != style) {
			this._style = style;
			if (this.node)
				this.updateDomStyle_();
		}
	},
	getSclass: function () {
		return this._sclass;
	},
	setSclass: function (sclass) {
		if (this._sclass != sclass) {
			this._sclass = sclass;
			this.updateDomClass_();
		}
	},
	getZclass: function () {
		return this._zclass;
	},
	setZclass: function (zclass) {
		if (this._zclass != zclass) {
			this._zclass = zclass;
			this.updateDomClass_();
		}
	},

	redraw: function (skipper) {
		var s = this.$class.molds[this._mold].call(this, skipper);
		return this.prolog ? this.prolog + s: s;
	},
	updateDomClass_: function () {
		var n = this.node;
		if (n) n.className = this.domClass_();
	},
	updateDomStyle_: function () {
		zDom.setStyle(this.node, zDom.parseStyle(this.domStyle_()));
	},

	domStyle_: function (no) {
		var style = '';
		if (!this.isVisible() && (!no || !no.visible))
			style = 'display:none;';
		if (!no || !no.style) {
			var s = this.getStyle(); 
			if (s) {
				style += s;
				if (s.charAt(s.length - 1) != ';') style += ';';
			}
		}
		if (!no || !no.width) {
			var s = this.getWidth();
			if (s) style += 'width:' + s + ';';
		}
		if (!no || !no.height) {
			var s = this.getHeight();
			if (s) style += 'height:' + s + ';';
		}
		if (!no || !no.left) {
			var s = this.getLeft();
			if (s) style += 'left:' + s + ';';
		}
		if (!no || !no.top) {
			var s = this.getTop();
			if (s) style += 'top:' + s + ';';
		}
		if (!no || !no.zIndex) {
			var s = this.getZIndex();
			if (s >= 0) style += 'z-index:' + s + ';';
		}
		return style;
	},
	domClass_: function (no) {
		var scls = '';
		if (!no || !no.sclass) {
			var s = this.getSclass();
			if (s) scls = s;
		}
		if (!no || !no.zclass) {
			var s = this.getZclass();
			if (s) scls += (scls ? ' ': '') + s;
		}
		return scls;
	},
	domAttrs_: function (no) {
		var html = !no || !no.id ? ' id="' + this.uuid + '"': '';
		if (!no || !no.domStyle) {
			var s = this.domStyle_();
			if (s) html += ' style="' + s + '"';
		}
		if (!no || !no.domclass) {
			var s = this.domClass_();
			if (s) html += ' class="' + s + '"';
		}
		if (!no || !no.tooltiptext) {
			var s = this.getTooltiptext();
			if (s) html += ' title="' + s + '"';
		}
		return html;
	},

	replaceHTML: function (n, desktop, skipper) {
		if (!desktop) desktop = this.desktop;

		var cf = zk.currentFocus;
		if (cf && zUtl.isAncestor(this, cf, true)) {
			zk.currentFocus = null;
		} else
			cf = null;

		var p = this.parent;
		if (p) p.replaceChildHTML_(this, n, desktop, skipper);
		else {
			if (n.z_wgt) n.z_wgt.unbind_(skipper); //unbind first (w/o removal)
			zDom.setOuterHTML(n, this.redraw(skipper));
			this.bind_(desktop, skipper);
		}

		//TODO: if (zAu.valid) zAu.valid.fixerrboxes();
		if (cf && !zk.currentFocus) cf.focus();

		zWatch.fireDown('beforeSize', -1, this);
		zWatch.fireDown('onSize', 5, this);
	},
	rerender: function (skipper) {
		if (this.node) {
			var skipInfo;
			if (skipper) skipInfo = skipper.skip(this);

			this.replaceHTML(this.node, null, skipper);

			if (skipInfo) skipper.restore(this, skipInfo);
		}
	},

	replaceChildHTML_: function (child, n, desktop, skipper) {
		if (n.z_wgt) n.z_wgt.unbind_(skipper); //unbind first (w/o removal)
		zDom.setOuterHTML(n, child.redraw(skipper));
		child.bind_(desktop, skipper);
	},
	insertChildHTML_: function (child, before, desktop) {
		if (before)
			zDom.insertHTMLBefore(before.node, child.redraw());
		else
			zDom.insertHTMLBeforeEnd(this.node, child.redraw());
		child.bind_(desktop);
	},
	removeChildHTML_: function (child, prevsib) {
		var n = child.node;
		if (!n) this._prepareRemove(n = []);

		child.unbind_();

		if (n.$array)
			for (var j = n.length; --j >= 0;)
				zDom.remove(n[j]);
		else
			zDom.remove(n);
	},
	_prepareRemove: function (ary) {
		for (var w = this.firstChild; w; w = w.nextSibling)
			if (w.node) ary.push(w.node);
			else w._prepareRemove(ary);
	},

	bind_: function (desktop, skipper) {
		var n = zDom.$(this.uuid);
		if (n) {
			n.z_wgt = this;
			this.node = n;
			if (!desktop) desktop = zk.Desktop.ofNode(n);
		} else
			zk.Widget._binds[this.uuid] = this;

		this.desktop = desktop;

		for (var child = this.firstChild; child; child = child.nextSibling)
			if (!skipper || !skipper.skipped(this, child))
				child.bind_(desktop); //don't pass skipper

		if (n)
			for (var lsns = this._lsns,
			evts = zk.Widget._domevts, j = evts.length; --j >= 0;) {
				var evtnm = evts[j],
					ls = lsns[evtnm];
				if (!ls || !ls.length) {
					if (!this.inServer) continue; //nothing to do
					if (this[evtnm] == null) continue; //nothing to do
				}
				this.listenDomEvent(n, evtnm);
			}
	},
	unbind_: function (skipper) {
		var n = this.node;
		if (n) {
			n.z_wgt = null;
			this.node = null;
		} else
			delete zk.Widget._binds[this.uuid];

		this.desktop = null;

		if (n) {
			var regevts = this._regevts;
			if (regevts) {
				this._regevts = null;
				for (var evtnm; evtnm = regevts.shift();)
					this.domUnlisten_(evtnm, zk.Widget._domEvtToZK);
			}
		}

		for (var child = this.firstChild; child; child = child.nextSibling)
			if (!skipper || !skipper.skipped(this, child))
				child.unbind_(); //don't pass skipper
	},
	domListen_: function (evtnm, fn) {
		zEvt.listen(this.node, evtnm.substring(2).toLowerCase(), fn);
	},
	domUnlisten_: function (evtnm, fn) {
		zEvt.unlisten(this.node, evtnm.substring(2).toLowerCase(), fn);
	},

	/** Sets the focus to this widget.
	 * <p>Default: call child widget's focus until it returns true,
	 * or no child at all.
	 * @return whether the focus is gained to this widget.
	 */
	focus: function (timeout) {
		if (this.node && this.isVisible() && this.canActivate({checkOnly:true})) {
			if (zDom.focus(this.node, timeout)) {
				zk.currentFocus = this;
				this.setTopmost();
				return true;
			}
			for (var w = this.firstChild; w; w = w.nextSibling)
				if (w.isVisible() && w.focus(timeout))
					return true;
		}
		return false;
	},
	/** Checks if this widget can be activated (gaining focus and so on).
	 * <p>Default: return false if it is not a descendant of zk.currentModal.
	 * <p>Allowed Options:
	 * <ul>
	 * <li>checkOnly: not to change focus back to modal dialog if unable
	 * to activate.</li>
	 * </ul>
	 */
	canActivate: function (opts) {
		var modal = zk.currentModal;
		if (modal && !zUtl.isAncestor(modal, this)) {
			if (!opts || !opts.checkOnly) {
				var cf = zk.currentFocus;
				//Note: browser might change focus later, so delay a bit
				if (cf && zUtl.isAncestor(modal, cf)) cf.focus(0);
				else modal.focus(0);
			}
			return false;
		}
		return true;
	},

	//ZK event//
	fireX: function (evt, timeout) {
		var evtnm = evt.name,
			lsns = this._lsns[evtnm],
			len = lsns ? lsns.length: 0;
		if (len) {
			for (var j = 0; j < len;) {
				var inf = lsns[j++], o = inf[1];
				(inf[2] || o[evtnm]).call(o, evt);
				if (evt._stop) return; //no more processing
			}
		}

		if (this.inServer && this.desktop) {
			var asap = this['$' + evtnm];
			if (asap != null || this.isImportantEvent_(evtnm))
				zAu.send(evt,
					asap ? timeout >= 0 ? timeout: 38: -1);
		}
	},
	fire: function (evtnm, data, opts, timeout) {
		this.fireX(new zk.Event(this, evtnm, data, opts), timeout);
	},
	isImportantEvent_: function (evtnm) {
		return false;
	},
	listen: function (evtnm, listener, fn, priority) {
		if (!priority) priority = 0;
		var inf = [priority, listener, fn],
			lsns = this._lsns[evtnm];
		if (!lsns) lsns = this._lsns[evtnm] = [inf];
		else
			for (var j = lsns.length; --j >= 0;)
				if (lsns[j][0] >= priority) {
					lsns.$addAt(j + 1, inf);
					break;
				}

		var n = this.node;
		if (n) {
			var regevts = this._regevts;
			if ((!regevts || !regevts.$contains(evtnm))
			&& zk.Widget._domevts.$contains(evnm)) {
				var regevts = this._regevts;
				if (regevts) regevts.push(evtnm);
				else this._regevts = [evtnm];
				this.domListen_(evtnm, zk.Widget._domEvtToZK);
			}
		}
	},
	unlisten: function (evtnm, listener, fn) {
		var lsns = this._lsns[evtnm];
		for (var j = lsns ? lsns.length: 0; --j >= 0;)
			if (lsns[j][1] == listener && lsns[j][2] == fn) {
				lsns.$removeAt(j);
				return true;
			}
		return false;
	},
	isListen: function (evtnm) {
		if (this['$' + evtnm]) return true;
		var lsns = this._lsns[evtnm];
		return lsns && lsns.length;
	},
	setListener: function (inf) {
		var evtnm = inf[0], fn = inf[1],
			lsns = this._$lsns;
			oldfn = lsns[evtnm];
		if (oldfn) { //unlisten first
			delete lsns[evtnm];
			this.unlisten(evtnm, this, oldfn);
		}
		if (fn) {
			if (typeof fn != 'function') fn = new Function(fn);
			this.listen(evtnm, this, lsns[evtnm] = fn);
		}
	},

	//ZK event handling//
	doClick_: function (evt) {
		if (this.isListen('onClick')) {
			this.fire("onClick", zEvt.mouseData(evt, this.node), {ctl:true});
			return true;
		}
		return false;
	},

	//DOM event handling//
	domFocus_: function () {
		if (!this.canActivate()) return false;

		zk.currentFocus = this;
		zWatch.fire('onFloatUp', -1, this); //notify all

		if (this.isListen('onFocus'))
			this.fire('onFocus');
		return true;
	},
	domBlur_: function () {
		zk.currentFocus = null;

		//TODO: handle validation

		if (this.isListen('onBlur'))
			this.fire('onBlur');
	}

}, {
	_floating: [], //[{widget,node}]
	_floatOrder: function (a, b) {
		return zUtl.isAncestor(a.widget, b.widget);
	},

	$: function (n, strict) {
		//1. No map from element to widget directly. rather, go thru DOM
		//2. We have to remove '$*' since $chdex is parentNode!
		if (typeof n == 'string') {
			var id = n;
			n = zDom.$(n);
			if (!n) return zk.Widget._binds[id];
		} else if (n)
			n = n.target || n.srcElement || n; //check DOM event first

		for (; n; n = zDom.parentNode(n)) {
			var wgt = n.z_wgt;
			if (wgt) return wgt;

			var id = n.id;
			if (id) {
				var j = id.lastIndexOf('$');
				if (j >= 0) {
					var n2 = zDom.$(id.substring(0, j));
					return n2 && (!strict || zDom.isAncestor(n2, n)) ?
						n2.z_wgt: null; //with '$', assume child node
				}
			}
		}
		return null;
	},
	_binds: {}, //Map(uuid, wgt): bind but no node

	//Event Handling//
	domMouseDown: function (wgt) {
		var modal = zk.currentModal;
		if (modal && !wgt) {
			var cf = zk.currentFocus;
			//Note: browser might change focus later, so delay a bit
			//(it doesn't work if we stop event instead of delay - IE)
			if (cf && zUtl.isAncestor(modal, cf)) cf.focus(0);
			else modal.focus(0);
		} else if (!wgt || wgt.canActivate()) {
			zk.currentFocus = wgt;
			if (wgt) zWatch.fire('onFloatUp', -1, wgt); //notify all
		}
	},
	_domEvtToZK: function (evt) {
		evt = evt || window.event;
		var $Widget = zk.Widget,
			wgt = $Widget.$(evt),
			type = evt.type;
		if (type.startsWith('mouse'))
			wgt.fire('onMouse' + type.charAt(5).toUpperCase() + type.substring(6),
				zEvt.mouseData(evt));
		else if (type.startsWth('key'))
			wgt.fire('onKey' + type.charAt(3).toUpperCase() + type.substring(4),
				zEvt.keyData(evt));
		else wgt.fire('on' + type.charAt(0).toUpperCase() + type.substring(1));
	},
	_domevts: ['onMouseOver','onMouseOut', 'onMouseDown', 'onMouseUp',
		'onMouseMove', 'onKeyDown', 'onKeyPress', 'onKeyUp'],
		//onFocus, onBlur, onClick, onDoubleClick, onChange are fired by widget

	//uuid//
	uuid: function (id) {
		var j = id.lastIndexOf('$');
		return j >= 0 ? id.substring(0, j): id;
	},
	nextUuid: function () {
		return "_z_" + zk.Widget._nextUuid++;
	},
	_nextUuid: 0
});

/** A ZK desktop. */
zk.Desktop = zk.$extends(zk.Object, {
	$init: function (dtid, updateURI) {
		this._aureqs = [];

		var zdt = zk.Desktop, dt = zdt.all[dtid];
		if (!dt) {
			this.id = dtid;
			this.updateURI = updateURI;
			zdt.all[dtid] = this;
			++zdt._ndt;
			if (!zdt._dt) zdt._dt = this; //default desktop
		} else if (updateURI)
			dt.updateURI = updateURI;

		zdt.destroy();
	}
},{
	/** Returns the desktop of the specified desktop ID.
	 */
	$: function (dtid) {
		return dtid ? typeof dtid == 'string' ?
			zk.Desktop.all[dtid]: dtid: zk.Desktop._dt;
	},
	/** Returns the desktop that the specified element belongs to.
	 */
	ofNode: function (n) {
		var zdt = zk.Desktop, dts = zdt.all;
		if (zdt._ndt > 1) {
			var wgt = zk.Widget.$(n);
			if (wgt)
				for (; wgt; wgt = wgt.parent)
					if (wgt.desktop)
						return wgt.desktop;
		}
		if (zdt._dt) return zdt._dt;
		for (var dtid in dts)
			return dts[dtid];
	},
	/** A map of (String dtid, zk.Desktop dt) (readonly). */
	all: {},
	_ndt: 0,
	/** Remove desktops that are no longer valid.
	 */
	destroy: function () {
		var zdt = zk.Desktop, dts = zdt.all;
		if (zdt._dt && zdt._dt.pguid && !zDom.$(zdt._dt.pguid)) //removed
			zdt._dt = null;
		for (var dtid in dts) {
			var dt = dts[dtid];
			if (dt.pguid && !zDom.$(dt.pguid)) { //removed
				delete dts[dtid];
				--zdt._ndt;
			} else if (!zdt._dt)
				zdt._dt = dt;
		}
	}
});

/** A ZK page. */
zk.Page = zk.$extends(zk.Widget, {//unlik server, we derive from Widget!
	_style: "width:100%;height:100%",

	$init: function (pguid, contained) {
		this.$super('$init', pguid);

		this._fellows = {};
		if (contained) zk.Page.contained.push(this);
	},
	redraw: function () {
		var html = '<div id="' + this.uuid + '" style="' + this.getStyle() + '">';
		for (var w = this.firstChild; w; w = w.nextSibling)
			html += w.redraw();
		return html + '</div>';
	}

},{
	/** An list of contained page (i.e., standalone but not covering
	 * the whole browser window.
	 */
	contained: []
});

zk.Skipper = zk.$extends(zk.Object, {
	/** Returns whether the specified child wiget is skipped by {@link #skip}.
	 * <p>Return if wgt.caption != child.
	 *In other words, it skip all children except the caption.
	 */
	skipped: function (wgt, child) {
		return wgt.caption != child;
	},
	/** Skips all or subset of the children of the specified wiget.
	 * <p>Default: it detaches all DOM elements whose parent element
	 * is zDOM.$(skipId).
	 * @param skipId the ID of the element where all its child elements
	 * shall be detached by this method, and restored later by {@link #restore}.
	 * @return an object to represent the skipped children
	 * (usually an array of DOM element).
	 * This object will be passed to {@link #restore}'s skipInfo argument.
	 */
	skip: function (wgt, skipId) {
		if (!skipId) skipId = wgt.uuid + '$cave';
		var skip = zDom.$(skipId);
		if (skip && skip.firstChild) {
			zDom.remove(skip);
			return skip;
		}
		return null;
	},
	/** Restores the children being skipped by {@link #skip}.
	 */
	restore: function (wgt, skip) {
		if (skip) {
			var loc = zDom.$(skip.id);
			for (var el; el = skip.firstChild;) {
				skip.removeChild(el);
				loc.appendChild(el);
			}
		}
	}
});
zk.Skipper.nonCaptionSkipper = new zk.Skipper();

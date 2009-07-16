/* Window.js

	Purpose:
		
	Description:
		
	History:
		Mon Nov 17 17:52:31     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zPkg.load('zul.wgt');
zul.wnd.Window = zk.$extends(zul.Widget, {
	_mode: 'embedded',
	_border: 'none',
	_minheight: 100,
	_minwidth: 200,
	_shadow: true,

	$init: function () {
		if (!zk.light) this._fellows = {};

		this.$supers('$init', arguments);

		this.listen({onClose: this,	onMove: this, onZIndex: this}, -1000);
		this._skipper = new zul.wnd.Skipper(this);
	},

	$define: { //zk.def
		mode: _zkf = function () {
			this._updateDomOuter();
		},
		title: _zkf,
		border: _zkf,
		closable: _zkf,
		sizable: _zkf,
		maximized: _zkf,
		maximizable: _zkf,
		minimized: _zkf,
		minimizable: _zkf,
		contentStyle: _zkf,
		contentSclass: _zkf,

		position: function (pos) {
			if (this.desktop && this._mode != 'embedded')
				this._updateDomPos(); //TODO: handle pos = 'parent'
		},

		minheight: null, //TODO
		minwidth: null, //TODO
		shadow: null
	},

	doOverlapped: function () {
		this.setMode('overlapped');
	},
	doPopup: function () {
		this.setMode('popup');
	},
	doHighlighted: function () {
		this.setMode('highlighted');
	},
	doModal: function () {
		this.setMode('modal');
	},
	doEmbedded: function () {
		this.setMode('embedded');
	},

	_doOverlapped: function () {
		var pos = this.getPosition(),
			n = this.$n(),
			$n = zk(n);
		if (!pos && !n.style.top && !n.style.left) {
			var xy = $n.revisedOffset();
			n.style.left = jq.px(xy[0]);
			n.style.top = jq.px(xy[1]);
		} else if (pos == "parent")
			this._posByParent();

		$n.makeVParent();
		this._syncShadow();
		this._updateDomPos();

		if (zk(this.$n()).isRealVisible()) {
			$n.cleanVisibility();
			this.setTopmost();
		}

		this._makeFloat();
	},
	_doModal: function () {
		var pos = this.getPosition(),
			n = this.$n(),
			$n = zk(n);
		if (pos == "parent") this._posByParent();

		$n.makeVParent();
		this._syncShadow();
		this._updateDomPos(true);

		if (!pos) { //adjust y (to upper location)
			var top = zk.parseInt(n.style.top), y = jq.innerY();
			if (y) {
				var y1 = top - y;
				if (y1 > 100) n.style.top = jq.px(top - (y1 - 100));
			} else if (top > 100)
				n.style.top = "100px";
		}

		//Note: modal must be visible
		var realVisible = $n.isRealVisible();
		if (realVisible) {
			$n.cleanVisibility();
			this.setTopmost();
		}

		this._mask = new zk.eff.FullMask({
			id: this.uuid + "-mask",
			anchor: this._shadowWgt ? this._shadowWgt.getBottomElement() : null,
				//bug 1510218: we have to make it as a sibling
			zIndex: this._zIndex,
			stackup: (zk.useStackup === undefined ? zk.ie6_: zk.useStackup),
			visible: realVisible});

		if (realVisible) {
			this._prevmodal = zk.currentModal;
			var modal = zk.currentModal = this;
			this._prevfocus = zk.currentFocus; //store
			this.focus(0);
		}

		this._makeFloat();
	},
	/** Must be called before calling makeVParent. */
	_posByParent: function () {
		var n = this.$n(),
			ofs = zk(n.parentNode).revisedOffset(),
			left = zk.parseInt(n.style.left), top = zk.parseInt(n.style.top);
		this._offset = ofs;
		n.style.left = jq.px(ofs[0] + zk.parseInt(n.style.left));
		n.style.top = jq.px(ofs[1] + zk.parseInt(n.style.top));
	},
	_syncShadow: function () {
		if (this._mode == 'embedded') {
			if (this._shadowWgt) {
				this._shadowWgt.destroy();
				this._shadowWgt = null;
			}
		} else if (this._shadow) {
			if (!this._shadowWgt)
				this._shadowWgt = new zk.eff.Shadow(this.$n(),
					{left: -4, right: 4, top: -2, bottom: 3, stackup: true});
			this._shadowWgt.sync();
		}
	},
	_syncMask: function () {
		if (this._mask && this._shadowWgt) this._mask.sync(this._shadowWgt.getBottomElement());
	},
	_hideShadow: function () {
		var shadow = this._shadowWgt;
		if (shadow) shadow.hide();
	},
	_makeFloat: function () {
		var handle = this.$n('cap');
		if (handle && !this._drag) {
			handle.style.cursor = "move";
			var Window = this.$class;
			this._drag = new zk.Draggable(this, null, {
				handle: handle, stackup: true,
				starteffect: Window._startmove,
				ghosting: Window._ghostmove,
				endghosting: Window._endghostmove,
				ignoredrag: Window._ignoremove,
				endeffect: Window._aftermove});
		}
	},
	_updateDomPos: function (force) {
		var n = this.$n(), pos = this._position;
		if (pos == "parent"/*handled by the caller*/ || (!pos && !force))
			return;

		var st = n.style;
		st.position = "absolute"; //just in case
		var ol = st.left, ot = st.top;
		zk(n).center(pos);
		var sdw = this._shadowWgt;
		if (pos && sdw) {
			var opts = sdw.opts, l = n.offsetLeft, t = n.offsetTop; 
			if (pos.indexOf("left") >= 0 && opts.left < 0)
				st.left = jq.px(l - opts.left);
			else if (pos.indexOf("right") >= 0 && opts.right > 0)
				st.left = jq.px(l - opts.right);
			if (pos.indexOf("top") >= 0 && opts.top < 0)
				st.top = jq.px(t - opts.top);
			else if (pos.indexOf("bottom") >= 0 && opts.bottom > 0)
				st.top = jq.px(t - opts.bottom);
		}
		this._syncShadow();
		if (ol != st.left || ot != st.top)
			this._fireOnMove();
	},

	_updateDomOuter: function () {
		this.rerender(this._skipper);
	},

	//event handler//
	onClose: function () {
		if (!this.inServer) //let server handle if in server
			this.parent.removeChild(this); //default: remove
	},
	onMove: function (evt) {
		this._left = evt.left;
		this._top = evt.top;
	},
	onZIndex: function (evt) {
		this._syncShadow();
		this._syncMask();
	},
	//watch//
	onSize: _zkf = function () {
		this._hideShadow();
		if (this.isMaximized()) {
			/** TODO 
			 * if (this._maximized)
				this._syncMaximized();
			this._maximized = false; // avoid deadloop
			*/
		}
		this._fixHgh();
		this._fixWdh();
		if (this._mode != 'embedded') {
			this._updateDomPos();
			this._syncShadow();
		}
	},
	onShow: _zkf,
	onFloatUp: function (wgt) {
		if (!this.isVisible() || this._mode == 'embedded')
			return; //just in case

		if (this._mode == 'popup') {
			for (var floatFound; wgt; wgt = wgt.parent) {
				if (wgt == this) {
					if (!floatFound) this.setTopmost();
					return;
				}
				floatFound = floatFound || wgt.isFloating_();
			}
			this.setVisible(false);
			this.fire('onOpen', {open:false});
		} else
			for (; wgt; wgt = wgt.parent) {
				if (wgt == this) {
					this.setTopmost();
					return;
				}
				if (wgt.isFloating_())
					return;
			}
	},
	_fixWdh: zk.ie7 ? function () {
		if (this._mode == 'embedded' || this._mode == 'popup' || !zk(this.$n()).isRealVisible()) return;
		var n = this.$n(),
			cave = this.$n('cave').parentNode,
			wdh = n.style.width,
			$n = jq(n),
			$tl = $n.find('>div:first'),
			tl = $tl[0],
			hl = tl && this.$n("cap") ? $tl.nextAll('div:first')[0]: null,
			bl = $n.find('>div:last')[0];

		if (!wdh || wdh == "auto") {
			var $cavp = zk(cave.parentNode),
				diff = $cavp.padBorderWidth() + $cavp.parent().padBorderWidth();
			if (tl) tl.firstChild.style.width = jq.px(cave.offsetWidth + diff);
			if (hl) hl.firstChild.firstChild.style.width = jq.px(cave.offsetWidth
				- (zk(hl).padBorderWidth() + zk(hl.firstChild).padBorderWidth() - diff));
			if (bl) bl.firstChild.style.width = jq.px(cave.offsetWidth + diff);
		} else {
			if (tl) tl.firstChild.style.width = "";
			if (hl) hl.firstChild.style.width = "";
			if (bl) bl.firstChild.style.width = "";
		}
	} : zk.$void,
	_fixHgh: function () {
		if (!zk(this.$n()).isRealVisible()) return;
		var n = this.$n(),
			hgh = n.style.height,
			cave = this.$n('cave'),
			cvh = cave.style.height;
		if (hgh && hgh != "auto") {
			if (zk.ie6_) cave.style.height = "0px";
			zk(cave).setOffsetHeight(this._offsetHeight(n));
		} else if (cvh && cvh != "auto") {
			if (zk.ie6_) cave.style.height = "0px";
			cave.style.height = "";
		}
	},
	_offsetHeight: function (n) {
		var h = n.offsetHeight - 1 - this._titleHeight(n);
		if(this._mode != 'embedded' && this._mode != 'popup') {
			var cave = this.$n('cave'),
				bl = jq(n).find('>div:last')[0],
				cap = this.$n("cap");
			h -= bl.offsetHeight;
			if (cave)
				h -= zk(cave.parentNode).padBorderHeight();
			if (cap)
				h -= zk(cap.parentNode).padBorderHeight();
		}
		return h - zk(n).padBorderHeight();
	},
	_titleHeight: function (n) {
		var cap = this.$n('cap'),
			$tl = jq(n).find('>div:first'), tl = $tl[0];
		return cap ? cap.offsetHeight + tl.offsetHeight:
			this._mode != 'embedded' && this._mode != 'popup' ?
				$tl.nextAll('div:first')[0].offsetHeight: 0;
	},

	_fireOnMove: function (keys) {
		var pos = this._position, node = this.$n(),
			x = zk.parseInt(node.style.left),
			y = zk.parseInt(node.style.top);
		if (pos == 'parent') {
			var vparent = node.vparent;
			if (vparent) {
				var ofs = zk(vparent).reviseOffset();
				x -= ofs[0];
				y -= ofs[1];
			}
		}
		this.fire('onMove', zk.copy({
			left: x + 'px',
			top: y + 'px'
		}, keys), {ignorable: true});
	},

	//super//
	setHeight: function (height) {
		this.$supers('setHeight', arguments);
		if (this.desktop) {
			this._fixHgh();
			this._syncShadow();

			zWatch.fireDown('beforeSize', null, this);
			zWatch.fireDown('onSize', null, this); // Note: IE6 is broken, because its offsetHeight doesn't update.
		}
	},
	setWidth: function (width) {
		this.$supers('setWidth', arguments);
		if (this.desktop) {
			this._fixWdh();
			this._syncShadow();

			zWatch.fireDown('beforeSize', null, this);
			zWatch.fireDown('onSize', null, this);
		}
	},
	setDomVisible_: function () {
		this.$supers('setDomVisible_', arguments);
		this._syncShadow();
		this._syncMask();
	},
	setZIndex: _zkf = function () {
		this.$supers('setZIndex', arguments);
		this._syncShadow();
		this._syncMask();
	},
	setZindex: _zkf,
	focus: function (timeout) {
		if (this.desktop && this.isVisible() && this.canActivate({checkOnly:true})) {
			var cap = this.caption;
			for (var w = this.firstChild; w; w = w.nextSibling)
				if (w != cap && w.focus(timeout))
					return true;
			return cap && cap.focus(timeout);
		}
		return false;
	},
	getZclass: function () {
		var zcls = this._zclass;
		return zcls != null ? zcls: "z-window-" + this._mode;
	},

	onChildAdded_: function (child) {
		this.$supers('onChildAdded_', arguments);
		if (child.$instanceof(zul.wgt.Caption))
			this.caption = child;
	},
	onChildRemoved_: function (child) {
		this.$supers('onChildRemoved_', arguments);
		if (child == this.caption)
			this.caption = null;
	},
	domStyle_: function (no) {
		var style = this.$supers('domStyle_', arguments),
			visible = this.isVisible();
		if ((!no || !no.visible) && visible && this.isMinimized())
			style = 'display:none;'+style;
		if (this._mode != 'embedded')
			style = (visible ? "position:absolute;visibility:hidden;" : "position:absolute;")
				+style;
		return style;
	},
	bind_: function () {
		this.$supers('bind_', arguments);

		var mode = this._mode;
		zWatch.listen({onSize: this, onShow: this});
		if (mode != 'embedded') {
			zWatch.listen({onFloatUp: this});
			this.setFloating_(true);

			if (mode == 'modal' || mode == 'highlighted') this._doModal();
			else this._doOverlapped();
		}
	},
	unbind_: function () {
		var node = this.$n();
		node.style.visibility = 'hidden'; //avoid unpleasant effect

		//we don't check this._mode here since it might be already changed
		if (this._shadowWgt) {
			this._shadowWgt.destroy();
			this._shadowWgt = null;
		}
		if (this._drag) {
			this._drag.destroy();
			this._drag = null;
		}
		if (this._mask) {
			this._mask.destroy();
			this._mask = null;
		}
		
		zk(node).undoVParent();
		zWatch.unlisten({onFloatUp: this, onSize: this, onShow: this});
		this.setFloating_(false);

		if (zk.currentModal == this) {
			zk.currentModal = this._prevmodal;
			var prevfocus = this._prevfocus;
			if (prevfocus) prevfocus.focus(0);
			this._prevfocus = this._prevmodal = null;
		}

		var Window = this.$class;
		for (var nms = ['close', 'max', 'min'], j = 3; j--;) {
			var nm = nms[j],
				n = this['e' + nm ];
			if (n) {
				this['e' + nm ] = null;
				jq(n).unbind('click', Window[nm + 'click'])
					.unbind('mouseover', Window[nm + 'over'])
					.unbind('mouseout', Window[nm + 'out']);
			}
		}
		this.$supers('unbind_', arguments);
	},
	doClick_: function (evt) {
		switch (evt.domTarget) {
		case this.$n('close'):
			this.fire('onClose');
			break;
		case this.$n('max'):
			// TODO
			break;
		case this.$n('min'):
			// TODO 
			// if (this.isMinimizable())
			//	this.setMinimized(!this.isMinimized());
			break;
		default:
			this.$supers('doClick_', arguments);
			return;
		}
		evt.stop();
	},
	doMouseOver_: function (evt) {
		switch (evt.domTarget) {
		case this.$n('close'):
			jq(this.$n('close')).addClass(this.getZclass() + '-close-over');
			break;
		case this.$n('max'):
			var zcls = this.getZclass(),
				added = this.isMaximized() ? ' ' + zcls + '-maxd-over' : '';
			jq(this.$n('max')).addClass(zcls + '-max-over' + added);
			break;
		case this.$n('min'):
			jq(this.$n('min')).addClass(this.getZclass() + '-min-over');
			break;
		}
		this.$supers('doMouseOver_', arguments);
	},
	doMouseOut_: function (evt) {
		switch (evt.domTarget) {
		case this.$n('close'):
			jq(this.$n('close')).removeClass(this.getZclass() + '-close-over');
			break;
		case this.$n('max'):
			var zcls = this.getZclass(),
				$max = jq(this.$n('max'));
			if (this.isMaximized())
				$max.removeClass(zcls + '-maxd-over');
			$max.removeClass(zcls + '-max-over');
			break;
		case this.$n('min'):
			jq(this.$n('min')).removeClass(this.getZclass() + '-min-over');
			break;
		}
		this.$supers('doMouseOut_', arguments);
	}
},{ //static
	//drag
	_startmove: function (dg) {
		//Bug #1568393: we have to change the percetage to the pixel.
		var el = dg.node;
		if(el.style.top && el.style.top.indexOf("%") >= 0)
			 el.style.top = el.offsetTop + "px";
		if(el.style.left && el.style.left.indexOf("%") >= 0)
			 el.style.left = el.offsetLeft + "px";
		zWatch.fire('onFloatUp', null, dg.control); //notify all
	},
	_ghostmove: function (dg, ofs, evt) {
		var wnd = dg.control,
			el = dg.node;
		wnd._hideShadow();
		var $el = jq(el),
			$top = $el.find('>div:first'),
			top = $top[0],
			header = $top.nextAll('div:first')[0],
			fakeT = jq(top).clone()[0],
			fakeH = jq(header).clone()[0],
			html = '<div id="zk_wndghost" class="z-window-move-ghost" style="position:absolute;top:'
			+ofs[1]+'px;left:'+ofs[0]+'px;width:'
			+$el.zk.offsetWidth()+'px;height:'+$el.zk.offsetHeight()
			+'px;z-index:'+el.style.zIndex+'"><dl></dl></div>';
		jq(document.body).prepend(html);
		dg._wndoffs = ofs;
		el.style.visibility = "hidden";
		var h = el.offsetHeight - top.offsetHeight - header.offsetHeight;
		el = jq("#zk_wndghost")[0];
		el.firstChild.style.height = jq.px(zk(el.firstChild).revisedHeight(h));
		el.insertBefore(fakeT, el.firstChild);
		el.insertBefore(fakeH, el.lastChild);
		return el;
	},
	_endghostmove: function (dg, origin) {
		var el = dg.node; //ghost
		origin.style.top = jq.px(origin.offsetTop + el.offsetTop - dg._wndoffs[1]);
		origin.style.left = jq.px(origin.offsetLeft + el.offsetLeft - dg._wndoffs[0]);

		document.body.style.cursor = "";
	},
	_ignoremove: function (dg, pointer, evt) {
		var el = dg.node,
			wgt = dg.control;
		switch (evt.domTarget) {
		case wgt.$n('close'):
		case wgt.$n('max'):
		case wgt.$n('min'):
			return true; //ignore special buttons
		}
		if (!wgt.isSizable()
		|| (el.offsetTop + 4 < pointer[1] && el.offsetLeft + 4 < pointer[0] 
		&& el.offsetLeft + el.offsetWidth - 4 > pointer[0]))
			return false; //accept if not sizable or not on border
		return true;
	},
	_aftermove: function (dg, evt) {
		dg.node.style.visibility = "";
		var wgt = dg.control;
		wgt._syncShadow();
		wgt._fireOnMove(evt.data);
	}
});

zul.wnd.Skipper = zk.$extends(zk.Skipper, {
	$init: function (wnd) {
		this._w = wnd;
	},
	restore: function () {
		this.$supers('restore', arguments);
		var w = this._w;
		if (w._mode != 'embedded') {
			w._updateDomPos(); //skipper's size is wrong in bind_
			w._syncShadow();
		}
	}
});

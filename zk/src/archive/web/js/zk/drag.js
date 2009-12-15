/* drag.js

	Purpose:
		
	Description:
		
	History:
		Mon Nov 10 11:06:57     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

script.aculo.us dragdrop.js v1.7.0,
Copyright (c) 2005, 2006 Thomas Fuchs (http://script.aculo.us, http://mir.aculo.us)
	(c) 2005, 2006 Sammi Williams (http://www.oriontransfer.co.nz, sammi@oriontransfer.co.nz)

This program is distributed under LGPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
(function () {
	var _drags = [],
		_dragging = [],
		_stackup, _activedg, _timeout, _initPt, _initEvt,
		_lastPt, _lastScrlPt;

	function _activate(dg, devt, pt) {
		_timeout = setTimeout(function () { 
			_timeout = null; 
			_activedg = dg; 
		}, dg.opts.delay);
		_initPt = pt;
		_initEvt = jq.Event.zk(devt, dg.control);
	}
	function _deactivate() {
		_activedg = null;
		setTimeout(function(){_initEvt=null;}, 0);
	}

	function _docmousemove(devt) {
		if(!_activedg || _activedg.dead) return;

		var evt = jq.Event.zk(devt),
			pt = [evt.pageX, evt.pageY];
		// Mozilla-based browsers fire successive mousemove events with
		// the same coordinates, prevent needless redrawing (moz bug?)
		if(_lastPt && _lastPt[0] == pt [0]
		&& _lastPt[1] == pt [1])
			return;

		_lastPt = pt;
		_activedg._updateDrag(pt, evt);
		devt.stop();
			//test/dragdrop.zul: it seems less stall-dragging when dragging
			//IMG (but still happens if dragging fast)
	}
	function _docmouseup(devt) {
		if(_timeout) { 
			clearTimeout(_timeout); 
			_timeout = null; 
		}
		if(!_activedg) return;

		_lastPt = null;
		var evt;
		_activedg._endDrag(evt = jq.Event.zk(devt));
		_activedg = null;
		if (evt.domStopped) devt.stop();
	}
	function _dockeypress(devt) {
		if(_activedg) _activedg._keypress(devt);
	}

	//default effect//
	function _defStartEffect(dg) {
		var node = dg.node;
		node._$opacity = jq(node).css('opacity');
		_dragging[node] = true;
		new zk.eff.Opacity(node, {duration:0.2, from:node._$opacity, to:0.7}); 
	}
	function _defEndEffect(dg) {
		var node = dg.node,
			toOpacity = typeof node._$opacity == 'number' ? node._$opacity : 1.0;
		new zk.eff.Opacity(node, {duration:0.2, from:0.7,
			to:toOpacity, queue: {scope:'_draggable', position:'end'},
			afterFinish: function () { 
				_dragging[node] = false;
			}
		});
	}
	function _defRevertEffect(dg, offset) {
		var dx, dy;
		if ((dx=offset[0]) || (dy=offset[1])) {
			var node = dg.node,
				orgpos = node.style.position,
				dur = Math.sqrt(Math.abs(dy^2)+Math.abs(dx^2))*0.02;
			new zk.eff.Move(node, { x: -dx, y: -dy,
				duration: dur, queue: {scope:'_draggable', position:'end'},
				afterFinish: function () {node.style.position = orgpos;}});
		}
	}

/** A draggable object used to make a DOM element draggable. 
 */
zk.Draggable = zk.$extends(zk.Object, {
	/** The control object for this draggable.
	 * @type Object
	 */
	//control: null,
	/** The DOM element that represents the handle that the user can
	 * drag the whole element ({@link #node}.
	 * It is either {@link #node} or a child element of it.
	 * @type DOMElement
	 */
	//handle: null,
	/** The DOM element that is draggable (the whole element).
	 * @type DOMElement
	 */
	//node: null,
	/** The options of this draggable.
	 * <p>Refer <a href="http://docs.zkoss.org/wiki/Options_of_zk.Draggable">here</a>
	 * for all possible options.
	 * @type Map
	 */
	//opts: null,
	/** Constructor.
	 * @param Object control the control object for this draggable.
	 * I can be anything, but it is usually a widget ({@link zk.Widget}).
	 * @param DOMElement node [optional] the DOM element that is made to be draggable.
	 * If omitted and control is a widget, {@link zk.Widget#$n} is assumed.
	 * @param Map opts [optional] options. Refer to <a href="http://docs.zkoss.org/wiki/Options_of_zk.Draggable">here</a> for allowed options.
	 */
	$init: function (control, node, opts) {
		if (!_stackup) {
		//IE: if we don't insert stackup at beginning, dragging is slow
			jq(_stackup = jq.newStackup(null, 'z_ddstkup')).hide();
			document.body.appendChild(_stackup);
		}

		this.control = control;
		this.node = node = node ? jq(node, zk)[0]: control.$n || control.$n();
		if (!node)
			throw "Handle required for "+control;

		opts = zk.$default(opts, {
//No default z-index (since caller, such as window, might set it)
			scrollSensitivity: 20,
			scrollSpeed: 15,
			initSensitivity: 3,
			delay: 0
		});

		if (opts.reverteffect == null)
			opts.reverteffect = _defRevertEffect;
		if (opts.endeffect == null) {
			opts.endeffect = _defEndEffect;
			if (opts.starteffect == null)
				opts.starteffect = _defStartEffect;
		}

		if(opts.handle) this.handle = jq(opts.handle, zk)[0];
		if(!this.handle) this.handle = node;

		if(opts.scroll && !opts.scroll.scrollTo && !opts.scroll.outerHTML) {
			opts.scroll = jq(opts.scroll, zk)[0];
			this._isScrollChild = zUtl.isAncestor(opts.scroll, node);
		}

		this.delta = this._currentDelta();
		this.opts = opts;
		this.dragging = false;   

		jq(this.handle).mousedown(this.proxy(this._mousedown));

		//register
		if(_drags.length == 0)
			jq(document).mouseup(_docmouseup)
				.mousemove(_docmousemove)
				.keypress(_dockeypress);
		_drags.push(this);
	},
	/** Destroys this draggable object. This method must be called to clean up, if you don't want to associate the draggable feature to a DOM element.
	 */
	destroy: function () {
		jq(this.handle).unbind("mousedown", this.proxy(this._mousedown));

		//unregister
		_drags.$remove(this);
		if(_drags.length == 0)
			jq(document).unbind("mouseup", _docmouseup)
				.unbind("mousemove", _docmousemove)
				.unbind("keypress", _dockeypress);
		if (_activedg == this) //just in case
			_activedg = null;

		this.node = this.control = this.handle = null;
		this.dead = true;
	},

	/** [left, right] of this node. */
	_currentDelta: function () {
		var $node = jq(this.node);
		return [zk.parseInt($node.css('left')), zk.parseInt($node.css('top'))];
	},

	_startDrag: function (evt) {
		//disable selection
		zk(document.body).disableSelection(); // Bug #1820433
		jq.clearSelection(); // Bug #2721980
		if (this.opts.stackup) { // Bug #1911280
			var stackup = document.createElement("DIV");
			document.body.appendChild(stackup);
			stackup.className = "z-dd-stackup";
			zk(stackup).disableSelection();
			var st = (this.stackup = stackup).style;
			st.width = jq.px0(jq.pageWidth());
			st.height = jq.px0(jq.pageHeight());
		}
		zk.dragging = this.dragging = true;

		var node = this.node;
		if(this.opts.ghosting)
			if (typeof this.opts.ghosting == 'function') {
				this.delta = this._currentDelta();
				this.orgnode = this.node;

				var $node = zk(this.node),
					ofs = $node.cmOffset();
				this.z_scrl = $node.scrollOffset();
				this.z_scrl[0] -= jq.innerX(); this.z_scrl[1] -= jq.innerY();
					//Store scrolling offset since _draw not handle DIV well
				ofs[0] -= this.z_scrl[0]; ofs[1] -= this.z_scrl[1];

				node = this.node = this.opts.ghosting(this, ofs, evt);
			} else {
				this._clone = jq(node).clone()[0];
				this.z_orgpos = node.style.position; //Bug 1514789
				if (this.z_orgpos != 'absolute')
					jq(node).absolutize();
				node.parentNode.insertBefore(this._clone, node);
			}

		if (this.opts.stackup) {
			if (zk(_stackup).isVisible()) //in use
				this._stackup = jq.newStackup(node, node.id + '-ddstk');
			else {
				this._stackup = _stackup;
				this._syncStackup();
				node.parentNode.insertBefore(_stackup, node);
			}
		}

		if(this.opts.zIndex) { //after ghosting
			this.orgZ = zk.parseInt(jq(node).css('z-index'));
			node.style.zIndex = this.opts.zIndex;
		}

		if(this.opts.scroll) {
			if (this.opts.scroll == window) {
				var where = this._getWndScroll(this.opts.scroll);
				this.orgScrlLeft = where.left;
				this.orgScrlTop = where.top;
			} else {
				this.orgScrlLeft = this.opts.scroll.scrollLeft;
				this.orgScrlTop = this.opts.scroll.scrollTop;
			}
		}

		if(this.opts.starteffect)
			this.opts.starteffect(this, evt);
	},
	_syncStackup: function () {
		if (this._stackup) {
			var node = this.node,
				st = this._stackup.style;
			st.display = 'block';
			st.left = node.offsetLeft + "px";
			st.top = node.offsetTop + "px";
			st.width = node.offsetWidth + "px";
			st.height = node.offsetHeight + "px";
		}
	},

	_updateDrag: function (pt, evt) {
		if(!this.dragging) {
			var v = this.opts.initSensitivity;
			if (v && (pt[0] <= _initPt[0] + v
			&& pt[0] >= _initPt[0] - v
			&& pt[1] <= _initPt[1] + v
			&& pt[1] >= _initPt[1] - v))
				return;
			this._startDrag(evt);
		}
		this._updateInnerOfs();

		this._draw(pt, evt);
		if (this.opts.change) this.opts.change(this, pt, evt);
		this._syncStackup();

		if(this.opts.scroll) {
			this._stopScrolling();

			var p;
			if (this.opts.scroll == window) {
				var o = this._getWndScroll(this.opts.scroll);
				p = [o.left, o.top, o.left + o.width, o.top + o.height];
			} else {
				p = zk(this.opts.scroll).viewportOffset();
				p[0] += this.opts.scroll.scrollLeft + this._innerOfs[0];
				p[1] += this.opts.scroll.scrollTop + this._innerOfs[1];
				p.push(p[0]+this.opts.scroll.offsetWidth);
				p.push(p[1]+this.opts.scroll.offsetHeight);
			}

			var speed = [0,0],
				v = this.opts.scrollSensitivity;
			if(pt[0] < (p[0]+v)) speed[0] = pt[0]-(p[0]+v);
			if(pt[1] < (p[1]+v)) speed[1] = pt[1]-(p[1]+v);
			if(pt[0] > (p[2]-v)) speed[0] = pt[0]-(p[2]-v);
			if(pt[1] > (p[3]-v)) speed[1] = pt[1]-(p[3]-v);
			this._startScrolling(speed);
		}

		// fix AppleWebKit rendering
		if(navigator.appVersion.indexOf('AppleWebKit')>0) window.scrollBy(0,0);

		evt.stop();
	},

	_finishDrag: function (evt, success) {
		this.dragging = false;
		if (this.stackup) {
			jq(this.stackup).remove();
			delete this.stackup;
		}

		//enable selection back and clear selection if any
		zk(document.body).enableSelection();
		setTimeout(jq.clearSelection, 0);

		var stackup = this._stackup;
		if (stackup) {
			if (stackup == _stackup) jq(stackup).hide();
			else jq(stackup).remove();
			delete this._stackup;
		}

		var node = this.node;
		if(this.opts.ghosting)
			if (typeof this.opts.ghosting == 'function') {
				if (this.opts.endghosting)
					this.opts.endghosting(this, this.orgnode);
				if (node != this.orgnode) {
					jq(node).remove();
					this.node = this.orgnode;
				}
				delete this.orgnode;
			} else {
				if (this.z_orgpos != "absolute") { //Bug 1514789
					zk(this.node).relativize();
					node.style.position = this.z_orgpos;
				}
				jq(this._clone).remove();
				this._clone = null;
			}

		var pt = [evt.pageX, evt.pageY];
		var revert = this.opts.revert;
		if(revert && typeof revert == 'function')
			revert = revert(this, pt, evt);

		var d = this._currentDelta();
		if(revert && this.opts.reverteffect) {
			this.opts.reverteffect(this,
				[d[0]-this.delta[0], d[1]-this.delta[1]]);
		} else {
			this.delta = d;
		}

		if(this.opts.zIndex)
			node.style.zIndex = this.orgZ;

		if(this.opts.endeffect) 
			this.opts.endeffect(this, evt);

		_deactivate(this);
		setTimeout(function(){zk.dragging=false;}, 0);
			//we have to reset it later since event is fired later (after onmouseup)
	},

	_mousedown: function (devt) {
		var node = this.node,
			evt = jq.Event.zk(devt);
		if(_dragging[node] || evt.which != 1)
			return;

		var pt = [evt.pageX, evt.pageY];
		if (this.opts.ignoredrag && this.opts.ignoredrag(this, pt, evt)) {
			if (evt.domStopped) devt.stop();
			return;
		}

		var pos = zk(node).cmOffset();
		this.offset = [pt[0] - pos[0], pt[1] - pos[1]];

		_activate(this, devt, pt);
		if (!zk.ie) devt.stop();
			//test/dragdrop.zul
			//IE: if stop, onclick won't be fired in IE (unable to select)
			//FF3: if not stop, IMG cannot be dragged
			//Opera: if not stop, 'easy' to become selecting text
	},
	_keypress: function (devt) {
		if(devt.keyCode == 27) {
			this._finishDrag(jq.Event.zk(devt), false);
			devt.stop();
		}
	},

	_endDrag: function (evt) {
		if(this.dragging) {
			this._stopScrolling();
			this._finishDrag(evt, true);
			evt.stop();
		} else
			_deactivate(this);
	},

	_draw: function (point, evt) {
		var node = this.node,
			$node = zk(node),
			pos = $node.cmOffset();
		if(this.opts.ghosting) {
			var r = $node.scrollOffset();
			pos[0] += r[0] - this._innerOfs[0]; pos[1] += r[1] - this._innerOfs[1];
		}

		var d = this._currentDelta();
		pos[0] -= d[0]; pos[1] -= d[1];

		if(this.opts.scroll && (this.opts.scroll != window && this._isScrollChild)) {
			pos[0] -= this.opts.scroll.scrollLeft-this.orgScrlLeft;
			pos[1] -= this.opts.scroll.scrollTop-this.orgScrlTop;
		}

		var p = [point[0]-pos[0]-this.offset[0],
			point[1]-pos[1]-this.offset[1]];

		if(this.opts.snap)
			if(typeof this.opts.snap == 'function') {
				p = this.opts.snap(this, p);
			} else {
				if(this.opts.snap instanceof Array) {
					p = [Math.round(p[0]/this.opts.snap[0])*this.opts.snap[0],
						Math.round(p[1]/this.opts.snap[1])*this.opts.snap[1]];
				} else {
					p = [Math.round(p[0]/this.opts.snap)*this.opts.snap,
						Math.round(p[1]/this.opts.snap)*this.opts.snap];
				}
			}

		//Resolve scrolling offset when DIV is used
		if (this.z_scrl) {
			p[0] -= this.z_scrl[0]; p[1] -= this.z_scrl[1];
		}

		var style = node.style;
		if (typeof this.opts.draw == 'function') {
			this.opts.draw(this, p, evt);
		} else if (typeof this.opts.constraint == 'function') {
			var np = this.opts.constraint(this, p, evt); //return null or [newx, newy]
			if (np) p = np;
			style.left = jq.px(p[0]);
			style.top  = jq.px(p[1]);
		} else {
			if((!this.opts.constraint) || (this.opts.constraint=='horizontal'))
				style.left = jq.px(p[0]);
			if((!this.opts.constraint) || (this.opts.constraint=='vertical'))
				style.top  = jq.px(p[1]);
		}

		if(style.visibility=="hidden") style.visibility = ""; // fix gecko rendering
	},

	_stopScrolling: function () {
		if(this.scrollInterval) {
			clearInterval(this.scrollInterval);
			this.scrollInterval = null;
			_lastScrlPt = null;
		}
	},
	_startScrolling: function (speed) {
		if(speed[0] || speed[1]) {
			this.scrollSpeed = [speed[0]*this.opts.scrollSpeed,speed[1]*this.opts.scrollSpeed];
			this.lastScrolled = new Date();
			this.scrollInterval = setInterval(this.proxy(this._scroll), 10);
		}
	},

	_scroll: function () {
		var current = new Date(),
			delta = current - this.lastScrolled;
		this.lastScrolled = current;
		if(this.opts.scroll == window) {
			if (this.scrollSpeed[0] || this.scrollSpeed[1]) {
				var o = this._getWndScroll(this.opts.scroll),
					d = delta / 1000;
				this.opts.scroll.scrollTo(o.left + d*this.scrollSpeed[0],
					o.top + d*this.scrollSpeed[1]);
			}
		} else {
			this.opts.scroll.scrollLeft += this.scrollSpeed[0] * delta / 1000;
			this.opts.scroll.scrollTop  += this.scrollSpeed[1] * delta / 1000;
		}

		this._updateInnerOfs();
		if (this._isScrollChild) {
			_lastScrlPt = _lastScrlPt || _lastPt;
			_lastScrlPt[0] += this.scrollSpeed[0] * delta / 1000;
			_lastScrlPt[1] += this.scrollSpeed[1] * delta / 1000;
			if (_lastScrlPt[0] < 0)
				_lastScrlPt[0] = 0;
			if (_lastScrlPt[1] < 0)
				_lastScrlPt[1] = 0;
			this._draw(_lastScrlPt);
		}

		if(this.opts.change) {
			var devt = window.event ? jq.event.fix(window.event): null,
				evt = devt ? jq.Event.zk(devt): null;
			this.opts.change(this,
				evt ? [evt.pageX, evt.pageY]: _lastPt, evt);
		}
	},

	_updateInnerOfs: function () {
		this._innerOfs = [jq.innerX(), jq.innerY()];
	},
	_getWndScroll: function (w) {
		var T, L, W, H,
			doc = w.document,
			de = doc.documentElement;
		if (de && de.scrollTop) {
			T = de.scrollTop;
			L = de.scrollLeft;
		} else if (w.document.body) {
			T = doc.body.scrollTop;
			L = doc.body.scrollLeft;
		}
		if (w.innerWidth) {
			W = w.innerWidth;
			H = w.innerHeight;
		} else if (de && de.clientWidth) {
			W = de.clientWidth;
			H = de.clientHeight;
		} else {
			W = doc.body.offsetWidth;
			H = doc.body.offsetHeight
		}
		return {top: T, left: L, width: W, height: H};
	}

},{//static
	ignoreMouseUp: function () { //called by mount
		return zk.dragging ? true: _initEvt;
	},
	ignoreClick: function () { //called by mount
		return zk.dragging;
	}
});
})();

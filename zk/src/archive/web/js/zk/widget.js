/* widget.js

	Purpose:
		Widget - the UI object at the client
	Description:
		
	History:
		Tue Sep 30 09:23:56     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under LGPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
(function () {
	var _binds = {}, //{uuid, wgt}: bind but no node
		_fixBindMem = zk.$void, //fix IE memory leak
		_bindcnt = 0,
		_floatings = [], //[{widget,node}]
		_nextUuid = 0,
		_globals = {}, //global ID space {id, wgt}
		_domevtfnm = {}, //{evtnm, funnm}
		_domevtnm = {onDoubleClick: 'dblclick'}, //{zk-evt-nm, dom-evt-nm}
		_wgtcls = {}, //{clsnm, cls}
		_hidden = []; //_autohide

	//IE doesn't free _binds (when delete _binds[x]); so clean it up
	if (zk.ie)
		_fixBindMem = function () {	
			if (++_bindcnt > 2000) {
				_bindcnt = 0;
				_binds = zk.copy({}, _binds);
			}
		};

	//Check if el is a prolog
	function _isProlog(el) {
		var txt;
		return el && el.nodeType == 3 //textnode
			&& (txt=el.nodeValue) && !txt.trim().length;
	}

	//Event Handling//
	function _cloneEvt(evt, target) {
		return new zk.Event(target, evt.name, evt.data, evt.opts, evt.domEvent);
	}
	function _domEvtInf(wgt, evtnm, fn) { //proxy event listener
		if (!fn && !(fn = _domevtfnm[evtnm]))
			_domevtfnm[evtnm] = fn = '_do' + evtnm.substring(2);

		var f = wgt[fn];
		if (!f)
			throw 'Listener ' + fn + ' not found in ' + wgt.className;

		var domn = _domevtnm[evtnm];
		if (!domn)
			domn = _domevtnm[evtnm] = evtnm.substring(2).toLowerCase();
		return [domn, _domEvtProxy(wgt, f)];
	}
	function _domEvtProxy(wgt, f) {
		var fps = wgt._$evproxs, fp;
		if (!fps) wgt._$evproxs = fps = {};
		else if (fp = fps[f]) return fp;
		return fps[f] = _domEvtProxy0(wgt, f);
	}
	function _domEvtProxy0(wgt, f) {
		return function (devt) {
			var args = [], evt;
			for (var j = arguments.length; --j > 0;)
				args.unshift(arguments[j]);
			args.unshift(evt = jq.Event.zk(devt, wgt));

			switch (devt.type){
			case 'focus':
				if (wgt.canActivate()) {
					zk.currentFocus = wgt;
					zWatch.fire('onFloatUp', wgt); //notify all
					break;
				}
				return; //ignore it
			case 'blur':
				//due to mimicMouseDown_ called, zk.currentFocus already corrected,
				//so we clear it only if caused by other case
				if (!zk._cfByMD) zk.currentFocus = null;
				break;
			case 'click':
			case 'dblclick':
			case 'mouseup': //we cannot simulate mousedown:(
				if (zk.Draggable.ignoreClick())
					return;
			}

			var ret = f.apply(wgt, args);
			if (typeof ret == 'undefined') ret = evt.returnValue;
			if (evt.domStopped) devt.stop();
			return devt.type == 'dblclick' && typeof ret == 'undefined' ? false: ret;
		};
	}

	function _bind0(wgt) {
		_binds[wgt.uuid] = wgt;
	}
	function _unbind0(wgt) {
		delete _binds[wgt.uuid];
		wgt.desktop = null;
		wgt.clearCache();
	}
	function _bindrod(wgt) {
		_bind0(wgt);
		wgt.z_rod = true;

		for (var child = wgt.firstChild; child; child = child.nextSibling)
			_bindrod(child);
	}
	function _unbindrod(wgt) {
		_unbind0(wgt);
		delete wgt.z_rod;

		for (var child = wgt.firstChild; child; child = child.nextSibling)
			_unbindrod(child);
	}
	function _isrod(wgt) { //in client rod
		var p;
		return wgt.z_rod || ((p=wgt.parent) && (p.z_rod || p.z_childrod));
			//z_rod: widget is in client rod
			//z_childrod: widget is not, but all descendants are (e.g. combobox)
	}

	function _fixBindLevel(wgt, v) {
		wgt.bindLevel = v++;
		for (wgt = wgt.firstChild; wgt; wgt = wgt.nextSibling)
			_fixBindLevel(wgt, v);
	}

	function _addIdSpace(wgt) {
		if (wgt._fellows) wgt._fellows[wgt.id] = wgt;
		var p = wgt.parent;
		if (p) {
			p = p.$o();
			if (p) p._fellows[wgt.id] = wgt;
		}
	}
	function _rmIdSpace(wgt) {
		if (wgt._fellows) delete wgt._fellows[wgt.id];
		var p = wgt.parent;
		if (p) {
			p = p.$o();
			if (p) delete p._fellows[wgt.id];
		}
	}
	function _addIdSpaceDown(wgt) {
		var ow = wgt.parent;
		ow = ow ? ow.$o(): null;
		if (ow)
			_addIdSpaceDown0(wgt, ow);
	}
	function _addIdSpaceDown0(wgt, owner) {
		if (wgt.id) owner._fellows[wgt.id] = wgt;
		for (wgt = wgt.firstChild; wgt; wgt = wgt.nextSibling)
			_addIdSpaceDown0(wgt, owner);
	}
	function _rmIdSpaceDown(wgt) {
		var ow = wgt.parent;
		ow = ow ? ow.$o(): null;
		if (ow)
			_rmIdSpaceDown0(wgt, ow);
	}
	function _rmIdSpaceDown0(wgt, owner) {
		if (wgt.id) delete owner._fellows[wgt.id];
		for (wgt = wgt.firstChild; wgt; wgt = wgt.nextSibling)
			_rmIdSpaceDown0(wgt, owner);
	}

	//set minimum flex size and return it
	function _setMinFlexSize(wgt, n, o) {
		//find the max size of all children
		if (o == 'height') {
			if (wgt._vflexsz === undefined) { //cached?
				wgt.setFlexSize_({height:'auto'});
				var zkn = zk(n),
					ntop = n.offsetTop,
					noffParent = n.offsetParent,
					pb = zkn.padBorderHeight(),
					max = 0;
				for (var cwgt = wgt.firstChild; cwgt; cwgt = cwgt.nextSibling) {
					var c = cwgt.$n(),
						sz = cwgt._vflex == 'min' && cwgt._vflexsz === undefined ? //recursive 
							_setMinFlexSize(cwgt, c, o) : 
							(c.offsetHeight + c.offsetTop - (c.offsetParent == noffParent ? ntop : 0) + zk(c).sumStyles("b", jq.margins));
					if (sz > max)
						max = sz;
				}
				var margin = zkn.sumStyles("tb", jq.margins),
					sz = wgt.setFlexSize_({height:(max + pb + margin)});
				if (sz && sz.height >= 0)
					wgt._vflexsz = sz.height + margin;
			}
			return wgt._vflexsz;
			
		} else if (o == 'width') {
			if (wgt._hflexsz === undefined) { //cached?
				wgt.setFlexSize_({width:'auto'});
				var zkn = zk(n),
					nleft = n.offsetLeft,
					noffParent = n.offsetParent,
					pb = zkn.padBorderWidth(),
					max = 0;
				for (var cwgt = wgt.firstChild; cwgt; cwgt = cwgt.nextSibling) {
					var c = cwgt.$n(),
						sz = cwgt._hflex == 'min' && cwgt._hflexsz === undefined ? //recursive
							_setMinFlexSize(cwgt, c, o) : 
							(c.offsetWidth + c.offsetLeft - (c.offsetParent == noffParent ? nleft : 0) + zk(c).sumStyles("r", jq.margins));
					if (sz > max)
						max = sz;
				}
				var margin = zkn.sumStyles("lr", jq.margins);
				var sz = wgt.setFlexSize_({width:(max + pb + margin)});
				if (sz && sz.width >= 0)
					wgt._hflexsz = sz.width + margin;
			}
			return wgt._hflexsz;
		} else
			return 0;
	}
	//fix vflex/hflex of all my sibling nodes
	function _fixFlex() {
		if (!this.parent.beforeChildrenFlex_(this)) { //don't do fixflex if return false
			return;
		}
		
		if (this._flexFixed || (!this._nvflex && !this._nhflex)) { //other vflex/hflex sibliing has done it!
			delete this._flexFixed;
			return;
		}
		
		this._flexFixed = true;
		
		var pretxt = false, //pre node is a text node
			prevflex = false, //pre node is vflex
			prehflex = false, //pre node is hflex
			vflexs = [],
			vflexsz = 0,
			hflexs = [],
			hflexsz = 0,
			p = this.$n().parentNode,
			zkp = zk(p),
			psz = this.getParentSize_(p),
			hgh = psz.height,
			wdh = psz.width,
			c = p.firstChild;
		
		for (; c; c = c.nextSibling)
			if (c.nodeType != 3) break; //until not a text node
		
		//ie6 must set parent div to 'relative' or the kid div's offsetTop is not correct
		var oldPos;
		if (zk.ie6_ && p.tagName == 'DIV') {
			oldPos = p.style.position;
			p.style.position = 'relative';
		}
		var sameOffParent = c ? c.offsetParent === p.offsetParent : false,
			tbp = zkp.sumStyles('t', jq.borders),
			lbp = zkp.sumStyles('l', jq.borders),
			segTop = sameOffParent ? (p.offsetTop + tbp) : tbp,
			segLeft = sameOffParent ? (p.offsetLeft + lbp) : lbp,
			segBottom = segTop,
			segRight = segLeft;

		for (; c; c = c.nextSibling) {
			var zkc = zk(c);
			if (zkc.isVisible()) {
				//In ZK, we assume all text node is space (otherwise, it will be span enclosed)
				if (c.nodeType === 3) { //a text node
					pretxt = true;
					prevflex = prehflex = false;
					continue;
				}
				var offhgh = zkc.offsetHeight(),
					offwdh = offhgh > 0 ? zkc.offsetWidth() : 0, //div with zero height might have 100% width
					offTop = c.offsetTop,
					offLeft = c.offsetLeft,
					marginRight = offLeft + offwdh + zkc.sumStyles("r", jq.margins),
					marginBottom = offTop + offhgh + zkc.sumStyles("b", jq.margins);
					
				var cwgt = _binds[c.id];
				//vertical size
				if (cwgt && cwgt._nvflex) {
					if (cwgt !== this)
						cwgt._flexFixed = true; //tell other vflex siblings I have done it.
					if (cwgt._vflex == 'min') {
						_setMinFlexSize(cwgt, c, 'height');
						//might change height in _setMinFlexSize(), so regain the value
						offTop = c.offsetTop;
						offhgh = zkc.offsetHeight();
						marginBottom = offTop + offhgh + zkc.sumStyles('b', jq.margins);
						segBottom = Math.max(segBottom, marginBottom);
						prevflex = false;
					} else {
						if (pretxt) {
							var txtmarginBottom = offTop - zkc.sumStyles('t', jq.margins);
							segBottom = Math.max(segBottom, txtmarginBottom);
						}
						if (!prevflex && segBottom > segTop) {
							hgh -= segBottom - segTop;
						}
						segTop = segBottom = marginBottom;
						
						vflexs.push(cwgt);
						vflexsz += cwgt._nvflex;
						prevflex = true;
					}
				} else {
					segBottom = Math.max(segBottom, marginBottom);
					prevflex = false;
				}
				
				//horizontal size
				if (cwgt && cwgt._nhflex) {
					if (cwgt !== this)
						cwgt._flexFixed = true; //tell other hflex siblings I have done it.
					if (cwgt._hflex == 'min') {
						_setMinFlexSize(cwgt, c, 'width');
						//might change width in _setMinFlexSize(), so regain the value
						offLeft = c.offsetLeft;
						offwdh = zkc.offsetWidth();
						marginRight = offLeft + offwdh + zkc.sumStyles('r', jq.margins);
						segRight = Math.max(segRight, marginRight);
						prehflex = false;
					} else {
						if (pretxt) {
							var txtmarginRight = offTop - zkc.sumStyles('l', jq.margins);
							segRight = Math.max(segRight, txtmarginRight);
						}
						if (!prehflex && segRight > segLeft) {
							wdh -= segRight - segLeft;
						}
						segLeft = segRight = marginRight;
						
						hflexs.push(cwgt);
						hflexsz += cwgt._nhflex;
						prehflex = true;
					}
				} else {
					segRight = Math.max(segRight, marginRight);
					prehflex = false;
				}
				pretxt = false;
			}
		}

		if (zk.ie6_ && p.tagName == 'DIV') { //ie6, restore to orignial position style
			p.style.position = oldPos;
		}

		if (segBottom > segTop) {
			hgh -= segBottom - segTop;
		}
		if (segRight > segLeft) {
			wdh -= segRight - segLeft;
		}
		
		//setup the height for the vflex child
		//avoid floating number calculation error(TODO: shall distribute error evenly)
		var lastsz = hgh > 0 ? hgh : 0;
		for (var j = vflexs.length - 1; j > 0; --j) {
			var cwgt = vflexs.shift(), 
				vsz = (cwgt._nvflex * hgh / vflexsz) | 0; //cast to integer
			cwgt.setFlexSize_({height:vsz});
			cwgt._vflexsz = vsz;
			lastsz -= vsz;
		}
		//last one with vflex
		if (vflexs.length) {
			var cwgt = vflexs.shift();
			cwgt.setFlexSize_({height:lastsz});
			cwgt._vflexsz = lastsz;
		}
		
		//setup the width for the hflex child
		//avoid floating number calculation error(TODO: shall distribute error evenly)
		lastsz = wdh > 0 ? wdh : 0;
		for (var j = hflexs.length - 1; j > 0; --j) {
			var cwgt = hflexs.shift(), //{n: node, f: hflex} 
				hsz = (cwgt._nhflex * wdh / hflexsz) | 0; //cast to integer
			cwgt.setFlexSize_({width:hsz});
			cwgt._hflexsz = hsz;
			lastsz -= hsz;
		}
		//last one with hflex
		if (hflexs.length) {
			var cwgt = hflexs.shift();
			cwgt.setFlexSize_({width:lastsz});
			cwgt._hflexsz = lastsz;
		}
		
		//notify parent widget that all of its children with vflex is done.
		this.parent.afterChildrenFlex_(this);
		this._flexFixed = false;
	}
	function _listenFlex(wgt) {
		if (!wgt._flexListened){
			zWatch.listen({onSize: [wgt, _fixFlex], onShow: [wgt, _fixFlex]});
			wgt._flexListened = true;
		}
	}
	function _unlistenFlex(wgt) {
		if (wgt._flexListened) {
			zWatch.unlisten({onSize: [wgt, _fixFlex], onShow: [wgt, _fixFlex]});
			delete wgt._flexListened;
		}
	}

	/** @class zk.DnD
	 * Drag-and-drop utility.
	 * It is the low-level utility reserved for overriding for advanced customization.
	 */
	zk.DnD = { //for easy overriding
		/** Returns the widget to drop to.
		 * @param zk.Draggable drag the draggable controller
		 * @param Offset pt the mouse pointer's position.
		 * @param jq.Event evt the DOM event
		 * @return zk.Widget
		 */
		getDrop: function (drag, pt, evt) {
			var wgt = evt.target;
			return wgt ? wgt.getDrop_(drag.control): null;
		},
		/** Ghost the DOM element being dragging
		 * @param zk.Draggable drag the draggable controller
		 * @param Offset ofs the offset of the returned element (left/top)
		 * @param String msg the message to show inside the returned element
		 * @return DOMElement the element representing what is being dragged
		 */
		ghost: function (drag, ofs, msg) {
			if (msg != null)  {
				jq(document.body).append(
					'<div id="zk_ddghost" class="z-drop-ghost" style="position:absolute;top:'
					+ofs[1]+'px;left:'+ofs[0]+'px;"><div class="z-drop-cnt"><span id="zk_ddghost-img" class="z-drop-disallow"></span>&nbsp;'+msg+'</div></div>');
				drag._dragImg = jq("#zk_ddghost-img")[0];
				return jq("#zk_ddghost")[0];
			}

			var dgelm = jq(drag.node).clone()[0];
			dgelm.id = "zk_ddghost";
			zk.copy(dgelm.style, {
				position: "absolute", left: ofs[0] + "px", top: ofs[1] + "px"
			});
			document.body.appendChild(dgelm);
			return dgelm;
		}
	};
	function DD_cleanLastDrop(drag) {
		if (drag) {
			var drop;
			if (drop = drag._lastDrop) {
				drag._lastDrop = null;
				drop.dropEffect_();
			}
			drag._lastDropTo = null;
		}
	}
	function DD_pointer(evt) {
		return [evt.pageX + 10, evt.pageY + 5];
	}
	function DD_enddrag(drag, evt) {
		DD_cleanLastDrop(drag);
		var pt = [evt.pageX, evt.pageY],
			wgt = zk.DnD.getDrop(drag, pt, evt);
		if (wgt) wgt.onDrop_(drag, evt);
	}
	function DD_dragging(drag, pt, evt) {
		var dropTo;
		if (!evt || (dropTo = evt.domTarget) == drag._lastDropTo)
			return;

		var dropw = zk.DnD.getDrop(drag, pt, evt),
			found = dropw && dropw == drag._lastDrop;
		if (!found) {
			DD_cleanLastDrop(drag); //clean _lastDrop
			if (dropw) {
				drag._lastDrop = dropw;
				dropw.dropEffect_(true);
				found = true;
			}
		}

		var dragImg = drag._dragImg;
		if (dragImg)
			dragImg.className = found ? 'z-drop-allow': 'z-drop-disallow';

		drag._lastDropTo = dropTo; //do it after _cleanLastDrop
	}
	function DD_ghosting(drag, ofs, evt) {
		return drag.control.cloneDrag_(drag, DD_pointer(evt));
	}
	function DD_endghosting(drag, origin) {
		drag.control.uncloneDrag_(drag);
		drag._dragImg = null;
	}
	function DD_constraint(drag, pt, evt) {
		return DD_pointer(evt);
	}
	function DD_ignoredrag(drag, pt, evt) {
		return drag.control.ingoreDrag_(pt);
	}

	function _topnode(n) {
		for (var v; n && n != document.body; n = n.parentNode) //no need to check vparentNode
			if ((v=n.style) && ((v=v.position) == 'absolute' || v == 'relative'))
				return n;
	}
	function _zIndex(n) {
		return n ? zk.parseInt(n.style.zIndex): 0;
	}

	function _getFirstNodeDown(wgt) {
		var n = wgt.$n();
		if (n) return n;
		for (var w = wgt.firstChild; w; w = w.nextSibling) {
			n = w._getFirstNodeDown();
			if (n) return n;
		}
	}
	//Returns if the specified widget's visibility depends the self widget.
	function _floatVisibleDependent(self, wgt) {
		for (; wgt; wgt = wgt.parent)
			if (wgt == self) return true;
			else if (!wgt.isVisible()) break;
		return false;
	}

	//Returns the topmost z-index for this widget
	function _topZIndex(wgt) {
		var zi = 1800; // we have to start from 1800 depended on all the css files.
		for (var j = _floatings.length; j--;) {
			var w = _floatings[j].widget;
			if (w._zIndex >= zi && !zUtl.isAncestor(wgt, w) && w.isVisible())
				zi = w._zIndex + 1;
		}
		return zi;
	}

	function _prepareRemove(wgt, ary) {
		for (wgt = wgt.firstChild; wgt; wgt = wgt.nextSibling) {
			var n = wgt.$n();
			if (n) ary.push(n);
			else _prepareRemove(wgt, ary);
		}
	}

/** A widget, i.e., an UI object.
 * Each component running at the server is associated with a widget
 * running at the client.
 * Refer to <a href="http://docs.zkoss.org/wiki/ZK5:_Component_Development_Guide">Component Development Guide</a>
 * for more information.
 * <p>Notice that, unlike the component at the server, {@link zk.Desktop}
 * and {@link zk.Page} are derived from zk.Widget. It means desktops, pages and widgets are in a widget tree. 
 */
zk.Widget = zk.$extends(zk.Object, {
	_visible: true,
	/** The number of children (readonly).
	 * @type int
	 */
	nChildren: 0,
	/** The bind level (readonly)
	 * The level in the widget tree after this widget is bound to a DOM tree ({@link #bind_}).
	 * For example, a widget's bind level is one plus the parent widget's
	 * <p>It starts at 0 if it is the root of the widget tree (a desktop, zk.Desktop), then 1 if a child of the root widget, and son on. Notice that it is -1 if not bound.
	 * <p>It is mainly useful if you want to maintain a list that parent widgets is in front of (or after) child widgets. 
	 * bind level.
	 * @type int
	 */
	bindLevel: -1,
	_mold: 'default',
	/** The class name of the widget.
	 * For example, zk.Widget's class name is "zk.Widget", while
	 * zul.wnd.Window's "zul.wnd.Window".
	 * <p>Notice that it is available if a widget class is loaded by WPD loader (i.e., specified in zk.wpd). If you create a widget class dynamically, you have to invoke #register to make this member available. 
	 * @type String
	 */
	className: 'zk.Widget',
	_floating: false,

	/** The first child, or null if no child at all (readonly).
	 * @see #getChildAt
	 * @type zk.Widget
	 */
	//firstChild: null,
	/** The last child, or null if no child at all (readonly).
	 * @see #getChildAt
	 * @type zk.Widget
	 */
	//firstChild: null,
	/** The parent, or null if this widget has no parent (readonly).
	 * @type zk.Widget
	 */
	//parent: null,
	/** The next sibling, or null if this widget is the last child (readonly).
	 * @type zk.Widget
	 */
	//nextSibling: null,
	/** The previous sibling, or null if this widget is the first child (readonly).
	 * @type zk.Widget
	 */
	//previousSibling: null,
	/** The desktop that this widget belongs to (readonly).
	 * It is set when it is bound to the DOM tree.
	 * <p>Notice it is always non-null if bound to the DOM tree, while
	 * {@link #$n} is always non-null if bound. For example, {@link zul.utl.Timer}.
	 * <p>It is readonly, and set automcatically when {@link #bind_} is called. 
	 * @type zk.Desktop
	 */
	//desktop: null,
	/** The identifier of this widget, or null if not assigned (readonly).
	 * It is the same as {@link #getId}.
	 * <p>To change the value, use {@link #setId}.
	 * @type String the ID
	 */
	//id: null,
	/** Whether this widget has a peer component (readonly).
	 * It is set if a widget is created automatically to represent a component
	 ( at the server. On the other hand, it is false if a widget is created
	 * by the client application (by calling, say, <code>new zul.inp.Textox()</code>). 
	 * @type boolean
	 */
	//inServer: false,
	/** The UUID. Don't change it if it is bound to the DOM tree, or {@link #inServer} is true.
	 * Developers rarely need to modify it since it is generated automatically. 
	 * <h3>Note of ZK Light</h3>
	 * It is the same as {@link #id} if {@link zk#spaceless} is true,
	 * such as ZK Light.
	 * @type String
	 */
	//uuid: null,

	/** The constructor.
	 * For example,
<pre><code>
new zul.wnd.Window{
  border: 'normal',
  title: 'Hello World',
  closable: true
});
</code></pre>
	 * @param Map props the properties to be assigned to this widget.
	 */
	$init: function (props) {
		this._asaps = {}; //event listened at server
		this._lsns = {}; //listeners(evtnm,listener)
		this._bklsns = {}; //backup for listners by setListeners
		this._subnodes = {}; //store sub nodes for widget(domId, domNode)

		this.$afterInit(function () {
			if (props) {
				var mold = props.mold;
				if (mold != null) {
					if (mold) this._mold = mold;
					delete props.mold; //avoid setMold being called
				}
				for (var nm in props)
					this.set(nm, props[nm]);
			}

			if (zk.spaceless) {
				if (this.id) this.uuid = this.id; //setId was called
				else if (this.uuid) this.id = this.uuid;
				else this.uuid = this.id = zk.Widget.nextUuid();
			} else if (!this.uuid) this.uuid = zk.Widget.nextUuid();
		});
	},

	$define: {
		mold: function () {
			this.rerender();
		},
		style: function () {
			this.updateDomStyle_();
		},
		sclass: function () {
			this.updateDomClass_();
		},
		zclass: function (){
			this.rerender();
		},
		width: function (v) {
			if (!this._nhflex) {
				var n = this.$n();
				if (n) n.style.width = v || '';
			}
		},
		height: function (v) {
			if (!this._nvflex) {
				var n = this.$n();
				if (n) n.style.height = v || '';
			}
		},
		left: function (v) {
			var n = this.$n();
			if (n) n.style.left = v || '';
		},
		top: function (v) {
			var n = this.$n();
			if (n) n.style.top = v || '';
		},
		tooltiptext: function (v) {
			var n = this.$n();
			if (n) n.title = v || '';
		},

		draggable: [
			_zkf = function (v) {
				return v && "false" != v ? v: null;
			},
			function (v) {
				var n = this.$n();
				if (this.desktop)
					if (v) this.initDrag_();
					else this.cleanDrag_();
			}
		],
		droppable: [
			_zkf,
			function (v) {
				var dropTypes;
				if (v && v != "true") {
					dropTypes = v.split(',');
					for (var j = dropTypes.length; j--;)
						if (!(dropTypes[j] = dropTypes[j].trim()))
							dropTypes.splice(j, 1);
				}
				this._dropTypes = dropTypes;
			}
		],
		vflex: function(v) {
			this._nvflex = (true === v || 'true' == v) ? 1 : v == 'min' ? -65500 : zk.parseInt(v);
			if (this._nvflex < 0 && v != 'min')
				this._nvflex = 0;
			if (_binds[this.uuid] === this) { //if already bind
				if (!this._nvflex) {
					this.setFlexSize_({height: ''}); //clear the height
					delete this._vflexsz;
					if (!this._nhflex)
						_unlistenFlex(this);
				} else
					_listenFlex(this);
				zWatch.fireDown('onSize', this.parent);
			}
		},
		hflex: function(v) {
			this._nhflex = (true === v || 'true' == v) ? 1 : v == 'min' ? -65500 : zk.parseInt(v);
			if (this._nhflex < 0 && v != 'min')
				this._nhflex = 0; 
			if (_binds[this.uuid] === this) { //if already bind
				if (!this._nhflex) {
					this.setFlexSize_({width: ''}); //clear the width
					delete this._hflexsz;
					if (!this._nvflex)
						_unlistenFlex(this);
				} else
					_listenFlex(this);
				zWatch.fireDown('onSize', this.parent);
			}
		}
	},
	$o: _zkf = function () {
		for (var w = this; w; w = w.parent)
			if (w._fellows) return w;
	},
	getSpaceOwner: _zkf,
	$f: _zkf = function (id, global) {
		var f = this.$o();
		for (var ids = id.split('/'), j = 0, len = ids.length; j < len; ++j) {
			id = ids[j];
			if (id) {
				if (f) f = f._fellows[id];
				if (!f && global) f = _globals[id];
				if (!f || zk.spaceless) break;
				global = false;
			}
		}
		return f;
	},
	getFellow: _zkf,
	/** Returns the identifier of this widget, or null if not assigned.
	 * It is the same as {@link #id}.
	 * @return String the ID
	 */
	getId: function () {
		return this.id;
	},
	setId: function (id) {
		if (id != this.id) {
			if (zk.spaceless && this.desktop)
				throw 'id cannot be changed after bound'; //since there might be subnodes

			var old = this.id;
			if (old) {
				if (!zk.spaceless) delete _globals[id];
				_rmIdSpace(this);
			}

			this.id = id;
			if (zk.spaceless) this.uuid = id;

			if (id) {
				if (!zk.spaceless) _globals[id] = this;
				_addIdSpace(this);
			}
		}
		return this;
	},

	/** Sets a property.
	 * @param String name the name of property.
	 * If the name starts with <code>on</code>, it is assumed to be
	 * an event listener and {@link #setListener} will be called.
	 * @param Object value the value
	 * @param Object extra the extra argument. It could be anything.
	 */
	set: function (name, value, extra) {
		var cc;
		if (name.length > 4 && name.startsWith('$$on')) {
			var cls = this.$class,
				ime = cls._importantEvts;
			(ime || (cls._importantEvts = {}))[name.substring(2)] = value;
		} else if (name.length > 3 && name.startsWith('$on'))
			this._asaps[name.substring(1)] = value;
		else if (name.length > 2 && name.startsWith('on')
		&& (cc = name.charAt(2)) >= 'A' && cc <= 'Z')
			this.setListener(name, value);
		else if (arguments.length >= 3)
			zk.set(this, name, value, extra);
		else
			zk.set(this, name, value);
		return this;
	},
	/** Return the child widget at the specified index.
	 * <p>Notice this method is not good if there are a lot of children
	 * since it iterates all children one by one.
	 * @param j the index of the child widget to return. 0 means the first
	 * child, 1 for the second and so on.
	 * @return zk.Widget the widget or null if no such index
	 * @see #getChildIndex
	 */
	getChildAt: function (j) {
		if (j >= 0 && j < this.nChildren)
			for (var w = this.firstChild; w; w = w.nextSibling)
				if (--j < 0)
					return w;
	},
	/** Returns the child index of this widget.
	 * By child index we mean the order of the child list of the parent. For example, if this widget is the parent's first child, then 0 is returned. 
	 * <p>Notice that {@link #getChildAt} is called against the parent, while
	 * this method called against the child. In other words,
	 * <code>w.parent.getChildAt(w.getChildIndex())</code> returns <code>w</code>.
	 * <p>Notice this method is not good if there are a lot of children
	 * since it iterates all children one by one.
	 * @return int the child index
	 */
	getChildIndex: function () {
		var w = this.parent, j = 0;
		if (w)
			for (w = w.firstChild; w; w = w.nextSibling, ++j)
				if (w == this)
					return j;
		return 0;
	},
	/** Appends an array of children.
	 * Notice this method does NOT remove any existent child widget.
	 * @param Array<zk.Widget> children an array of children to add
	 * @return zk.Widget this widget
	 */
	setChildren: function (children) {
		if (children)
			for (var j = 0, l = children.length; j < l;)
				this.appendChild(children[j++]);
		return this;
	},
	appendChild: function (child, _ignoreBind_) {
		if (child == this.lastChild)
			return false;

		var oldpt = child.parent;
		if (oldpt != this)
			child.beforeParentChanged_(this);

		if (oldpt)
			oldpt.removeChild(child);

		child.parent = this;
		var ref = this.lastChild;
		if (ref) {
			ref.nextSibling = child;
			child.previousSibling = ref;
			this.lastChild = child;
		} else {
			this.firstChild = this.lastChild = child;
		}
		++this.nChildren;

		_addIdSpaceDown(child);

		if (!_ignoreBind_)
			if (_isrod(child))
				_bindrod(child);
			else {
				var dt = this.desktop;
				if (dt) this.insertChildHTML_(child, null, dt);
			}

		this.onChildAdded_(child);
		return true;
	},
	insertBefore: function (child, sibling, _ignoreBind_) {
		if (!sibling || sibling.parent != this)
			return this.appendChild(child, _ignoreBind_);

		if (child == sibling || child.nextSibling == sibling)
			return false;

		if (child.parent != this)
			child.beforeParentChanged_(this);

		if (child.parent)
			child.parent.removeChild(child);

		child.parent = this;
		var ref = sibling.previousSibling;
		if (ref) {
			child.previousSibling = ref;
			ref.nextSibling = child;
		} else this.firstChild = child;

		sibling.previousSibling = child;
		child.nextSibling = sibling;

		++this.nChildren;

		_addIdSpaceDown(child);

		if (!_ignoreBind_)
			if (_isrod(child))
				_bindrod(child);
			else {
				var dt = this.desktop;
				if (dt) this.insertChildHTML_(child, sibling, dt);
			}

		this.onChildAdded_(child);
		return true;
	},
	/** Removes a child.
	 * @param zk.Widget child the child to remove.
	 */
	removeChild: function (child, _ignoreDom_) {
		if (!child.parent)
			return false;
		if (this != child.parent)
			return false;

		child.beforeParentChanged_(null);

		var p = child.previousSibling, n = child.nextSibling;
		if (p) p.nextSibling = n;
		else this.firstChild = n;
		if (n) n.previousSibling = p;
		else this.lastChild = p;
		child.nextSibling = child.previousSibling = child.parent = null;

		--this.nChildren;

		_rmIdSpaceDown(child);

		if (_isrod(child))
			_unbindrod(child);
		else if (child.desktop)
			this.removeChildHTML_(child, p, _ignoreDom_);
		this.onChildRemoved_(child);
		return true;
	},
	detach: function () {
		if (this.parent) this.parent.removeChild(this);
		else {
			var cf = zk.currentFocus;
			if (cf && zUtl.isAncestor(this, cf))
				zk.currentFocus = null;
			var n = this.$n();
			if (n) {
				this.unbind();
				jq(n).remove();
			}
		}
	},
	clear: function () {
		while (this.lastChild)
			this.removeChild(this.lastChild);
	},
	_replaceWgt: function (newwgt) { //called by au's outer
		var node = this.$n(),
			p = newwgt.parent = this.parent,
			s = newwgt.previousSibling = this.previousSibling;
		if (s) s.nextSibling = newwgt;
		else if (p) p.firstChild = newwgt;

		s = newwgt.nextSibling = this.nextSibling;
		if (s) s.previousSibling = newwgt;
		else if (p) p.lastChild = newwgt;

		_rmIdSpaceDown(this);
		_addIdSpaceDown(newwgt);

		if (_isrod(this)) {
			_unbindrod(this);
			_bindrod(newwgt);
		} else if (this.desktop) {
			if (!newwgt.desktop) newwgt.desktop = this.desktop;
			if (node) newwgt.replaceHTML(node, newwgt.desktop);
			else {
				this.unbind();
				newwgt.bind();
			}

			_fixBindLevel(newwgt, p ? p.bindLevel + 1: 0);
			zWatch.fire('onBindLevelMove', newwgt);
		}

		if (p) {
			p.onChildRemoved_(this);
			p.onChildAdded_(newwgt);
		}
		//avoid memory leak
		this.parent = this.nextSibling = this.previousSibling
			= this._node = this._nodeSolved = null;
		this._subnodes = {};
	},
	/** Replaced the child widgets with the specified.
	 * It is usefull if you want to replace a part of children whose
	 * DOM element is a child element of <code>subId</code> (this.$n(subId)).
	 * @param String subId the ID of the cave that contains the child widgets
	 * to replace with.
	 * @param Array wgts an arrray of widgets that will become children of this widget
	 * @param String tagBeg the beginning of HTML tag, such as <tbody>.
	 * Ignored if null.
	 * @param String tagEnd the ending of HTML tag, such as </tbody>
	 * Ignored if null.
	 * @see zAu.createWidgets
	 */
	replaceCavedChildren_: function (subId, wgts, tagBeg, tagEnd) {
		//1. remove (but don't update DOM)
		var cave = this.$n(subId), fc;
		for (var w = this.firstChild; w;) {
			var sib = w.nextSibling;
			if (jq.isAncestor(cave, w.$n())) {
				if (!fc || fc == w) fc = sib;
				this.removeChild(w, true);
			}
			w = sib;
		}

		//2. insert (but don't update DOM)
		for (var j = 0, len = wgts.length; j < len; ++j)
			this.insertBefore(wgts[j], fc, true);

		if (fc = this.desktop) {
			//3. generate HTML
			var out = [];
			if (tagBeg) out.push(tagBeg);
			for (var j = 0, len = wgts.length; j < len; ++j)
				wgts[j].redraw(out);
			if (tagEnd) out.push(tagEnd);

			//4. update DOM
			jq(cave).html(out.join(''));

			//5. bind
			for (var j = 0, len = wgts.length; j < len; ++j)
				wgts[j].bind(fc);
		}
	},

	beforeParentChanged_: function () {
	},

	isRealVisible: function (opts) {
		var dom = opts && opts.dom;
		for (var wgt = this; wgt; wgt = wgt.parent) {
			if (dom) {
				if (!zk(wgt.$n()).isVisible())
					return false;
			} else if (!wgt.isVisible())
				return false;

			//check if it is hidden by parent, such as child of hbox/vbox or border-layout
			var p = wgt.parent, n;
			if (p && p.isVisible() && (p=p.$n()) && (n=wgt.$n()))
				while ((n=zk(n).vparentNode()||n.parentNode) && p != n)
					if ((n.style||{}).display == 'none')
						return false; //hidden by parent

			if (opts && opts.until == wgt)
				break;
		}
		return true;
	},
	isVisible: function (strict) {
		var visible = this._visible;
		if (!strict || !visible)
			return visible;
		var n = this.$n();
		return !n || zk(n).isVisible();
	},
	setVisible: function (visible) {
		if (this._visible != visible) {
			this._visible = visible;

			var p = this.parent;
			if (p && visible) p.onChildVisible_(this, true); //becoming visible
			if (this.desktop) {
				var parentVisible = !p || p.isRealVisible(),
					node = this.$n(),
					floating = this._floating;

				if (!parentVisible) {
					if (!floating) this.setDomVisible_(node, visible);
					return;
				}

				if (visible) {
					var zi;
					if (floating)
						this.setZIndex(zi = _topZIndex(this), {fire:true});

					this.setDomVisible_(node, true);

					//from parent to child
					for (var j = 0, fl = _floatings.length; j < fl; ++j) {
						var w = _floatings[j].widget,
							n = _floatings[j].node;
						if (this == w)
							w.setDomVisible_(n, true, {visibility:1});
						else if (_floatVisibleDependent(this, w)) {
							zi = zi >= 0 ? ++zi: _topZIndex(w);
							if (n != w.$n()) w.setFloatZIndex_(n, zi); //only a portion
							else w.setZIndex(zi, {fire:true});
		
							w.setDomVisible_(n, true, {visibility:1});
						}
					}

					zWatch.fireDown('onShow', this);
				} else {
					zWatch.fireDown('onHide', this);

					for (var j = _floatings.length, bindLevel = this.bindLevel; j--;) {
						var w = _floatings[j].widget;
						if (bindLevel >= w.bindLevel)
							break; //skip non-descendant (and this)
						if (_floatVisibleDependent(this, w))
							w.setDomVisible_(_floatings[j].node, false, {visibility:1});
					}

					this.setDomVisible_(node, false);
				}
			}
			if (p && !visible) p.onChildVisible_(this, false); //become invisible
		}
		return this;
	},
	show: function () {this.setVisible(true);},
	hide: function () {this.setVisible(false);},
	setDomVisible_: function (n, visible, opts) {
		if (!opts || opts.display)
			n.style.display = visible ? '': 'none';
		if (opts && opts.visibility)
			n.style.visibility = visible ? 'visible': 'hidden';
	},
	onChildAdded_: function (/*child*/) {
	},
	onChildRemoved_: function (/*child*/) {
	},
	onChildVisible_: function (/*child, visible*/) {
	},
	setTopmost: function () {
		if (!this.desktop) return -1;

		for (var wgt = this; wgt; wgt = wgt.parent)
			if (wgt._floating) {
				var zi = _topZIndex(wgt);
				wgt.setZIndex(zi, {fire:true});

				for (var j = 0, fl = _floatings.length; j < fl; ++j) { //parent first
					var w = _floatings[j].widget;
					if (wgt != w && zUtl.isAncestor(wgt, w) && w.isVisible()) {
						var n = _floatings[j].node;
						if (n != w.$n()) w.setFloatZIndex_(n, ++zi); //only a portion
						else w.setZIndex(++zi, {fire:true});
					}
				}
				return zi;
			}
		return -1;
	},
	/** Returns the top widget, which is the first floating ancestor,
	 * or null if no floating ancestor.
	 * @return zk.Widget
	 * @see #isFloating_
	 */
	getTopWidget: function () {
		for (var wgt = this; wgt; wgt = wgt.parent)
			if (wgt._floating)
				return wgt;
	},
	isFloating_: function () {
		return this._floating;
	},
	setFloating_: function (floating, opts) {
		if (this._floating != floating) {
			if (floating) {
				//parent first
				var inf = {widget: this, node: opts && opts.node? opts.node: this.$n()},
					bindLevel = this.bindLevel;
				for (var j = _floatings.length;;) {
					if (--j < 0) {
						_floatings.unshift(inf);
						break;
					}
					if (bindLevel >= _floatings[j].widget.bindLevel) { //parent first
						_floatings.splice(j + 1, 0, inf);
						break;
					}
				}
				this._floating = true;
			} else {
				for (var j = _floatings.length; j--;)
					if (_floatings[j].widget == this)
						_floatings.splice(j, 1);
				this._floating = false;
			}
		}
		return this;
	},

	getZIndex: _zkf = function () {
		return this._zIndex;
	},
	getZindex: _zkf,
	setZIndex: _zkf = function (zIndex, opts) {
		if (this._zIndex != zIndex) {
			this._zIndex = zIndex;
			var n = this.$n();
			if (n) {
				n.style.zIndex = zIndex = zIndex >= 0 ? zIndex: '';
				if (opts && opts.fire) this.fire('onZIndex', zIndex, {ignorable: true});
			}
		}
		return this;
	},
	setZindex: _zkf,

	getScrollTop: function () {
		var n = this.$n();
		return n ? n.scrollTop: 0;
	},
	getScrollLeft: function () {
		var n = this.$n();
		return n ? n.scrollLeft: 0;
	},
	setScrollTop: function (val) {
		var n = this.$n();
		if (n) n.scrollTop = val;
		return this;
	},
	setScrollLeft: function (val) {
		var n = this.$n();
		if (n) n.scrollLeft = val;
		return this;
	},
	scrollIntoView: function () {
		zk(this.$n()).scrollIntoView();
		return this;
	},

	redraw: function (out) {
		var s = this.prolog;
		if (s) out.push(s);

		for (var p = this, mold = this._mold; p; p = p.superclass) {
			var f = p.$class.molds[mold];
			if (f) return f.apply(this, arguments);
		}
		throw "mold "+mold+" not found in "+this.className;
	},
	updateDomClass_: function () {
		if (this.desktop) {
			var n = this.$n();
			if (n) n.className = this.domClass_();
		}
	},
	updateDomStyle_: function () {
		if (this.desktop) {
			var s = jq.parseStyle(this.domStyle_());
			zk(this.$n()).setStyles(s);

			var n = this.getTextNode();
			if (n) zk(n).css(jq.filterTextStyle(s));
		}
	},
	getTextNode: function () {
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
			var s = this.domStyle_(no);
			if (s) html += ' style="' + s + '"';
		}
		if (!no || !no.domclass) {
			var s = this.domClass_();
			if (s) html += ' class="' + s + '"';
		}
		if (!no || !no.tooltiptext) {
			var s = this._tooltiptext;
			if (s) html += ' title="' + s + '"';
		}
		return html;
	},
	domTextStyleAttr_: function () {
		var s = this.getStyle();
		if (s) {
			s = jq.filterTextStyle(s);
			if (s) s = ' style="' + s + '"';
		}
		return s;
	},

	replaceHTML: function (n, desktop, skipper) {
		if (!desktop) {
			desktop = this.desktop;
			if (!zk.Desktop._ndt) zk.stateless();
		}

		var cf = zk.currentFocus;
		if (cf && zUtl.isAncestor(this, cf)) {
			zk.currentFocus = null;
		} else
			cf = null;

		var p = this.parent;
		if (p) p.replaceChildHTML_(this, n, desktop, skipper);
		else {
			var oldwgt = zk.Widget.$(n, {exact:true});
			if (oldwgt) oldwgt.unbind(skipper); //unbind first (w/o removal)
			else if (_isrod(this)) _unbindrod(this); //possible (if replace directly)
			jq(n).replaceWith(this.redrawHTML_(skipper, true));
			this.bind(desktop, skipper);
		}

		if (!skipper) {
			zWatch.fireDown('beforeSize', this);
			zWatch.fireDown('onSize', this);
		}

		if (cf && cf.desktop && !zk.currentFocus) cf.focus();
		return this;
	},
	/** Returns the HTML fragment of this widget.
	 * @param zk.Skipper skipper the skipper. Ignored if null
	 * @param boolean noprolog whether <i>not</i> to generate the prolog
	 * @return String the HTML fragment
	 */
	redrawHTML_: function (skipper, noprolog) {
		var out = [];
		this.redraw(out, skipper);
		if (noprolog && this.prolog && out[0] == this.prolog)
			out[0] = '';
			//Don't generate this.prolog if it is the one to re-render;
			//otherwise, prolog will be generated twice if invalidated
			//test: <div> <button onClick="self.invalidate()"/></div>
		return out.join('');
	},
	rerender: function (skipper) {
		if (this.desktop) {
			var n = this.$n();
			if (n) {
				if (skipper) {
					var skipInfo = skipper.skip(this);
					if (skipInfo) {
						this.replaceHTML(n, null, skipper);

						skipper.restore(this, skipInfo);

						zWatch.fireDown('beforeSize', this);
						zWatch.fireDown('onSize', this);
						return this; //done
					}
				}
				this.replaceHTML(n);
			}
		}
		return this;
	},

	replaceChildHTML_: function (child, n, desktop, skipper) {
		var oldwgt = zk.Widget.$(n, {exact:true});
		if (oldwgt) oldwgt.unbind(skipper); //unbind first (w/o removal)
		else if (_isrod(child)) _unbindrod(child); //possible (e.g., Errorbox: jq().replaceWith)
		jq(n).replaceWith(child.redrawHTML_(skipper, true));
		child.bind(desktop, skipper);
	},
	insertChildHTML_: function (child, before, desktop) {
		var ben;
		if (before)
			before = before.getFirstNode_();
		if (!before)
			for (var w = this;;) {
				ben = w.getCaveNode();
				if (ben) break;

				var w2 = w.nextSibling;
				if (w2 && (before = w2.getFirstNode_()))
					break;

				if (!(w = w.parent)) {
					ben = document.body;
					break;
				}
			}

		if (before) {
			var sib = before.previousSibling;
			if (_isProlog(sib)) before = sib;
			jq(before).before(child.redrawHTML_());
		} else
			jq(ben).append(child.redrawHTML_());
		child.bind(desktop);
	},
	getCaveNode: function () {
		return this.$n('cave') || this.$n();
	},
	/** Returns the first DOM element of this widget.
	 * If this widget has no corresponding DOM element, this method will look
	 * for its siblings.
	 * <p>This method is designed to be used with {@link #insertChildHTML_}
	 * for retrieving the DOM element of the <code>before</code> widget.
	 */
	getFirstNode_: function () {
		for (var w = this; w; w = w.nextSibling) {
			var n = _getFirstNodeDown(w);
			if (n) return n;
		}
	},
	removeChildHTML_: function (child, prevsib, _ignoreDom_) {
		var cf = zk.currentFocus;
		if (cf && zUtl.isAncestor(child, cf))
			zk.currentFocus = null;

		var n = child.$n();
		if (n) {
			var sib = n.previousSibling;
			if (child.prolog && _isProlog(sib))
				jq(sib).remove();
		} else
			_prepareRemove(child, n = []);

		child.unbind();

		if (!_ignoreDom_)
			jq(n).remove();
	},
	$n: _zkf = function (name) {
		if (name) {
			var n = this._subnodes[name];
			if (!n && this.desktop)
				n = this._subnodes[name] = jq(this.uuid + '-' + name, zk)[0];
			return n;
		}
		var n = this._node;
		if (!n && this.desktop && !this._nodeSolved) {
			this._node = n = jq(this.uuid, zk)[0];
			this._nodeSolved = true;
		}
		return n;
	},
	/** Clears the cached nodes (by {@link #$n}). */
	clearCache: function () {
		this._node = null;
		this._subnodes = {};
		this._nodeSolved = false;
	},
	getNode: _zkf,
	getPage: function () {
		if (this.desktop && this.desktop.nChildren == 1)
			return this.desktop.firstChild;
			
		for (var page = this.parent; page; page = page.parent)
			if (page.$instanceof(zk.Page))
				return page;
				
		return null;
	},
	bind: function (desktop, skipper) {
		if (this.z_rod) 
			_bindrod(this);
		else {
			var after = [];
			this.bind_(desktop, skipper, after);
			for (var j = 0, len = after.length; j < len;)
				after[j++]();
		}
		return this;
	},
	unbind: function (skipper) {
		if (this.z_rod)
			_unbindrod(this);
		else {
			var after = [];
			this.unbind_(skipper, after);
			for (var j = 0, len = after.length; j < len;)
				after[j++]();
		}
		return this;
	},

	bind_: function (desktop, skipper, after) {
		_bind0(this);

		if (!desktop) desktop = zk.Desktop.$(this.uuid);
		this.desktop = desktop;

		var p = this.parent;
		this.bindLevel = p ? p.bindLevel + 1: 0;

		if (this._draggable) this.initDrag_();
		
		if (this._nvflex || this._nhflex)
			_listenFlex(this);

		for (var child = this.firstChild; child; child = child.nextSibling)
			if (!skipper || !skipper.skipped(this, child))
				if (child.z_rod) _bindrod(child);
				else child.bind_(desktop, null, after); //don't pass skipper

		if (this.isListen('onBind')) {
			var self = this;
			zk.afterMount(function () {
				if (self.desktop) //might be unbound
					self.fire('onBind');
			});
		}
	},

	unbind_: function (skipper, after) {
		_unbind0(this);
		_fixBindMem();
		_unlistenFlex(this);

		for (var child = this.firstChild; child; child = child.nextSibling)
			if (!skipper || !skipper.skipped(this, child))
				if (child.z_rod) _unbindrod(child);
				else child.unbind_(null, after); //don't pass skipper

		if (this._draggable) this.cleanDrag_();

		if (this.isListen('onUnbind')) {
			var self = this;
			zk.afterMount(function () {
				if (!self.desktop) //might be bound
					self.fire('onUnbind');
			});
		}
	},
	extraBind_: function (id, add) {
		if (add == false) delete _binds[id];
		else _binds[id] = this;
	},
	setFlexSize_: function(sz) {
		var n = this.$n();
		if (sz.height !== undefined) {
			if (sz.height == 'auto')
				n.style.height = '';
			else if (sz.height != '')
				n.style.height = jq.px0(zk(n).revisedHeight(sz.height, true));
			else
				n.style.height = this._height ? this._height : '';
		}
		if (sz.width !== undefined) {
			if (sz.width == 'auto')
				n.style.width = '';
			else if (sz.width != '')
				n.style.width = jq.px0(zk(n).revisedWidth(sz.width, true));
			else
				n.style.width = this._width ? this._width : '';
		}
		return {height: n.offsetHeight, width: n.offsetWidth};
	},
	beforeChildrenFlex_: function(kid) {
		//to be overridden
		return true; //return true to continue children flex fixing
	},
	afterChildrenFlex_: function(kid) {
		//to be overridden
	},
	getParentSize_: function(p) {
		//to be overridden
		var zkp = zk(p);
		return zkp ? {height: zkp.revisedHeight(p.offsetHeight), width: zkp.revisedWidth(p.offsetWidth)} : {};
	},
	fixFlex_: function() {
		_fixFlex.apply(this);
	},
	initDrag_: function () {
		this._drag = new zk.Draggable(this, this.getDragNode(), zk.copy({
			starteffect: zk.$void, //see bug #1886342
			endeffect: DD_enddrag, change: DD_dragging,
			ghosting: DD_ghosting, endghosting: DD_endghosting,
			constraint: DD_constraint,
			ignoredrag: DD_ignoredrag,
			zIndex: 88800
		}, this.getDragOptions_()));
	},
	cleanDrag_: function () {
		var drag = this._drag;
		if (drag) {
			this._drag = null;
			drag.destroy();
		}
	},
	getDragNode: function () {
		return this.$n();
	},
	getDragOptions_: function () {
	},
	ingoreDrag_: function (pt) {
		return false;
	},
	getDrop_: function (dragged) {
		if (this != dragged) {
			var dropType = this._droppable,
				dragType = dragged._draggable;
			if (dropType == 'true') return this;
			if (dropType && dragType != "true")
				for (var dropTypes = this._dropTypes, j = dropTypes.length; j--;)
					if (dragType == dropTypes[j])
						return this;
		}
		return this.parent ? this.parent.getDrop_(dragged): null;
	},
	dropEffect_: function (over) {
		jq(this.$n()||[])[over ? "addClass" : "removeClass"]("z-drag-over");
	},
	getDragMessage_: function () {
		var tn = this.getDragNode().tagName;
		if ("TR" == tn || "TD" == tn || "TH" == tn) {
			var n = this.$n('real') || this.getCaveNode();
			return n ? n.textContent || n.innerText || '': '';
		}
	},
	onDrop_: function (drag, evt) {
		var data = zk.copy({dragged: drag.control}, evt.data);
		this.fire('onDrop', data, null, 38);
	},
	cloneDrag_: function (drag, ofs) {
		//See also bug 1783363 and 1766244

		var msg = this.getDragMessage_();
		if (typeof msg == 'string' && msg.length > 15)
			msg = msg.substring(0, 15) + "...";

		var dgelm = zk.DnD.ghost(drag, ofs, msg);

		drag._orgcursor = document.body.style.cursor;
		document.body.style.cursor = "pointer";
		jq(this.getDragNode()).addClass('z-dragged'); //after clone
		return dgelm;
	},
	uncloneDrag_: function (drag) {
		document.body.style.cursor = drag._orgcursor || '';

		jq(this.getDragNode()).removeClass('z-dragged');
	},

	focus: function (timeout) {
		var node;
		if (this.isVisible() && this.canActivate({checkOnly:true})
		&& (node = this.$n())) {
			if (zk(node).focus(timeout)) {
				this.setTopmost();
				return true;
			}
			for (var w = this.firstChild; w; w = w.nextSibling)
				if (w.isVisible() && w.focus(timeout))
					return true;
		}
		return false;
	},
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

	//server comm//
	smartUpdate: function (nm, val, timeout) {
		zAu.send(new zk.Event(this, 'setAttr', [nm, val]),
			timeout >= 0 ? timeout: -1);
		return this;
	},

	//widget event//
	fireX: function (evt, timeout) {
		evt.currentTarget = this;
		var evtnm = evt.name,
			lsns = this._lsns[evtnm],
			len = lsns ? lsns.length: 0;
		if (len) {
			for (var j = 0; j < len;) {
				var inf = lsns[j++], o = inf[0];
				(inf[1] || o[evtnm]).call(o, evt);
				if (evt.stopped) return evt; //no more processing
			}
		}

		if (!evt.auStopped) {
			var toServer = evt.opts && evt.opts.toServer;
			if (toServer || (this.inServer && this.desktop)) {
				if (evt.opts.sendAhead) {
					zAu.sendAhead(_cloneEvt(evt, this), timeout >= 0 ? timeout : 38);
					//since evt will be used later, we have to make a copy and use this as target
				} else {
					var asap = toServer || this._asaps[evtnm];
					if (asap == null) {
						var ime = this.$class._importantEvts;
						if (ime) {
							var ime = ime[evtnm];
							if (ime != null) 
								asap = ime;
						}
					}
					if (asap != null) //true or false
						zAu.send(_cloneEvt(evt, this), asap ? timeout >= 0 ? timeout : 38 : -1);
						//since evt will be used later, we have to make a copy and use this as target
				}
			}
		}
		return evt;
	},
	fire: function (evtnm, data, opts, timeout) {
		return this.fireX(new zk.Event(this, evtnm, data, opts), timeout);
	},
	listen: function (infs, priority) {
		priority = priority ? priority: 0;
		for (var evt in infs) {
			var inf = infs[evt];
			if (jq.isArray(inf)) inf = [inf[0]||this, inf[1]];
			else if (typeof inf == 'function') inf = [this, inf];
			else inf = [inf||this, null];
			inf.priority = priority;

			var lsns = this._lsns[evt];
			if (!lsns) this._lsns[evt] = [inf];
			else
				for (var j = lsns.length;;)
					if (--j < 0 || lsns[j].priority >= priority) {
						lsns.splice(j + 1, 0, inf);
						break;
					}
		}
		return this;
	},
	unlisten: function (infs) {
		l_out:
		for (var evt in infs) {
			var inf = infs[evt],
				lsns = this._lsns[evt], lsn;
			for (var j = lsns ? lsns.length: 0; j--;) {
				lsn = lsns[j];
				if (jq.isArray(inf)) inf = [inf[0]||this, inf[1]];
				else if (typeof inf == 'function') inf = [this, inf];
				else inf = [inf||this, null];
				if (lsn[0] == inf[0] && lsn[1] == inf[1]) {
					lsns.splice(j, 1);
					continue l_out;
				}
			}
		}
		return this;
	},
	isListen: function (evt, opts) {
		var v = this._asaps[evt];
		if (v) return true;
		if (opts && opts.asapOnly) {
			v = this.$class._importantEvts;
			return v && v[evt];
		}
		if (opts && opts.any) {
			if (v != null) return true;
			v = this.$class._importantEvts;
			if (v && v[evt] != null) return true;
		}

		var lsns = this._lsns[evt];
		return lsns && lsns.length;
	},
	setListeners: function (infs) {
		for (var evt in infs)
			this.setListener(evt, infs[evt]);
	},
	/** Sets a listener
	 * @param Array inf a two-element array. The first element is the event name,
	 * while the second is the listenr function
	 */
	/** Sets a listener
	 * @param String evt the event name
	 * @param Function fn the listener function.
	 */
	setListener: function (evt, fn) { //used by server
		if (arguments.length == 1) {
			fn = evt[1];
			evt = evt[0]
		}

		var bklsns = this._bklsns,
			oldfn = bklsns[evt],
			inf = {};
		if (oldfn) { //unlisten first
			delete bklsns[evt];
			inf[evt] = oldfn
			this.unlisten(inf);
		}
		if (fn) {
			inf[evt] = bklsns[evt]
				= typeof fn != 'function' ? new Function("var event=arguments[0];"+fn): fn;
			this.listen(inf);
		}
	},
	setOverrides: function (infs) { //used by server
		for (var nm in infs) {
			var val = infs[nm];
			if (val) {
				var oldnm = '$' + nm;
				if (this[oldnm] == null && this[nm]) //only once
					this[oldnm] = this[nm];
				this[nm] = val;
					//use eval, since complete func decl
			} else {
				var oldnm = '$' + nm;
				this[nm] = this[oldnm]; //restore
				delete this[oldnm];
			}
		}
	},

	//ZK event handling//
	doClick_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doClick_(evt);
		}	
	},
	doDoubleClick_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doDoubleClick_(evt);
		}
	},
	doRightClick_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doRightClick_(evt);
		}
	},
	doMouseOver_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doMouseOver_(evt);
		}
	},
	doMouseOut_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doMouseOut_(evt);
		}
	},
	doMouseDown_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doMouseDown_(evt);
		}
	},
	doMouseUp_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doMouseUp_(evt);
		}
	},
	doMouseMove_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doMouseMove_(evt);
		}
	},
	doKeyDown_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doKeyDown_(evt);
		}
	},
	doKeyUp_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doKeyUp_(evt);
		}
	},
	doKeyPress_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doKeyPress_(evt);
		}
	},

	doFocus_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doFocus_(evt);
		}
	},
	doBlur_: function (evt) {
		if (!this.fireX(evt).stopped) {
			var p = this.parent;
			if (p) p.doBlur_(evt);
		}
	},

	//DOM event handling//
	domListen_: function (n, evtnm, fn) {
		if (!this.$weave) {
			var inf = _domEvtInf(this, evtnm, fn);
			jq(n, zk).bind(inf[0], inf[1]);
		}
		return this;
	},
	domUnlisten_: function (n, evtnm, fn) {
		if (!this.$weave) {
			var inf = _domEvtInf(this, evtnm, fn);
			jq(n, zk).unbind(inf[0], inf[1]);
		}
		return this;
	},
	toJSON: function () {
		return this.uuid;
	}

}, {
	$: function (n, opts) {
		if (typeof n == 'string') {
			if (n.charAt(0) == '#') n = n.substring(1);
			var j = n.indexOf('-');
			return _binds[j >= 0 ? n.substring(0, j): n];
		}

		if (!n || zk.Widget.isInstance(n)) return n;
		else if (!n.nodeType) { //skip Element
			var e = n.originalEvent;
			n = (e?e.z$target:null) || n.target || n; //check DOM event first
		}

		for (; n; n = zk(n).vparentNode()||n.parentNode) {
			var id = n.id;
			if (id) {
				var j = id.indexOf('-');
				if (j >= 0) {
					id = id.substring(0, j);
					if (opts && opts.child) {
						var wgt = _binds[id];
						if (wgt) {
							var n2 = wgt.$n();
							if (n2 && jq.isAncestor(n2, n)) return wgt;
						}
						if (opts && opts.exact) break;
						continue;
					}
				}
				wgt = _binds[id];
				if (wgt) return wgt;
			}
			if (opts && opts.exact) break;
		}
		return null;
	},

	mimicMouseDown_: function (wgt, noFocusChange) { //called by mount
		var modal = zk.currentModal;
		if (modal && !wgt) {
			var cf = zk.currentFocus;
			//Note: browser might change focus later, so delay a bit
			//(it doesn't work if we stop event instead of delay - IE)
			if (cf && zUtl.isAncestor(modal, cf)) cf.focus(0);
			else modal.focus(0);
		} else if (!wgt || wgt.canActivate()) {
			if (!noFocusChange) {
				zk.currentFocus = wgt;
				zk._cfByMD = true;
				setTimeout(function(){zk._cfByMD = false;}, 0);
					//turn it off later since onBlur_ needs it
			}
			if (wgt) zWatch.fire('onFloatUp', wgt); //notify all
		}
	},

	//uuid//
	uuid: function (id) {
		var uuid = typeof id == 'object' ? id.id || '' : id,
			j = uuid.indexOf('-');
		return j >= 0 ? uuid.substring(0, j): id;
	},
	nextUuid: function () {
		return '_z_' + _nextUuid++;
	},

	isAutoId: function (id) {
		return !id || id.startsWith('_z_') || id.startsWith('z_');
	},

	register: function (clsnm, blankprev) {
		var cls = zk.$import(clsnm);
		cls.prototype.className = clsnm;
		var j = clsnm.lastIndexOf('.');
		if (j >= 0) clsnm = clsnm.substring(j + 1);
		_wgtcls[clsnm.substring(0,1).toLowerCase()+clsnm.substring(1)] = cls;
		if (blankprev) cls.prototype.blankPreserved = true;
	},
	getClass: function (wgtnm) {
		return _wgtcls[wgtnm];
	},
	newInstance: function (wgtnm, opts) {
		var cls = _wgtcls[wgtnm];
		if (!cls)
			throw 'widget not found: '+wgtnm;
		return new cls(opts);
	},

	_autohide: function () { //called by effect.js
		if (!_floatings.length) {
			for (var n; n = _hidden.shift();)
				n.style.visibility = n.getAttribute('z_ahvis')||'';
			return;
		}
		for (var tns = ['IFRAME', 'APPLET'], i = 2; i--;)
			l_nxtel:
			for (var ns = document.getElementsByTagName(tns[i]), j = ns.length; j--;) {
				var n = ns[j], $n = zk(n), visi;
				if ((!(visi=$n.isVisible(true)) && !_hidden.$contains(n))
				|| (!i && !n.getAttribute("z_autohide") && !n.getAttribute("z.autohide"))) //check z_autohide (5.0) and z.autohide (3.6) if iframe
					continue; //ignore

				for (var tc = _topnode(n), k = _floatings.length; k--;) {
					var f = _floatings[k].node,
						tf = _topnode(f);
					if (tf == tc || _zIndex(tf) < _zIndex(tc) || !$n.isOverlapped(f))
						continue;

					if (visi) {
						_hidden.push(n);
						try {
							n.setAttribute('z_ahvis', n.style.visibility);
						} catch (e) {
						}
						n.style.visibility = 'hidden';
					}
					continue l_nxtel;
				}

				if (_hidden.$remove(n))
					n.style.visibility = n.getAttribute('z_ahvis')||'';
			}
	}
});

/** A reference widget.
 */
zk.RefWidget = zk.$extends(zk.Widget, {
	bind_: function () {
		var w = zk.Widget.$(this.uuid);
		if (!w || !w.desktop) throw 'illegal: '+w;

		var p = w.parent, q;
		if (p) { //shall be a desktop
			var dt = w.desktop, n = w._node;
			w.desktop = w._node = null; //avoid unbind/bind
			p.removeChild(w);
			w.desktop = dt; w._node = n;
		}

		p = w.parent = this.parent,
		q = w.previousSibling = this.previousSibling;
		if (q) q.nextSibling = w;
		else if (p) p.firstChild = w;

		q = w.nextSibling = this.nextSibling;
		if (q) q.previousSibling = w;
		else if (p) p.lastChild = w;

		this.parent = this.nextSibling = this.previousSibling = null;

		_addIdSpaceDown(w);
		//no need to call super since it is bound
	}
});
})();

//desktop//
zk.Desktop = zk.$extends(zk.Widget, {
	bindLevel: 0,
	className: 'zk.Desktop',

	$init: function (dtid, contextURI, updateURI, stateless) {
		this.$super('$init', {uuid: dtid}); //id also uuid

		this._aureqs = [];
		//Sever side effect: this.desktop = this;

		var Desktop = zk.Desktop, dts = Desktop.all, dt = dts[dtid];
		if (!dt) {
			this.uuid = this.id = dtid;
			this.updateURI = updateURI || zk.updateURI;
			this.contextURI = contextURI || zk.contextURI;
			this.stateless = stateless;
			dts[dtid] = this;
			++Desktop._ndt;
			if (!Desktop._dt) Desktop._dt = this; //default desktop
		} else {
			if (updateURI) dt.updateURI = updateURI;
			if (contextURI) dt.contextURI = contextURI;
		}

		Desktop.sync();
	},
	_exists: function () {
		var id = this._pguid; //_pguid not assigned at beginning
		return !id || jq(id, zk)[0];
	},
	bind_: zk.$void,
	unbind_: zk.$void,
	setId: zk.$void
},{
	$: function (dtid) {
		var Desktop = zk.Desktop, dts = Desktop.all, w;
		if (Desktop._ndt > 1) {
			if (typeof dtid == 'string') {
				w = dts[dtid];
				if (w) return w;
			}
			w = zk.Widget.$(dtid);
			if (w)
				for (; w; w = w.parent) {
					if (w.desktop)
						return w.desktop;
					if (w.$instanceof(Desktop))
						return w;
				}
		}
		if (w = Desktop._dt) return w;
		for (dtid in dts)
			return dts[dtid];
	},
	all: {},
	_ndt: 0, //used in au.js/dom.js
	sync: function () {
		var Desktop = zk.Desktop, dts = Desktop.all, dt;
		if ((dt = Desktop._dt) && !dt._exists()) //removed
			Desktop._dt = null;
		for (var dtid in dts) {
			if (!(dt = dts[dtid])._exists()) { //removed
				delete dts[dtid];
				--Desktop._ndt;
			} else if (!Desktop._dt)
				Desktop._dt = dt;
		}
		return Desktop._dt;
	}
});

zk.Page = zk.$extends(zk.Widget, {//unlik server, we derive from Widget!
	_style: "width:100%;height:100%",
	className: 'zk.Page',

	$init: function (props, contained) {
		this._fellows = {};

		this.$super('$init', props);

		if (contained) zk.Page.contained.push(this);
	},
	redraw: function (out) {
		out.push('<div', this.domAttrs_(), '>');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</div>');
	}
},{
	contained: []
});
zk.Widget.register('zk.Page', true);

zk.Native = zk.$extends(zk.Widget, {
	className: 'zk.Native',

	redraw: function (out) {
		var s = this.prolog;
		if (s) {
			if (zk.ie) zjq._fix1stJS(out, s); //in domie.js
			out.push(s);
		}

		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);

		s = this.epilog;
		if (s) out.push(s);
	}
});

zk.Macro = zk.$extends(zk.Widget, {
	className: 'zk.Macro',

	redraw: function (out) {
		out.push('<span', this.domAttrs_(), '>');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</span>');
	}
});

zk.Skipper = zk.$extends(zk.Object, {
	skipped: function (wgt, child) {
		return wgt.caption != child;
	},
	skip: function (wgt, skipId) {
		var skip = jq(skipId || (wgt.uuid + '-cave'), zk)[0];
		if (skip && skip.firstChild) {
			skip.parentNode.removeChild(skip);
				//don't use jq to remove, since it unlisten events
			return skip;
		}
		return null;
	},
	restore: function (wgt, skip) {
		if (skip) {
			var loc = jq(skip.id, zk)[0];
			for (var el; el = skip.firstChild;) {
				skip.removeChild(el);
				loc.appendChild(el);

				if (zk.ie) zjq._fixIframe(el); //in domie.js, Bug 2900274
			}
		}
	}
});
zk.Skipper.nonCaptionSkipper = new zk.Skipper();

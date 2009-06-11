/* MeshWidget.js

	Purpose:
		
	Description:
		
	History:
		Sat May  2 09:36:31     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.mesh.MeshWidget = zk.$extends(zul.Widget, {
	_pagingPosition: "bottom",

	$init: function () {
		this.$supers('$init', arguments);
		this.heads = [];
	},

	//ROD Mold
	_innerWidth: "100%",
	_innerHeight: "100%",
	_innerTop: "height:0px;display:none",
	_innerBottom: "height:0px;display:none",

	$define: {
		pagingPosition: _zkf = function () {
			this.rerender();
		},
		fixedLayout: _zkf,

		vflex: function (vflex) {
			var n = this.getNode();
			if (n) {
				if (vflex) {
					// added by Jumper for IE to get a correct offsetHeight so we need 
					// to add this command faster than the this._calcSize() function.
					var hgh = n.style.height;
					if (!hgh || hgh == "auto") n.style.height = "99%"; // avoid border 1px;
				}
				this.onSize();
			}
		},

		model: null,
		innerWidth: function (v) {
			if (v == null) this._innerWidth = v = "100%";
			if (this.eheadtbl) this.eheadtbl.style.width = v;
			if (this.ebodytbl) this.ebodytbl.style.width = v;
			if (this.efoottbl) this.efoottbl.style.width = v;
		},
		//ROD
		innerHeight: function (v) {
			if (v == null) this._innerHeight = v = "100%";
			// TODO for ROD Mold
		},
		innerTop: function (v) {
			if (v == null) this._innerTop = v = "height:0px;display:none";
			// TODO for ROD Mold
		},
		innerBottom: function (v) {
			if (v == null) this._innerBottom = v = "height:0px;display:none";
			// TODO for ROD Mold
		}
	},

	getPageSize: function () {
		return this.paging.getPageSize();
	},
	setPageSize: function (pgsz) {
		this.paging.setPageSize(pgsz);
	},
	getPageCount: function () {
		return this.paging.getPageCount();
	},
	getActivePage: function () {
		return this.paging.getActivePage();
	},
	setActivePage: function (pg) {
		this.paging.setActivePage(pg);
	},
	inPagingMold: function () {
		return "paging" == this.getMold();
	},

	setHeight: function (height) {
		this.$supers('setHeight', arguments);
		if (this.desktop) {
			if (zk.ie6Only && this.ebody) 
				this.ebody.style.height = height;
			// IE6 cannot shrink its height, we have to specify this.body's height to equal the element's height. 
			this._setHgh(height);
			this.onSize();
		}
	},
	setWidth: function (width) {
		this.$supers('setWidth', arguments);
		if (this.eheadtbl) this.eheadtbl.style.width = "";
		if (this.efoottbl) this.efoottbl.style.width = "";
		if (this.desktop)
			this.onSize();
	},
	setStyle: function (style) {
		if (this._style != style) {
			this.$supers('setStyle', arguments);
			if (this.desktop)
				this.onSize();
		}
	},

	bind_: function () {
		this.$supers('bind_', arguments);
		if (this.isVflex()) {
			// added by Jumper for IE to get a correct offsetHeight so we need 
			// to add this command faster than the this._calcSize() function.
			var hgh = this.getNode().style.height;
			if (!hgh || hgh == "auto") this.getNode().style.height = "99%"; // avoid border 1px;
		}
		this._bindDomNode();
		this._fixHeaders();
		if (this.ebody) {
			this.domListen_(this.ebody, 'onScroll');
			this.ebody.style.overflow = ''; // clear
		}
		zWatch.listen({onSize: this, onShow: this, beforeSize: this});
	},
	unbind_: function () {
		if (this.ebody)
			this.domUnlisten_(this.ebody, 'onScroll');
			
		this.ebody = this.ehead = this.efoot = this.ebodytbl
			= this.eheadtbl = this.efoottbl = null;
		
		zWatch.unlisten({onSize: this, onShow: this, beforeSize: this});
		
		this.$supers('unbind_', arguments);
	},
	_fixHeaders: function () {
		if (this.head && this.ehead) {
			var empty = true;
			for (var w = this.head.firstChild; w; w = w.nextSibling) 
				if (w.getLabel() || w.getImage()) {
					empty = false;
					break;
				}
			this.ehead.style.display = empty ? 'none' : '';
		}
	},
	_bindDomNode: function () {
		for (var n = this.getNode().firstChild; n; n = n.nextSibling)
			switch(n.id) {
			case this.uuid + '$head':
				this.ehead = n;
				this.eheadtbl = zDom.firstChild(n, 'TABLE');
				break;
			case this.uuid + '$body':
				this.ebody = n;
				this.ebodytbl = zDom.firstChild(n, 'TABLE');
				break;
			case this.uuid + '$foot':
				this.efoot = n;
				this.efoottbl = zDom.firstChild(n, 'TABLE');
				break;
			}

		if (this.ebody) {
			var bds = this.ebodytbl.tBodies;
			if (!bds || !bds.length || (this.ehead && bds.length < 2))
				this.ebodytbl.appendChild(document.createElement("TBODY"));
			this.ebodyrows = this.ebodytbl.tBodies[this.ehead ? 1 : 0].rows;
				//Note: bodyrows is null in FF if no rows, so no err msg
		}
		if (this.ehead) {
			this.ehdfaker = this.eheadtbl.tBodies[0].rows[0];
			this.ebdfaker = this.ebodytbl.tBodies[0].rows[0];
			if (this.efoottbl)
				this.eftfaker = this.efoottbl.tBodies[0].rows[0];
		}
	},
	fireOnRender: function (timeout) {
		if (!this._pendOnRender) {
			this._pendOnRender = true;
			setTimeout(this.proxy(this._onRender), timeout ? timeout : 100);
		}
	},
	_doScroll: function () {
		if (this.ehead)
			this.ehead.scrollLeft = this.ebody.scrollLeft;
		if (this.efoot)
			this.efoot.scrollLeft = this.ebody.scrollLeft;
		if (!this.paging) this.fireOnRender(zk.gecko ? 200 : 60);
	},
	_onRender: function () {
		this._pendOnRender = false;

		var rows = this.ebodyrows;
		if (!this._model || !rows || !rows.length) return;

		//Note: we have to calculate from top to bottom because each row's
		//height might diff (due to different content)
		var items = [],
			min = this.ebody.scrollTop, max = min + this.ebody.offsetHeight;
		for (var j = 0, it = this.getBodyWidgetIterator(), w; (w = it.next()); j++) {
			if (w.isVisible()) {
				var row = rows[j],
					top = zDom.offsetTop(row);
				if (top + zDom.offsetHeight(row) < min) continue;
				if (top > max) break; //Bug 1822517
				if (!w._loaded)
					items.push(w);
			}
		}
		if (items.length)
			this.fire('onRender', {items: items});
	},

	//derive must override
	//getHeadWidgetClass
	//getBodyWidgetIterator

	//watch//
	beforeSize: function () {
		// IE6 needs to reset the width of each sub node if the width is a percentage
		var wd = zk.ie6Only ? this.getWidth() : this.getNode().style.width;
		if (!wd || wd == "auto" || wd.indexOf('%') >= 0) {
			if (this.ebody) this.ebody.style.width = "";
			if (this.ehead) this.ehead.style.width = "";
			if (this.efoot) this.efoot.style.width = "";
		}
	},
	onSize: _zkf = function () {
		if (this.isRealVisible()) {
			var n = this.getNode();
			if (n._lastsz && n._lastsz.height == n.offsetHeight && n._lastsz.width == n.offsetWidth)
				return; // unchanged
				
			this._calcSize();// Bug #1813722
			this.fireOnRender(155);
		}
	},
	onShow: _zkf,
	_vflexSize: function (hgh) {
		var n = this.getNode();
		if (zk.ie6Only) { 
			// ie6 must reset the height of the element,
			// otherwise its offsetHeight might be wrong.
			n.style.height = "";
			n.style.height = hgh;
		}
		
		var pgHgh = 0
		if (this.paging) {
			var pgit = this.getSubnode('pgit'),
				pgib = this.getSubnode('pgib');
			if (pgit) pgHgh += pgit.offsetHeight;
			if (pgib) pgHgh += pgib.offsetHeight;
		}
		return n.offsetHeight - 2 - (this.ehead ? this.ehead.offsetHeight : 0)
			- (this.efoot ? this.efoot.offsetHeight : 0) - pgHgh; // Bug #1815882 and Bug #1835369
	},
	/* set the height. */
	_setHgh: function (hgh) {
		if (this.isVflex() || (hgh && hgh != "auto" && hgh.indexOf('%') < 0)) {
			var h = this._vflexSize(hgh); 
			if (h < 0) h = 0;

			this.ebody.style.height = h + "px";
			
			//2007/12/20 We don't need to invoke the body.offsetHeight to avoid a performance issue for FF. 
			if (zk.ie && this.ebody.offsetHeight) {} // bug #1812001.
			// note: we have to invoke the body.offestHeight to resolve the scrollbar disappearing in IE6 
			// and IE7 at initializing phase.
		} else {
			//Bug 1556099: it is strange if we ever check the value of
			//body.offsetWidth. The grid's body's height is 0 if init called
			//after grid become visible (due to opening an accordion tab)
			this.ebody.style.height = "";
			this.getNode().style.height = hgh;
		}
	},
	/** Calculates the size. */
	_calcSize: function () {
		var n = this.getNode();
		this._setHgh(n.style.height);
		//Bug 1553937: wrong sibling location
		//Otherwise,
		//IE: element's width will be extended to fit body
		//FF and IE: sometime a horizontal scrollbar appear (though it shalln't)
		//note: we don't solve this bug for paging yet
		var wd = n.style.width;
		if (!wd || wd == "auto" || wd.indexOf('%') >= 0) {
			wd = zDom.revisedWidth(n, n.offsetWidth);
			if (wd < 0) wd = 0;
			if (wd) wd += "px";
		}
		if (wd) {
			this.ebody.style.width = wd;
			if (this.ehead) this.ehead.style.width = wd;
			if (this.efoot) this.efoot.style.width = wd;
		}
		//Bug 1659601: we cannot do it in init(); or, IE failed!
		var tblwd = this.ebody.clientWidth;
		if (zk.ie) {//By experimental: see zk-blog.txt
			if (this.eheadtbl &&
			this.eheadtbl.offsetWidth !=
			this.ebodytbl.offsetWidth) 
				this.ebodytbl.style.width = ""; //reset 
			if (tblwd && this.ebody.offsetWidth == this.ebodytbl.offsetWidth &&
			this.ebody.offsetWidth - tblwd > 11) { //scrollbar
				if (--tblwd < 0) 
					tblwd = 0;
				this.ebodytbl.style.width = tblwd + "px";
			}
			// bug #2799258
			var hgh = this.getHeight();
			if (!hgh || hgh == "auto") {
				hgh = this.ebody.offsetWidth - this.ebody.clientWidth;
				if (hgh > 11) 
					this.ebody.style.height = this.ebody.offsetHeight + zDom.scrollbarWidth(); + "px";
			}
		}
		if (this.ehead) {
			if (tblwd) this.ehead.style.width = tblwd + 'px';
			if (!this.isFixedLayout() && this.ebodyrows && this.ebodyrows.length)
				this.$class._adjHeadWd(this);
		} else if (this.efoot) {
			if (tblwd) this.efoot.style.width = tblwd + 'px';
			if (this.efoottbl.rows.length && this.ebodyrows && this.ebodyrows.length)
				this.$class.cpCellWidth(this);
		}
		n._lastsz = {height: n.offsetHeight, width: n.offsetWidth}; // cache for the dirty resizing.
	},
	domFaker_: function (out, fakeId, zcls) { //used by mold
		var head = this.head;
		out.push('<tbody style="visibility:hidden;height:0px"><tr id="',
				head.uuid, fakeId, '" class="', zcls, '-faker">');
		for (var w = head.firstChild; w; w = w.nextSibling)
			out.push('<th id="', w.uuid, fakeId, '"', w.domAttrs_(),
				 	'><div style="overflow:hidden"></div></th>');
		out.push('</tr></tbody>');
	},

	//super//
	onChildAdded_: function (child) {
		this.$supers('onChildAdded_', arguments);

		if (child.$instanceof(this.getHeadWidgetClass()))
			this.head = child;
		else if (!child.$instanceof(zul.mesh.Auxhead))
			return;

		var nsib = child.nextSibling;
		if (nsib)
			for (var hds = this.heads, j = 0, len = hds.length; j < len; ++j)
				if (hds[j] == nsib) {
					this.heads.$addAt(j, child);
					return; //done
				}
		this.heads.push(child);
	},
	onChildRemoved_: function (child) {
		this.$supers('onChildRemoved_', arguments);

		if (child == this.head) {
			this.head = null;
			this.heads.$remove(child);
		} else if (child.$instanceof(zul.mesh.Auxhead))
			this.heads.$remove(child);
	}
}, { //static
	_adjHeadWd: function (wgt) {
		var hdfaker = wgt.ehdfaker,
			bdfaker = wgt.ebdfaker,
			ftfaker = wgt.eftfaker;
		if (!hdfaker || !bdfaker || !hdfaker.cells.length
		|| !bdfaker.cells.length || !zDom.isRealVisible(hdfaker)
		|| !wgt.getBodyWidgetIterator().hasNext()) return;
		
		var hdtable = wgt.ehead.firstChild, head = wgt.head.getNode();
		if (!head) return; 
		if (zk.opera) {
			if (!hdtable.style.width) {
				var isFixed = true, tt = wgt.ehead.offsetWidth;
				for(var i = hdfaker.cells.length; --i >=0;) {
					if (!hdfaker.cells[i].style.width || hdfaker.cells[i].style.width.indexOf("%") >= 0) {
						isFixed = false; 
						break;
					}
					tt -= zk.parseInt(hdfaker.cells[i].style.width);
				}
				if (!isFixed || tt >= 0) hdtable.style.tableLayout = "auto";
			}
		}
		
		// Bug #1886788 the size of these table must be specified a fixed size.
		var bdtable = wgt.ebody.firstChild,
			total = Math.max(hdtable.offsetWidth, bdtable.offsetWidth), 
			tblwd = Math.min(bdtable.parentNode.clientWidth, bdtable.offsetWidth);
			
		if (total == wgt.ebody.offsetWidth && 
			wgt.ebody.offsetWidth > tblwd && wgt.ebody.offsetWidth - tblwd < 20)
			total = tblwd;
			
		var count = total;
		hdtable.style.width = total + "px";	
		
		if (bdtable) bdtable.style.width = hdtable.style.width;
		if (wgt.efoot) wgt.efoot.firstChild.style.width = hdtable.style.width;
		
		for (var i = bdfaker.cells.length; --i >= 0;) {
			if (!zDom.isVisible(hdfaker.cells[i])) continue;
			var wd = i != 0 ? bdfaker.cells[i].offsetWidth : count;
			bdfaker.cells[i].style.width = zDom.revisedWidth(bdfaker.cells[i], wd) + "px";
			hdfaker.cells[i].style.width = bdfaker.cells[i].style.width;
			if (ftfaker) ftfaker.cells[i].style.width = bdfaker.cells[i].style.width;
			var cpwd = zDom.revisedWidth(head.cells[i], zk.parseInt(hdfaker.cells[i].style.width));
			head.cells[i].style.width = cpwd + "px";
			var cell = head.cells[i].firstChild;
			cell.style.width = zDom.revisedWidth(cell, cpwd) + "px";
			count -= wd;
		}
		
		// in some case, the total width of this table may be changed.
		if (total != hdtable.offsetWidth) {
			total = hdtable.offsetWidth;
			tblwd = Math.min(wgt.ebody.clientWidth, bdtable.offsetWidth);
			if (total == wgt.ebody.offsetWidth && 
				wgt.ebody.offsetWidth > tblwd && wgt.ebody.offsetWidth - tblwd < 20)
				total = tblwd;
				
			hdtable.style.width = total + "px";	
			if (bdtable) bdtable.style.width = hdtable.style.width;
			if (wgt.efoot) wgt.efoot.firstChild.style.width = hdtable.style.width;
		}
	},
	cpCellWidth: function (wgt) {
		var dst = wgt.efoot.firstChild.rows[0],
			srcrows = wgt.ebodyrows;
		if (!dst || !srcrows || !srcrows.length || !dst.cells.length)
			return;
		var ncols = dst.cells.length,
			src, maxnc = 0;
		for (var j = 0, it = wgt.getBodyWidgetIterator(), w; (w = it.next());) {
			if (!w.isVisible() || !w._loaded) continue;

			var row = srcrows[j++],
				cells = row.cells, nc = zDom.ncols(row),
				valid = cells.length == nc && zDom.isVisible(row);
				//skip with colspan and invisible
			if (valid && nc >= ncols) {
				maxnc = ncols;
				src = row;
				break;
			}
			if (nc > maxnc) {
				src = valid ? row: null;
				maxnc = nc;
			} else if (nc == maxnc && !src && valid) {
				src = row;
			}
		}
		if (!maxnc) return;
	
		var fakeRow = !src;
		if (fakeRow) { //the longest row containing colspan
			src = document.createElement("TR");
			src.style.height = "0px";
				//Note: we cannot use display="none" (offsetWidth won't be right)
			for (var j = 0; j < maxnc; ++j)
				src.appendChild(document.createElement("TD"));
			srcrows[0].parentNode.appendChild(src);
		}
	
		//we have to clean up first, since, in FF, if dst contains %
		//the copy might not be correct
		for (var j = maxnc; --j >=0;)
			dst.cells[j].style.width = "";
	
		var sum = 0;
		for (var j = maxnc; --j >= 0;) {
			var d = dst.cells[j], s = src.cells[j];
			if (zk.opera) {
				sum += s.offsetWidth;
				d.style.width = zDom.revisedWidth(s, s.offsetWidth);
			} else {
				d.style.width = s.offsetWidth + "px";
				if (maxnc > 1) { //don't handle single cell case (bug 1729739)
					var v = s.offsetWidth - d.offsetWidth;
					if (v != 0) {
						v += s.offsetWidth;
						if (v < 0) v = 0;
						d.style.width = v + "px";
					}
				}
			}
		}
	
		if (zk.opera && !wgt.isFixedLayout())
			dst.parentNode.parentNode.style.width = sum + "px";
	
		if (fakeRow)
			src.parentNode.removeChild(src);
	}
});

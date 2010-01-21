/* Listbox.js

	Purpose:
		
	Description:
		
	History:
		Thu Apr 30 22:16:07     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under LGPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
/**
 * A listbox.
 * 
 * <p>
 * Event:
 * <ol>
 * <li>onSelect event is sent when user changes the selection.</li>
 * </ol>
 * 
 * <p>
 * Default {@link #getZclass}: z-listbox.
 * 
 * <p>
 * To have a list box without stripping, you can specify a non-existent style
 * class to {@link #setOddRowSclass}.
 */
zul.sel.Listbox = zk.$extends(zul.sel.SelectWidget, {
	_nrows: 0,
	$init: function () {
		this.$supers('$init', arguments);
		this._groupsInfo = [];
	},
	/**
	 * Returns the number of listgroup
	 * @return int
	 */
	getGroupCount: function () {
		return this._groupsInfo.length;
	},
	/**
	 * Returns a list of all {@link Listgroup}.
	 * @return Array
	 */
	getGroups: function () {
		return this._groupsInfo.$clone();
	},
	/**
	 * Returns whether listgroup exists.
	 * @return boolean
	 */
	hasGroup: function () {
		return this._groupsInfo.length;
	},
	/**
	 * Returns the next item.
	 * @return Listitem
	 * @param zk.Widget item
	 */
	nextItem: function (p) {
		if (p)
			while ((p = p.nextSibling)
			&& !p.$instanceof(zul.sel.Listitem))
				;
		return p;
	},
	/**
	 * Returns the previous item.
	 * @return Listitem
	 * @param zk.Widget item
	 */
	previousItem: function (p) {
		if (p)
			while ((p = p.previousSibling)
			&& !p.$instanceof(zul.sel.Listitem))
				;
		return p;
	},
	/**
	 * Returns the style class for the odd rows.
	 * <p>
	 * Default: {@link #getZclass()}-odd.
	 * @return String
	 */
	getOddRowSclass: function () {
		return this._scOddRow == null ? this.getZclass() + "-odd" : this._scOddRow;
	},
	/**
	 * Sets the style class for the odd rows. If the style class doesn't exist,
	 * the striping effect disappears. You can provide different effects by
	 * providing the proper style classes.
	 * @param String oddRowSclass
	 */
	setOddRowSclass: function (scls) {
		if (!scls) scls = null;
		if (this._scOddRow != scls) {
			this._scOddRow = scls;
			var n = this.$n();
			if (n && this.rows)
				this.stripe();
		}
		return this;
	},
	/**
	 * Returns whether the HTML's select tag is used.
	 * @return boolean
	 */
	inSelectMold: function () {
		return "select" == this.getMold();
	},
	bind_: function () {
		this.$supers('bind_', arguments);
		zWatch.listen({onResponse: this});
		this._shallStripe = true;
		zk.afterMount(this.proxy(this.onResponse));
	},
	unbind_: function () {
		zWatch.unlisten({onResponse: this});
		this.$supers('unbind_', arguments);
	},
	onResponse: function () {
		if (this.desktop && this._shallStripe) {
			this.stripe();
			if (this._shallSize)
				this.$supers('onResponse', arguments);
		}
	},
	_syncStripe: function () {
		this._shallStripe = true;
		if (!this.inServer && this.desktop)
			this.onResponse();
	},
	/**
	 * Stripes the class for each item.
	 */
	stripe: function () {
		var scOdd = this.getOddRowSclass();
		if (!scOdd) return;
		var odd = this._offset & 1;
		for (var j = 0, even = !odd, it = this.getBodyWidgetIterator(), w; (w = it.next()); j++) {
			if (w.isVisible() && w.isStripeable_()) {
				jq(w.$n())[even ? 'removeClass' : 'addClass'](scOdd);
				even = !even;
			}
		}
		this._shallStripe = false;
		return this;
	},
	rerender: function () {
		this.$supers('rerender', arguments);
		this._syncStripe();		
		return this;
	},
	setItemsInvalid_: function(wgts) {
		this.replaceCavedChildren_('rows', zAu.createWidgets(wgts));
	},	
	//-- super --//
	getCaveNode: function () {
		return this.$n('rows') || this.$n('cave');
	},	
	insertChildHTML_: function (child, before, desktop) {
		if (before = before && (!child.$instanceof(zul.sel.Listitem) || before.$instanceof(zul.sel.Listitem)) ? before.getFirstNode_(): null)
			jq(before).before(child.redrawHTML_());
		else
			jq(this.getCaveNode()).append(child.redrawHTML_());
		child.bind(desktop);
	},
	getZclass: function () {
		return this._zclass == null ? "z-listbox" : this._zclass;
	},
	insertBefore: function (child, sibling, ignoreDom) {
		this.$super('insertBefore', child, sibling, ignoreDom || !child.$instanceof(zul.sel.Listitem));
		this._fixOnAdd(child, ignoreDom);
	},
	appendChild: function (child, ignoreDom) {
		this.$super('appendChild', child, ignoreDom || !child.$instanceof(zul.sel.Listitem));
		this._fixOnAdd(child, ignoreDom);
	},
	_fixOnAdd: function (child, ignoreDom) {
		var rerender;
		if (child.$instanceof(zul.sel.Listitem)) {
			if (child.$instanceof(zul.sel.Listgroup))
				this._groupsInfo.push(child);
			if (!this.firstItem || !this.previousItem(child))
				this.firstItem = child;
			if (!this.lastItem || !this.nextItem(child))
				this.lastItem = child;
			++this._nrows;
			
			if (child.isSelected() && !this._selItems.$contains(child))
				this._selItems.push(child);
		} else if (child.$instanceof(zul.sel.Listhead)) {
			rerender = true;
			this.listhead = child;
		} else if (child.$instanceof(zul.mesh.Paging)) {
			rerender = true;
			this.paging = child;
		} else if (child.$instanceof(zul.sel.Listfoot)) {
			rerender = true;
			this.listfoot = child;
		} else if (child.$instanceof(zul.mesh.Frozen)) {
			rerender = true;
			this.frozen = child;
		}
		if (!ignoreDom) {
			if (rerender) this.rerender();
			else this._syncStripe();
			this._syncSize();
		}
	},
	onChildRemoved_: function (child) {
		this.$supers('onChildRemoved_', arguments);
		if (child == this.listhead)
			this.listhead = null;
		else if (child == this.paging)
			this.paging = null;
		else if (child == this.frozen)
			this.frozen = null;
		else if (child == this.listfoot)
			this.listfoot = null;
		else if (!child.$instanceof(zul.mesh.Auxhead)) {
			if (child == this.firstItem) {
				for (var p = this.firstChild, Listitem = zul.sel.Listitem;
				p && !p.$instanceof(Listitem); p = p.nextSibling)
					;
				this.firstItem = p;
			}
			if (child == this.lastItem) {
				for (var p = this.lastChild, Listitem = zul.sel.Listitem;
				p && !p.$instanceof(Listitem); p = p.previousSibling)
					;
				this.lastItem = p;
			}
			if (child.$instanceof(zul.sel.Listgroup))
				this._groupsInfo.$remove(child);
			--this._nrows;
			
			if (child.isSelected())
				this._selItems.$remove(child);
		}
		this._syncStripe();
		this._syncSize();
	},
	getHeadWidgetClass: function () {
		return zul.sel.Listhead;
	},
	itemIterator: _zkf = function () {
		return new zul.sel.ItemIter(this);
	},
	getBodyWidgetIterator: _zkf
});
/**
 * The listitem iterator.
 */
zul.sel.ItemIter = zk.$extends(zk.Object, {
	/** Constructor
	 * @param Listbox listbox the widget that the iterator belongs to
	 */
	$init: function (box) {
		this.box = box;
	},
	_init: function () {
		if (!this._isInit) {
			this._isInit = true;
			this.p = this.box.firstItem;
		}
	},
	 /**
     * Returns <tt>true</tt> if the iteration has more elements
     * @return boolean
     */
	hasNext: function () {
		this._init();
		return this.p;
	},
	/**
     * Returns the next element in the iteration.
     *
     * @return Listitem the next element in the iteration.
     */
	next: function () {
		this._init();
		var p = this.p;
		if (p) this.p = p.parent.nextItem(p);
		return p;
	}
});

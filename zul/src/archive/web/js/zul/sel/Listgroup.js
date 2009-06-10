/* Listgroup.js

	Purpose:
		
	Description:
		
	History:
		Wed Jun 10 09:29:45     2009, Created by jumperchen

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 2.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.sel.Listgroup = zk.$extends(zul.sel.Listitem, {
	_open: true,

	$define: {
		open: function (open) {
			this._openItem(open, true);
			if (open)
				this.getListbox().stripe();
		}
	},

	getItems: function () {
		var item = [];
		for (var w = this.nextSibling; w && !w.$instanceof(zul.sel.Listgroup); w = w.nextSibling)
			item.push(w);
		return item;
	},
	getItemCount: function () {
		return this.getItems().length;
	},
	getZclass: function () {
		return this._zclass != null ? this._zclass : "z-listgroup";
	},
	getLabel: function () {
		return this.firstChild != null ? this.firstChild.getLabel() : null;
	},
	/** A combination of image ([[#domImage_]]) and label ([[#domLabel_]]). */
	domContent_: function () {
		var label = this.getLabel(),
			img = this.domImage_();
		return label ? img + ' ': img;
	},
	isStripeable_: function () {
		return false;
	},
	_doImgClick: function (evt) {
		var toOpen = !this.isOpen(), listbox = this.getListbox();
		this._openItem(toOpen);
		
		if (toOpen || listbox.getModel()) {
			if (toOpen) this.parent.stripe();
			if (!listbox.paging) listbox.onSize(); 
				// group in paging will invalidate the whole rows.
		}
		evt.stop();
	},
	_openItem: function (open, silent) {
		this._open = open;
		var img = this.getSubnode('img'),
			zcls = this.getZclass();
		if (img) {
			zDom[open ? "rmClass" : "addClass"](img, zcls + "-img-close");
			zDom[open ? "addClass" : "rmClass"](img, zcls + "-img-open");
		}
		this._openItemNow(open);
		if (!silent)
			this.fire('onOpen', {open: open});
				//always send since the client has to update Openable
	},
	_openItemNow: function (toOpen) {
		for (var r, w = this.nextSibling; w && (!w.$instanceof(zul.sel.Listgroup) && !w.$instanceof(zul.sel.Listgroupfoot));
				w = w.nextSibling)
			if (w.desktop && w.isVisible())
				zDom[toOpen ? "show" : "hide"](w.getNode());
	},
	beforeParentChanged_: function (p) {
		if (p == null) {
			var box = this.getListbox(),
			prev = box.hasGroup();
				
			if (prev) {
				for (var g = box.getGroups(), c = box.getGroupCount(); --c >= 0;) {
					if (g[c] == this) {
						prev = g[--c];
						break;
					}
				}
			}
			if (prev)
				this._unbindafter = [function () {
					if (prev)
						prev._openItem(prev.isOpen(), true);
				}];
			else this._openItemNow(true, true);
		}
	},
	bind_: function () {		
		this.$supers('bind_', arguments);
		var n = this.getNode(),
			img = this.getSubnode("img");
		if (img)
			this.domListen_(img, 'onClick', '_doImgClick');
		var table = n.parentNode.parentNode;
		if (table.tBodies.length > 1) {
			var span = 0;
			for (var row = table.rows[0], i = row.cells.length; --i >=0;)
				if(zDom.isVisible(row.cells[i])) span++;
			for (var cells = n.cells, i = cells.length; --i >= 0;)
				span -= cells[i].colSpan;
			if (span > 0 && n.cells.length) n.cells[n.cells.length - 1].colSpan += span;
		}
	},
	unbind_: function (skipper, after) {
		this.domUnlisten_(this.getSubnode("img"), 'onClick', '_doImgClick');
		if (this._unbindafter && this._unbindafter.length) after.push(this._unbindafter.pop());
		this._unbindafter = null;
		this.$supers('unbind_', arguments);
	}
});
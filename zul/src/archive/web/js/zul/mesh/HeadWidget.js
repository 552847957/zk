/* HeadWidget.js

	Purpose:
		
	Description:
		
	History:
		Mon Dec 29 17:15:38     2008, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under LGPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zul.mesh.HeadWidget = zk.$extends(zul.Widget, {
	$init: function () {
		this.$supers('$init', arguments);
		this.listen({onColSize: this}, -1000);
	},

	$define: {
		sizable: function () {
			this.rerender();
		}
	},

	removeChildHTML_: function (child, prevsib) {
		this.$supers('removeChildHTML_', arguments);
		if (!this.$instanceof(zul.mesh.Auxhead))
			for (var faker, fs = child.$class._faker, i = fs.length; i--;)
				jq(child.uuid + '-' + fs[i], zk).remove();
	},
	
	onColSize: function (evt) {
		var owner = this.parent;
		if (owner.isSizedByContent()) owner.$class._adjHeadWd(owner);
		evt.column._width = evt.width;
		owner._innerWidth = owner.eheadtbl.style.width;
		owner.fire('onInnerWidth', owner.eheadtbl.style.width);
		owner.fireOnRender(zk.gecko ? 200 : 60);
	},
	unbind_: function () {
		jq(this.hdfaker).remove();
		jq(this.bdfaker).remove();
		jq(this.ftfaker).remove();
		this.$supers('unbind_', arguments);
	},
	onChildAdded_: function (child) {
		this.$supers('onChildAdded_', arguments);
		if (this.desktop) {
			if (this.parent._fixHeaders())
				this.parent.onSize();
		}
	},
	onChildRemove: function () {
		this.$supers('onChildRemove', arguments);
		if (this.desktop) {
			if (this.parent._fixHeaders())
				this.parent.onSize();
		}
	},
	beforeChildrenFlex_: function(hwgt) { //avoid unnecessary zk.Widget#_fixFlex()
		return !this._inAfterChildrenFlex && !this.parent._flexRerender; 
	},
	afterChildrenFlex_: function (hwgt) { //hflex in HeaderWidget
		this._inAfterChildrenFlex = true;
		try {
			hwgt.updateMesh_(); //might recursive back #beforeChildrenFlex_()
		} finally {
			delete this._inAfterChildrenFlex;
		}
	}
},{ //static
	redraw: function (out) {
		out.push('<tr', this.domAttrs_(), ' align="left">');
		for (var w = this.firstChild; w; w = w.nextSibling)
			w.redraw(out);
		out.push('</tr>');
	}
});

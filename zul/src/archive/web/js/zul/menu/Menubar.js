/* Menubar.js

	Purpose:

	Description:

	History:
		Thu Jan 15 09:02:32     2009, Created by jumperchen

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zk.def(zul.menu.Menubar = zk.$extends(zul.Widget, {
	_orient: "horizontal",

	getZclass: function () {
		return this._zclass == null ? "z-menubar" +
				("vertical" == this.getOrient() ? "-ver" : "-hor") : this._zclass;
	},
	unbind_: function () {
		this._lastTarget = null;
		this.$supers('unbind_', arguments);
	},
	insertChildHTML_: function (child, before, desktop) {
		if (before)
			zDom.insertHTMLBefore(before.getSubnode('chdextr'),
				this.encloseChildHTML_({child: child, vertical: 'vertical' == this.getOrient()}));
		else
			zDom.insertHTMLBeforeEnd(this.getNode(),
				this.encloseChildHTML_({child: child, vertical: 'vertical' == this.getOrient()}));

		zk.Widget.bind(child, desktop);
	},
	removeChildHTML_: function (child, prevsib) {
		this.$supers('removeChildHTML_', arguments);
		zDom.remove(child.uuid + '$chdextr');
	},
	encloseChildHTML_: function (opts) {
		var out = opts.out || [],
			child = opts.child,
			isVert = opts.vertical;
		if (isVert) {
			out.push('<td id="', child.uuid, '$chdextr"');
			if (child.getHeight())
				out.push(' height="', child.getHeight(), '"');
			out.push('>');
		}
		child.redraw(out);
		if (isVert)
			out.push('</tr>');
		if (!opts.out) return out.join('');
	}
}), {//zk.def
	orient: function () {
		this.rerender();
	},
	autodrop: null
});

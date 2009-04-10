/* zuml.js

	Purpose:
		
	Description:
		
	History:
		Fri Apr 10 14:30:00     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zk.zuml.Parser = {
	createWidgets: function (parent, doc, vars) {
		if (typeof doc == 'string')
			doc = zUtl.parseXML(doc);

		var cwgts = [], oldvars;
		if (vars) {
			oldvars = window._;
			window._ = vars;
		}
		try {
			zk.zuml.Parser._cw(parent, doc.documentElement, cwgts);
		} finally {
			if (vars) window._ = oldvars;
		}

		var l = cwgts.length;
		return l ? l == 1 ? cwgts[0]: cwgts: null;
	},
	_cw: function (parent, e, cwgts) {
		if (!e) return null;

		var $Parser = zk.zuml.Parser,
			forEach = $Parser._eval(e.getAttribute('forEach'));
		if (forEach != null) {
			var oldEach = window.each;
			for (var l = forEach.length, j = 0; j < l; j++) {
				window.each = forEach[j];
				$Parser._cw2(parent, e, cwgts);
			}
			window.each = oldEach;
		} else
			$Parser._cw2(parent, e, cwgts);
	},
	_cw2: function (parent, e, cwgts) {
		var $Parser = zk.zuml.Parser;
		var ifc = $Parser._eval(e.getAttribute('if')),
			unless = $Parser._eval(e.getAttribute('unless'));
		if ((ifc == null || ifc) && (unless == null || !unless)) {
			var wgt = zk.Widget.newInstance(e.tagName),
				atts = e.attributes;
			if (cwgts) cwgts.push(wgt);
			if (parent) parent.appendChild(wgt);

			for (var l = atts.length, j = 0; j < l; ++j) {
				var att = atts[j],
					nm = att.name,
					val = $Parser._eval(att.value);
				if (nm.startsWith('on') && nm.length > 2) {
					att = nm.charAt(2);
					if (att >= 'A' && att <= 'Z') {
						wgt.setListener([nm, val]);
						continue;
					}
				}
				wgt.set(nm, val);
			}

			for (e = e.firstChild; e; e = e.nextSibling) {
				var nt = e.nodeType;
				if (nt == 1) $Parser._cw(wgt, e);
				else if (nt == 3) {
					var w = new zk.Native();
					w.prolog = $Parser._eval(e.nodeValue);
					wgt.appendChild(w);
				}
			}
		}
	},
	_eval: function (s) {
		if (s)
			for (var j = 0, k, l, t, last = s.length - 1, s2;;) {
				k = s.indexOf('${', j);
				if (k < 0) {
					if (s2) s = s2 + s.substring(j);
					break;
				}

				t = s.substring(j, k);
				l = s.indexOf('}', k + 2);
				if (l < 0) {
					s = s2 ? s2 + t: t; //ignore ${...
					break;
				}

				s2 = s2 ? s2 + t: t;
				t = s.substring(k + 2, l); //EL

				try {
					eval('t=' + t);
				} catch (e) {
					throw 'Failed to evaluate '+t;
				}

				if (!s2 && l == last) return t; //don't convert to string

				if (t) s2 += t;

				j = l + 1;
			}
		return s;
	}
};

/* util.js

	Purpose:
		
	Description:
		
	History:
		Tue Sep 30 09:02:06     2008, Created by tomyeh

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
zUtl = { //static methods
	//HTML/XML
	encodeXML: function (txt, opts) {
		var out = "";
		if (!txt) return out;

		var k = 0, tl = txt.length,
			pre = opts && opts.pre,
			multiline = pre || (opts && opts.multiline);
		for (var j = 0; j < tl; ++j) {
			var cc = txt.charAt(j);
			if (cc == '\n') {
				if (multiline) {
					out += txt.substring(k, j) + "<br/>\n";
					k = j + 1;
				}
			} else if (cc == ' ' || cc == '\t') {
				if (pre) {
					out += txt.substring(k, j) + '&nbsp;';
					if (cc == '\t') out += '&nbsp;&nbsp;&nbsp;';
					k = j + 1;
				}
			} else {
				var enc = zUtl._encs[cc];
				if (enc) {
					out += txt.substring(k, j) + '&' + enc + ';';
					k = j + 1;
				}
			}
		}
		return !k ? txt:
			k < tl ? out + txt.substring(k): out;
	},
	decodeXML: function (txt) {
		var out = "";
		if (!txt) return out;

		var k = 0, tl = txt.length;
		for (var j = 0; j < tl; ++j) {
			var cc = txt.charAt(j);
			if (cc == '&') {
				var l = txt.indexOf(';', j + 1);
				if (l >= 0) {
					var dec = zUtl._decs[txt.substring(j + 1, l)];
					if (dec) {
						out += txt.substring(k, j) + dec;
						k = (j = l) + 1;
					}
				}
			}
		}
		return !k ? txt:
			k < tl ? out + txt.substring(k): out;
	},
	_decs: {lt: '<', gt: '>', amp: '&', quot: '"'},
	_encs: {},

	renType: function (url, type) {
		var j = url.lastIndexOf(';');
		var suffix;
		if (j >= 0) {
			suffix = url.substring(j);
			url = url.substring(0, j);
		} else
			suffix = "";

		j = url.lastIndexOf('.');
		if (j < 0) j = url.length; //no extension at all
		var	k = url.lastIndexOf('-'),
			m = url.lastIndexOf('/'),
			ext = j <= m ? "": url.substring(j),
			pref = k <= m ? j <= m ? url: url.substring(0, j): url.substring(0, k);
		if (type) type = "-" + type;
		else type = "";
		return pref + type + ext + suffix;
	},

	getElementValue: function (el) {
		var txt = ""
		for (el = el.firstChild; el; el = el.nextSibling)
			if (el.data) txt += el.data;
		return txt;
	},

 	cellps0: ' cellpadding="0" cellspacing="0" border="0"',
 	img0: '<img style="height:0;width:0"/>',
 
	now: function () {
		return new Date().getTime();
	},
	today: function () {
		var d = new Date();
		return new Date(d.getFullYear(), d.getMonth(), d.getDate());
	},
	isAncestor: function (p, c) {
		if (!p) return true;
		for (; c; c = c.getParent ? c.getParent(): c.parent)
			if (p == c)
				return true;
		return false;
	},
	isDescendant: function (c, p) {
		return zUtl.isAncestor(p, c);
	},

	//progress//
	progressbox: function (id, msg, mask) {
		if (mask && zk.Page.contained.length) {
			for (var c = zk.Page.contained.length, e = zk.Page.contained[--c]; e; e = zk.Page.contained[--c]) {
				if (!e._applyMask)
					e._applyMask = new zk.eff.Mask({
						id: e.uuid + "$mask",
						anchor: e.getNode()
					});
			}
			return;
		}

		var x = zDom.innerX(), y = zDom.innerY(),
			style = ' style="left:'+x+'px;top:'+y+'px"',
			idtxt = id + '$t',
			idmsk = id + '$m',
			html = '<div id="'+id+'"';
		if (mask)
			html += '><div id="' + idmsk + '" class="z-modal-mask"'+style+'></div';
		html += '><div id="'+idtxt+'" class="z-loading"'+style
			+'><div class="z-loading-indicator"><span class="z-loading-icon"></span> '
			+msg+'</div></div></div>'
		var n = document.createElement("DIV");
		document.body.appendChild(n);
		n = zDom.setOuterHTML(n, html);

		var txt = zDom.$(idtxt);
		if (mask)
			n.z_mask = new zk.eff.FullMask({
				mask: zDom.$(idmsk),
				zIndex: zDom.getStyle(txt, 'z-index') - 1
			});

		if (mask && txt) { //center
			txt.style.left = (zDom.innerWidth() - txt.offsetWidth) / 2 + x + "px";
			txt.style.top = (zDom.innerHeight() - txt.offsetHeight) / 2 + y + "px";
		}
		zDom.cleanVisibility(n);
	},
	destroyProgressbox: function (id) {
		var n = zDom.$(id);
		if (n) {
			if (n.z_mask) n.z_mask.destroy();
			zDom.remove(n);
		}

		for (var c = zk.Page.contained.length, e = zk.Page.contained[--c]; e; e = zk.Page.contained[--c])
			if (e._applyMask) {
				e._applyMask.destroy();
				e._applyMask = null;
			}
	},

	//HTTP//
	go: function (url, overwrite, target) {
		if (!url) {
			location.reload();
		} else if (overwrite) {
			location.replace(url);
		} else if (target) {
			//we have to process query string because browser won't do it
			//even if we use zDom.insertHTMLBeforeEnd("<form...")
			try {
				var frm = document.createElement("FORM");
				document.body.appendChild(frm);
				var j = url.indexOf('?');
				if (j > 0) {
					var qs = url.substring(j + 1);
					url = url.substring(0, j);
					zk.queryToHiddens(frm, qs);
				}
				frm.name = "go";
				frm.action = url;
				frm.method = "GET";
				frm.target = target;
				frm.submit();
			} catch (e) { //happens if popup block
			}
		} else {
			location.href = url;
		}
	},

	newAjax: function () {
		if (window.XMLHttpRequest) {
			return new XMLHttpRequest();
		} else {
			try {
				return new ActiveXObject('Msxml2.XMLHTTP');
			} catch (e2) {
				return new ActiveXObject('Microsoft.XMLHTTP');
			}
		}
	},
	intsToString: function (ary) {
		if (!ary) return "";

		var sb = [];
		for (var j = 0, k = ary.length; j < k; ++j)
			sb.push(ary[j]);
		return sb.join();
	},
	stringToInts: function (numbers, defaultValue) {
		if (numbers == null)
			return null;

		var list = [];
		for (var j = 0;;) {
			var k = numbers.indexOf(',', j),
				s = (k >= 0 ? numbers.substring(j, k): numbers.substring(j)).trim();
			if (s.length == 0) {
				if (k < 0) break;
				list.push(defaultValue);
			} else
				list.push(zk.parseInt(s));

			if (k < 0) break;
			j = k + 1;
		}
		return list;
	},
	_init: function () {
		delete zUtl._init;

		var encs = zUtl._encs, decs = zUtl._decs;
		for (var v in decs)
			encs[decs[v]] = v;
	}
};
zUtl._init();

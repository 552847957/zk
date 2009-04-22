/* widget.js

{{IS_NOTE
	Purpose:

	Description:

	History:
		Sun Jan 29 15:25:10     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

////
// groupbox, caption //
zkGrbox = {
	setAttr: function (cmp, nm, val) {
		switch (nm) {
		case "z.open":
			zkGrbox.open(cmp, val == "true", true);
			return true; //no need to store z.open

		case "z.cntStyle":
			var n = $e(cmp.id + "!cave");
			if (n) {
				zk.setStyle(n, val != null ? val: "");
				zkGrbox._fixHgh(cmp);
			}
			return true; //no need to store z.cntType
		case "style":
		case "style.height":
			zkau.setAttr(cmp, nm, val);
			zkGrbox._fixHgh(cmp);
			return true;
		}
		return false;
	},
	onclick: function (evt, uuid) {
		if (!evt) evt = window.event;

		var target = Event.element(evt);

		// Bug: 1991550
		for (var type; (type = $type(target)) != "Grbox"; target = $parent(target)) {
			var tn = $tag(target);
			if (type == "Button" || "BUTTON" == tn || "INPUT" == tn || "TEXTAREA" == tn || "SELECT" == tn ||
				"A" == tn || ("TD" != tn && "TR" != tn && target.onclick))
				return;
		}

		if (uuid) {
			var cmp = $e(uuid);
			if (getZKAttr(cmp, "closable") == "false")
				return;

			cmp = $e(uuid + "!slide");
			if (cmp)
				zkGrbox.open(uuid, !$visible(cmp), false, true);
		}
	},
	open: function (gb, open, silent, ignorable) {
		var gb = $e(gb);
		if (gb) {
			var panel = $e(gb.id + "!slide");
			if (panel && open != $visible(panel)
			&& !panel.getAttribute("zk_visible")
			&& (!ignorable || !getZKAttr(panel, "animating"))) {
				if (open) anima.slideDown(panel);
				else anima.slideUp(panel);

				if (!silent)
					zkau.sendasap({uuid: gb.id, cmd: "onOpen", data: [open]});

				if (open) setTimeout(function() {zkGrbox._fixHgh(gb);}, 500); //after slide down
			}
		}
	}
};
zkGrbox.onSize = zkGrbox.onVisi = zkGrbox._fixHgh = function (cmp) {
	var n = $e(cmp.id + "!cave");
	if (n) {
		var hgh = cmp.style.height;
		if (hgh && hgh != "auto") {
			if (zk.ie6Only) n.style.height = "";
			zk.setOffsetHeight(n, zk.getVflexHeight(n.parentNode));
		}

		//if no border-bottom, hide the shadow
		var sdw = $e(cmp.id + "!sdw");
		if (sdw) {
			var w = $int(Element.getStyle(n, "border-bottom-width"));
			sdw.style.display = w ? "": "none";
		}
	}

};
// groupbox default mold;
zkGrfs = {
	init: function (cmp) {
		var head = zk.firstChild(cmp, "LEGEND");
		if (head) zk.listen(head, "click", zkGrfs.onclick);
	},
	onclick: function (evt) {
		if (!evt) evt = window.event;
		var target = Event.element(evt),
			tn = $tag(target);
		if ("BUTTON" == tn || "INPUT" == tn || "TEXTAREA" == tn || "SELECT" == tn
		|| "A" == tn || ("TD" != tn && "TR" != tn && target.onclick))
			return;

		var cmp = $parentByTag(target, "FIELDSET");

		if (getZKAttr(cmp, "closable") == "false") return; // Bug 2125673

		zkGrfs.open(cmp);
	},
	open: function (cmp, silent) {
		var zcls = getZKAttr(cmp, "zcls") + "-colpsd",
			open = zk.hasClass(cmp, zcls);
		zk[open ? "rmClass" : "addClass"](cmp, zcls);
		if (!silent)
			zkau.sendasap({uuid: cmp.id, cmd: "onOpen", data: [open]});
		if (open) zk.onSizeAt(cmp);
	},
	setAttr: function (cmp, nm, val) {
		switch (nm) {
			case "z.open":
				zkGrfs.open(cmp, val == "true", true);
				return true; //no need to store z.open

			case "z.cntStyle":
				var n = $e(cmp.id + "!cave");
				if (n)
					zk.setStyle(n, val != null ? val: "");

				return true; //no need to store z.cntType
		}
	}
};
zkCapt = {};

zkCapt.init = function (cmp) {
	var gb = zkCapt._parentGrbox(cmp);
	cmp = cmp.rows[0]; //first row
	if (gb && cmp) {
		zk.listen(cmp, "click",
			function (evt) {zkGrbox.onclick(evt, gb.id);});
	}
};
zkCapt._parentGrbox = function (p) {
	while (p = p.parentNode) { //yes, assign
		var type = $type(p);
		if (type == "Grbox") return p;
		if (type) break;
	}
	return null;
};

////
// Image//
zkImg = {};

if (zk.ie6Only) {
	//Request 1522329: PNG with alpha color in IE
	//To simplify the implementation, Image.java invalidates instead of smartUpdate
	zkImg.init = function (cmp) {
		// this function should be invoked faster than zkau.initdrag(), otherwise its drag-drop will fail.
		return zkImg._fixpng(cmp);
	};
	zkImg._fixpng = function (img) {
		if (getZKAttr(img, "alpha") && img.src
		&& img.src.toLowerCase().endsWith(".png")) {
			var id = img.id;
			var wd = img.width, hgh = img.height;
			if (!wd) wd = img.offsetWidth;
			if (!hgh) hgh = img.offsetHeight;

			var commonStyle = "width:"+wd+"px;height:"+hgh+"px;";
			if (img.hspace) commonStyle +="margin-left:"+img.hspace+"px;margin-right:"+img.hspace+"px;";
			if (img.vspace) commonStyle +="margin-top:"+img.vspace+"px;margin-bottom:"+img.vspace+"px;";
			commonStyle += img.style.cssText;

			var html = '<span id="'+id
				+'" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\''
				+img.src+"', sizingMethod='scale');display:inline-block;";
			if (img.align == "left") html += "float:left;";
			else if (img.align == "right") html += "float:right;";
			if ($tag(img.parentNode) == "A") html += "cursor:hand;";
			html += commonStyle+'"';
			if (img.className) html += ' class="'+img.className+'"';
			if (img.title) html += ' title="'+img.title+'"';

			//process zk_xxx
			for (var attrs = img.attributes, j = 0, al = attrs.length; j < al; ++j) {
				var attr = attrs.item(j);
				if (attr.name.startsWith("z."))
					html += ' '+attr.name+'="'+attr.value+'"';
			}

			html += '></span>';

			if (img.isMap) {
				html += '<img style="position:relative;left:-'+wd+'px;'+commonStyle
					+'" src="'+zk.getUpdateURI('/web/img/spacer.gif')
					+'" ismap="ismap"';
				if (img.useMap) html += ' usemap="'+img.useMap+'"';
				html += '/>';
			}
			img.outerHTML = html;
			return $e(id); //transformed
		}
	}
}

////
// Imagemap //
zkMap = {};
zkArea = {};

zkMap.init = function (cmp) {
	zkMap._ckchd(cmp);
	zk.newFrame("zk_hfr_",
		null, zk.safari ? "width:0;height:0;display:inline": "display:none");
		//creates a hidden frame. However, in safari, we cannot use invisible frame
		//otherwise, safari will open a new window
};
/** Check if any child (area). */
zkMap._ckchd = function (cmp) {
	var mapid = cmp.id + "_map",
		map = $e(mapid),
		img = $real(cmp),
		bArea = map && map.areas.length;
	img.useMap = bArea ? "#" + mapid: "";
	img.isMap = !bArea;
};
zkMap.setAttr = function (cmp, nm, val) {
	if ("ckchd" == nm) {
		zkMap._ckchd(cmp);
		return true;
	}
	return false;
};
zkMap.onSize = function (cmp) {
	if (zk.ie6Only) {
		var img = $real(cmp);
		return zkImg._fixpng(img);
	}
};
zkArea.init = function (cmp) {
	zk.listen(cmp, "click", zkArea.onclick);
};

/** Called when an area is clicked. */
zkArea.onclick = function (evt) {
	if (zkMap._toofast()) return;

	var cmp = Event.element(evt);
	if (cmp) {
		var map = $parentByType(cmp, "Map");
		if (map)
			zkau.send({uuid: map.id,
				cmd: "onClick", data: [getZKAttr(cmp, "aid")], ctl: true});
	}
};
/** Called by map-done.dsp */
zkMap.onclick = function (href) {
	if (zkMap._toofast()) return;

	var j = href.indexOf('?');
	if (j < 0) return;

	var k = href.indexOf('?', ++j);
	if (k < 0 ) return;

	var id = href.substring(j, k);
	if (!$e(id)) return; //component might be removed

	j = href.indexOf(',', ++k);
	if (j < 0) return;

	var x = href.substring(k, j);
	var y = href.substring(j + 1);
	zkau.send({uuid: id, cmd: "onClick", data: [x, y], ctl: true});
};
zkMap._toofast = function () {
	if (zk.gecko) { //bug 1510374
		var now = $now();
		if (zkMap._stamp && now - zkMap._stamp < 800)
			return true;
		zkMap._stamp = now;
	}
	return false;
};

//popup//
zkPop = {
	position: function (el, dim, where) {
		where = where || "overlap";
		var x = dim.left, y = dim.top,
			wd = zk.getDimension(el), hgh = wd[1]; //only width and height
		wd = wd[0];
		switch(where) {
		case "before_start":
			y -= hgh;
			break;
		case "before_end":
			y -= hgh;
			x += dim.width - wd;
			break;
		case "after_start":
			y += dim.height;
			break;
		case "after_end":
			y += dim.height;
			x += dim.width - wd;
			break;
		case "start_before":
			x -= wd;
			break;
		case "start_after":
			x -= wd;
			y += dim.height - hgh;
			break;
		case "end_before":
			x += dim.width;
			break;
		case "end_after":
			x += dim.width;
			y += dim.height - hgh;
			break;
		case "at_pointer":
			var offset = zkau._mspos;
			x = offset[0];
			y = offset[1];
			break;
		case "after_pointer":
			var offset = zkau._mspos;
			x = offset[0];
			y = offset[1] + 20;
			break;
		default: // overlap is assumed
			// nothing to do.
		}

		var scX = zk.innerX(),
			scY = zk.innerY(),
			scMaxX = scX + zk.innerWidth(),
			scMaxY = scY + zk.innerHeight();

		if (x + wd > scMaxX) x = scMaxX - wd;
		if (x < scX) x = scX;
		if (y + hgh > scMaxY) y = scMaxY - hgh;
		if (y < scY) y = scY;
		
		var ofs = zk.toStyleOffset(el, x, y);
		el.style.left = ofs[0] + "px";
		el.style.top = ofs[1] + "px";
	},
	/** Called by au.js's context menu. */
	context: function (ctx, ref, position) {
		if (position) {
			ref = $e(ref);
			if (!ref) return;
			ctx.style.position = "absolute";
			zk.setVParent(ctx);
			var offs = zk.revisedOffset(ref);
			zkPop.position(ctx, {top: offs[1], left: offs[0], width: zk.offsetWidth(ref),
				height: zk.offsetHeight(ref)}, position);
		}
		zk.show(ctx); //onVisiAt is called in zk.show
		var asap= zkau.asap(ctx, "onOpen");
		if (asap) {
			//use a progress bar to hide the popup
			var mask = zk.applyMask(ctx.id, "");
			//register addOnReponse to remove the progress bar after receiving the response from server
			if (mask) zkau.addOnResponse("zk.remove($e('"+mask.id+"'))");
		}
		zkPop._pop.addFloatId(ctx.id, true); //it behaves like Popup (rather than dropdown)
		zkau.hideCovered();
		if (zk.ie6Only) {
			if (!ctx._stackup)
				ctx._stackup = zk.makeStackup(ctx);
			else {
				ctx._stackup.style.top = ctx.style.top;
				ctx._stackup.style.left = ctx.style.left;
				ctx._stackup.style.display = "block";
			}
		}
		zkPop._fixWdh(ctx);
		zkPop._fixHgh(ctx);
		zk.cleanVisibility(ctx);
		if (asap)
			zkau.send({uuid: ctx.id, cmd: "onOpen",
				data: ref ? [true, ref.id]: [true]});
	},
	close: function (ctx) {
		zkPop._pop.removeFloatId(ctx.id);
		zkPop._close(ctx);

		rmZKAttr(ctx, "owner"); //it is set by au.js after calling zkPop.context
	},
	_close: function (ctx) {
		ctx.style.display = "none";

		if (ctx._stackup)
			ctx._stackup.style.display = "none";

		zk.unsetVParent(ctx);
		zkau.hideCovered();

		if (zkau.asap(ctx, "onOpen"))
			zkau.send({uuid: ctx.id, cmd: "onOpen", data: [false]});
	},
	cleanup: function (ctx) {
		if (ctx._stackup)
			zk.remove(ctx._stackup);
		ctx._stackup = null;
	},
	getOffsetHeight: function (cmp) {
		var h = cmp.offsetHeight - 1, 
			tl = zk.firstChild(cmp, "DIV"),	
			bl = zk.lastChild(cmp, "DIV"),
			n = $e(cmp.id + "!cave").parentNode,
			bd = $e(cmp.id + "!body");
		
			h -= tl.offsetHeight;
			h -= bl.offsetHeight;
			h -= zk.getPadBorderHeight(n);
			h -= zk.getPadBorderHeight(bd);
		return h;
	},
	setAttr: function (cmp, nm, val) {
		if ("style.width" == nm || "style.height" == nm) {
			zkau.setAttr(cmp, nm, val);
			zk.onVisiAt(cmp);
			zkPop._fixWdh(cmp);
			zkPop._fixHgh(cmp);
			return true;
		}
		return false;
	}
};
zk.Popup = zClass.create();
Object.extend(Object.extend(zk.Popup.prototype, zk.Floats.prototype), {
	_close: function (el) {
		zkPop._close(el);
	}
});
zkPop._fixWdh = zk.ie7 ? function(cmp) {
	var wdh = cmp.style.width,
		tl = zk.firstChild(cmp, "DIV"),	
		bl = zk.lastChild(cmp, "DIV"),
		n = $e(cmp.id + "!cave").parentNode;
	if (!wdh || wdh == "auto") {
		var diff = zk.getPadBorderWidth(n.parentNode) + zk.getPadBorderWidth(n.parentNode.parentNode);
		if (tl) {
			var tr = zk.firstChild(tl,"DIV");
			tr.style.width = n.offsetWidth + diff + "px";
		}		
		if (bl) {
			var br = zk.firstChild(bl,"DIV");
			br.style.width = n.offsetWidth + diff + "px";
		}
	} else {
		if (tl) tl.firstChild.style.width = "";
		if (bl) bl.firstChild.style.width = "";
	}
} : zk.voidf;
zkPop._fixHgh = function (cmp) {
	var hgh = cmp.style.height,
	n = $e(cmp.id + "!cave");
	if (zk.ie6Only && ((hgh && hgh != "auto" )|| n.style.height)) n.style.height = "0px";
	if (hgh && hgh != "auto")
		zk.setOffsetHeight(n, zkPop.getOffsetHeight(cmp));
	else 
		n.style.height = "auto";
	
};
if (!zkPop._pop)
	zkau.floats.push(zkPop._pop = new zk.Popup()); //hook to zkau.js

//iframe//
zkIfr = {}

if (zk.ie) {
	zkIfr.init = function (cmp) {
	//Bug 1896797: setVParent (for overlapped) cause IE7 malfunction, so reload
	//1. it is OK if under AU (so only booting)
	//2. no 2nd load so the performance not hurt
		if (!zk.booted)
			for (var n = cmp; n = n.parentNode;) {
				var m = getZKAttr(n, "mode");
				if (m && m != "embedded")
					cmp.src = cmp.src;
			}
	};
} else if (zk.gecko) { //Bug 1692495 and 2443726
	zkIfr.onVisi = function (cmp) {
		if (cmp.src.indexOf(".xml") >= 0 || cmp.src.indexOf(".pdf") >= 0)
			cmp.src = cmp.src; //strange workaround: reload xml
	};
}

//Style//
var zkStyle = {};
zkStyle.init = function (cmp) {
	var src = getZKAttr(cmp, "src");
	if (src) zk.loadCSSDirect(src, cmp.id + "-");
};
zkStyle.cleanup = function (cmp) {
	var css = $e(cmp.id + "-");
	if (css) zk.remove(css);
};

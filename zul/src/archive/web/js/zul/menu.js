/* menu.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Sep 23 10:43:37     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
////
zkMenu = {};

zk.FloatMenu = Class.create();
Object.extend(Object.extend(zk.FloatMenu.prototype, zk.Floats.prototype), {
	_close: function (el) {
		zkMenu._close(el);
	}
});

if (!zkMenu._pop)
	zkau.floats.push(zkMenu._pop = new zk.FloatMenu()); //hook to zkau.js

////
// menu //
zkMenu.onover = function (evt) {
	if (!evt) evt = window.event;
	var cmp = $outer(Event.element(evt));

	if ($type(cmp) != "Menusp")
		zk.addClass(cmp, "seld");

	var menubar = $parentByType(cmp, "Menubar");
	var autodrop = !menubar || getZKAttr(menubar, "autodrop") == "true";
	if (autodrop) zkMenu._shallClose = false;
		//turn off pending auto-close

	var popupIds = zkMenu._pop.getFloatIds();
	if (!autodrop && popupIds.length == 0) return;

	//Close non-child menu
	for (var j = popupIds.length; --j >= 0;) {
		var pp = $e(popupIds[j]);
		if (!zk.isAncestor(cmp, pp) && !zk.isAncestor(pp, cmp))
			zkMenu.close(pp);
	}

	zkMenu.open(cmp, false);
};
zkMenu.onout = function (evt) {
	if (!evt) evt = window.event;
	zkMenu._onout($outer(Event.element(evt)));
};
zkMenu._onout = function (cmp) {
	zk.rmClass(cmp, "seld");

	if (zkMenu._pop.getFloatIds().length == 0) return; //nothing to do

	var menubar = $parentByType(cmp, "Menubar");
	if (menubar && getZKAttr(menubar, "autodrop") == "true") {
		zkMenu._shallClose = true;
		setTimeout("if (zkMenu._shallClose) zkau.closeFloats('"+menubar.id+"');", 500);
	}
};
zkMenu.onclick = function (evt) {
	if (!evt) evt = window.event;
	var cmp = $outer(Event.element(evt));
	if ("Menu" == $type(cmp)) //note: Menuit also go thru this method
		zkMenu.open(cmp, getZKAttr(cmp, "top") == "true");
};

/** Opens a menupopup belong to the specified menu.
 * @param toggle whether to close all menu first and then open the specified menu
 */
zkMenu.open = function (menu, toggle) {
	if (toggle) zkau.closeFloats(menu);

	var popupId = getZKAttr(menu, "mpop");
	if (!popupId) return; //menuitem

	var pp = $e(popupId);
	if (!pp) {
		zk.error(mesg.INVALID_STRUCTURE+"z.mpop not exists");
		return;
	}

	if (!$visible(pp)) {
		var top = getZKAttr(menu, "top") == "true"; //top-level menu
		var ref = top || $tag(menu) != "TD" ? menu: $parent(menu); //use TR if not top
		var pos = top && getZKAttr(menu, "vert") == null ? "after-start": "end_before";
		
		pp.style.position = "absolute"; //just in case
		zk.setVParent(pp);
		zkMenu._open(pp, top, ref, pos);

		if (zkau.asap(pp, "onOpen"))
			zkau.send({uuid: pp.id, cmd: "onOpen", data: [true, menu.id]});
	}
};
/** Opens the specified menupopup
 * @param pp menupopup
 * @param top whether it belongs to the top-level menu
 * @param ref the reference element to position menu.
 * @param pos how to position the menu
 */
zkMenu._open = function (pp, top, ref, pos) {
	//FF: Bug 1486840
	//IE: Bug 1766244 (after specifying position:relative to grid/tree/listbox)
	if (ref) zk.position(pp, ref, pos);
	zk.show(pp); //animation effect, if any

	zkMenu._pop.addFloatId(pp.id);
	zkau.hideCovered();
	if (zk.gecko)
		setTimeout("zkMenu._fixWidth('"+pp.id+"')", 10);
};
/** Fixes a Mozilla bug that div's width might be smaller than
 * the table it contains.
 */
zkMenu._fixWidth = function (popupId) {
	var pp = $e(popupId);
	if (pp) {
		var tbl = pp.firstChild;
		for (;; tbl = tbl.nextSibling) {
			if (!tbl) return;
			if ($tag(tbl) == "TABLE")
				break;
		}
		if (pp.offsetWidth < tbl.offsetWidth)
			pp.style.width = (tbl.offsetWidth + 4) + "px";
	}
};

/** Closes the menu. */
zkMenu.close = function (pp) {
	zkMenu._pop.removeFloatId(pp.id);
	zkMenu._close(pp);
	zkau.hideCovered();
};
zkMenu._close = function (pp) {
	pp = $e(pp);
	if (pp) {
		pp.style.display = "none";
		zk.unsetVParent(pp);

		if (zkau.asap(pp, "onOpen"))
			zkau.send({uuid: pp.id, cmd: "onOpen", data: [false]});
			//for better performance, sent only if non-deferable
	}
};

zkMenu.init = function (cmp) {
	var anc = $e(cmp.id + "!a");
	if (getZKAttr(cmp, "top") == "true") {
		zk.listen(anc, "click", zkMenu.onclick);
		zk.listen(anc, "mouseover", zkMenu.onover);
		zk.listen(anc, "mouseout", zkMenu.onout);
	} else {
		zk.listen(cmp, "click", zkMenu.onclick);
		zk.listen(cmp, "mouseover", zkMenu.onover);
		zk.listen(cmp, "mouseout", zkMenu.onout);

		zk.listen(anc, "focus", zkau.onfocus);
		zk.listen(anc, "blur", zkau.onblur);
	}
};

////
// menubar, menuitem //
zkMenubar = {};
zkMenuit = {}; //menuitem
zkMenusp = {}; //menuseparator

zkMenuit.init = function (cmp) {
	zk.listen(cmp, "click", zkMenuit.onclick);
	zk.listen(cmp, "mouseover", zkMenu.onover);
	zk.listen(cmp, "mouseout", zkMenu.onout);

	if (getZKAttr(cmp, "top") != "true") { //non-topmost
		var anc = $e(cmp.id + "!a");
		zk.listen(anc, "focus", zkau.onfocus);
		zk.listen(anc, "blur", zkau.onblur);
	}
};
zkMenuit.onclick = function (evt) {
	if (!evt) evt = window.event;
	var cmp = $parentByType(Event.element(evt), "Menuit");
	zkMenu._onout(cmp); //Bug 1822720

	var anc = $e(cmp.id + "!a");
	if ("javascript:;" == anc.href) {
		var cmp = $outer(anc);
		var uuid = cmp.id;
		if (getZKAttr(cmp, "autock")) {
			var newval = getZKAttr(cmp, "checked") != "true";
			zkau.send({uuid: uuid, cmd: "onCheck", data: [newval]}, -1);
		}
		zkau.send({uuid: uuid, cmd: "onClick", data: null, ctl: true});
	} else {
		var t = anc.getAttribute("target");
		if (anc.href && !zk.isNewWindow(anc.href, t))
			zk.progress();
		//Note: we cannot eat onclick. or, <a> won't work
	}
	zkau.closeFloats(cmp); //bug 1711822: fire onClick first
};

zkMenusp.init = function (cmp) {
	zk.listen(cmp, "mouseover", zkMenu.onover);
	zk.listen(cmp, "mouseout", zkMenu.onout);
};

//menupopup//
zkMpop = {};

/** Called by au.js's context menu. */
zkMpop.context = function (ctx, ref) {
	if (!$visible(ctx)) {
		zkMenu._open(ctx, true);

		if (zkau.asap(ctx, "onOpen"))
			zkau.send({uuid: ctx.id, cmd: "onOpen", data: [true, ref.id]});
	}
};

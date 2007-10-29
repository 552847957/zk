/* zul.js

{{IS_NOTE
	Purpose:
		Common utilities for zul.
	Description:
		
	History:
		Thu Jul 14 15:02:27     2005, Created by tomyeh
}}IS_NOTE

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
zk.load("zul.lang.msgzul*");

////
zul = {};
zul._movs = {}; //(id, Draggable): movables

/////////
// Movable
/** Make a component movable (by moving). */
zul.initMovable = function (cmp, options) {
	zul._movs[cmp.id] = new Draggable(cmp, options);
};
/** Undo movable for a component. */
zul.cleanMovable = function (id) {
	if (zul._movs[id]) {
		zul._movs[id].destroy();
		delete zul._movs[id];
	}
};

/////////
// Headers
//For sortable header, e.g., Column and Listheader (TH or TD is assumed)
zulHdrs = {};
zulHdrs.setAttr = function (cmp, nm, val) {
	zkau.setAttr(cmp, nm, val);

	if (nm == "z.sizable") {
		var cells = cmp.cells;
		if (cells) {
			var sizable = val == "true";
			for (var j = 0; j < cells.length; ++j)
				zulHdr.setSizable(cells[j], sizable);
		}
	}
	return true;
};
zulHdr = {}; //listheader
zulHdr._szs = {};
zulHdr.init = function (cmp) {
	zulHdr._show(cmp);
	zk.listen(cmp, "click", function (evt) {zulHdr.onclick(evt, cmp);});
	zk.listen(cmp, "mousemove", function (evt) {if (window.zulHdr) zulHdr.onmove(evt, cmp);});
		//FF: at the moment of browsing to other URL, listen is still attached but
		//our js are unloaded. It causes JavaScript error though harmlessly
		//This is a dirty fix (since onclick and others still fail but hardly happen)

	zulHdr.setSizable(cmp, zulHdr.sizable(cmp));
		//Note: IE6 failed to crop a column if it is draggable
		//Thus we init only necessary (to avoid the IE6 bug)
};
zulHdr.sizable = function (cmp) {
	return cmp.parentNode && getZKAttr(cmp.parentNode, "sizable") == "true";
};
zulHdr.setSizable = function (cmp, sizable) {
	var id = cmp.id;
	if (sizable) {
		if (!zulHdr._szs[id]) {
			var snap = function (x, y) {return zulHdr._snap(cmp, x, y);};
			zulHdr._szs[id] = new Draggable(cmp, {
				starteffect: zk.voidf,
				endeffect: zulHdr._endsizing, ghosting: zulHdr._ghostsizing,
				revert: true, ignoredrag: zulHdr._ignoresizing, snap: snap,
				constraint: "horizontal"
			});
		}
	} else {
		if (zulHdr._szs[id]) {
			zulHdr._szs[id].destroy();
			delete zulHdr._szs[id];
		}
	}
};
/** Resize all rows. (Utilities used by derived). */
zulHdr.resizeAll = function (mate, cmp, icol, col, wd, keys) {
	if(mate.paging) return;
	mate.bodytbl.style.width = mate.headtbl.style.width;
	wd = $int(wd);		
	if (mate.foottbl) {
		mate.foottbl.style.width = mate.headtbl.style.width;
		if (mate.foottbl.rows.length) {
			var cells = mate.foottbl.rows[0].cells;
			if (icol < cells.length) {
				var rwd = zk.revisedSize(cells[icol], wd);
				cells[icol].style.width = rwd + "px";
				var cell = $e($uuid(cells[icol]) + "!cave");
				rwd = zk.revisedSize(cell, rwd);
				cell.style.width = rwd + "px";		
			}
		}
	}
	var head;
	for(var j =0; j < mate.headtbl.rows.length; j++) {
		var type = $type(mate.headtbl.rows[j]);
		if (type == "Cols" || type == "Lhrs" || type == "Tcols") {
			head = mate.headtbl.rows[j];
			break;
		}
	}
	zk.cpCellWidth(head, mate.bodyrows, mate);
	zkau.send({uuid: cmp.id, cmd: "onColSize",
		data: [icol, col.id, wd, keys]},
		zkau.asapTimeout(cmp, "onColSize"));
};
zulHdr.cleanup = function (cmp) {
	zulHdr.setSizable(cmp, false);
};
zulHdr.setAttr = function (cmp, nm, val) {
	zkau.setAttr(cmp, nm, val);
	if (nm == "z.sort") zulHdr._show(cmp);
	return true;
};

zulHdr.onclick = function (evt, cmp) {
	if (!zk.dragging && zulHdr._sortable(cmp) && zkau.insamepos(evt))
		zkau.send({uuid: cmp.id, cmd: "onSort", data: null}, 10);
};
zulHdr.onmove = function (evt, cmp) {
	if (zk.dragging) return;
	var ofs = zk.revisedOffset(cmp); // Bug #1812154
	var v = zulHdr._insizer(cmp, Event.pointerX(evt) - ofs[0]);
	if (v) {
		zk.backupStyle(cmp, "cursor");
		cmp.style.cursor = v == 1 ? "e-resize": "w-resize";
	} else {
		zk.restoreStyle(cmp, "cursor");
	}
};
/** Called by zkau._ignoredrag (in au.js) for generic drag&drop. */
zulHdr.ignoredrag = function (cmp, pointer) {
	var ofs = zk.revisedOffset(cmp); // Bug #1812154
	return zulHdr._insizer(cmp, pointer[0] - ofs[0]);
};
/** Returns 1 if right, -1 if left, 0 if none. */
zulHdr._insizer = function (cmp, x) {
	if (zulHdr.sizable(cmp)) {
		if (x >= cmp.offsetWidth - 10) return 1;		
	}
	return 0;
};
/** Called by zulHdr._szs[]'s ignoredrag for resizing column. */
zulHdr._ignoresizing = function (cmp, pointer) {
	var dg = zulHdr._szs[cmp.id];
	if (dg) {
		var ofs = zk.revisedOffset(cmp); // Bug #1812154
		var v = zulHdr._insizer(cmp, pointer[0] - ofs[0]);
		if (v) {
			dg.z_min = 5 + zk.sumStyles(cmp, "lr", zk.borders) + zk.sumStyles(cmp, "lr", zk.paddings);		
			return false;
		}
	}
	return true;
};
zulHdr._endsizing = function (cmp, evt) {
	var dg = zulHdr._szs[cmp.id];
	if (dg && dg.z_szofs) {
		var cells = cmp.parentNode.cells, j = 0;
		for (;; ++j) {
			if (j >= cells.length) return; //impossible, but just in case
			if (cmp == cells[j]) {
				break;
			} 
		}
		var keys = "";
		if (evt) {
			if (evt.altKey) keys += 'a';
			if (evt.ctrlKey) keys += 'c';
			if (evt.shiftKey) keys += 's';
		}
		
		var wd = dg.z_szofs;
		var rwd = zk.safari ? wd : zk.revisedSize(cmp, wd);
		var table = $parentByTag(cmp, "TABLE");
		var head;
		
		for(var j =0; j < table.rows.length; j++) {
			var type = $type(table.rows[j]);
			if (type == "Cols" || type == "Lhrs" || type == "Tcols") {
				head = table.rows[j];
				break;
			}
		}
		var cells = head.cells;
		var total = 0;
		for (var k = 0; k < cells.length; ++k)
			if (cells[k] != cmp) total += cells[k].offsetWidth;
		var row = table.rows[0];
		row.cells[cmp.cellIndex].style.width = $int(rwd) + zk.sumStyles(cmp, "lr", zk.borders) + zk.sumStyles(cmp, "lr", zk.paddings) + "px";
		cmp.style.width = rwd + "px";
		var uuid = $uuid(cmp);
		var cell = $e(uuid + "!cave");
		cell.style.width = zk.revisedSize(cell, rwd) + "px";	
		table.style.width = total + wd + "px";					
		setTimeout("zk.eval($e('"+cmp.id+"'),'resize',null,"+j+",'"+wd+"','"+keys+"')", 0);		
	}
};
/* @param ghosting whether to create or remove the ghosting
 */
zulHdr._ghostsizing = function (dg, ghosting, pointer) {
	if (ghosting) {
		var el = dg.element.parentNode.parentNode.parentNode;
		var of = zk.revisedOffset(el);
		var ofs = zkau.beginGhostToDIV(dg);
		ofs[1] = of[1];
		var head = $parentByTag(dg.element, "DIV");		
		ofs[0] += zk.offsetWidth(dg.element);
		document.body.insertAdjacentHTML("afterbegin",
			'<div id="zk_ddghost" style="position:absolute;top:'
			+ofs[1]+'px;left:'+ofs[0]+'px;width:3px;height:'+zk.offsetHeight(head.parentNode)
			+'px;background:darkgray"><img src="'+zk.getUpdateURI('/web/img/spacer.gif')
					+'"/></div>');
		dg.element = $e("zk_ddghost");
	} else {
		var org = zkau.getGhostOrgin(dg);
		if (org) {
			var ofs1 = zk.revisedOffset(dg.element);
			var ofs2 = zk.revisedOffset(org);
			dg.z_szofs = ofs1[0] - ofs2[0];
		} else {
			dg.z_szofs = 0;
		}
		zkau.endGhostToDIV(dg);
	}
};
zulHdr._snap = function (cmp, x, y) {
	var dg = zulHdr._szs[cmp.id];
	if (dg) {
		var ofs = zk.revisedOffset(cmp);
		x += zk.offsetWidth(cmp); 
		if (ofs[0] + dg.z_min >= x)
			x = ofs[0] + dg.z_min;
	}
	return [x, y];
};
/** Tests whether it is sortable.
 */
zulHdr._sortable = function (cmp) {
	return getZKAttr(cmp, "asc") || getZKAttr(cmp, "dsc");
};
/** Shows the hint, ascending or descending image.
 */
zulHdr._show = function (cmp) {
	switch (getZKAttr(cmp, "sort")) {
	case "ascending": zulHdr._renCls(cmp, "asc"); break;
	case "descending": zulHdr._renCls(cmp, "dsc"); break;
	case "natural": zulHdr._renCls(cmp); break;
	}
};
zulHdr._renCls = function (cmp, ext) {
	var clsnm = cmp.className || "";
	if (clsnm.endsWith("-asc") || clsnm.endsWith("-dsc"))
		clsnm = clsnm.substring(0, clsnm.length - 4);
	if (ext) clsnm += '-' + ext;
	if (clsnm != cmp.className) cmp.className = clsnm;
};

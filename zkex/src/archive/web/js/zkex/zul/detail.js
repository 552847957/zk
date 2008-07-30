/* detail.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Wed Jul 30 11:05:56 TST 2008, Created by jumperchen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
/** override for zkDetail */
var _zkexdetail = {};
zk.override(zkau.cmd1, "addAft",  _zkexdetail, function (uuid, cmp, html) {
	var detail = zkDetail.getDetailByRow(cmp), isOpen;
	if (detail && getZKAttr(detail, "open") == "true") {
		isOpen = true;
		zkDetail.open(detail, false, true);
	}
	_zkexdetail.addAft(uuid, cmp, html);
	if (isOpen)
		zkDetail.open(detail, true, true);
});
zk.override(zkau.cmd1, "addBfr",  _zkexdetail, function (uuid, cmp, html) {
	var detail = zkDetail.getDetailByRow(cmp), isOpen;
	if (detail && getZKAttr(detail, "open") == "true") {
		isOpen = true;
		zkDetail.open(detail, false, true);
	}
	_zkexdetail.addBfr(uuid, cmp, html);
	if (isOpen)
		zkDetail.open(detail, true, true);
});
/**
 * A detail component.
 * @since 3.5.0
 */
zkDetail = {
	/**
     * The keywords of  CSS class are applied to this component.
     * <p>Default: ["expanded", "fake"]
     * @type Array
     */
	cssKeywords: ["expanded", "fake"],
	/**
	 * Returns the detail component belongs to the specified row, if any.
	 * @param {Object} tr a TR tag
	 */
	getDetailByRow: function (tr) {
		if ($tag(tr) == "TR" && tr.cells.length) {
			var outer = $outer(tr.cells[0]);
			if (outer && $type(outer) == "Detail")
				return outer;
		}
		return null;
	},
	init: function (cmp) {
		zk.listen(cmp, "click", this.onOpen);
		zk.on($childExterior(cmp).parentNode, "stripe", this.onStripe);
		if (getZKAttr(cmp, "open") == "true")
			this.open(cmp, true, true);
	},
	onOpen: function (evt) {
		if (!evt) evt = window.event;
		var cmp = $parentByType(Event.element(evt), "Detail");
		if (cmp)
			zkDetail.open(cmp, getZKAttr(cmp, "open") != "true", false);
	},
	open: function (cmp, open, silent) {
		if (cmp) {
			var cls = zk.realClass(cmp, zkDetail.cssKeywords);
			zk[open ? "addClass" : "rmClass"](cmp, cls + "-expanded");
			if (open) {
				var td = $childExterior(cmp),
					cave = $e(cmp, "cave"),
					tr = td.parentNode,
					fake = tr.parentNode.insertRow(tr.rowIndex),
					cell = fake.insertCell(0);
				fake.id = $uuid(cmp) + "!fake";
				setZKAttr(fake, "nostripe", "true");
				cell.colSpan = zk.ncols(tr.cells) - 1;
				
				zk.addClass(fake, tr.className + " " + cls + "-fake");
				zk.addClass(cell, zk.realClass(td, [cls + "-td"]));
				td.rowSpan = 2;
				cell.appendChild(cave);
				cave.style.display = "";
				zk.onVisiAt(cave);
			} else {
				var td = $childExterior(cmp),
					cave = $e(cmp, "cave"),
					tr = td.parentNode,
					fake = $e(cmp, "fake");
				cave.style.display = "none";
				cmp.appendChild(cave);
				td.rowSpan = 1;
				zk.remove(fake);
			}
			if (!silent)
				zkau.sendasap({uuid: cmp.id, cmd: "onOpen", data: [open]});
			setZKAttr(cmp, "open", open ? "true" : "false");
		}
	},
	/**A callback function for changing stripe*/
	onStripe: function (tr) {
		var cmp = zkDetail.getDetailByRow(tr);
		if (cmp && getZKAttr(cmp, "open") == "true") {
			var fake = $e(cmp, "fake"),
				cls = zk.realClass(cmp, zkDetail.cssKeywords);
			fake.className = "";
			zk.addClass(fake, tr.className + " " + cls + "-fake");
		}
		
	},
	cleanup: function (cmp) {
		zk.un($childExterior(cmp).parentNode, "stripe", this.onStripe);
		if (getZKAttr(cmp, "open") == "true")
			this.open(cmp, false, true);
	},
	setAttr: function (cmp, nm, val) {
		if (nm == "z.open") {
			this.open(cmp, val == "true", true);
			return true; //no need to store z.open
		}
		return false;
	}
};
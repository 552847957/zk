/* Calendar.js

{{IS_NOTE
	Purpose:

	Description:

	History:
		Fri Jan 23 10:32:34 TST 2009, Created by Flyworld
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
/**
 * A calendar.
 * <p>Default {@link #getZclass}: z-calendar.
 */
zul.db.Calendar = zk.$extends(zul.Widget, {
	_view : "day", //"day", "month", "year", "decade",
	
	$init: function () {
		this.$supers('$init', arguments);
		this.listen({onChange: this}, -1000);
	},
	$define: {
		/** Assigns a value to this component.
		 * @param Date value the date to assign. If null, today is assumed.
		 */
		/** Returns the value that is assigned to this component.
		 * @return Date
	 	 */
		value: _zkf = function() {
			this.rerender();
		},
		/** Set the date limit for this component with yyyyMMdd format,
		 * such as 20100101 is mean Jan 01 2010 
		 * 
		 * <dl>
		 * <dt>Example:</dt>
		 * <dd>between 20091201 and 20091231</dd>
		 * <dd>before 20091201</dd>
		 * <dd>after 20091231</dd>
		 * </dl>
		 * 
		 * @param String constraint
		 */
		/** Returns the constraint of this component.
		 * @return String
		 */
		constraint: function() {
			var constraint = this._constraint || '';
			if (constraint.startsWith("between")) {
				var j = constraint.indexOf("and", 7);
				if (j < 0 && zk.debugJS) 
					zk.error('Unknown constraint: ' + constraint);
				this._beg = zk.fmt.Date.parseDate(constraint.substring(7, j), 'yyyyMMdd');
				this._end = zk.fmt.Date.parseDate(constraint.substring(j + 3), 'yyyyMMdd');
				if (this._beg.getTime() > this._end.getTime()) {
					var d = this._beg;
					this._beg = this._end;
					this._end = d;
				}
				
				this._beg.setHours(0);
				this._beg.setMinutes(0);
				this._beg.setSeconds(0);
				this._beg.setMilliseconds(0);
				
				this._end.setHours(0);
				this._end.setMinutes(0);
				this._end.setSeconds(0);
				this._end.setMilliseconds(0);
			} else if (constraint.startsWith("before")) {
				this._end = zk.fmt.Date.parseDate(constraint.substring(6), 'yyyyMMdd');
				this._end.setHours(0);
				this._end.setMinutes(0);
				this._end.setSeconds(0);
				this._end.setMilliseconds(0);
			} else if (constraint.startsWith("after")) {
				this._beg = zk.fmt.Date.parseDate(constraint.substring(5), 'yyyyMMdd');
				this._beg.setHours(0);
				this._beg.setMinutes(0);
				this._beg.setSeconds(0);
				this._beg.setMilliseconds(0);
			}
		},
		/** Sets the name of this component.
		 * <p>The name is used only to work with "legacy" Web application that
		 * handles user's request by servlets.
		 * It works only with HTTP/HTML-based browsers. It doesn't work
		 * with other kind of clients.
		 * <p>Don't use this method if your application is purely based
		 * on ZK's event-driven model.
		 *
		 * @param String name the name of this component.
		 */
		/** Returns the name of this component.
		 * <p>The name is used only to work with "legacy" Web application that
		 * handles user's request by servlets.
		 * It works only with HTTP/HTML-based browsers. It doesn't work
		 * with other kind of clients.
		 * <p>Don't use this method if your application is purely based
		 * on ZK's event-driven model.
		 * <p>Default: null.
		 * @return String
		 */
		name: function () {
			if (this.efield)
				this.efield.name = this._name;
		}
	},
	onChange: function (evt) {
		this.updateFormData(evt.data.value);
	},
	doKeyDown_: function (evt) {
		var keyCode = evt.keyCode,
			ofs = keyCode == 37 ? -1 : keyCode == 39 ? 1 : keyCode == 38 ? -7 : keyCode == 40 ? 7 : 0;
		if (ofs) {
			this._shift(ofs);
		} else 
			this.$supers('doKeyDown_', arguments);
	},
	_shift: function (ofs) {
		var oldTime = this.getTime();	
		
		switch(this._view) {
		case 'month':
		case 'year':
			if (ofs == 7)
				ofs = 4;
			else if (ofs == -7)
				ofs = -4;
			break;
		case 'decade':
			if (ofs == 7)
				ofs = 4;
			else if (ofs == -7)
				ofs = -4;
			ofs *= 10;
			
			var y = oldTime.getFullYear();
			if (y + ofs < 1900 || y + ofs > 2100)
				return;// out of range
			break;
		}		
		this._shiftDate(this._view, ofs);
		var newTime = this.getTime();
		switch(this._view) {
		case 'day':
			if (oldTime.getYear() == newTime.getYear() &&
				oldTime.getMonth() == newTime.getMonth()) {
				this._markCal();
			} else 
				this.rerender();
			break;
		case 'month':
			if (oldTime.getYear() == newTime.getYear())
				this._markCal();
			else
				this.rerender();
			break;
		default:			
			this.rerender();
			break;
		}
	},
	/** Returns the format of this component.
	 * @return String
	 */
	getFormat: function () {
		return this._fmt || "yyyy/MM/dd";
	},
	getZclass: function () {
		var zcs = this._zclass;
		return zcs != null ? zcs: "z-calendar";
	},
	updateFormData: function (val) {
		if (this._name) {
			val = val || '';
			if (!this.efield)
				this.efield = jq.newHidden(this._name, val, this.$n());
			else
				this.efield.value = val;
		}
	},
	bind_: function (){
		this.$supers('bind_', arguments);
		var title = this.$n("title"),
			mid = this.$n("mid"),
			ly = this.$n("ly"),
			ry = this.$n("ry"),
			zcls = this.getZclass();
		jq(title).hover(
			function () {
				$(this).toggleClass(zcls + "-title-over");
			},
			function () {
			    $(this).toggleClass(zcls + "-title-over");
			}
		);
		if (this._view != 'decade') 
			this._markCal();
		else {
			var anc = jq(this.$n()).find('.' + zcls + '-seld')[0];
			if (anc)
				jq(anc.firstChild).focus();
		}
			
		this.domListen_(title, "onClick", '_changeView')
			.domListen_(mid, "onClick", '_choiceData')
			.domListen_(ly, "onClick", '_doclickArrow')
			.domListen_(ry, "onClick", '_doclickArrow')
			.domListen_(mid, "onMouseOver", '_doMouseEffect')
			.domListen_(mid, "onMouseOut", '_doMouseEffect');
		this.updateFormData(this._value || zk.fmt.Date.formatDate(this.getTime(), this.getFormat()));
	},
	unbind_: function () {
		var title = this.$n("title"),
			mid = this.$n("mid"),
			ly = this.$n("ly"),
			ry = this.$n("ry");
		this.domUnlisten_(title, "onClick", '_changeView')
			.domUnlisten_(mid, "onClick", '_choiceData')
			.domUnlisten_(ly, "onClick", '_doclickArrow')
			.domUnlisten_(ry, "onClick", '_doclickArrow')
			.domUnlisten_(mid, "onMouseOver", '_doMouseEffect')
			.domUnlisten_(mid, "onMouseOut", '_doMouseEffect')
			.$supers('unbind_', arguments);
		this.efield = null;
	},
	_doclickArrow: function (evt) {
		var node = evt.domTarget,
			ofs = node.id.indexOf("-ly") > 0 ? -1 : 1;
		if (jq(node).hasClass(this.getZclass() + (ofs == -1 ? '-left-icon-disd' : '-right-icon-disd')))
			return;
		switch(this._view) {
			case "day" :
				this._shiftDate("month", ofs);
				break;
			case "month" :
				this._shiftDate("year", ofs);
				break;
			case "year" :
				this._shiftDate("year", ofs*10);
				break;
			case "decade" :
				this._shiftDate("year", ofs*100);
				break;
		}
		this.rerender();
	},
	_doMouseEffect: function (evt) {
		var node = evt.domTarget.tagName == "TD" ? evt.domTarget : evt.domTarget.parentNode,
			zcls = this.getZclass();
			
		if (jq(node).hasClass(zcls + '-disd'))
			return;
			
		if (jq(node).is("."+zcls+"-seld")) {
			jq(node).removeClass(zcls + "-over");
			jq(node).toggleClass(zcls + "-over-seld");
		}else {
			jq(node).toggleClass(zcls + "-over");
		}
	},
	/** Returns the Date that is assigned to this component.
	 *  <p>returns today if value is null
	 * @return Date
	 */
	getTime : function () {
		return this._value ? zk.fmt.Date.parseDate(this._value, this.getFormat()) : new Date();
	},
	_setTime : function (y, m, d, hr, mi) {
		var dateobj = this.getTime(),
			year = y != null ? y  : dateobj.getFullYear(),
			month = m != null ? m : dateobj.getMonth(),
			day = d != null ? d : dateobj.getDate();
		this._value = zk.fmt.Date.formatDate(new Date(year, month, day), this.getFormat());
		this.fire('onChange', {value: this._value});
	},
	_choiceData: function (evt) {
		var target = evt.domTarget;
		target = target.tagName == "TD" ? target : target.parentNode;
		
		var val = jq(target).attr('_dt');
		if (target && !jq(target).hasClass(this.getZclass() + '-disd') && !isNaN(val)) {
			var cell = target,
				dateobj = this.getTime();
			switch(this._view) {
				case "day" :
					var oldTime = this.getTime();
					this._setTime(null, cell._monofs != null && cell._monofs != 0 ?
							dateobj.getMonth() + cell._monofs : null, val);
					var newTime = this.getTime();
					if (oldTime.getYear() == newTime.getYear() &&
						oldTime.getMonth() == newTime.getMonth()) {
							this._markCal();
					} else
						this.rerender();
					break;
				case "month" :
					this._setTime(null, val);
					this._setView("day");
					break;
				case "year" :
					this._setTime(val);
					this._setView("month");
					break;
				case "decade" :
					//Decade mode Set Year Also
					this._setTime(val);
					this._setView("year");
					break;
			}
		}
	},
	_shiftDate: function (opt, ofs) {
		var dateobj = this.getTime(),
			year = dateobj.getFullYear(),
			month = dateobj.getMonth(),
			day = dateobj.getDate();
		switch(opt) {
			case "day" :
				day = day + ofs;
				break;
			case "month" :
				month = month + ofs;
				break;
			case "year" :
				year = year + ofs;
				break;
			case "decade" :
				year = year + ofs;
				break;
		}
		this._value = zk.fmt.Date.formatDate(new Date(year, month, day), this.getFormat());
		this.fire('onChange', {value: this._value, shallClose: false});
	},
	_changeView : function (evt) {
		var tm = this.$n("tm"),
			ty = this.$n("ty"),
			tyd = this.$n("tyd");
		if (evt.domTarget == tm)
			this._setView("month");
		else if (evt.domTarget == ty)
			this._setView("year");
		else if (evt.domTarget == tyd )
			this._setView("decade");
	},
	_setView : function (view) {
		if (view != this._view) {
			this._view = view;
			this.rerender();
		}
	},
	_invalid: function (y, m, v, today) {
		var d = new Date(y, m, v, 0, 0, 0, 0);
		switch (this._constraint) {
		case 'no today':
			return today - d == 0;
			break;
		case 'no past':
			return (d - today) / 86400000 < 0;
			break;
		case 'no future':
			return (today - d)/ 86400000 < 0;
			break;
		default:
			var result = false;
			if (this._beg && (result = (d - this._beg) / 86400000 < 0))
				return result;
			if (this._end && (result = (this._end - d) / 86400000 < 0))
				return result;
			return result;
		}
	},
	_markCal : function () {
		var	zcls = this.getZclass(),
		 	val = this.getTime(),
		 	m = val.getMonth(),
			d = val.getDate(),
			y = val.getFullYear(),
			last = new Date(y, m + 1, 0).getDate(), //last date of this month
			prev = new Date(y, m, 0).getDate(), //last date of previous month
			v = new Date(y, m, 1).getDay()- zk.DOW_1ST,
			today = new Date();
		//hightlight month & year
		for (var j = 0; j < 12; ++j) {
			var mon = this.$n("m" + j),
				year = this.$n("y" + j),
				yy = y % 10 + 1;
			if (mon) {
				if (m == j) {
					jq(mon).addClass(zcls+"-seld");
					jq(mon).removeClass(zcls+"-over");
					jq(mon.firstChild).focus();
				} else
					jq(mon).removeClass(zcls+"-seld");
			}
			if (year) {
				if (yy == j) {
					jq(year).addClass(zcls+"-seld");
				    jq(year).removeClass(zcls+"-over");
					jq(year.firstChild).focus();
				} else
					jq(year).removeClass(zcls+"-seld");
			}
		}

		if (v < 0) v += 7;
		for (var j = 0, cur = -v + 1; j < 6; ++j) {
			var week = this.$n("w" + j);
			if ( week != null) {
				for (var k = 0; k < 7; ++k, ++cur) {
					v = cur <= 0 ? prev + cur: cur <= last ? cur: cur - last;
					if (k == 0 && cur > last)
						week.style.display = "none";
					else {
						if (k == 0) week.style.display = "";
						var cell = week.cells[k],
							monofs = cur <= 0 ? -1: cur <= last ? 0: 1,
							sel = cur == d;
						cell.style.textDecoration = "";
						cell._monofs = monofs;
						//hightlight day
						jq(cell).removeClass(zcls+"-over");
						jq(cell).removeClass(zcls+"-over-seld");
						if (sel)
							jq(cell).addClass(zcls+"-seld");
						else
							jq(cell).removeClass(zcls+"-seld");
							
						if (this._invalid(y, m + monofs, v, today)) {
							jq(cell).addClass(zcls+"-disd");
						} else
							jq(cell).removeClass(zcls+"-disd");
							 
						jq(cell).html('<a href="javascript:;">' + v + '</a>').attr('_dt', v);
						if (sel)
							jq(cell.firstChild).focus();
					}
				}
			}
		}
	}

});
